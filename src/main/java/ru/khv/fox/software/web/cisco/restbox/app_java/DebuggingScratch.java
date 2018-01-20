/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import ru.khv.fox.software.web.cisco.restbox.app_java.configuration.AppProperties;

import java.util.Locale;

/**
 * Scratchpad for debugging purposes.
 */
@Slf4j
@RequiredArgsConstructor
@Component
class DebuggingScratch implements MessageSourceAware {
	private final AppProperties properties;
	private MessageSource messageSource;

	@Override
	public void setMessageSource(@Nullable final MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/*
		This method fires after application start-up process is complete.
		 */
	@EventListener(ApplicationReadyEvent.class)
	public void runAfterStartup() {
		log.trace("test1 trace");
		log.debug("test1 debug");
		log.info("test1 info");
		log.warn("test1 warn");
		log.error("test1 error");

		// print out a properties bean contents
		log.debug("app properties = " + properties);
		log.debug("message source = " + messageSource);
		if (messageSource != null) {
			log.debug("test error message system = " + messageSource.getMessage("test.errmsg", null, Locale.getDefault()));
			log.debug("test error message en = " + messageSource.getMessage("test.errmsg", null, Locale.ENGLISH));
			log.debug("test error message en_US = " + messageSource.getMessage("test.errmsg", null, Locale.US));
			log.debug("test error message de = " + messageSource.getMessage("test.errmsg", null, Locale.GERMAN));
			log.debug("test user message system = " + messageSource.getMessage("test.usermsg", null, Locale.getDefault()));
			log.debug("test user message en = " + messageSource.getMessage("test.usermsg", null, Locale.ENGLISH));
			log.debug("test user message en_US = " + messageSource.getMessage("test.usermsg", null, Locale.US));
			log.debug("test user message de = " + messageSource.getMessage("test.usermsg", null, Locale.GERMAN));
			// Accessor with binded specified or locale
			//final MessageSourceAccessor accessor = new MessageSourceAccessor(messageSource, Locale.getDefault());
			final MessageSourceAccessor accessor = new MessageSourceAccessor(messageSource);
			log.debug("test error message = " + accessor.getMessage("test.errmsg"));
			log.debug("test user message = " + accessor.getMessage("test.usermsg"));
		}
	}
}
