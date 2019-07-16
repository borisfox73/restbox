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
public class BoxControlSensor extends BoxControl {
	// state
	@JsonProperty("onfunc")
	@JsonPropertyDescription("Box control sensor ACTION ON function")
	@Nullable
	volatile String onFunc;    // matched with the RouterFunction#name
	@JsonProperty("offfunc")
	@JsonPropertyDescription("Box control sensor ACTION OFF function")
	@Nullable
	volatile String offFunc;   // matched with the RouterFunction#name


	BoxControlSensor(@NonNull final BoxControlType type, final int id, @Nullable final String descr,
	                 @Nullable final String onFunc, @Nullable final String offFunc) {
		super(type, id, descr);
		this.onFunc = onFunc;
		this.offFunc = offFunc;
	}

	@NonNull
	@Override
	public Optional<String> getRouterFunc() {
		// TODO Switch expressions are still preview feature in Java 12.0.1
/*
		return Optional.ofNullable(
			switch(getAction()) {
				case ON -> onFunc;
				case OFF -> offFunc;
				default -> null;
			}
		);
*/
		switch (getAction()) {
			case ON:
				return Optional.ofNullable(onFunc);
			case OFF:
				return Optional.ofNullable(offFunc);
			default:
				return Optional.empty();
		}
	}
}
