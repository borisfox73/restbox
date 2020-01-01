/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.Router;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.RouterFunction;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.Box;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.AclInterfaceDTO;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.InterfaceStateDTO;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.RestApiDTO;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Configuration
class EntityConfiguration {

	// Can't inject appProperties bean to this one as it would make circular dependencies between AppProperties,
	// ValidBoxControlValidator, and EntityConfiguration itself. So use static methods with Value injection.

	@Bean
	static Collection<Box> boxes(@Value("#{appProperties.boxcontrol}") final Collection<AppProperties.BoxProperties> boxControls) {
		// Use configuration properties to instantiate dynamic box objects holding runtime state
		return boxControls.stream()
		                  .map(Box::getInstance)
		                  .collect(Collectors.toUnmodifiableList());
	}

	@Bean
	static Collection<Router> routers(@Value("#{appProperties.routers}") final Map<String, AppProperties.RouterProperties> routers) {
		// Use configuration properties to instantiate dynamic router objects holding runtime state
		return routers.entrySet()
		              .stream()
		              .map(entry -> Router.getInstance(entry.getKey(), entry.getValue()))
		              .collect(Collectors.toUnmodifiableSet());
	}

	@Bean
	static Map<String, RouterFunction<? extends RestApiDTO, ? extends RestApiDTO, ?>> routerFunctions(final Collection<Router> routers) {
		// map with router descriptors keyed by name
		val routerMap = routers.stream().collect(Collectors.toUnmodifiableMap(Router::getName, r -> r));

		// TODO make configurable from YAML configuration properties file. Create static constructor from conf.props object
		// ACTION functions
		val f1 =
				RouterFunction.builder(routerMap)
				              .type(RouterFunction.FunctionType.ACTION)
				              .name("anone")
				              .descr("No Action")
				              .build();
		val f2 =
				RouterFunction.builder(routerMap)
				              .type(RouterFunction.FunctionType.ACTION)
				              .name("afunc1")
				              .descr("CSR-WAN Gi3 down")
				              .routerName("CSR-WAN")
				              .uriPath("interfaces/gigabitEthernet3/state")
				              .requestMethod(HttpMethod.PUT)
				              .requestObject(InterfaceStateDTO.create("gigabitEthernet3", false))
				              .build();
		val f3 =
				RouterFunction.builder(routerMap)
				              .type(RouterFunction.FunctionType.ACTION)
				              .name("afunc2")
				              .descr("CSR-WAN Gi3 up")
				              .routerName("CSR-WAN")
				              .uriPath("interfaces/gigabitEthernet3/state")
				              .requestMethod(HttpMethod.PUT)
				              .requestObject(InterfaceStateDTO.create("gigabitEthernet3", true))
				              .build();
		val f4 =
				RouterFunction.builder(routerMap)
				              .type(RouterFunction.FunctionType.ACTION)
				              .name("afunc3")
				              .descr("CSR-WAN Gi3 enable ACL STOP80")
				              .routerName("CSR-WAN")
				              .uriPath("acl/STOP80/interfaces")
				              .requestMethod(HttpMethod.POST)
				              .requestObject(AclInterfaceDTO.create("gigabitEthernet3", "inside"))
				              .build();
		val f5 =
				RouterFunction.builder(routerMap)
				              .type(RouterFunction.FunctionType.ACTION)
				              .name("afunc4")
				              .descr("CSR-WAN Gi3 disable ACL STOP80")
				              .routerName("CSR-WAN")
				              .uriPath("acl/STOP80/interfaces/gigabitEthernet3_inside")
				              .requestMethod(HttpMethod.DELETE)
				              .build();
		val f6 =
				RouterFunction.builder(routerMap)
				              .type(RouterFunction.FunctionType.ACTION)
				              .name("afunc5")
				              .descr("CSR-WAN Gi3 enable ACL STOP22")
				              .routerName("CSR-WAN")
				              .uriPath("acl/STOP22/interfaces")
				              .requestMethod(HttpMethod.POST)
				              .requestObject(AclInterfaceDTO.create("gigabitEthernet3", "inside"))
				              .build();
		val f7 =
				RouterFunction.builder(routerMap)
				              .type(RouterFunction.FunctionType.ACTION)
				              .name("afunc6")
				              .descr("CSR-WAN Gi3 disable ACL STOP22")
				              .routerName("CSR-WAN")
				              .uriPath("acl/STOP22/interfaces/gigabitEthernet3_inside")
				              .requestMethod(HttpMethod.DELETE)
				              .build();
		val f8 =
				RouterFunction.builder(routerMap)
				              .type(RouterFunction.FunctionType.ACTION)
				              .name("afunc7")
				              .descr("CSR-AWS Gi1 enable ACL STOP80")
				              .routerName("CSR-AWS")
				              .uriPath("acl/STOP80/interfaces")
				              .requestMethod(HttpMethod.POST)
				              .requestObject(AclInterfaceDTO.create("gigabitEthernet1", "inside"))
				              .build();
		val f9 =
				RouterFunction.builder(routerMap)
				              .type(RouterFunction.FunctionType.ACTION)
				              .name("afunc8")
				              .descr("CSR-AWS Gi1 disable ACL STOP80")
				              .routerName("CSR-AWS")
				              .uriPath("acl/STOP80/interfaces/gigabitEthernet1_inside")
				              .requestMethod(HttpMethod.DELETE)
				              .build();
		val f10 =
				RouterFunction.builder(routerMap)
				              .type(RouterFunction.FunctionType.ACTION)
				              .name("afunc9")
				              .descr("CSR-AWS Gi1 enable ACL STOPPING")
				              .routerName("CSR-AWS")
				              .uriPath("acl/STOPPING/interfaces")
				              .requestMethod(HttpMethod.POST)
				              .requestObject(AclInterfaceDTO.create("gigabitEthernet1", "inside"))
				              .build();
		val f11 =
				RouterFunction.builder(routerMap)
				              .type(RouterFunction.FunctionType.ACTION)
				              .name("afunc10")
				              .descr("CSR-AWS Gi1 disable ACL STOPPING")
				              .routerName("CSR-AWS")
				              .uriPath("acl/STOPPING/interfaces/gigabitEthernet1_inside")
				              .requestMethod(HttpMethod.DELETE)
				              .build();
		val f12 =
				RouterFunction.builder(routerMap)
				              .type(RouterFunction.FunctionType.ACTION)
				              .name("afunc11")
				              .descr("CSR-AWS Gi1 enable ACL STOP443")
				              .routerName("CSR-AWS")
				              .uriPath("acl/STOP443/interfaces")
				              .requestMethod(HttpMethod.POST)
				              .requestObject(AclInterfaceDTO.create("gigabitEthernet1", "inside"))
				              .build();
		val f13 =
				RouterFunction.builder(routerMap)
				              .type(RouterFunction.FunctionType.ACTION)
				              .name("afunc12")
				              .descr("CSR-AWS Gi1 disable ACL STOP443")
				              .routerName("CSR-AWS")
				              .uriPath("acl/STOP443/interfaces/gigabitEthernet1_inside")
				              .requestMethod(HttpMethod.DELETE)
				              .build();
		// READ functions
		val rf1 =
				RouterFunction.builder(routerMap)
				              .type(RouterFunction.FunctionType.READ)
				              .name("rnone")
				              .descr("No Action")
				              .build();
		// translation function translates response body to box indicator value
		// if callback is not specified, response body itself will be returned by action execution function
		val rf2 =
				RouterFunction.<RestApiDTO, InterfaceStateDTO, Integer>builder(routerMap)
						.type(RouterFunction.FunctionType.READ)
						.name("rfunc1")
						.descr("Gi3 down on CSR-WAN")
						.routerName("CSR-WAN")
						.uriPath("interfaces/gigabitEthernet3/state")
						.requestMethod(HttpMethod.GET)
						.responseClazz(InterfaceStateDTO.class)
						.mapFunction(responseDto -> bool2int(!responseDto.isEnabled()))
						.build();
		val rf3 =
				RouterFunction.<RestApiDTO, InterfaceStateDTO, Integer>builder(routerMap)
						.type(RouterFunction.FunctionType.READ)
						.name("rfunc2")
						.descr("Gi3 up on CSR-WAN")
						.routerName("CSR-WAN")
						.uriPath("interfaces/gigabitEthernet3/state")
						.requestMethod(HttpMethod.GET)
						.responseClazz(InterfaceStateDTO.class)
						.mapFunction(responseDto -> bool2int(responseDto.isEnabled()))
						.build();
		val rf4 =
				RouterFunction.<RestApiDTO, InterfaceStateDTO, Integer>builder(routerMap)
						.type(RouterFunction.FunctionType.READ)
						.name("rfunc3")
						.descr("Gi1 up on CSR-AWS")
						.routerName("CSR-AWS")
						.uriPath("interfaces/gigabitEthernet1/state")
						.requestMethod(HttpMethod.GET)
						.responseClazz(InterfaceStateDTO.class)
						.mapFunction(responseDto -> bool2int(responseDto.isEnabled()))
						.build();
		val rf5 =
				RouterFunction.<RestApiDTO, AclInterfaceDTO, Integer>builder(routerMap)
						.type(RouterFunction.FunctionType.READ)
						.name("rfunc4")
						.descr("Gi1 acl STOPPING enabled")
						.routerName("CSR-AWS")
						.uriPath("acl/STOPPING/interfaces/gigabitEthernet1_inside")
						.requestMethod(HttpMethod.GET)
						.responseClazz(AclInterfaceDTO.class)
						.mapFunction(responseDto -> 1)
						.resourceNotFoundFunction(errorDto -> 0)
						.build();
		// pack into map
		return Stream.of(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, rf1, rf2, rf3, rf4, rf5)
		             .collect(Collectors.<RouterFunction<? extends RestApiDTO, ? extends RestApiDTO, ?>, String, RouterFunction<? extends RestApiDTO, ? extends RestApiDTO, ?>>toUnmodifiableMap(RouterFunction::getName, f -> f));
	}

	// boolean to integer conversion to calculate box indicator state to be set
	private static Integer bool2int(final boolean b) {
		return b ? 1 : 0;
	}
}