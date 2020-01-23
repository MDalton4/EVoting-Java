package com.evoting.models;

import java.math.BigInteger;
import java.util.concurrent.CopyOnWriteArrayList;

public class Proof {
    private BigInteger hash;

    private BigInteger primaryR, secondaryR;    //randoms used in mix

    private CopyOnWriteArrayList<Integer> primaryShuffle;   //primary shuffle of ballots
    private CopyOnWriteArrayList<Integer> secondaryShuffle; //secondary shuffle of ballots

    public Proof(BigInteger p, BigInteger s, CopyOnWriteArrayList<Integer> ps, CopyOnWriteArrayList<Integer> ss, BigInteger h) {
        primaryR = p;
        secondaryR = s;
        primaryShuffle = ps;
        secondaryShuffle = ss;
        hash = h;
    }

    public Proof(BigInteger s, CopyOnWriteArrayList<Integer> ss, BigInteger h) {
        secondaryR = s;
        secondaryShuffle = ss;
        hash = h;
    }

    public BigInteger getHash() {
        return hash;
    }

    public void setHash(BigInteger hash) {
        this.hash = hash;
    }

    public void setPrimaryR(BigInteger primaryR) {
        this.primaryR = primaryR;
    }

    public void setSecondaryR(BigInteger secondaryR) {
        this.secondaryR = secondaryR;
    }

    public void setPrimaryShuffle(CopyOnWriteArrayList<Integer> primaryShuffle) {
        this.primaryShuffle = primaryShuffle;
    }

    public void setSecondaryShuffle(CopyOnWriteArrayList<Integer> secondaryShuffle) {
        this.secondaryShuffle = secondaryShuffle;
    }

    public BigInteger getPrimaryR() {
        return primaryR;
    }

    public BigInteger getSecondaryR() {
        return secondaryR;
    }

    public CopyOnWriteArrayList<Integer> getPrimaryShuffle() {
        return primaryShuffle;
    }

    public CopyOnWriteArrayList<Integer> getSecondaryShuffle() {
        return secondaryShuffle;
    }
}
