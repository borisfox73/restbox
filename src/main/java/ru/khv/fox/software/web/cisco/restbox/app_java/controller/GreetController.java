/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
public class GreetController {

/*
	private final GreetService greetService;

	public GreetController(final GreetService greetService) {
		this.greetService = greetService;
	}
*/

	@GetMapping("/")
	public Mono<String> greet(Mono<Principal> principal) {
		return principal
				.map(Principal::getName)
				.map(name -> String.format("Hello, %s", name));
	}

	@GetMapping("/admin")
	public Mono<String> greetAdmin(Mono<Principal> principal) {
		return principal
				.map(Principal::getName)
				.map(name -> String.format("Admin access: %s", name));
	}

/*
	@PreAuthorize("hasRole('OTHER')")
	@GetMapping("/greet")
	public Mono<String> greetService() {
		return greetService.greet();
	}
*/
}
