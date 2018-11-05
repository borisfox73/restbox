/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.box;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import static ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlType.BoxControlKind.INDICATOR;
import static ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlType.BoxControlKind.SENSOR;

@RequiredArgsConstructor
public enum BoxControlType {
	SWITCH(SENSOR), BUTTON(SENSOR), USONIC(SENSOR), LED(INDICATOR);

	enum BoxControlKind {
		SENSOR {
			@Override
			@NonNull
			BoxControlSensor instantiate(@NonNull final BoxControlType type, final int id, @Nullable final String description) {
				return new BoxControlSensor(type, id, description);
			}
		},
		INDICATOR {
			@Override
			@NonNull
			BoxControlIndicator instantiate(@NonNull final BoxControlType type, final int id, @Nullable final String description) {
				return new BoxControlIndicator(type, id, description);
			}
		};

		@NonNull
		abstract BoxControl instantiate(@NonNull final BoxControlType type, final int id, @Nullable final String description);
	}

	private final BoxControlKind kind;

	public boolean isSensor() {
		return kind == SENSOR;
	}

	public boolean isIndicator() {
		return kind == INDICATOR;
	}

	@NonNull
	BoxControl getInstance(final int id, @Nullable final String description) {
		return kind.instantiate(this, id, description);
	}

	/* TODO cleanup
		// for serialization
		@Nullable
		@JsonCreator
		private static BoxControlType fromJson(@Nullable final String value) {
			return value != null ? BoxControlType.valueOf(value.toUpperCase()) : null;
		}
	*/
	// for deserialization
	@NonNull
	@JsonValue
	private String toJson() {
		return this.name().toLowerCase();
	}
}
