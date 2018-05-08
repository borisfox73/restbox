/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.reactive.PathRequest;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.annotation.Nonnull;

@Configuration
@RequiredArgsConstructor
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {
	@Nonnull private final AppProperties appProperties;
	// for customized JSON error response in Authentication Entry Point
	@NonNull private final ErrorWebExceptionHandler exceptionHandler;


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
	private MapReactiveUserDetailsService userDetailsService() {
		// TODO cleanup test settings
		//noinspection deprecation
		return new MapReactiveUserDetailsService(appProperties.getUsers().stream()
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
	 *
	 * @return Authentication manager instance
	 */
	@Bean
	UserDetailsRepositoryReactiveAuthenticationManager authenticationManager() {
		// default password encoder is PasswordEncoderFactories#createDelegatingPasswordEncoder
		return new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService());
	}

	// TODO bean? for injecting into Login controller
	private CustomAuthEntryPoint authenticationEntryPoint() {
		return new CustomAuthEntryPoint(exceptionHandler);
	}


	/**
	 * Security Configuration bean.
	 *
	 * @param http ServerHttpSecurity object
	 *
	 * @return SecurityWebFilterChain instance
	 */
	@Bean
	SecurityWebFilterChain securityWebFilterChain(@Nonnull final ServerHttpSecurity http) {
		// TODO cleanup
/*
		http.csrf().disable()
		    .logout().disable()
		    .formLogin().disable()
		    .httpBasic().disable()
		    .addFilterAt(filter, SecurityWebFiltersOrder.AUTHENTICATION)
			.and().build();
*/
		// UserDetailsRepositoryReactiveAuthenticationManager based on this MapReactiveUserDetailsService bean is already added to http.

		// @formatter:off
		return http.csrf().disable()
		           .logout().disable()
		           .formLogin().disable()   // already none
		           .httpBasic().disable()   // already none
		           .headers().disable()     // TODO need? adds http response headers
		           .authenticationManager(null) // discard autoconfigured from a bean in ServerHttpSecurityConfiguration
//		           .addFilterAt(filter, SecurityWebFiltersOrder.AUTHENTICATION)
	               .exceptionHandling()
	                    .authenticationEntryPoint(authenticationEntryPoint())
	               .and()
				        .authorizeExchange()
		                    .matchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()   // TODO really need in this app?
		                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
		                    .pathMatchers("/login").permitAll()
		                    .pathMatchers("/admin").hasAuthority("ROLE_ADMIN")  // FIXME test
		                .anyExchange()
		                    .authenticated()
		           .and()
		                .build();
/*
		return http.authorizeExchange()
                   .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                   .matchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                   .pathMatchers("/admin").hasAuthority("ROLE_ADMIN")
                   .anyExchange().authenticated()
		           .and().formLogin()
                   .and().build();
*/
		// @formatter:on
// TODO create /login endpoint to acquire credentials from LoginRequest JSON object and compose JWT for response.
// TODO add security web filter to initialize security context from JWT passed in Authorization Bearer header (?).
// TODO custom exceptions should inherit ResponseStatusException or be wrapped in it.
	}
}
