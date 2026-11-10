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
import com.soklet.Soklet;
import com.soklet.SokletConfig;
import com.soklet.annotation.GET;
import com.soklet.annotation.QueryParameter;

import java.util.List;
import java.util.Map;

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
				.headers(Map.of("Content-Type", List.of("application/json; charset=UTF-8")))
				// A real application would not construct JSON in this manner
				.body(String.format("{\"input\": %d}", input))
				.build();
	}

	public static void main(String[] args) throws Exception {
		SokletConfig sokletConfig = SokletConfig.withHttpServer(
				HttpServer.withPort(8080).host("127.0.0.1").build()
		).build();

		Soklet soklet = Soklet.run(sokletConfig);
		System.out.println("Soklet Barebones App started on port 8080");
		soklet.awaitShutdown();
	}
}
