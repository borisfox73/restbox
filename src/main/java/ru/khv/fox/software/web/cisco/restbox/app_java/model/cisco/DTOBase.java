/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.lang.NonNull;

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
abstract class DTOBase implements RestApiDTO {
	// Exclude field from serialization as kind is not applicable to mutating requests
	@Getter(onMethod_ = {@JsonIgnore})
	@NonNull
	private final String kind;
}
