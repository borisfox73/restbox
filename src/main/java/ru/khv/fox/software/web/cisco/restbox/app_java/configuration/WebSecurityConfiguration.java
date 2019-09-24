/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.springframework.boot.autoconfigure.security.reactive.PathRequest;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import ru.khv.fox.software.web.cisco.restbox.app_java.security.*;

import java.util.Collections;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
class WebSecurityConfiguration {
	private static final String LOGIN_ENDPOINT = "/login";

	@NonNull
	JwtService jwtService;
	@NonNull
	RestApiAuthenticationEntryPoint authenticationEntryPoint;
	@NonNull
	RestApiAccessDeniedHandler accessDeniedHandler;


	// Autowiring beans
	WebSecurityConfiguration(@NonNull final ErrorWebExceptionHandler exceptionHandler,
	                         @NonNull final JwtService jwtService) {
		this.jwtService = jwtService;
		this.authenticationEntryPoint = new RestApiAuthenticationEntryPoint(exceptionHandler);
		this.accessDeniedHandler = new RestApiAccessDeniedHandler(exceptionHandler);
	}

	/*
	 * Authentication filter to authenticate by JWT tokens.
	 */
	private AuthenticationWebFilter jwtAuthenticationWebFilter() {
		val restMatcher = new MediaTypeServerWebExchangeMatcher(MediaType.APPLICATION_JSON);
		restMatcher.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
		val authenticationFilter = new AuthenticationWebFilter(new JwtReactiveAuthenticationManager(jwtService));
		authenticationFilter.setRequiresAuthenticationMatcher(restMatcher);
		authenticationFilter.setServerAuthenticationConverter(new ServerHttpJwtAuthenticationConverter());
		authenticationFilter.setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(authenticationEntryPoint));
		return authenticationFilter;
	}

	/**
	 * Security Configuration bean.
	 *
	 * @param http ServerHttpSecurity object
	 *
	 * @return SecurityWebFilterChain instance
	 */
	@Bean
	SecurityWebFilterChain securityWebFilterChain(@NonNull final ServerHttpSecurity http) {
		// @formatter:off
		return http.csrf().disable()
		           .logout().disable()
		           .authenticationManager(null) // discard autoconfigured from a bean in ServerHttpSecurityConfiguration
		           .addFilterAt(jwtAuthenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
	               .exceptionHandling()     // для всех фильтров, в том числе authorization
	                    .authenticationEntryPoint(authenticationEntryPoint)
		                .accessDeniedHandler(accessDeniedHandler)
	               .and()
				        .authorizeExchange()
		                    .matchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
		                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
		                    .pathMatchers("/", "/favicon.ico", "/views/**").permitAll() // Website resources
		                    .pathMatchers(LOGIN_ENDPOINT).permitAll()            // API Login endpoint
		                    .pathMatchers("/api/**").permitAll()            // Rest Boxes endpoints
		                    .pathMatchers("/webapi/**").authenticated()     // Single page web app endpoints
		                    .pathMatchers("/jsontest").hasAuthority("ROLE_ADMIN")  // Test endpoint
		                .anyExchange()  // any other paths
		                    .authenticated()
		           .and()
		                .build();
		// @formatter:on
	}
}
