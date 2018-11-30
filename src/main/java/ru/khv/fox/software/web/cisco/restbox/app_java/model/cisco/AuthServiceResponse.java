/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.net.URI;
import java.time.LocalDateTime;

/**
 * Cisco RESTful API Authentication service response.
 * <pre>
 * {
 *     "kind": "object#auth-token",
 *     "expiry-time": "Sun Nov 25 15:33:53 2018",
 *     "token-id": "fZ6PMAeV0LcBzOh7ZLZnVmbozurKQNX9nWfmjJjXPv0=",
 *     "link": "https://192.168.70.70:55443/api/v1/auth/token-services/3209748457"
 * }
 * </pre>
 */
// TODO which validation annotation is applicable ?
@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
//@Valid
//@Validated
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthServiceResponse extends SuccessResponseBase {
	//	@JsonProperty
//	@JsonPropertyDescription("authentication token expiry time")
//	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEE MMM d hh:mm:ss yyyy", locale = "C")
//	@NotNull
	@NonNull
	private LocalDateTime expiryTime;
	//	@JsonProperty
//	@JsonPropertyDescription("authentication token string encoded in base64")
//	@NotEmpty
	@NonNull
	private String tokenId;
	//	@JsonProperty
//	@JsonPropertyDescription("authentication token object instance in service")
	@Nullable
//	private final URL link;
	private URI link;


	@JsonCreator
	AuthServiceResponse(@JsonProperty(value = "kind", required = true) @NonNull final String kind,
	                    @JsonProperty(value = "expiry-time", required = true) @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEE MMM d HH:mm:ss yyyy", locale = "C") @NonNull final LocalDateTime expiryTime,
	                    @JsonProperty(value = "token-id", required = true) @NonNull final String tokenId,
//	                      @JsonProperty(value = "link") @Nullable final URL link) {
                        @JsonProperty(value = "link") @Nullable final URI link) {
		super(kind);
		this.expiryTime = expiryTime;
		this.tokenId = tokenId;
		this.link = link;
	}
}
