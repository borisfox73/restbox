/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

//import org.springframework.lang.NonNull;
//import org.springframework.lang.Nullable;
//
//import java.beans.PropertyEditorSupport;
//
///**
// * Case-insensitive converter for Enums.
// * <p>
// * @see <a href="https://stackoverflow.com/questions/4617099/spring-3-0-mvc-binding-enums-case-sensitive">Based on Toby Hobson answer</a>
// *</p>
// *
// * @param <T> Target Enum type
// */
//class CaseInsensitiveConverter<T extends Enum<T>> extends PropertyEditorSupport {
//
//	private final Class<T> typeParameterClass;
//
//
//	/**
//	 * Construct converter object for a given type.
//	 *
//	 * @param typeParameterClass    Type class
//	 */
//	CaseInsensitiveConverter(@NonNull final Class<T> typeParameterClass) {
//		this.typeParameterClass = typeParameterClass;
//	}
//
//	/**
//	 * Sets the property value by parsing a given String.  May raise
//	 * {@link IllegalArgumentException} if either the String is
//	 * badly formatted or if this kind of property can't be expressed
//	 * as text.
//	 *
//	 * @param text  The string to be parsed.
//	 * @throws IllegalArgumentException On conversion errors
//	 */
//	@Override
//	public void setAsText(@Nullable final String text) throws IllegalArgumentException {
//		if (text != null)
//			setValue(T.valueOf(typeParameterClass, text.toUpperCase()));
//		else
//			throw new IllegalArgumentException("Value is null");
//	}
//}
