/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
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
package ortus.boxlang.springboot.testapp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Live integration tests that start the full Spring Boot application on a
 * random port and verify BoxLang template rendering end-to-end.
 */
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT )
class IntegrationTest {

	@LocalServerPort
	private int					port;

	@Autowired
	private TestRestTemplate	restTemplate;

	// -----------------------------------------------------------------------
	// Helper
	// -----------------------------------------------------------------------

	private String url( String path ) {
		return "http://localhost:" + this.port + path;
	}

	// -----------------------------------------------------------------------
	// Home page
	// -----------------------------------------------------------------------

	@Test
	void homePage_returns200() {
		ResponseEntity<String> response = this.restTemplate.getForEntity( url( "/" ), String.class );
		assertThat( response.getStatusCode() ).isEqualTo( HttpStatus.OK );
	}

	@Test
	void homePage_containsTitle() {
		String body = this.restTemplate.getForObject( url( "/" ), String.class );
		assertThat( body ).contains( "Hello from BoxLang" );
	}

	@Test
	void homePage_containsFramework() {
		String body = this.restTemplate.getForObject( url( "/" ), String.class );
		assertThat( body ).contains( "Spring Boot 3" );
	}

	// -----------------------------------------------------------------------
	// Greeting page
	// -----------------------------------------------------------------------

	@Test
	void greetingPage_defaultName() {
		String body = this.restTemplate.getForObject( url( "/greeting" ), String.class );
		assertThat( body ).contains( "World" );
	}

	@Test
	void greetingPage_customName() {
		String body = this.restTemplate.getForObject( url( "/greeting?name=Developer" ), String.class );
		assertThat( body ).contains( "Developer" );
	}

	@Test
	void greetingPage_containsMessage() {
		String body = this.restTemplate.getForObject( url( "/greeting?name=BoxLang" ), String.class );
		assertThat( body ).contains( "Welcome, BoxLang!" );
	}

	// -----------------------------------------------------------------------
	// Items page
	// -----------------------------------------------------------------------

	@Test
	void itemsPage_returns200() {
		ResponseEntity<String> response = this.restTemplate.getForEntity( url( "/items" ), String.class );
		assertThat( response.getStatusCode() ).isEqualTo( HttpStatus.OK );
	}

	@Test
	void itemsPage_containsListItems() {
		String body = this.restTemplate.getForObject( url( "/items" ), String.class );
		assertThat( body )
		    .contains( "Apple" )
		    .contains( "Banana" )
		    .contains( "Cherry" );
	}

}
