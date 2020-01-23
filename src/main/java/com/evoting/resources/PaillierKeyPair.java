package com.evoting.resources;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;

public class PaillierKeyPair {

    PaillierPubKey pubKey;
    PaillierPrivKey privKey;

    /**
     * Generate keys method
     * Generates a public and private paillier key pair
     * @return key pair
     */
    public KeyPair generateKeys() {
        //generate two random primes
        BigInteger p = new BigInteger(128, 64, new SecureRandom());
        BigInteger q = new BigInteger(128, 64, new SecureRandom());

        //LCM = p * (q / gcd(p, q))
        BigInteger lambda = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE))
                .divide(p.subtract(BigInteger.ONE).gcd(q.subtract(BigInteger.ONE)));

        //n = p*q
        BigInteger n = p.multiply(q);
        //nsqr = n*n
        BigInteger nsqr = n.multiply(n);
        //g = n+1
        BigInteger g = n.add(BigInteger.ONE);

        //u = L(g^lambda mod n^2)^-1 mod n and L(x) = L(x-1)/n
        BigInteger u = g.modPow(lambda, nsqr).subtract(BigInteger.ONE).divide(n).modInverse(n);

        //create pub and priv keys
        pubKey = new PaillierPubKey(n, nsqr, g);
        privKey = new PaillierPrivKey(lambda, n, nsqr, u);

        //return them as keypair
        return new KeyPair(pubKey, privKey);
    }

    //return publickey
    public PaillierPubKey getPubKey() {
        return pubKey;
    }

    //return private key
    public PaillierPrivKey getPrivKey() {
        return privKey;
    }
}
