/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.box;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/*
 Box Control is a mutable object.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"type", "id"})
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class BoxControl {
	// parameters
	@JsonProperty
	@JsonPropertyDescription("Box control type")
	@NonNull
	private final BoxControlType type;
	@JsonProperty
	@JsonPropertyDescription("Box control id")
	private final int id;
	@JsonProperty
	@JsonPropertyDescription("Box control description")
	@Nullable
	private final String descr;
	@JsonProperty
	@JsonPropertyDescription("Box control status")
	// state
	private int status;


	// stub methods to be overriden in childs
	@Nullable
	public BoxControlOnOffFunctions getOnFunc() {
		throw new UnsupportedOperationException("Method is not implemented");
	}

	@Nullable
	public BoxControlOnOffFunctions getOffFunc() {
		throw new UnsupportedOperationException("Method is not implemented");
	}

	@Nullable
	public BoxControlRFunctions getRFunc() {
		throw new UnsupportedOperationException("Method is not implemented");
	}
}
