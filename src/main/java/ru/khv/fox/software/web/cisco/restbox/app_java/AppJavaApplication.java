/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * Application entry point.
 */
@SpringBootApplication
//@ComponentScan(basePackages = {"ru.khv.fox.software.web.cisco.restbox.app_java"})
@EnableWebFlux
public class AppJavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppJavaApplication.class, args);
	}

/*
	public static void main(String[] args) {
		try (AnnotationConfigApplicationContext context
				     = new AnnotationConfigApplicationContext(
				SpringSecurity5Application.class)) {

			context.getBean(NettyContext.class).onClose().block();
		}
	}

	@Bean
	public NettyContext nettyContext(ApplicationContext context) {
		HttpHandler handler = WebHttpHandlerBuilder
				.applicationContext(context).build();
		ReactorHttpHandlerAdapter adapter
				= new ReactorHttpHandlerAdapter(handler);
		HttpServer httpServer = HttpServer.create("localhost", 8080);
		return httpServer.newHandler(adapter).block();
	}
*/

}
