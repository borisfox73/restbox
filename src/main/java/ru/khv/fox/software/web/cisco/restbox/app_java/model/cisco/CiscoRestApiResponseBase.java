/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
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
abstract class CiscoRestApiResponseBase {
	@NonNull
	private final String kind;
}
