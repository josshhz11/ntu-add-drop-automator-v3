package com.ntu.adddrop.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class EncryptionService {
    
    @Value("${app.encryption.key}")
    private String encryptionKey;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /* Encrypts plaintext using AES encryption */
    public String encrypt(String plainText) {
        try {
            // Create a key from the encryption key (hash it to ensure 32 bytes)
            SecretKeySpec secretKey = createSecretKey(encryptionKey);

            // Initialize cipher for encryption
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // Encrypt the plaintext
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Return Base64 encoded result
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /* Decrypts encrypted text using AES decryption */
    public String decrypt(String encryptedText) {
        try {
            // Create a key from the encryption key
            SecretKeySpec secretKey = createSecretKey(encryptionKey);

            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // Decode Base64 and decrypt
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            // Return decrypted string
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }

    /* Creates a proper AES key from the provided key string */
    private SecretKeySpec createSecretKey(String key) {
        try {
            // Hash the key to ensure it's exactly 32 bytes (256 bits) for AES-256
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(key.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create encryption key");
        }
    }

    /* Utility method to validate if text is likely encrypted */
    public boolean isEncrypted(String text) {
        try {
            // Try to decode as Base64 - if it fails, probably not encoded
            Base64.getDecoder().decode(text);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
