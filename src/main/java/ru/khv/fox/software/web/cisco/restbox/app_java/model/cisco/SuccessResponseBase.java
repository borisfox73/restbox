/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco;

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
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
abstract class SuccessResponseBase {
	@NonNull
	private final String kind;
}
