/*
 * Copyright 2022-2025 Revetware LLC.
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
import com.soklet.Server;
import com.soklet.ShutdownTrigger;
import com.soklet.Soklet;
import com.soklet.SokletConfig;
import com.soklet.annotation.GET;
import com.soklet.annotation.QueryParameter;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="https://www.revetware.com">Mark Allen</a>
 */
public class App {
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

	public static void main(String[] args) throws Exception {
		int port = 8080;
		
		SokletConfig sokletConfig = SokletConfig.withServer(
				Server.fromPort(port)
		).build();

		// In an interactive console environment, it makes sense to stop on `Enter` keypress.
		// In a Docker container, it makes sense to wait for JVM shutdown (e.g. SIGTERM)
		boolean stopOnEnterKey = !"true".equals(System.getenv("RUNNING_IN_DOCKER"));

		try (Soklet soklet = Soklet.fromConfig(sokletConfig)) {
			soklet.start();

			System.out.printf("Soklet Barebones App started on port %d\n", port);

			if (stopOnEnterKey) {
				System.out.println("Press [enter] to exit");
				soklet.awaitShutdown(ShutdownTrigger.ENTER_KEY);
			} else {
				soklet.awaitShutdown();
			}
		}
	}
}
