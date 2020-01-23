package com.evoting.resources;

import java.math.BigInteger;
import java.security.PublicKey;

public class PaillierPubKey implements PublicKey {

    private BigInteger n, nsqr, g;

    public PaillierPubKey(BigInteger n1, BigInteger nsqr1, BigInteger g1) {
        n = n1;
        nsqr = nsqr1;
        g = g1;
    }

    public BigInteger getN() {
        return n;
    }

    public BigInteger getNsqr() {
        return nsqr;
    }

    public BigInteger getG() {
        return g;
    }

    @Override
    public String getAlgorithm() {
        return "Paillier";
    }

    @Override
    public String getFormat() {
        return null;
    }

    @Override
    public byte[] getEncoded() {
        return new byte[0];
    }
}
