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
public class BoxControlIndicator extends BoxControl {
	// state
	@JsonProperty("rfunc")
	@JsonPropertyDescription("Box control indicator R function")
	@Nullable
	private String rFunc = "rnone"; // TODO convert to string, FK of the RouterFunction#name
	// TODO needed ?
	@JsonIgnore
	@Nullable
	private RouterFunction rFuncRef;

	BoxControlIndicator(@NonNull final BoxControlType type, final int id, @Nullable final String descr) {
		super(type, id, descr);
		// TODO debug. Obtain rfunc from config?
		if (type == BoxControlType.LED) {
			switch (id) {
				case 0:
					this.rFunc = "rfunc2";
					break;
				case 1:
					this.rFunc = "rfunc3";
					break;
			}
		}
	}
}
