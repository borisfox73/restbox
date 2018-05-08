/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.khv.fox.software.web.cisco.restbox.app_java.configuration.AppProperties;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AppInitializationTests {

	@Test
	public void contextLoads() {
	}

	@Autowired
	private AppProperties properties;

	@Test
	public void loadingOfProperties() {
		/*
		  test: value
		  routers:
		    testcsr1:
		      name: CSR-WAN
		      host: 10.1.2.3
		      username: rest
		      password: restpwd
		      type: csrv
		    testcsr2:
		      name: CSR-WAN2
		      host: 10.1.2.4
		      username: rest2
		      password: restpwd2
		      type: asr
		 */
		final String testRouterKey = "testcsr1";
		System.out.println("loaded properties = " + properties);
		assertThat(properties, notNullValue(AppProperties.class));
		assertThat(properties, hasProperty("teststr", is(equalTo("value"))));
		assertThat(properties, hasProperty("testint", is(equalTo(123))));
		assertThat(properties, hasProperty("routers", hasKey(testRouterKey)));
		assertThat(properties.getRouters().get(testRouterKey), instanceOf(AppProperties.RouterProperties.class));
		assertThat(properties.getRouters().get(testRouterKey).getName(), is(equalTo("CSR-WAN")));
		assertThat(properties.getRouters().get(testRouterKey).getHost(), is(equalTo("10.1.2.3")));
		assertThat(properties.getRouters().get(testRouterKey).getUsername(), is(equalTo("rest")));
		assertThat(properties.getRouters().get(testRouterKey).getPassword(), is(equalTo("restpwd")));
		assertThat(properties.getRouters().get(testRouterKey).getType(), is(equalTo(AppProperties.RouterProperties.RouterTypes.CSRV)));

		assertThat(properties, hasProperty("users", notNullValue(AppProperties.UserProperties.class)));
		assertThat(properties.getUsers().isEmpty(), is(false));
		assertThat(properties.getUsers().get(0).getUsername(), is(equalTo("user1")));
		assertThat(properties.getUsers().get(0).getPassword(), is(equalTo("user123")));
		assertThat(properties.getUsers().get(0).getRoles(), instanceOf(String[].class));
		assertThat(properties.getUsers().get(0).getRoles().length, is(greaterThan(0)));
		assertThat(properties.getUsers().get(0).getRoles()[0], is(equalTo("USER")));
	}
}
