package com.mahesh.fna.rest.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mahesh.cf.customexeception.AppBaseExceptionLogger;

class AesOrBcryptPasswordEncoderTest {
	
	@BeforeAll
	public static void setUpClass() {
		System.setProperty("logEnabledFlag", "true");
	}

	@AfterAll
	public static void tearDownClass() {
		
	}
	
	private static final String APP_PASSWORD_AES_KEY = "VPgaUGcVHFzs8juQmGdB1F32/w+vJa3BbdPc5p7PJGs=";

	@Test
	void matchesAesEncryptedPasswordUsingConfiguredKey() throws Exception {
		String encryptedPassword = PasswordEncoderUtil.encrypt("Password#2026", APP_PASSWORD_AES_KEY);
		AppBaseExceptionLogger.printInfoMessage("AES_KEY", APP_PASSWORD_AES_KEY);
		AppBaseExceptionLogger.printInfoMessage("Encrypted Password", encryptedPassword);
		System.out.println(encryptedPassword);
		AesOrBcryptPasswordEncoder encoder = new AesOrBcryptPasswordEncoder(APP_PASSWORD_AES_KEY);
		assertTrue(encoder.matches("Password#2026", encryptedPassword));
		assertFalse(encoder.matches("wrong-password", encryptedPassword));
	}

	@Test
	void matchesBcryptPasswordAndEncodesNewPasswordsWithBcrypt() {
		AesOrBcryptPasswordEncoder encoder = new AesOrBcryptPasswordEncoder(APP_PASSWORD_AES_KEY);
		String encodedPassword = encoder.encode("Password#2026");
		AppBaseExceptionLogger.printInfoMessage("AES_KEY", APP_PASSWORD_AES_KEY);
		AppBaseExceptionLogger.printInfoMessage("Encrypted Password", encodedPassword);
		assertTrue(encoder.matches("Password#2026", encodedPassword));
		assertFalse(encoder.matches("wrong-password", encodedPassword));
	}
}
