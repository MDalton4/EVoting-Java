package com.evoting.models;

import com.evoting.resources.MixNet;
import com.evoting.resources.PaillierCipher;
import com.evoting.resources.PaillierKeyPair;
import com.evoting.resources.PaillierPubKey;

import java.io.IOException;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.*;

public class Election {
    private String owner, code, title;  //election owner, code and name

    private String eid; //election id

    private boolean isClosed;

    private CopyOnWriteArrayList<String> candidates;    //election candidates
    private ConcurrentHashMap<User, Boolean> participants;  //election participants
    private HashMap<String, String> results;  //election results

    private CopyOnWriteArrayList<CopyOnWriteArrayList<Proof>> proofs;

    private PaillierKeyPair pkp;    //election paillier key pair
    private PaillierCipher paillier;    //election cipher
    private KeyPair paillierKeys;   //election key pair
    private PaillierPubKey paillierPubKey;

    private BulletinBoard board; //election bulletin board

    private MixNet mixNet;  //ections mixnet

    public Election(String owner, String title, CopyOnWriteArrayList<String> candidates) {
        this.owner = owner;
        this.title = title;
        this.candidates = candidates;

        //create new UUID
        eid = UUID.randomUUID().toString();

        //set the code to the first substring of the UUID
        code = eid.substring(0, eid.indexOf("-"));

        isClosed = false;

        participants = new ConcurrentHashMap<>();

        proofs = new CopyOnWriteArrayList<>();

        pkp = new PaillierKeyPair();
        paillierKeys = pkp.generateKeys();
        paillier = new PaillierCipher(paillierKeys);
        paillierPubKey = pkp.getPubKey();

        board = new BulletinBoard();

        mixNet = new MixNet(paillierKeys, paillier, this.eid);
    }

    public void addProof(CopyOnWriteArrayList<Proof> m) {
        proofs.add(m);
    }

    public CopyOnWriteArrayList<CopyOnWriteArrayList<Proof>> getProofs() {
        return proofs;
    }

    public void setResults(HashMap<String, String> results) {
        this.results = results;
    }

    public HashMap<String, String> getResults() {
        return results;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public void hasCasted(User u) {
        participants.put(u, true);
    }

    public void joinElection(User u) {
        participants.put(u, false);
    }

    public boolean hasVoted(User u) {
        return participants.get(u);
    }

    public MixNet getMixNet() {
        return mixNet;
    }

    public ConcurrentHashMap<User, Boolean> getParticipants() {
        return participants;
    }

    public CopyOnWriteArrayList<String> getCandidates() {
        return candidates;
    }

    public PaillierPubKey getPaillierPubKey(){
        return paillierPubKey;
    }

    public PaillierCipher getPaillier() {
        return paillier;
    }

    public BulletinBoard getBoard(){
        return board;
    }

    public String getTitle() {
        return title;
    }

    public String getOwner() {
        return owner;
    }

    public String getCode() {
        return code;
    }

    public String getEid() {
        return eid;
    }
}
