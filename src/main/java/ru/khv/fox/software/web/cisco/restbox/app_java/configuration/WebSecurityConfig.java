/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

//@Slf4j
//@Configuration
//// @EnableWebSecurity
//public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
//
///*
//  @Bean
//  @Primary
//  UserDetailsService userDetailsService(final AppProperties appProperties) throws Exception {
//      final InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
//      foreach(final AppProperties.UserProperties u : appProperties.getUsers()) {
//          manager.createUser(User.withDefaultPasswordEncoder()1111111
//  		    .username(u.getUsername())
//  		    .password(u.getPassword())
//  		    .roles(u.getRole())
//  		    .build());
//      }
//  	return manager;
//  }
//*/
//
//	/**
//	 * Configure global authentication manager.
//	 *
//	 * @param builder   Authentication manager builder
//	 */
//	@Autowired
//	public void initialize(final AuthenticationManagerBuilder builder, final AppProperties appProperties) throws Exception {
//		final InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> conf = builder.inMemoryAuthentication();
//		appProperties.getUsers().forEach(u -> conf.withUser(u.getUsername()).password(u.getPassword()).roles(u.getRoles()));
//	}
//
//	// TODO how to use built-in properties-based default user?
////	@Override
////	protected void configure(@NonNull final AuthenticationManagerBuilder auth) throws Exception {
////		auth.inMemoryAuthentication()
////		    .withUser("user1")
////		    .password("{noop}user123")
////		    .roles("USER");
////      foreach(final AppProperties.UserProperties u : appProperties.getUsers()) {
////  		auth.inMemoryAuthentication()
////	    	    .withUser(u.getUsername())
////		        .password(u.getPassword())
////		        .roles(u.getRole());
////      }
////	}
//
////	/**
////	 * Expose default Authentication Manager as a bean.
////	 *
////	 * @return Authentication manager bean
////	 *
////	 * @throws Exception On instantiation errors
////	 */
////	@Bean
////	@Override
////	public AuthenticationManager authenticationManagerBean() throws Exception {
////		return super.authenticationManagerBean();
////	}
//
//
//	@Bean
//	JsonAuthenticationFilter jsonAuthenticationFilter() throws Exception {
//		final JsonAuthenticationFilter filter = new JsonAuthenticationFilter();
//		filter.setAuthenticationManager(authenticationManagerBean());
//		return filter;
//	}
//
//	private RestAuthenticationSuccessHandler authenticationSuccessHandler() {
//		return new RestAuthenticationSuccessHandler();
//	}
//
//	private RestAuthenticationFailureHandler authenticationFailureHandler() {
//		return new RestAuthenticationFailureHandler();
//	}
//
//	private RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
//		return new RestAuthenticationEntryPoint("/login");
//	}
//
//	@Override
//	protected void configure(@NonNull final HttpSecurity http) throws Exception {
//		// @formatter:off
////		http.addFilterBefore(jsonAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
//		http.csrf().disable()
//		    .sessionManagement().disable()
//		    .authorizeRequests()
//		        .antMatchers( "/login", "/logout").permitAll()
//		        .anyRequest().authenticated()
////		    .antMatchers("/services/anonymous/**").permitAll()
////		    .antMatchers("/services/authenticated/**").authenticated()
//			.and().addFilterAt(jsonAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
//		    .formLogin().loginProcessingUrl("/login")
////		                      .usernameParameter("username")
////		                      .passwordParameter("password")
//		                      .successHandler(authenticationSuccessHandler())
//		                      .failureHandler(authenticationFailureHandler())
////		    .and().logout().logoutUrl("/logout")
//			.and().exceptionHandling().authenticationEntryPoint(restAuthenticationEntryPoint());
//		// @formatter:off
//	}
//
//
//	private static class JsonAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
//
//		@Override
//		public Authentication attemptAuthentication(@NonNull final HttpServletRequest request,
//		                                            @NonNull final HttpServletResponse response)
//				throws AuthenticationException {
//			if (!request.getMethod().equals("POST"))
//				throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
//
//			final UsernamePasswordAuthenticationToken authRequest;
//			try {
//				authRequest = getUserNamePasswordAuthenticationToken(request);
//				log.debug("auth request: {}", authRequest);
//			} catch (Exception e) {
//				throw new AuthenticationServiceException("Obtain Authentication request parameters exception", e);
//			}
//			if (authRequest == null)
//				throw new AuthenticationServiceException("No username and password parameters in the Authentication request");
//
//			// Allow subclasses to set the "details" property
//			setDetails(request, authRequest);
//
//			return this.getAuthenticationManager().authenticate(authRequest);
//		}
//
//		@Nullable
//		private UsernamePasswordAuthenticationToken getUserNamePasswordAuthenticationToken(@NonNull final HttpServletRequest request)
//				throws Exception {
//			UsernamePasswordAuthenticationToken at = null;
//
//			if ("application/json".equals(request.getHeader("Content-Type")))
//				try (final BufferedReader br = request.getReader()) {
//					final LoginRequest sr = new ObjectMapper().readValue(br, LoginRequest.class);
//					log.debug("sr = {}", sr);
//					at = new UsernamePasswordAuthenticationToken(sr.getUsername(), sr.getPassword());
//				}
//			return at;
//		}
//	}
//
//	// FIXME success handler did not get called on authentication success
//
//	// TODO brush it up
//	private static class RestAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//
//		@Override
//		public void onAuthenticationSuccess(@NonNull final HttpServletRequest request,
//											@NonNull final HttpServletResponse response,
//											@Nullable final Authentication auth)
//				throws IOException, ServletException {
//			log.debug("oas request: {}", request);
//			// FIXME use less hacky way
//			if ("application/json".equals(request.getHeader("Content-Type"))) {
//				/*
//				 * USED if you want to AVOID redirect to LoginSuccessful.htm in JSON authentication
//				 */
//				// TODO should return JWT token
//				response.getWriter().print("{\"status\":\"OK\"}");
//				response.getWriter().flush();
//			} else {
//				super.onAuthenticationSuccess(request, response, auth);
//			}
//		}
//	}
//
//	// TODO brush it up
//	private static class RestAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
//
//		@Override
//		public void onAuthenticationFailure(@NonNull final HttpServletRequest request,
//		                                    @NonNull final HttpServletResponse response,
//		                                    @Nullable final AuthenticationException authException)
//				throws IOException, ServletException {
//			// FIXME use less hacky way
//			if ("application/json".equals(request.getHeader("Content-Type"))) {
//				/*
//				 * USED if you want to AVOID redirect to LoginSuccessful.htm in JSON authentication
//				 */
//				response.getWriter().print("{\"message\":\"authentication failed\"}");
//				response.getWriter().flush();
//			} else {
//				super.onAuthenticationFailure(request, response, authException);
//			}
//		}
//	}
//
//	// TODO brush it up
//	private static class RestAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {
//		RestAuthenticationEntryPoint(final String loginUrl)  {
//			super(loginUrl);
//		}
//
//		@Override
//		public void commence(@NonNull final HttpServletRequest request,
//		                     @NonNull final HttpServletResponse response,
//		                     @NonNull final AuthenticationException authException) throws IOException {
//			response.sendError(403, "Forbidden");
//		}
//	}
//}
