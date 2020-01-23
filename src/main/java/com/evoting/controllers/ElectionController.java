package com.evoting.controllers;

import com.evoting.models.Election;
import com.evoting.models.Proof;
import com.evoting.models.User;
import com.evoting.resources.PaillierCipher;
import com.evoting.resources.PaillierPubKey;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
public class ElectionController {

    @Resource
    private ConcurrentHashMap<String, User> users;  //users hashmap

    @Resource
    private ConcurrentHashMap<String, Election> elections;  //elections hashmap

    /**
     * Method to display selected elections page
     * @param eid UUID of the election
     * @param model thymeleaf model for html
     * @param auth the token of the logged in user
     * @return election page
     */
    @GetMapping(value = "/election/{eid}")
    public String displayElection(@PathVariable String eid, Model model, OAuth2AuthenticationToken auth) {

        String currentUser = auth.getPrincipal().getAttributes().get("email").toString();   //current users email
        Election election = elections.get(eid); //current election

        //model for election attributes
        model.addAttribute("election", election);

        //if the user hasn't voted display a vote button
        if(election.getParticipants().get(users.get(currentUser)) == false) {
            model.addAttribute("hasVoted", false);
        }
        //else the user has voted, don't display the vote button
        else {
            model.addAttribute("hasVoted", true);
        }

        //if the election owner is viewing the page
        if(election.getOwner().equals(currentUser)) {
            //set owener to true and allow for other buttons for being in the election
            model.addAttribute("owner", true);
            model.addAttribute("curId", election.getEid());
        }
        //else the user isn't owner, but in election
        else if (election.getParticipants().containsKey(users.get(currentUser))){
            //set owner false and still show other buttons
            model.addAttribute("owner", false);
            model.addAttribute("curId", election.getEid());
        }

        //model for bulletin board
        model.addAttribute("ballots", election.getBoard().getBallots());

        return "election";
    }

    /**
     * Mix method to mix ballots upon button click
     * @param eid UUID of the election
     * @param auth token for the logged in user
     * @return election page
     */
    @GetMapping(value = "/election/{eid}/mix")
    @ResponseBody
    public String mix(@PathVariable String eid, OAuth2AuthenticationToken auth) {

        String currentUser = auth.getPrincipal().getAttributes().get("email").toString();   //current logged in user
        Election election = elections.get(eid); //current election

        //set the mix net ballots
        election.getMixNet().setBallots(election.getBoard().getBallots());

        //mix once, more causes heap error
        election.getBoard().setShuffledBallots(election.getMixNet().mix());

        //add the proof to the election
        election.addProof(election.getMixNet().getProofs());

        return "election";
    }

    /**
     * Tally method for tallying ballots upon button click
     * @param eid UUID of the election
     * @param auth token for the logged in user
     * @return results of the election
     */
    @GetMapping(value = "/election/{eid}/tally", produces = "application/json")
    @ResponseBody
    public HashMap<String, String> tally(@PathVariable String eid, OAuth2AuthenticationToken auth) {

        String currentUser = auth.getPrincipal().getAttributes().get("email").toString();   //current logged in user
        Election election = elections.get(eid); //current election

        PaillierCipher cipher = election.getPaillier(); //election cipher

        HashMap<String, String> results = new HashMap<>();  //results map

        CopyOnWriteArrayList<BigInteger> encBallots = election.getBoard().getShuffledBallots(); //encrypted ballots
        ArrayList<BigInteger> decBallots = new ArrayList<>();   //decrypted ballots

        //for each ballot, decrypt and add the list
        for(int i=0; i<encBallots.size(); i++) {
            decBallots.add(cipher.decrypt(encBallots.get(i)));
        }

        //for each candidate get their name and number of votes
        for(int j=0; j<election.getCandidates().size(); j++) {
            results.put(election.getCandidates().get(j), Integer.toString(Collections.frequency(decBallots, BigInteger.valueOf(j))));
        }

        //close the election
        election.setClosed(true);

        //set the elections results
        election.setResults(results);

        return results;
    }

    /**
     * Method for displaying the vote page for a selected election
     * @param eid UUID of the election
     * @param model thymeleaf model for displaying info in html
     * @param auth token for the logged in user
     * @return voting page or election page if they have already voted
     */
    @GetMapping(value = "/election/{eid}/vote")
    public String displayVote(@PathVariable String eid, Model model, OAuth2AuthenticationToken auth) {

        String currentUser = auth.getPrincipal().getAttributes().get("email").toString();   //current user logged in
        Election election = elections.get(eid); //current election

        CopyOnWriteArrayList<String> cands = election.getCandidates();  //candidates list

        //if the user has already voted, don't display page
        if(election.getParticipants().get(users.get(currentUser))) {
            return "election";
        }
        //else the user hasn't voted model attributes for thymeleaf
        else {
            model.addAttribute("electionTitle", election.getTitle());

            model.addAttribute("candidates", cands);

            model.addAttribute("eid", election.getEid());

            return "vote";
        }

    }

    /**
     * Vote method for casting a vote
     * @param eid UUID of the election
     * @param auth token for logged in user
     * @param request form data from http request
     * @return vote page, redirecting to election
     */
    @GetMapping(value = "/election/{eid}/vote/cast")
    public String vote(@PathVariable String eid, OAuth2AuthenticationToken auth, HttpServletRequest request) {

        String currentUser = auth.getPrincipal().getAttributes().get("email").toString();   //current logged in user
        Election election = elections.get(eid); //current election

        Map<String, String[]> input = request.getParameterMap();    //map of inputs
        String selection = input.get("choice")[0];  //option user selected

        int index = election.getCandidates().indexOf(selection);    //index of selected candidate
        BigInteger plainText = BigInteger.valueOf(index);   //make into big int

        BigInteger cipherText = encryptVote(plainText, eid);    //encrypt big int based on election id

        //add the vote to the bulletin board
        election.getBoard().addVote(cipherText);

        //set the current user to already voted
        election.hasCasted(users.get(currentUser));

        return "vote";
    }

