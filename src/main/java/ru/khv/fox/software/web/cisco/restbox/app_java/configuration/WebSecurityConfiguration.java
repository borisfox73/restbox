/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import lombok.RequiredArgsConstructor;
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
import ru.khv.fox.software.web.cisco.restbox.app_java.security.JwtReactiveAuthenticationManager;
import ru.khv.fox.software.web.cisco.restbox.app_java.security.JwtUtility;
import ru.khv.fox.software.web.cisco.restbox.app_java.security.ServerHttpJwtAuthenticationConverter;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.RestApiAuthEntryPoint;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class WebSecurityConfiguration {
	private static final String LOGIN_ENDPOINT = "/login";

	@NonNull private final ErrorWebExceptionHandler exceptionHandler;
	@NonNull private final JwtUtility jwtUtility;
	private RestApiAuthEntryPoint authenticationEntryPoint;


	/*
	 * Authentication filter to authenticate by JWT tokens.
	 */
	private AuthenticationWebFilter jwtAuthenticationWebFilter() {
		val restMatcher = new MediaTypeServerWebExchangeMatcher(MediaType.APPLICATION_JSON);
		restMatcher.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
		val authenticationFilter = new AuthenticationWebFilter(new JwtReactiveAuthenticationManager(jwtUtility));
		authenticationFilter.setRequiresAuthenticationMatcher(restMatcher);
		authenticationFilter.setAuthenticationConverter(new ServerHttpJwtAuthenticationConverter());
		authenticationFilter.setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(authenticationEntryPoint()));
		return authenticationFilter;
	}

	/*
	 * Authentication entry point.
	 */
	private RestApiAuthEntryPoint authenticationEntryPoint() {
		if (authenticationEntryPoint == null)
			authenticationEntryPoint = new RestApiAuthEntryPoint(exceptionHandler);
		return authenticationEntryPoint;
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
//		           .headers().disable()     // TODO need? adds http response headers for caching and protection control
		           .authenticationManager(null) // discard autoconfigured from a bean in ServerHttpSecurityConfiguration
		           .addFilterAt(jwtAuthenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
	               .exceptionHandling()
	                    .authenticationEntryPoint(authenticationEntryPoint())
	               .and()
				        .authorizeExchange()
		                    .matchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
		                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
		                    .pathMatchers(HttpMethod.POST, LOGIN_ENDPOINT).permitAll()
//		                    .pathMatchers("/admin").hasAuthority("ROLE_ADMIN")  // TODO cleanup test
		                .anyExchange()
		                    .authenticated()
		           .and()
		                .build();
		// @formatter:on
	}
}
