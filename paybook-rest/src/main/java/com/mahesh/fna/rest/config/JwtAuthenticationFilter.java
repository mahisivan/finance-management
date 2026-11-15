package com.mahesh.fna.rest.config;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mahesh.fna.rest.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtService jwtService;
	
	@Autowired
	private UserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String token = null;

		if (request.getCookies() != null) {
			token = Arrays.stream(request.getCookies()).filter(c -> "JWT_TOKEN".equals(c.getName()))
					.map(Cookie::getValue).findFirst().orElse(null);
		}

		if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {

			try {
				String username = jwtService.extractUsername(token);

				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				if (jwtService.isTokenValid(token) && userDetails.isEnabled() && userDetails.isAccountNonLocked()) {

					var authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
							userDetails.getAuthorities());

					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					SecurityContextHolder.getContext().setAuthentication(authentication);
				}

			} catch (Exception e) {
				SecurityContextHolder.clearContext();
			}
		}

		filterChain.doFilter(request, response);
	}
}