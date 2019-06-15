/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Cisco RESTful API Hostname service response.
 * <pre>
 *  {
 *      "error-code": {number},
 *      "error-message": "{string}",
 *      "detail": "{string}"
 * }
 * </pre>
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestApiErrorResponse implements RestApiErrorDTO {
	private int errorCode;
	@Nullable
	private String errorMessage;
	@Nullable
	private String errorDetail;


	@JsonCreator
	RestApiErrorResponse(@JsonProperty(value = "error-code", required = true) final int errorCode,
	                     @JsonProperty(value = "error-message") @Nullable final String errorMessage,
	                     @JsonProperty(value = "detail") @Nullable final String errorDetail) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.errorDetail = errorDetail;
	}

	@JsonIgnore
	@NonNull
	public String getReason() {
		return "errorCode=" + this.getErrorCode() + ", errorMessage=" + this.getErrorMessage() + ", errorDetail=" + this.getErrorDetail();
	}
}
