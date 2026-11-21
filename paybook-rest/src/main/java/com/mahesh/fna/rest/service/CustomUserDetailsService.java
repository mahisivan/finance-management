package com.mahesh.fna.rest.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mahesh.fna.rest.dao.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String emailId) throws UsernameNotFoundException {
		var user = userRepository.findByEmailId(emailId)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + emailId));
		return User.builder()
				.username(user.getEmailId())
				.password(user.getLoginPassword())
				.roles("ADMIN")
				.build();
	}
}