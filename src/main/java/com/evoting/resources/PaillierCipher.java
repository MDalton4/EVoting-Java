package com.evoting.resources;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;

public class PaillierCipher {

    private KeyPair paillierKeys;
    private BigInteger r;

    public PaillierCipher(KeyPair kp) {
        paillierKeys = kp;
    }

    /**
     * Encryption method
     * Encrypts a big integer
     * @param m input message m
     * @return encrypted value
     */
    public BigInteger encrypt(BigInteger m) {
        //public key info for encryption
        PaillierPubKey pk = (PaillierPubKey) paillierKeys.getPublic();

        BigInteger n = pk.getN();
        BigInteger nsqr = pk.getNsqr();
        BigInteger g = pk.getG();

        //randomness r
        r = new BigInteger(32, new SecureRandom());

        //ciphertext = g^m * r^n mod n^2
        return g.modPow(m, nsqr).multiply(r.modPow(n, nsqr)).mod(nsqr);
    }

    /**
     * Decryption method
     * Decrypts a big integer
     * @param c ciphertext input
     * @return decrypted value
     */
    public BigInteger decrypt(BigInteger c) {
        //private key info for decryption
        PaillierPrivKey sk = (PaillierPrivKey) paillierKeys.getPrivate();

        //get key info
        BigInteger lambda = sk.getLambda();
        BigInteger n = sk.getN();
        BigInteger nsqr = sk.getNsqr();
        BigInteger u = sk.getU();

        //Dec = L(c^lambda mod n^2) * u mod n
        return c.modPow(lambda, nsqr).subtract(BigInteger.ONE).divide(n).multiply(u).mod(n);
    }

    public BigInteger getR() {
        return r;
    }
}