    /**
     * Verify method for verifying encryption
     * @param eid UUID of election
     * @param auth token for logged in user
     * @param request form data from http request
     * @return map of key info to display
     */
    @GetMapping(value = "/election/{eid}/vote/verify", produces = "application/json")
    @ResponseBody
    public HashMap<String, String> verify(@PathVariable String eid, OAuth2AuthenticationToken auth, HttpServletRequest request) {

        String currentUser = auth.getPrincipal().getAttributes().get("email").toString();   //current logged in user
        Election election = elections.get(eid); //selected election

        Map<String, String[]> input = request.getParameterMap();    //map of inputs
        String selection = input.get("choice")[0];  //set selection to the user's choice

        int index = election.getCandidates().indexOf(selection);    //get the index from list
        BigInteger plainText = BigInteger.valueOf(index);   //make into big int

        PaillierCipher cipher = election.getPaillier(); //get cipher

        BigInteger cipherText = encryptVote(plainText, eid);    //encrypt vote

        PaillierPubKey pk = election.getPaillierPubKey();   //get pub key
        BigInteger rand = election.getPaillier().getR();    //get random

        HashMap<String, String> keyInfo = new HashMap<>();  //map for key info

        //add all the key info to the hashmap
        keyInfo.put("encryption", cipherText.toString());
        keyInfo.put("n", pk.getN().toString());
        keyInfo.put("nsqr", pk.getNsqr().toString());
        keyInfo.put("g", pk.getG().toString());
        keyInfo.put("rand", rand.toString());
        keyInfo.put("dec", cipher.decrypt(cipherText).toString());

        return keyInfo;
    }

    /**
     * Method for displaying proof of shuffle
     * @param eid UUID for election
     * @param model thymeleaf model to display info in html
     * @param auth token of logged in user
     * @return proof page
     */
    @GetMapping("/election/{eid}/proof")
    public String displayProof(@PathVariable String eid, Model model, OAuth2AuthenticationToken auth) {

        String currentUser = auth.getPrincipal().getAttributes().get("email").toString();   //get current logged in user
        Election election = elections.get(eid); //get current election

        //add election to model
        model.addAttribute("eid", election.getEid());

        model.addAttribute("election", election);

        return "proof";
    }

    /**
     * Method for loading the proof on the page
     * @param eid UUID for election
     * @param auth token for logged in user
     * @return map of proof for each mix
     */
    @GetMapping("/election/{eid}/outputProof")
    @ResponseBody
    public HashMap<String, String> loadProof(@PathVariable String eid, OAuth2AuthenticationToken auth) {

        String currentUser = auth.getPrincipal().getAttributes().get("email").toString();   //current logged in user
        Election election = elections.get(eid); //current election

        HashMap<String, String> proofInfo = new HashMap<>();    //map for proof info
        CopyOnWriteArrayList<CopyOnWriteArrayList<Proof>> proofs = election.getProofs();    //proofs for the election
        String curProof;    //string to hold proof

        //for each proof
        for(int i=0; i<proofs.size(); i++) {
            //reset the string
            curProof = "";
            //get the tmp selected list
            CopyOnWriteArrayList<Proof> tmpData = proofs.get(i);
            //get the SHA hash of the selected proof
            BigInteger tmpHash = tmpData.get(i).getHash();

            //for 10 bits (could change for more lines)
            for(int j=0; j<10; j++) {
                //test the bit, if its a 1
                if(tmpHash.testBit(j)) {
                    //create proof for bit 1, including both randoms and permutation data
                    curProof += "Bit: 1. \n";
                    curProof += "Primary Random: " + tmpData.get(i).getPrimaryR() + ". \n";
                    curProof += "Secondary Random: " + tmpData.get(i).getSecondaryR() + ". \n";
                    for(int x=0; x<tmpData.get(i).getPrimaryShuffle().size(); x++) {
                        curProof += "Primary Swap: " + tmpData.get(i).getPrimaryShuffle().get(x) + ". Secondary Swap: "
                                + tmpData.get(i).getSecondaryShuffle().get(x) + ". \n";
                    }
                }
                //else the bit is a 0
                else {
                    //create proof for bit 0, including secondary random and permutation data
                    curProof += "Bit: 0. \n";
                    curProof += "Secondary Random: " + tmpData.get(i).getSecondaryR() + ". \n";
                    for(int y=0; y<tmpData.get(i).getSecondaryShuffle().size(); y++) {
                        curProof += "Swap: " + tmpData.get(i).getSecondaryShuffle().get(y) + ". \n";
                    }
                }
            }
            //add the proof to the map
            proofInfo.put(Integer.toString(i), curProof);
        }
        //return map
        return proofInfo;
    }

    /**
     * Encrypt vote method for taking a vote and election and encrypting the vote
     * @param p plaintext Big int
     * @param eid UUID for election
     * @return ciphertext
     */
    public BigInteger encryptVote(BigInteger p, String eid) {
        Election election = elections.get(eid);

        PaillierCipher cipher = election.getPaillier();

        return cipher.encrypt(p);
    }

}
