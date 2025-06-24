package com.hotel.chatbox;

import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import io.jsonwebtoken.SignatureAlgorithm;

public class GenerateHs512Key {
    public static void main(String[] args) {
        // Generates a secure random key specifically for HS512 algorithm
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String base64EncodedKey = Encoders.BASE64.encode(key.getEncoded());
        System.out.println("Generated HS512 Base64 Encoded Key: " + base64EncodedKey);
        System.out.println("Decoded Key Length (bytes): " + key.getEncoded().length); // Should be 64
    }
}