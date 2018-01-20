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
		assertThat(properties, notNullValue());
		assertThat(properties.getTeststr(), is(equalTo("value")));
		assertThat(properties.getTestint(), is(equalTo(123)));
		assertThat(properties.getRouters(), hasKey(testRouterKey));
		assertThat(properties.getRouters().get(testRouterKey), instanceOf(AppProperties.RouterProperties.class));
		assertThat(properties.getRouters().get(testRouterKey).getName(), is(equalTo("CSR-WAN")));
		assertThat(properties.getRouters().get(testRouterKey).getHost(), is(equalTo("10.1.2.3")));
		assertThat(properties.getRouters().get(testRouterKey).getUsername(), is(equalTo("rest")));
		assertThat(properties.getRouters().get(testRouterKey).getPassword(), is(equalTo("restpwd")));
		assertThat(properties.getRouters().get(testRouterKey).getType(), is(equalTo(AppProperties.RouterProperties.RouterTypes.CSRV)));
	}
}
