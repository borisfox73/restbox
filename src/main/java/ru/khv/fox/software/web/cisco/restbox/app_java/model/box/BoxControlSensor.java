/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.box;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.RouterFunction;

@Setter
@Getter
//@Getter(onMethod_ = {@Override})
@ToString(callSuper = true)
public class BoxControlSensor extends BoxControl {
	// state
	@JsonProperty("onfunc")
	@JsonPropertyDescription("Box control sensor On function")
	@Nullable
	private String onFunc = "anone";    // TODO convert to string, FK of the RouterFunction#name
	@JsonProperty("offfunc")
	@JsonPropertyDescription("Box control sensor Off function")
	@Nullable
	private String offFunc = "anone";   // TODO convert to string, FK of the RouterFunction#name
	// TODO needed ?
	@JsonIgnore
	@Nullable
	private RouterFunction onFuncRef;
	@JsonIgnore
	@Nullable
	private RouterFunction offFuncRef;


	BoxControlSensor(@NonNull final BoxControlType type, final int id, @Nullable final String descr) {
		super(type, id, descr);
	}
}
