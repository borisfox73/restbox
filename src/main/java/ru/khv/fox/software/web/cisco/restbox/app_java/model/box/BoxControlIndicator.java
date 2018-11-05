/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.box;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import static ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlRFunctions.RNONE;

@Setter
@Getter(onMethod_ = {@Override})
@ToString(callSuper = true)
public class BoxControlIndicator extends BoxControl {
	// state
	@JsonProperty("rfunc")
	@JsonPropertyDescription("Box control indicator R function")
	@Nullable
	private BoxControlRFunctions rFunc = RNONE;

	BoxControlIndicator(@NonNull final BoxControlType type, final int id, @Nullable final String descr) {
		super(type, id, descr);
	}
}
