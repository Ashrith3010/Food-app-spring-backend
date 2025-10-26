
package com.food_api.food_api.service.jwt;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class JwtKeyGenerator {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        // Generate a 256-bit (32-byte) secret key
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
        keyGen.init(256);
        SecretKey key = keyGen.generateKey();

        // Convert to Base64
        String secretKey = Base64.getEncoder().encodeToString(key.getEncoded());

        System.out.println("Generated Secret Key (copy this to your application.properties):");
        System.out.println(secretKey);
    }
}
