/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Getter;
import lombok.ToString;
import lombok.val;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;

// TODO craft according to frontend requirements.
// that simple: { 'message': 'authentication failed' }
// add 'reason' with verbose error message from exception
// and probably 'code' with HTTP status code.
// Сделать свой класс исключения, наследуемый от ResponseStatusException.
// В нём формировать этот объект для тела ответа.
// Внутри программы основная работа ведётся с исключениями, а не с этим объектом.
// Он используется только в ErrorHandler (ControllerAdvice) и в RestApiErrorWebExceptionHandler.
// После переделки на functional framework первый станет ненужным.
@Getter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse extends CommonResponse {
	private static final String ERROR_ATTRIBUTES_MESSAGE = "message";
	private static final String ERROR_ATTRIBUTES_SUPPRESS_DETAILS = "suppress_details";

	@JsonProperty("error")
	@JsonPropertyDescription("Error details object")
	@Nullable
	private final Map<String, Object> errorAttributesModel;


	private ErrorResponse(@NonNull final String message, @Nullable final Map<String, Object> errorAttributesModel) {
		super(message);
		this.errorAttributesModel = errorAttributesModel;
	}

	// Create response object from ErrorAttributes model map
	// used in RestApiErrorWebExceptionHandler
	@NonNull
	public static ErrorResponse from(@NonNull final Map<String, Object> errorAttributesModel) {
		val message = (String) errorAttributesModel.remove(ERROR_ATTRIBUTES_MESSAGE);
		val suppressDetails = Boolean.TRUE.equals(errorAttributesModel.remove(ERROR_ATTRIBUTES_SUPPRESS_DETAILS));
		return new ErrorResponse(message, suppressDetails ? null : errorAttributesModel);
	}

	// TODO нужно ли?
	@NonNull
	public static ErrorResponse with(@NonNull final String message) {
		return new ErrorResponse(message, null);
	}

	// TODO from application exception with short message
	// TODO убрать?
/*
	@NonNull
	public static ErrorResponse create(@NonNull final String message, @Nullable final HttpStatus status, @Nullable final Exception exception) {
		val errorAttributesModel = new LinkedHashMap<String,Object>();
		errorAttributesModel.put("timestamp", new Date());
		errorAttributesModel.put("path", "UNKNOWN");    // TODO get actual path
		if (status != null)
			errorAttributesModel.put("status", status.value());
		if (exception != null) {
			errorAttributesModel.put("error", exception.getLocalizedMessage());
			Optional.ofNullable(exception.getCause()).map(Throwable::getLocalizedMessage).ifPresent(r -> errorAttributesModel.put("reason", r));
		} else
			errorAttributesModel.put("error", message);
		return new ErrorResponse(message, errorAttributesModel);
	}
*/

/*
	// TODO убрать
	// Endpoint not mapped
	public static Mono<Void> notFound(@NonNull final ServerWebExchange exchange) {
		return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No handler found for " + exchange.getRequest().getPath().value()));
	}
*/
}
