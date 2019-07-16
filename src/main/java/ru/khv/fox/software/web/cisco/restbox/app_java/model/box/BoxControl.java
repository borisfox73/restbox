/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.box;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;
import lombok.experimental.FieldDefaults;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class BoxControl {
	// parameters
	@JsonProperty
	@JsonPropertyDescription("Box control type")
	@NonNull
	final BoxControlType type;
	@JsonProperty
	@JsonPropertyDescription("Box control id")
	final int id;
	@JsonProperty
	@JsonPropertyDescription("Box control description")
	@Nullable
	final String descr;
	@JsonProperty
	@JsonPropertyDescription("Box control status")
	// state
			int status;


	// Get required action
	@NonNull
	public BoxControlAction getAction() {
		return type.getAction(status);
	}

	// Get router function depending on state (to be implemented in concrete controls)
	@NonNull
	public abstract Optional<String> getRouterFunc();
}
