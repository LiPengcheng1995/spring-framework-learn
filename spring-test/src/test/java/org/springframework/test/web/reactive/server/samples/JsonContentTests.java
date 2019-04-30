/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.test.web.reactive.server.samples;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.net.URI;

/**
 * Samples of tests using {@link WebTestClient} with serialized JSON content.
 *
 * @author Rossen Stoyanchev
 * @author Sam Brannen
 * @since 5.0
 */
public class JsonContentTests {

	private final WebTestClient client = WebTestClient.bindToController(new PersonController()).build();


	@Test
	public void jsonContent() {
		this.client.get().uri("/persons")
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.exchange()
				.expectStatus().isOk()
				.expectBody().json("[{\"name\":\"Jane\"},{\"name\":\"Jason\"},{\"name\":\"John\"}]");
	}

	@Test
	public void jsonPathIsEqualTo() {
		this.client.get().uri("/persons")
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].name").isEqualTo("Jane")
				.jsonPath("$[1].name").isEqualTo("Jason")
				.jsonPath("$[2].name").isEqualTo("John");
	}

	@Test // https://stackoverflow.com/questions/49149376/webtestclient-check-that-jsonpath-contains-sub-string
	public void jsonPathContainsSubstringViaRegex() {
		this.client.get().uri("/persons/John")
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				// The following determines if at least one person is returned with a
				// name containing "oh", and "John" matches that.
				.jsonPath("$[?(@.name =~ /.*oh.*/)].name").hasJsonPath();
	}

	@Test
	public void postJsonContent() {
		this.client.post().uri("/persons")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.syncBody("{\"name\":\"John\"}")
				.exchange()
				.expectStatus().isCreated()
				.expectBody().isEmpty();
	}


	@RestController
	@RequestMapping("/persons")
	static class PersonController {

		@GetMapping
		Flux<Person> getPersons() {
			return Flux.just(new Person("Jane"), new Person("Jason"), new Person("John"));
		}

		@GetMapping("/{name}")
		Person getPerson(@PathVariable String name) {
			return new Person(name);
		}

		@PostMapping
		ResponseEntity<String> savePerson(@RequestBody Person person) {
			return ResponseEntity.created(URI.create("/persons/" + person.getName())).build();
		}
	}

}
