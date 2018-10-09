/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.RestApiException;


// TODO может ещё понадобиться для обработки других исключений.

/**
 * REST Endpoints exception handling.
 */
@Slf4j
@RestControllerAdvice
// public class ErrorHandler extends ResponseEntityExceptionHandler {   // not available with WebFlux
class ErrorHandler {

	// TODO определить ещё для Unauthorized / Access Denied ? - для перехвата из Authentication Entry Point не получится, т.к. она отрабатывает раньше, чем запрос доходит до диспетчера.
	// TODO прочитать, какие статусы HTTP нужно возвращать при ошибках.

	// обработка исключений:
	// https://stackoverflow.com/questions/24292373/spring-boot-rest-controller-how-to-return-different-http-status-codes
	// https://spring.io/guides/tutorials/bookmarks/
	// http://engineering.pivotal.io/post/must-know-spring-boot-annotations-controllers/

/*
	@NonNull
	private ResponseEntity<Object> composeResponse(@NonNull final ResponseResultCode resultCode,
	                                               @NonNull final Exception ex,
	                                               @NonNull final HttpHeaders headers,
	                                               @NonNull final HttpStatus status) {
		return new ResponseEntity<>(composeResponseBody(resultCode, ex), headers, status);
	}

	@NonNull
	private PullResponse composeResponseBody(@NonNull final ResponseResultCode resultCode,
	                                         @NonNull final Exception ex) {
		return composeResponseBody(resultCode, ex, appProperties.isExposeErrorMessages());
	}

	@NonNull
	private PullResponse composeResponseBody(@NonNull final ResponseResultCode resultCode,
	                                         @NonNull final Exception ex, final boolean exposeErrorMessage) {
		logException(ex);
		return exposeErrorMessage ? new PullResponse(resultCode, ex.getMessage()) : new PullResponse(resultCode);
	}

	private void logException(@NonNull Exception ex) {
		log.error("{} exception caught: {}", ex.getClass().getSimpleName(), ex.getLocalizedMessage());
		log.trace("exception:", ex);
	}
*/


	// --- Application-specific ----------------------------------------------------------------------------------------

	// --- Authentication and authorization ----------------------------------------------------------------------------

/* These interceptions is not really needed.
	// Should be handled by security framework through the authentication entry point.
	@NonNull
	@ExceptionHandler({AuthenticationException.class})
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public Mono<ErrorResponse> handleAuthenticationException(@NonNull final AuthenticationException ex) {
		log.debug("authentication exception caught");
		return Mono.just(ErrorResponse.create(401, "Unauthenticated access: " + ex.getLocalizedMessage()));
	}
*/

