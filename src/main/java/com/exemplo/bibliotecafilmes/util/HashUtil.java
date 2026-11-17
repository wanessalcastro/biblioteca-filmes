package com.exemplo.bibliotecafilmes.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Utilitário de hash SHA-256 (usado para guardar chaves de API e impressões digitais de requisições). */
public final class HashUtil {

    private HashUtil() {
    }

    public static String sha256Hex(String texto) {
        return sha256Hex(texto.getBytes(StandardCharsets.UTF_8));
    }

    public static String sha256Hex(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível na JVM", e);
        }
    }
}
