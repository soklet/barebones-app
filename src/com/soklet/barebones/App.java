/*
 * Copyright 2022-2026 Revetware LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.soklet.barebones;

import com.soklet.Response;
import com.soklet.HttpServer;
import com.soklet.ShutdownTrigger;
import com.soklet.SokletApplication;
import com.soklet.SokletConfig;
import com.soklet.annotation.GET;
import com.soklet.annotation.QueryParameter;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="https://www.revetware.com">Mark Allen</a>
 */
public class App {
	private static final int DEFAULT_HTTP_PORT = 8080;
	private static final String LOOPBACK_HTTP_HOST = "127.0.0.1";
	private static final String LOOPBACK_HTTP_PORT_ENVIRONMENT_VARIABLE = "SOKLET_BAREBONES_LOOPBACK_PORT";

	@GET("/")
	public String index() {
		return "Hello, world!";
	}

	@GET("/test-input")
	public Response testInput(@QueryParameter Integer input) {
		return Response.withStatusCode(200)
				.headers(Map.of("Content-Type", Set.of("application/json; charset=UTF-8")))
				// A real application would not construct JSON in this manner
				.body(String.format("{\"input\": %d}", input))
				.build();
	}

	private static int resolveHttpPort(String value) {
		if (value == null)
			return DEFAULT_HTTP_PORT;

		int port;

		try {
			port = Integer.parseInt(value);
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException(String.format(
					"%s must be an integer from 1 through 65535",
					LOOPBACK_HTTP_PORT_ENVIRONMENT_VARIABLE), exception);
		}

		if (port < 1 || port > 65535)
			throw new IllegalArgumentException(String.format(
					"%s must be an integer from 1 through 65535", LOOPBACK_HTTP_PORT_ENVIRONMENT_VARIABLE));

		return port;
	}

	public static void main(String[] args) throws Exception {
		String loopbackPortOverride = System.getenv(LOOPBACK_HTTP_PORT_ENVIRONMENT_VARIABLE);
		int port = resolveHttpPort(loopbackPortOverride);
		HttpServer.Builder httpServerBuilder = HttpServer.withPort(port);

		if (loopbackPortOverride != null)
			httpServerBuilder.host(LOOPBACK_HTTP_HOST);
		
		SokletConfig sokletConfig = SokletConfig.withHttpServer(
				httpServerBuilder.build()
		).build();

		// In an interactive console environment, it makes sense to stop on `Enter` keypress.
		// In a Docker container, it makes sense to wait for JVM shutdown (e.g. SIGTERM)
		boolean stopOnEnterKey = !"true".equals(System.getenv("RUNNING_IN_DOCKER"));

		System.out.printf("Starting Soklet Barebones App on port %d\n", port);

		if (stopOnEnterKey) {
			System.out.println("Press [enter] to exit once ready");
			SokletApplication.run(sokletConfig, ShutdownTrigger.ENTER_KEY);
		} else {
			SokletApplication.run(sokletConfig);
		}
	}
}
