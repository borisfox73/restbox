/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.box;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Optional;

/*
 Box Control is a mutable object.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"type", "id"})
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
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


	// Get required action
	@NonNull
	public BoxControlAction getAction() {
		return type.getAction(status);
	}

	// Get router function depending on state (to be implemented in concrete controls)
	@NonNull
	public String getRouterFunc() {
		// Use empty string as non-null placeholder to be used in streams and as map key
		return getRouterFuncInternal().orElse("");
	}

	@NonNull
	abstract Optional<String> getRouterFuncInternal();

/*
	// stub methods to be overriden in childs
	@Nullable
	public String getOnFunc() {
		throw new UnsupportedOperationException("Method is not implemented");
	}

	@Nullable
	public String getOffFunc() {
		throw new UnsupportedOperationException("Method is not implemented");
	}

	@Nullable
	public String getRFunc() {
		throw new UnsupportedOperationException("Method is not implemented");
	}
*/
}
