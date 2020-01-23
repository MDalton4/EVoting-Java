package com.evoting.resources;

import com.evoting.models.Proof;
import com.nimbusds.jose.util.BigIntegerUtils;

import java.io.*;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MixNet {

    private static KeyPair paillierKey; //paillier keys
    private static PaillierCipher paillier; //paillier cipher

    private CopyOnWriteArrayList<BigInteger> ballots;   //initial ballots
    private CopyOnWriteArrayList<BigInteger> primaryShuffle;   //primary shuffled ballots
    private CopyOnWriteArrayList<BigInteger> secondaryShuffle;  //secondary shuffled ballots

    private CopyOnWriteArrayList<Integer> primaryProof; //primary proof permutation data
    private CopyOnWriteArrayList<Integer> secondaryProof;   //secondary proof permutation data

    private CopyOnWriteArrayList<Proof> proofs; //proofs list

    private String eid; //election eid

    private BigInteger priR;    //primary random
    private BigInteger secR;    //secondary random

    public MixNet(KeyPair kp, PaillierCipher c, String id) {
        paillierKey = kp;
        paillier = c;
        eid = id;
        primaryShuffle = new CopyOnWriteArrayList<>();
        secondaryShuffle = new CopyOnWriteArrayList<>();

        primaryProof = new CopyOnWriteArrayList<>();
        secondaryProof = new CopyOnWriteArrayList<>();

        proofs = new CopyOnWriteArrayList<>();
    }

    /**
     * Mix method
     * Takes the initial ballots, re-encrypts each with a new random, shuffles list
     * @return shuffled ballots
     */
    public CopyOnWriteArrayList<BigInteger> mix() {
        BigInteger m;

        //get public key
        PaillierPubKey pk = (PaillierPubKey) paillierKey.getPublic();
        BigInteger n = pk.getN();
        BigInteger nsqr = pk.getNsqr();


        //new random value for re encryption
        priR = new BigInteger(64, new SecureRandom());
        secR = new BigInteger(64, new SecureRandom());

        //for each ballot
        for(int j=0; j<ballots.size(); j++) {
            m = ballots.get(j);

            //re encrypt
            BigInteger pc = m.multiply(priR.modPow(n, nsqr));
            BigInteger sc = m.multiply(secR.modPow(n, nsqr));

            primaryShuffle.add(pc);
            secondaryShuffle.add(sc);
        }

        primaryShuffle = shuffle(primaryShuffle, 'p');
        secondaryShuffle = shuffle(secondaryShuffle, 's');

        ballots = primaryShuffle;
        proof();

        return ballots;

    }

    /**
     * Shuffle method
     * Shuffles ballots psuedo random permutation
     * @param b ballot set
     * @param bSet type of ballot, primary or secondary
     * @return shuffled ballot set
     */
    public CopyOnWriteArrayList<BigInteger> shuffle(CopyOnWriteArrayList<BigInteger> b, char bSet) {
        int max = b.size(); //max values for rand
        int r;  //random r
        CopyOnWriteArrayList<BigInteger> newBal = b;   //new ballot set
        SecureRandom rand = new SecureRandom(); //random generator

        //for the each ballot
        for(int i=0; i<b.size(); i++) {
            //select a new random between max and i
            r = rand.nextInt(max-i);
            //get the value at r
            BigInteger randInt = newBal.get(r);
            //swap
            newBal.set(r, newBal.get(i));
            newBal.set(i, randInt);
            //keep track of swap
            //if primary shuffle, add to primary proof
            if(bSet=='p') {
                primaryProof.add(r);
            }
            //else secondary shuffle, add to secondary proof
            else  if(bSet=='s'){
                secondaryProof.add(r);
            }
        }
        return newBal;
    }

    /**
     * Proof method
     * Creates a proof based off fiat-shamir transform
     */
    private void proof() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");    //hash function
            byte[] byteArr = new byte[0];
            int size = 0;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();   //byte array stream to read bytes

            try {
                //for each value in shuffle array
                for (int i = 0; i < secondaryShuffle.size(); i++) {
                    //add the current byte array length to size
                    size += secondaryShuffle.get(i).toByteArray().length;
                    //write the bite array to the output stream
                    outputStream.write(secondaryShuffle.get(i).toByteArray());
                    //create new array with correct size
                    byteArr = new byte[size];
                }
                //set byte array
                byteArr = outputStream.toByteArray();

            } catch (IOException e) {
                System.err.println("IOException: "+e);
            }

            //hash message
            byte[] messageDigest = md.digest(byteArr);

            //turn hash byte array into biginteger
            BigInteger ba = new BigInteger(1, messageDigest);

            //add the new proof
            proofs.add(new Proof(priR, secR, primaryProof, secondaryProof, ba));

        } catch (NoSuchAlgorithmException e){
            System.err.println("No such algorithm "+ e);
        }
    }

    public void setBallots(CopyOnWriteArrayList<BigInteger> ballots) {
        this.ballots = ballots;
    }

    public CopyOnWriteArrayList<Proof> getProofs() {
        return proofs;
    }
}
