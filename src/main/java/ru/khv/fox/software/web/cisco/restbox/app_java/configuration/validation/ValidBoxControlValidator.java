/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration.validation;

import lombok.val;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static ru.khv.fox.software.web.cisco.restbox.app_java.configuration.AppProperties.BoxControlProperties;

public class ValidBoxControlValidator implements ConstraintValidator<ValidBoxControl, BoxControlProperties> {

	@Override
	public void initialize(final ValidBoxControl constraintAnnotation) {
	}

	@Override
	public boolean isValid(@Nullable final BoxControlProperties boxControlProperties, @NonNull final ConstraintValidatorContext context) {
		if (boxControlProperties == null)
			return true;

		val type = boxControlProperties.getType();
		//noinspection ConstantConditions - leave nullability validation of type to field level annotation
		if (type == null)
			return true;

		var valid = true;
		String propertyName = null;
		String template = null;

		if (!type.isSensor()) {
			if (boxControlProperties.getOnFunc() != null) {
				template = "{constraints.ValidBoxControl.onfunc}";
				propertyName = "onFunc";
				valid = false;
			}
			if (boxControlProperties.getOffFunc() != null) {
				template = "{constraints.ValidBoxControl.offfunc}";
				propertyName = "offFunc";
				valid = false;
			}
		}

		if (!type.isIndicator()) {
			if (boxControlProperties.getRFunc() != null) {
				template = "{constraints.ValidBoxControl.rfunc}";
				propertyName = "rFunc";
				valid = false;
			}
		}

		if (!valid) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(template).addPropertyNode(propertyName).addConstraintViolation();
		}
		return valid;
	}
}
