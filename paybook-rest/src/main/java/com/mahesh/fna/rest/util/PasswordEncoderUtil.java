package com.mahesh.fna.rest.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class PasswordEncoderUtil {

	private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
	private static final int GCM_IV_LENGTH = 12; // 96-bit IV recommended for GCM
	private static final int GCM_TAG_LENGTH = 128; // 128-bit authentication tag

	private PasswordEncoderUtil() {

	}

	/**
	 * Encrypts plain text using AES-256-GCM and prepends the random IV.
	 */
	public static String encrypt(String plainText, String base64SecretKey) throws GeneralSecurityException {
		byte[] iv = new byte[GCM_IV_LENGTH];
		new SecureRandom().nextBytes(iv);
		Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, decodeKey(base64SecretKey), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
		byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
		ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
		byteBuffer.put(iv);
		byteBuffer.put(cipherText);
		return Base64.getEncoder().encodeToString(byteBuffer.array());
	}

	/**
	 * Decrypts a Base64 string encrypted by the encrypt() method above.
	 */
	public static String decrypt(String encryptedBase64, String base64SecretKey) throws GeneralSecurityException {
		byte[] decoded = Base64.getDecoder().decode(encryptedBase64);
		if (decoded.length < GCM_IV_LENGTH + GCM_TAG_LENGTH / Byte.SIZE) {
			throw new IllegalArgumentException("Encrypted password is too short to contain an AES-GCM IV and tag.");
		}
		ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
		byte[] iv = new byte[GCM_IV_LENGTH];
		byteBuffer.get(iv);
		byte[] cipherText = new byte[byteBuffer.remaining()];
		byteBuffer.get(cipherText);
		Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, decodeKey(base64SecretKey), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
		return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
	}

	static void validateAesKey(String base64SecretKey) {
		decodeKey(base64SecretKey);
	}

	private static SecretKey decodeKey(String base64SecretKey) {
		byte[] decodedKey = Base64.getDecoder().decode(base64SecretKey);
		if (decodedKey.length != 16 && decodedKey.length != 24 && decodedKey.length != 32) {
			throw new IllegalArgumentException("AES key must decode to 16, 24, or 32 bytes.");
		}
		return new SecretKeySpec(decodedKey, "AES");
	}

	/**
	 * Helper to generate a new 256-bit AES key encoded as Base64. Store this
	 * securely in an environment variable or vault.
	 */
	public static String generateAesKey() throws GeneralSecurityException {
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(256);
		return Base64.getEncoder().encodeToString(keyGen.generateKey().getEncoded());
	}

}
