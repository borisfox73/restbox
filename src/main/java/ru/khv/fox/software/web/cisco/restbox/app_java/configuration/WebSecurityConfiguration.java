/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import lombok.val;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.LoginResponse;
import ru.khv.fox.software.web.cisco.restbox.app_java.security.*;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.WebRequestHelper;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.WebResponseHelper;

import java.util.Collections;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class WebSecurityConfiguration {
	private static final String LOGIN_ENDPOINT = "/login";

	@NonNull private final UserDetailsRepositoryReactiveAuthenticationManager userAuthenticationManager;
	@NonNull private final WebRequestHelper webRequestHelper;
	@NonNull private final WebResponseHelper webResponseHelper;
	@NonNull private final JwtService jwtService;
	@NonNull private final RestApiAuthenticationEntryPoint authenticationEntryPoint;
	@NonNull private final RestApiAccessDeniedHandler accessDeniedHandler;


	// Autowiring beans
	WebSecurityConfiguration(@NonNull final UserDetailsRepositoryReactiveAuthenticationManager userAuthenticationManager,
	                         @NonNull final ErrorWebExceptionHandler exceptionHandler,
	                         @NonNull final WebRequestHelper webRequestHelper,
	                         @NonNull final WebResponseHelper webResponseHelper,
	                         @NonNull final JwtService jwtService) {
		this.userAuthenticationManager = userAuthenticationManager;
		this.webRequestHelper = webRequestHelper;
		this.webResponseHelper = webResponseHelper;
		this.jwtService = jwtService;
		this.authenticationEntryPoint = new RestApiAuthenticationEntryPoint(exceptionHandler);
		this.accessDeniedHandler = new RestApiAccessDeniedHandler(exceptionHandler);
	}

// TODO сделать фильтр для /login endpoint

	/*
	 * Authentication filter to authenticate by Login request object.
	 */
	private AuthenticationWebFilter loginAuthenticationWebFilter() {
		val restMatcher = new MediaTypeServerWebExchangeMatcher(MediaType.APPLICATION_JSON);
		restMatcher.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
		val loginPathMatcher = new PathPatternParserServerWebExchangeMatcher(LOGIN_ENDPOINT, HttpMethod.POST);
		val loginEndpointMatcher = new AndServerWebExchangeMatcher(loginPathMatcher, restMatcher);
		val authenticationFilter = new AuthenticationWebFilter(new LoginReactiveAuthenticationManager(userAuthenticationManager, jwtService));
		authenticationFilter.setRequiresAuthenticationMatcher(loginEndpointMatcher);
		authenticationFilter.setAuthenticationConverter(new ServerHttpLoginAuthenticationConverter(webRequestHelper));
		authenticationFilter.setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(authenticationEntryPoint));
		authenticationFilter.setAuthenticationSuccessHandler(new ResponseBodyServerAuthenticationSuccessHandler(webResponseHelper, LoginResponse::from, HttpStatus.CREATED));
		return authenticationFilter;
	}

	/*
	 * Authentication filter to authenticate by JWT tokens.
	 */
	private AuthenticationWebFilter jwtAuthenticationWebFilter() {
		val restMatcher = new MediaTypeServerWebExchangeMatcher(MediaType.APPLICATION_JSON);
		restMatcher.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
		val authenticationFilter = new AuthenticationWebFilter(new JwtReactiveAuthenticationManager(jwtService));
		authenticationFilter.setRequiresAuthenticationMatcher(restMatcher);
		authenticationFilter.setAuthenticationConverter(new ServerHttpJwtAuthenticationConverter());
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
//		           .headers().disable()     // TODO need? adds http response headers for caching and protection control
		           .authenticationManager(null) // discard autoconfigured from a bean in ServerHttpSecurityConfiguration
		           .addFilterAt(loginAuthenticationWebFilter(), SecurityWebFiltersOrder.HTTP_BASIC)
		           .addFilterAt(jwtAuthenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
	               .exceptionHandling()     // для всех фильтров, в том числе authorization. TODO включить обратно
	                    .authenticationEntryPoint(authenticationEntryPoint)
		                .accessDeniedHandler(accessDeniedHandler)
/* now relies on method security
	               .and()
				        .authorizeExchange()
		                    .matchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
		                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//		                    .pathMatchers(HttpMethod.POST, LOGIN_ENDPOINT).permitAll()  // TODO cleanup, now implemented as filter
//		                    .pathMatchers("/jsontest").hasAuthority("ROLE_ADMIN")  // TODO cleanup test
		                .anyExchange()
		                    .authenticated()
*/
		           .and()
		                .build();
		// @formatter:on
	}
}
