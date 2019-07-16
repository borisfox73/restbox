/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.box;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Optional;

@Setter
@Getter
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BoxControlIndicator extends BoxControl {
	// state
	@JsonProperty("rfunc")
	@JsonPropertyDescription("Box control indicator READ function")
	@Nullable
	volatile String rFunc;     // matched with the RouterFunction#name


	BoxControlIndicator(@NonNull final BoxControlType type, final int id, @Nullable final String descr,
	                    @Nullable final String rFunc) {
		super(type, id, descr);
		this.rFunc = rFunc;
	}

	@NonNull
	@Override
	public Optional<String> getRouterFunc() {
		return Optional.ofNullable(rFunc);
	}
}
