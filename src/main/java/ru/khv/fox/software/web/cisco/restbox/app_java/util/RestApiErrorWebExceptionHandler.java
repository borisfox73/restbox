/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.util;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.ErrorResponse;

/**
 * Custom Error Web Exception Handler for formatting error response in JSON with custom object.
 * Intercepts JSON media type compatible requests, with fallback to {@link DefaultErrorWebExceptionHandler} for HTML and all other.
 */
@Slf4j
public class RestApiErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {
	/**
	 * Create a new {@code DefaultErrorWebExceptionHandler} instance.
	 *
	 * @param errorAttributes    the error attributes
	 * @param resourceProperties the resources configuration properties
	 * @param errorProperties    the error configuration properties
	 * @param applicationContext the current application context
	 */
	public RestApiErrorWebExceptionHandler(final ErrorAttributes errorAttributes, final ResourceProperties resourceProperties,
	                                       final ErrorProperties errorProperties, final ApplicationContext applicationContext) {
		super(errorAttributes, resourceProperties, errorProperties, applicationContext);
	}

	@Override
	protected RouterFunction<ServerResponse> getRoutingFunction(final ErrorAttributes errorAttributes) {
		return RouterFunctions.route(acceptsJson(), this::renderJsonErrorResponse)
		                      .and(super.getRoutingFunction(errorAttributes));
	}

	/*
	 * Default implementation of {@link ErrorAttributes}. Provides the following attributes
	 * when possible:
	 * <ul>
	 * <li>timestamp - The time that the errors were extracted</li>
	 * <li>status - The status code</li>
	 * <li>error - The error reason</li>
	 * <li>exception - The class name of the root exception (if configured)</li>
	 * <li>message - The exception message</li>
	 * <li>errors - Any {@link ObjectError}s from a {@link BindingResult} exception
	 * <li>trace - The exception stack trace</li>
	 * <li>path - The URL path when the exception was raised</li>
	 * </ul>
	 */

	/*
	 * Default handler composes message like this:
	 * {
	 *   "timestamp": 1524142959101,
	 *   "path": "/testjson",
	 *   "status": 405,
	 *   "error": "Method Not Allowed",
	 *   "message": "Request method 'POST' not supported"
	 * }
	 * This representation is generated in DefaultErrorWebExceptionHandler#renderErrorResponse
	 * from DefaultErrorAttributes#getErrorAttributes Map object.
	 */

	/**
	 * Render the error information as a JSON payload.
	 *
	 * @param request the current request
	 *
	 * @return a {@code Publisher} of the HTTP response
	 */
	@NonNull
	private Mono<ServerResponse> renderJsonErrorResponse(@NonNull final ServerRequest request) {
		// from superclass
		val includeStackTrace = isIncludeStackTrace(request, MediaType.ALL);
		val error = getErrorAttributes(request, includeStackTrace);
		val errorStatus = getHttpStatus(error);
		// compose custom error response object
		log.trace("Error attributes: {}", error);
		val errorResponse = ErrorResponse.from(error);
		log.trace("Error response: {}", errorResponse);
		// from superclass
		return ServerResponse.status(errorStatus)
		                     .contentType(MediaType.APPLICATION_JSON_UTF8)
		                     .body(BodyInserters.fromObject(errorResponse));
//		                     .doOnNext(resp -> logError(request, errorStatus));
	}

/*
	// moved from DefaultErrorWebExceptionHandler to AbstractErrorWebExceptionHandler and became private in Spring Boot 2.1.4
	// Resurrected here.
	private static final Log logger = HttpLogging.forLogName(RestApiErrorWebExceptionHandler.class);

	private void logError(ServerRequest request, HttpStatus errorStatus) {
		Throwable throwable = getError(request);
		if (logger.isDebugEnabled()) {
			logger.debug(
					request.exchange().getLogPrefix() + formatError(throwable, request));
		}
		if (errorStatus.equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
			logger.error(request.exchange().getLogPrefix() + "500 Server Error for "
			             + formatRequest(request), throwable);
		}
	}

	private String formatError(Throwable ex, ServerRequest request) {
		String reason = ex.getClass().getSimpleName() + ": " + ex.getMessage();
		return "Resolved [" + reason + "] for HTTP " + request.methodName() + " "
		       + request.path();
	}

	private String formatRequest(ServerRequest request) {
		String rawQuery = request.uri().getRawQuery();
		String query = StringUtils.hasText(rawQuery) ? "?" + rawQuery : "";
		return "HTTP " + request.methodName() + " \"" + request.path() + query + "\"";
	}
*/

	/**
	 * Predicate that checks whether the current request explicitly support
	 * {@code "text/html"} media type.
	 * <p>
	 * The "match-all" media type is not considered here.
	 *
	 * @return the request predicate
	 */
	@NonNull
	private RequestPredicate acceptsJson() {
		return (serverRequest) -> {
			val acceptedMediaTypes = serverRequest.headers().accept();
			// acceptedMediaTypes.remove(MediaType.ALL);
			// Should ignore parameters, such as charset and quality
			acceptedMediaTypes.removeIf(v -> v.isWildcardType() && v.isWildcardSubtype());
			MediaType.sortBySpecificityAndQuality(acceptedMediaTypes);
			return acceptedMediaTypes.stream().anyMatch(MediaType.APPLICATION_JSON::isCompatibleWith);
		};
	}
}
