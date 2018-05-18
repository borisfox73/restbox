/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Configuration
public class UserAuthenticationConfiguration {
/*
	@Bean
	PasswordEncoder passwordEncoder() {
		// TODO may be revert to more specific
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
*/

	/**
	 * User Details Service bean.
	 *
	 * @return ReactiveUserDetailsService instance
	 */
	private static MapReactiveUserDetailsService userDetailsService(@NonNull final Collection<AppProperties.UserProperties> users) {
		// TODO cleanup test settings
		//noinspection deprecation
		return new MapReactiveUserDetailsService(users.stream()
		                                              .map(u -> User.withDefaultPasswordEncoder()
		                                                            .username(u.getUsername())
		                                                            .password(u.getPassword())
		                                                            .roles(u.getRoles())
//		                                                                    .disabled(true)
//		                                                                    .accountLocked(true)
//		                                                                    .accountExpired(true)
//		                                                                    .credentialsExpired(true)
                                                                    .build())
		                                              .toArray(UserDetails[]::new));
/*
		return new MapReactiveUserDetailsService(appProperties.getUsers().stream()
		                                                      .map(u -> User.withUsername(u.getUsername())
		                                                                    .password(u.getPassword())
		                                                                    .roles(u.getRoles())
		                                                                    .passwordEncoder(passwordEncoder()::encode)
		                                                                    .build())
		                                                      .toArray(UserDetails[]::new));
*/
	}

	/**
	 * Authentication manager bean to be used in Login controller.
	 * Need to be a bean to prevent autoconfiguration.
	 *
	 * @return Authentication manager instance
	 */
	@Bean
	static UserDetailsRepositoryReactiveAuthenticationManager userDetailsauthenticationManager(@NonNull final AppProperties appProperties) {
		// default password encoder is PasswordEncoderFactories#createDelegatingPasswordEncoder
		return new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService(appProperties.getUsers()));
	}
}
