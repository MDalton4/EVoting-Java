package com.evoting.resources;

import java.math.BigInteger;
import java.security.PrivateKey;

public class PaillierPrivKey implements PrivateKey {

    private BigInteger lambda, n, nsqr, u;

    public PaillierPrivKey(BigInteger lambda1, BigInteger n1, BigInteger nsqr1, BigInteger u1) {
        lambda = lambda1;
        n = n1;
        nsqr = nsqr1;
        u = u1;
    }

    public BigInteger getLambda() {
        return lambda;
    }

    public BigInteger getN() {
        return n;
    }

    public BigInteger getNsqr() {
        return nsqr;
    }

    public BigInteger getU() {
        return u;
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