	// Should be handled by security framework through the access denied handler.
	// This handler intercepts exceptions thrown in Advices, e.g. PrePostAdviceReactiveMethodInterceptor for method security
	@NonNull
	@ExceptionHandler({AccessDeniedException.class})
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public Mono<RestApiException> handleAccessDeniedException(@NonNull final AccessDeniedException ex) {
		log.debug("access denied exception caught");
//		return Mono.just(ErrorResponse.create(403, "Access is denied: " + ex.getLocalizedMessage()));
//		return Mono.just(ErrorResponse.create("Access is denied", HttpStatus.FORBIDDEN, ex));
		// let RestApiErrorWebExceptionHandler handle this condition unified way
		return Mono.error(new RestApiException("access denied error", HttpStatus.FORBIDDEN, ex));
	}

/*
	// --- Validation and data-binding ---------------------------------------------------------------------------------

	// JSR-303 bean validation on DTO objects.
	@Nullable
	@ExceptionHandler({ConstraintViolationException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public PullResponse handleConstraintViolation(@NonNull final ConstraintViolationException ex) {
		return composeResponseBody(ResponseResultCode.INVALID_ELEMENT_VALUE, ex);
	}

	// Method argument type mismatch or failed conversion.
	// Unknown enum value falls into this too.
	@Override
	@NonNull
	protected ResponseEntity<Object> handleTypeMismatch(@NonNull final TypeMismatchException ex,
	                                                    @NonNull final HttpHeaders headers,
	                                                    @NonNull final HttpStatus status,
	                                                    @NonNull final WebRequest request) {
		return composeResponse(ResponseResultCode.INVALID_ELEMENT_VALUE, ex, headers, status);
	}

	// Method argument validation failed
	@Override
	@NonNull
	protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull final MethodArgumentNotValidException ex,
	                                                              @NonNull final HttpHeaders headers,
	                                                              @NonNull final HttpStatus status,
	                                                              @NonNull final WebRequest request) {
		return composeResponse(ResponseResultCode.INVALID_ELEMENT_VALUE, ex, headers, status);
	}

	@Override
	@NonNull
	protected ResponseEntity<Object> handleBindException(@NonNull final BindException ex,
	                                                     @NonNull final HttpHeaders headers,
	                                                     @NonNull final HttpStatus status,
	                                                     @NonNull final WebRequest request) {
		return composeResponse(ResponseResultCode.SYNTAX_ERROR, ex, headers, status);
	}

	@Override
	@NonNull
	protected ResponseEntity<Object> handleServletRequestBindingException(@NonNull final ServletRequestBindingException ex,
	                                                                      @NonNull final HttpHeaders headers,
	                                                                      @NonNull final HttpStatus status,
	                                                                      @NonNull final WebRequest request) {
		return composeResponse(ResponseResultCode.SYNTAX_ERROR, ex, headers, status);
	}

	@Override
	@NonNull
	protected ResponseEntity<Object> handleMissingServletRequestParameter(@NonNull final MissingServletRequestParameterException ex,
	                                                                      @NonNull final HttpHeaders headers,
	                                                                      @NonNull final HttpStatus status,
	                                                                      @NonNull final WebRequest request) {
		return composeResponse(ResponseResultCode.SYNTAX_ERROR, ex, headers, status);
	}

	@Override
	@NonNull
	protected ResponseEntity<Object> handleMissingServletRequestPart(@NonNull final MissingServletRequestPartException ex,
	                                                                 @NonNull final HttpHeaders headers,
	                                                                 @NonNull final HttpStatus status,
	                                                                 @NonNull final WebRequest request) {
		return composeResponse(ResponseResultCode.FORMAT_ERROR, ex, headers, status);
	}

	@Override
	@NonNull
	protected ResponseEntity<Object> handleMissingPathVariable(@NonNull final MissingPathVariableException ex,
	                                                           @NonNull final HttpHeaders headers,
	                                                           @NonNull final HttpStatus status,
	                                                           @NonNull final WebRequest request) {
		return composeResponse(ResponseResultCode.FORMAT_ERROR, ex, headers, status);
	}

	// ------------ Message format errors ------------------------------------------------------------------------------

	@Override
	@NonNull
	protected ResponseEntity<Object> handleConversionNotSupported(@NonNull final ConversionNotSupportedException ex,
	                                                              @NonNull final HttpHeaders headers,
	                                                              @NonNull final HttpStatus status,
	                                                              @NonNull final WebRequest request) {
		return composeResponse(ResponseResultCode.FORMAT_ERROR, ex, headers, status);
	}

	@Override
	@NonNull
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(@NonNull final HttpMediaTypeNotSupportedException ex,
	                                                                 @NonNull final HttpHeaders headers,
	                                                                 @NonNull final HttpStatus status,
	                                                                 @NonNull final WebRequest request) {
		super.handleHttpMediaTypeNotSupported(ex, headers, status, request);
		return composeResponse(ResponseResultCode.FORMAT_ERROR, ex, headers, status);
	}

	@Override
	@NonNull
	protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(@NonNull final HttpMediaTypeNotAcceptableException ex,
	                                                                  @NonNull final HttpHeaders headers,
	                                                                  @NonNull final HttpStatus status,
	                                                                  @NonNull final WebRequest request) {
		logException(ex);

		// Give client a hint of supported media types
		final List<MediaType> mediaTypes = ex.getSupportedMediaTypes();
		if (!CollectionUtils.isEmpty(mediaTypes))
			headers.setAccept(mediaTypes);

		// Can't marshall response object because media type is not XML, so use default implementation with simple text
		//return composeResponse(ResponseResultCode.FORMAT_ERROR, ex, headers, status);
		headers.setContentType(MediaType.TEXT_PLAIN);
		final String body = ex.getMessage() != null ? ex.getMessage() : "Media Type is not acceptable";
		return new ResponseEntity<>(body, headers, status);
	}

	@Override
	@NonNull
	protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull final HttpMessageNotReadableException ex,
	                                                              @NonNull final HttpHeaders headers,
	                                                              @NonNull final HttpStatus status,
	                                                              @NonNull final WebRequest request) {
		return composeResponse(ResponseResultCode.UNSPECIFIED, ex, headers, status);
	}

	@Override
	@NonNull
	protected ResponseEntity<Object> handleHttpMessageNotWritable(@NonNull final HttpMessageNotWritableException ex,
	                                                              @NonNull final HttpHeaders headers,
	                                                              @NonNull final HttpStatus status,
	                                                              @NonNull final WebRequest request) {
		return composeResponse(ResponseResultCode.UNSPECIFIED, ex, headers, status);
	}

	// ------------ Transport and servlet layer errors -----------------------------------------------------------------

	@Override
	@NonNull
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(@NonNull final HttpRequestMethodNotSupportedException ex,
	                                                                     @NonNull final HttpHeaders headers,
	                                                                     @NonNull final HttpStatus status,
	                                                                     @NonNull final WebRequest request) {
		super.handleHttpRequestMethodNotSupported(ex, headers, status, request);
		return composeResponse(ResponseResultCode.UNSUPPORTED_SERVICE, ex, headers, status);
	}

	// Resource (endpoint mapping) handler not found
	@Override
	@NonNull
	protected ResponseEntity<Object> handleNoHandlerFoundException(@NonNull final NoHandlerFoundException ex,
	                                                               @NonNull final HttpHeaders headers,
	                                                               @NonNull final HttpStatus status,
	                                                               @NonNull final WebRequest request) {
		return composeResponse(ResponseResultCode.UNSUPPORTED_SERVICE, ex, headers, status);
	}


	// Internal Server Error
	@Override
	@NonNull
	protected ResponseEntity<Object> handleExceptionInternal(@NonNull final Exception ex,
	                                                         @Nullable final Object body,
	                                                         @NonNull final HttpHeaders headers,
	                                                         @NonNull final HttpStatus status,
	                                                         @NonNull final WebRequest request) {
		logException(ex);
		return super.handleExceptionInternal(ex, composeResponseBody(ResponseResultCode.FAILED, ex, false),
		                                     headers, status, request);
	}

	// --- All other exceptions -====-----------------------------------------------------------------------------------

	@Nullable
	@ExceptionHandler({Exception.class})
	public ResponseEntity<Object> handleOtherExceptions(@NonNull final Exception ex, @NonNull final WebRequest request) {
		return handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
	}
*/
}
