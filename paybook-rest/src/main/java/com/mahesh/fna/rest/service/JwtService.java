package com.mahesh.fna.rest.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service(value = "jwtService")
public class JwtService {

	@Value("${app.jwt.secret}")
	private String secret;

	@Value("${app.jwt.expiration}")
	private long expiration;

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
	}

	public String generateToken(String username) {

		Date now = new Date();

		return Jwts.builder().subject(username).issuedAt(now).expiration(new Date(now.getTime() + expiration))
				.signWith(getSigningKey()).compact();
	}

	public String extractUsername(String token) {

		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().getSubject();
	}

	public boolean isTokenValid(String token) {
		try {
			extractUsername(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
