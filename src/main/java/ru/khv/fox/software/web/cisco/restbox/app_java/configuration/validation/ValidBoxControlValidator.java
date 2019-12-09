/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration.validation;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.RouterFunction;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.Set;

import static ru.khv.fox.software.web.cisco.restbox.app_java.configuration.AppProperties.BoxControlProperties;

@Slf4j
public class ValidBoxControlValidator implements ConstraintValidator<ValidBoxControl, BoxControlProperties> {

	private final Set<String> routerFunctionNames;


	// Autowiring constructor
	private ValidBoxControlValidator(final Map<String, RouterFunction<?, ?, ?>> routerFunctions) {
		this.routerFunctionNames = routerFunctions.keySet();
		log.trace("Available router function names: {}", this.routerFunctionNames);
	}

	@Override
	public boolean isValid(@Nullable final BoxControlProperties boxControlProperties, @NonNull final ConstraintValidatorContext context) {
		if (boxControlProperties == null)
			return true;

		val type = boxControlProperties.getType();
		if (type == null)
			return true;

		var valid = true;
		String propertyName = null;
		String propertyValue = null;
		String template = null;

		if (!type.isSensor()) {
			if (boxControlProperties.getOnFunc() != null) {
				template = "{constraints.ValidBoxControl.onfunc.na}";
				propertyName = "onFunc";
				valid = false;
			}
			if (boxControlProperties.getOffFunc() != null) {
				template = "{constraints.ValidBoxControl.offfunc.na}";
				propertyName = "offFunc";
				valid = false;
			}
		}

		if (!type.isIndicator()) {
			if (boxControlProperties.getRFunc() != null) {
				template = "{constraints.ValidBoxControl.rfunc.na}";
				propertyName = "rFunc";
				valid = false;
			}
		}

		// Validate by router functions set
		if (valid && boxControlProperties.getOnFunc() != null && !routerFunctionNames.contains(boxControlProperties.getOnFunc())) {
			template = "{constraints.ValidBoxControl.onfunc.inv}";
			propertyName = "onFunc";
			propertyValue = boxControlProperties.getOnFunc();
			valid = false;
		}
		if (valid && boxControlProperties.getOffFunc() != null && !routerFunctionNames.contains(boxControlProperties.getOffFunc())) {
			template = "{constraints.ValidBoxControl.offfunc.inv}";
			propertyName = "offFunc";
			propertyValue = boxControlProperties.getOffFunc();
			valid = false;
		}
		if (valid && boxControlProperties.getRFunc() != null && !routerFunctionNames.contains(boxControlProperties.getRFunc())) {
			template = "{constraints.ValidBoxControl.rfunc.inv}";
			propertyName = "rFunc";
			propertyValue = boxControlProperties.getRFunc();
			valid = false;
		}

		if (!valid) {
			if (propertyValue != null)
				context.unwrap(HibernateConstraintValidatorContext.class).addMessageParameter("value", propertyValue);
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(template).addPropertyNode(propertyName).addConstraintViolation();
		}
		return valid;
	}
}
