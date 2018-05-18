/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SecurityTest {

	@Autowired
	private ApplicationContext context;
	private WebTestClient rest;

	@Before
	public void setup() {
		this.rest = WebTestClient
				.bindToApplicationContext(this.context)
				.configureClient()
				.build();
	}

	@Test
	public void whenNoCredentials_thenUnauthorized() {
		this.rest.get()
		         .uri("/")
		         .exchange()
		         .expectStatus()
		         .isUnauthorized();
//		            .is3xxRedirection();
	}

	@Test
//	@WithMockUser(username = "user1", password = "user123")
	@WithMockUser
	public void whenHasCredentials_thenSeesGreeting() {
		this.rest.get()
		         .uri("/")
		         .exchange()
		         .expectStatus().isOk()
		         .expectBody(String.class).isEqualTo("Hello, user");
	}

	@Test
	@WithMockUser(username = "admin", roles = {"ADMIN"})
	public void whenHasCredentials_thenSeesAdminGreeting() {
		this.rest.get()
		         .uri("/admin")
		         .exchange()
		         .expectStatus().isOk()
		         .expectBody(String.class).isEqualTo("Admin access: admin");
	}
}
