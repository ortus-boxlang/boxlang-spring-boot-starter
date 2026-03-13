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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the BoxLang Spring Boot test application.
 *
 * <p>
 * This application is used for live integration testing of the
 * {@code boxlang-spring-boot} library. It registers two controllers that
 * serve BoxLang {@code .bxm} templates and validates end-to-end rendering.
 * </p>
 */
@SpringBootApplication
public class TestApplication {

	public static void main( String[] args ) {
		SpringApplication.run( TestApplication.class, args );
	}

}
