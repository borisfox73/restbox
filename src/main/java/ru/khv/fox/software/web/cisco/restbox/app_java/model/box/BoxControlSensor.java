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

import static ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlOnOffFunctions.ANONE;

@Setter
@Getter(onMethod_ = {@Override})
@ToString(callSuper = true)
public class BoxControlSensor extends BoxControl {
	// state
	@JsonProperty("onfunc")
	@JsonPropertyDescription("Box control sensor On function")
	@Nullable
	private BoxControlOnOffFunctions onFunc = ANONE;
	@JsonProperty("offfunc")
	@JsonPropertyDescription("Box control sensor Off function")
	@Nullable
	private BoxControlOnOffFunctions offFunc = ANONE;


	BoxControlSensor(@NonNull final BoxControlType type, final int id, @Nullable final String descr) {
		super(type, id, descr);
	}
}
