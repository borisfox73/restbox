/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.ErrorResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Custom Error Web Exception Handler for formatting error response in JSON with custom object.
 * Intercepts JSON media type compatible requests, with fallback to {@link DefaultErrorWebExceptionHandler} for HTML and all other.
 */
@Slf4j
class CustomErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {
	/**
	 * Create a new {@code DefaultErrorWebExceptionHandler} instance.
	 *
	 * @param errorAttributes    the error attributes
	 * @param resourceProperties the resources configuration properties
	 * @param errorProperties    the error configuration properties
	 * @param applicationContext the current application context
	 */
	CustomErrorWebExceptionHandler(final ErrorAttributes errorAttributes, final ResourceProperties resourceProperties,
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
	@Nonnull
	private Mono<ServerResponse> renderJsonErrorResponse(final ServerRequest request) {
		// from superclass
		val includeStackTrace = isIncludeStackTrace(request, MediaType.ALL);
		val error = getErrorAttributes(request, includeStackTrace);
		val errorStatus = getHttpStatus(error);
		// compose custom error response object
		log.trace("Errpr attributes: {}", error);
		val errorResponse = ErrorResponse.create(errorStatus.value(), determineErrorMessage(error));
		log.trace("Errpr response: {}", errorResponse);
		// from superclass
		return ServerResponse.status(errorStatus)
		                     .contentType(MediaType.APPLICATION_JSON_UTF8)
		                     .body(BodyInserters.fromObject(errorResponse))
		                     .doOnNext((resp) -> logError(request, errorStatus));
	}

	/**
	 * Get reason phrase from exception with fallback to HTTP status reason.
	 *
	 * @param errorAttributes Error attributes map
	 *
	 * @return Error message to be used in response object
	 */
	@Nullable
	private String determineErrorMessage(@Nonnull final Map<String, Object> errorAttributes) {
		val error = (String) errorAttributes.get("error");
		val errorMessage = (String) errorAttributes.get("message");
		return StringUtils.hasText(errorMessage) ? errorMessage : error;
	}

	// from superclass

	/**
	 * Predicate that checks whether the current request explicitly support
	 * {@code "text/html"} media type.
	 * <p>
	 * The "match-all" media type is not considered here.
	 *
	 * @return the request predicate
	 */
	@Nonnull
	private RequestPredicate acceptsJson() {
		return (serverRequest) -> {
			List<MediaType> acceptedMediaTypes = serverRequest.headers().accept();
			acceptedMediaTypes.remove(MediaType.ALL);
			MediaType.sortBySpecificityAndQuality(acceptedMediaTypes);
			return acceptedMediaTypes.stream().anyMatch(MediaType.APPLICATION_JSON::isCompatibleWith);
		};
	}
}
