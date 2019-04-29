/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

/*
 * Controller holding generic purpose endpoints and handlers.
 */
// TODO cleanup.
/*
@Slf4j
@RestController
class Default {
	// Gracefully disable favicon
	@GetMapping("favicon.ico")
	@ResponseBody
	public Mono<Void> noFavicon() {
		return Mono.empty();
	}

	// Landing point for requests to endpoints not mapped otherwise
	@RequestMapping
	public Mono<Void> endpointUnknown(@NonNull final ServerWebExchange exchange) {
		// TODO what is the best practice to return error response from controllers ?
		//throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
		return Mono.error(new RestApiException("Resource with path " + exchange.getRequest().getPath().value() + " does not exist", HttpStatus.NOT_FOUND));
	}
}
*/
