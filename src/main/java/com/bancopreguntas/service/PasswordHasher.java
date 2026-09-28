package com.bancopreguntas.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

public final class PasswordHasher {

    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String password) {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return HexFormat.of().formatHex(salt) + ":" + HexFormat.of().formatHex(derive(password, salt));
    }

    public static boolean verificar(String password, String encoded) {
        if (encoded == null) return false;
        String[] parts = encoded.split(":", 2);
        if (parts.length != 2) return false;
        try {
            byte[] salt = HexFormat.of().parseHex(parts[0]);
            byte[] expected = HexFormat.of().parseHex(parts[1]);
            return MessageDigest.isEqual(expected, derive(password, salt));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo verificar la contraseña con PBKDF2.", e);
        } finally {
            spec.clearPassword();
        }
    }
}
