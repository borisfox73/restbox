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
import org.springframework.lang.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/*
 Box Control is a mutable object.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"type", "id"})
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class BoxControl {
	// parameters
	@JsonProperty
	@JsonPropertyDescription("Box control type")
	BoxControlType type;
	@JsonProperty
	@JsonPropertyDescription("Box control id")
	int id;
	@JsonProperty
	@JsonPropertyDescription("Box control description")
	@Nullable
	String descr;
	@JsonProperty
	@JsonPropertyDescription("Box control status")
	// state
			AtomicInteger status = new AtomicInteger();


	public void setStatus(final int status) {
		this.status.set(status);
	}

	public int getStatus() {
		return status.get();
	}

	// Get required action
	public BoxControlAction getAction() {
		return type.getAction(getStatus());
	}

	// Get router function depending on state (to be implemented in concrete controls)
	public abstract Optional<String> getRouterFunc();
}
