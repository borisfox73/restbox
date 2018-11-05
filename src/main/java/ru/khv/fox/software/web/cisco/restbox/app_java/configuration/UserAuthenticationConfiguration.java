/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration for authentication-related beans for user-based authentication.
 * Instantiates Password Encoder and Authentication Manager bound to user repository
 * of user base from the application properties.
 */
@RequiredArgsConstructor
@Configuration
public class UserAuthenticationConfiguration {

	@NonNull private final AppProperties appProperties;


	// TODO may be replace by something more specific
	@NonNull private static final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();


	/**
	 * User Details Service.
	 * Not exposed as a bean.
	 *
	 * @return ReactiveUserDetailsService instance
	 */
	private MapReactiveUserDetailsService userDetailsService() {
		return new MapReactiveUserDetailsService(appProperties.getUsers()
		                                                      .stream()
		                                                      .map(u -> User.withUsername(u.getUsername())
		                                                                    .password(u.getPassword())
		                                                                    .roles(u.getRoles())
		                                                                    .passwordEncoder(passwordEncoder::encode)
		                                                                    .build())
		                                                      .toArray(UserDetails[]::new));
	}

	/**
	 * Password encoder bean to be used with user information.
	 * Ovverrides autoconfigured default password encoder ({@link PasswordEncoderFactories#createDelegatingPasswordEncoder}).
	 *
	 * @return Password encoder instance
	 */
	@Bean
	static PasswordEncoder passwordEncoder() {
		return passwordEncoder;
	}

	/**
	 * Authentication manager bean to be used in Login controller.
	 * Need to be a bean to prevent autoconfiguration.
	 *
	 * @return Authentication manager instance
	 */
	@Bean
	UserDetailsRepositoryReactiveAuthenticationManager userDetailsauthenticationManager() {
		return new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService());
	}
}
