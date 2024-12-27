package main.java.controllers;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class HashClass {
    private final String pwd;
    private byte[] salt;
    private byte[] hash;

    HashClass(String pwd) {
        this(null, pwd); // Delegate to the second constructor
    }

    HashClass(byte[] salt, String pwd) {
        this.pwd = pwd;
        this.salt = salt;

        hashPassword();
    }

    private void hashPassword() {
        try {
            if (salt == null) {
                hash();
            } else {
                hashWithSalt();
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.err.println("Failed to hash password: " + e.getMessage());
        }
    }

    private void hash() throws NoSuchAlgorithmException, InvalidKeySpecException {
        final var random = new SecureRandom();
        salt = new byte[32];
        random.nextBytes(salt);

        final var spec = new PBEKeySpec(pwd.toCharArray(), salt, 65536, 128);
        final var factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        hash = factory.generateSecret(spec).getEncoded();
    }

    private void hashWithSalt() throws NoSuchAlgorithmException, InvalidKeySpecException {
        final var spec = new PBEKeySpec(pwd.toCharArray(), salt, 65536, 128);
        final var factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        hash = factory.generateSecret(spec).getEncoded();
    }

    public byte[] getHash() {
        return hash;
    }

    public byte[] getSalt() {
        return salt;
    }

    public boolean compareHashes(byte[] x, byte[] y) {
        return Arrays.equals(x, y);
    }
}