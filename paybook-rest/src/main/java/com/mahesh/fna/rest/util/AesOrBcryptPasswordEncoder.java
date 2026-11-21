package com.mahesh.fna.rest.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class AesOrBcryptPasswordEncoder implements PasswordEncoder {

	private final BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder(12);
	private final String aesKey;

	public AesOrBcryptPasswordEncoder(String aesKey) {
		PasswordEncoderUtil.validateAesKey(aesKey);
		this.aesKey = aesKey;
	}

	@Override
	public String encode(CharSequence rawPassword) {
		return bcryptPasswordEncoder.encode(rawPassword);
	}

	@Override
	public boolean matches(CharSequence rawPassword, String storedPassword) {
		if (storedPassword == null || rawPassword == null) {
			return false;
		}
		if (storedPassword.startsWith("$2")) {
			return bcryptPasswordEncoder.matches(rawPassword, storedPassword);
		}

		try {
			String decryptedPassword = PasswordEncoderUtil.decrypt(storedPassword, aesKey);
			return MessageDigest.isEqual(rawPassword.toString().getBytes(StandardCharsets.UTF_8),
					decryptedPassword.getBytes(StandardCharsets.UTF_8));
		} catch (GeneralSecurityException | IllegalArgumentException ex) {
			throw new IllegalStateException(
					"Unable to verify AES-encrypted password. Check the configured app.password.aes-key.", ex);
		}
	}
}
