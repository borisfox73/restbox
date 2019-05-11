/*
 * Copyright (c) 2019 Boris Fox.
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

import java.util.Optional;

@Setter
@Getter
@ToString(callSuper = true)
public class BoxControlIndicator extends BoxControl {
	// state
	@JsonProperty("rfunc")
	@JsonPropertyDescription("Box control indicator READ function")
	@Nullable
	private String rFunc;     // matched with the RouterFunction#name
	// TODO assert function type in setter (should be RouterFunction.FunctionType.READ)

	BoxControlIndicator(@NonNull final BoxControlType type, final int id, @Nullable final String descr,
	                    @Nullable final String rFunc) {
		super(type, id, descr);
		this.rFunc = rFunc;
/*
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
*/
	}

	@NonNull
	@Override
	Optional<String> getRouterFuncInternal() {
		return Optional.ofNullable(rFunc);
	}
}
