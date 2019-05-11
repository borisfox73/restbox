/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.box;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import static ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlAction.*;
import static ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlType.BoxControlKind.INDICATOR;
import static ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlType.BoxControlKind.SENSOR;

@RequiredArgsConstructor
public enum BoxControlType {
	SWITCH(SENSOR),
	BUTTON(SENSOR),
	USONIC(SENSOR) {
		@NonNull
		@Override
		public BoxControlAction getAction(final int state) {
			return state > 5 && state < 50 ? OFF : state > 50 && state < 100 ? ON : NOOP;
		}
	},
	LED(INDICATOR) {
		@NonNull
		@Override
		public BoxControlAction getAction(final int state) {
			throw new UnsupportedOperationException("Method is not implemented");
		}
	};

	enum BoxControlKind {
		SENSOR {
			@NonNull
			@Override
			BoxControlSensor instantiate(@NonNull final BoxControlType type, final int id, @Nullable final String description,
			                             @Nullable final String rFunc, @Nullable final String onFunc, @Nullable final String offFunc) {
				return new BoxControlSensor(type, id, description, onFunc, offFunc);
			}
		},
		INDICATOR {
			@NonNull
			@Override
			BoxControlIndicator instantiate(@NonNull final BoxControlType type, final int id, @Nullable final String description,
			                                @Nullable final String rFunc, @Nullable final String onFunc, @Nullable final String offFunc) {
				return new BoxControlIndicator(type, id, description, rFunc);
			}
		};

		@NonNull
		abstract BoxControl instantiate(@NonNull final BoxControlType type, final int id, @Nullable final String description,
		                                @Nullable final String rFunc, @Nullable final String onFunc, @Nullable final String offFunc);
	}

	private final BoxControlKind kind;

	public boolean isSensor() {
		return kind == SENSOR;
	}

	public boolean isIndicator() {
		return kind == INDICATOR;
	}

	@NonNull
	BoxControl getInstance(final int id, @Nullable final String description,
	                       @Nullable final String rFunc,
	                       @Nullable final String onFunc, @Nullable final String offFunc) {
		return kind.instantiate(this, id, description, rFunc, onFunc, offFunc);
	}

	@NonNull
	public BoxControlAction getAction(final int state) {
		return state == 1 ? ON : state == 0 ? OFF : NOOP;
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
