/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco;

import com.fasterxml.jackson.annotation.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
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
@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthServiceResponse extends DTOBase {
	@JsonPropertyDescription("authentication token expiry time")
	LocalDateTime expiryTime;
	@JsonPropertyDescription("authentication token string encoded in base64")
	String tokenId;
	@JsonPropertyDescription("authentication token object instance in service")
	@Nullable
	URI link;


	@JsonCreator
	AuthServiceResponse(@JsonProperty(value = "kind", required = true) final String kind,
	                    @JsonProperty(value = "expiry-time", required = true) @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEE MMM ppd HH:mm:ss yyyy", locale = "C") final LocalDateTime expiryTime,
	                    @JsonProperty(value = "token-id", required = true) final String tokenId,
	                    @JsonProperty(value = "link") @Nullable final URI link) {
		super(kind);
		this.expiryTime = expiryTime;
		this.tokenId = tokenId;
		this.link = link;
	}
}
