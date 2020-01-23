package com.evoting.models;

import java.math.BigInteger;
import java.util.concurrent.CopyOnWriteArrayList;

public class BulletinBoard {
    private CopyOnWriteArrayList<BigInteger> ballots;   //ballots encrypted once
    private CopyOnWriteArrayList<BigInteger> shuffledBallots;   //ballots shuffled and re-encrypted

    public BulletinBoard() {
        ballots = new CopyOnWriteArrayList<>();
    }

    public void addVote(BigInteger v) {
        ballots.add(v);
    }

    public CopyOnWriteArrayList<BigInteger> getBallots() {
        return ballots;
    }

    public void setBallots(CopyOnWriteArrayList<BigInteger> ballots) {
        this.ballots = ballots;
    }

    public void setShuffledBallots(CopyOnWriteArrayList<BigInteger> sballots) {
        this.shuffledBallots = sballots;
    }

    public CopyOnWriteArrayList<BigInteger> getShuffledBallots() {
        return shuffledBallots;
    }
}
