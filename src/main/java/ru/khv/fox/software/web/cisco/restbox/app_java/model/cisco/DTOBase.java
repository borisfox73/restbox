/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * Common fields of Cisco RESTful API response body.
 * <pre>
 * {
 *     "kind": "object#kind",
 * }
 * </pre>
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy.class)
abstract class DTOBase implements RestApiDTO {
	// Exclude field from serialization as kind is not applicable to mutating requests
	@JsonPropertyDescription("Object type")
	@Getter(onMethod_ = {@JsonIgnore})
	private final String kind;
}
