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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Sample controllers used to verify BoxLang template rendering.
 *
 * <h3>Routes</h3>
 * <ul>
 * <li>{@code GET /} → renders {@code templates/home.bxm}</li>
 * <li>{@code GET /greeting?name=...} → renders {@code templates/greeting.bxm}</li>
 * <li>{@code GET /error-demo} → renders {@code templates/error.bxm}</li>
 * </ul>
 */
@Controller
public class HomeController {

	/**
	 * Home page — injects a title attribute into the BoxLang variables scope.
	 *
	 * @param model Spring Model
	 *
	 * @return logical view name resolved to {@code classpath:/templates/home.bxm}
	 */
	@GetMapping( "/" )
	public String home( Model model ) {
		model.addAttribute( "title", "Hello from BoxLang + Spring Boot!" );
		model.addAttribute( "framework", "Spring Boot 3" );
		return "home";
	}

	/**
	 * Greeting page — accepts an optional {@code name} query parameter.
	 *
	 * @param name  the person to greet (defaults to "World")
	 * @param model Spring Model
	 *
	 * @return logical view name resolved to {@code classpath:/templates/greeting.bxm}
	 */
	@GetMapping( "/greeting" )
	public String greeting( @RequestParam( name = "name", defaultValue = "World" ) String name, Model model ) {
		model.addAttribute( "name", name );
		model.addAttribute( "message", "Welcome, " + name + "!" );
		return "greeting";
	}

	/**
	 * A page that exercises passing multiple complex model attributes.
	 *
	 * @param model Spring Model
	 *
	 * @return logical view name resolved to {@code classpath:/templates/items.bxm}
	 */
	@GetMapping( "/items" )
	public String items( Model model ) {
		model.addAttribute( "items", java.util.List.of( "Apple", "Banana", "Cherry" ) );
		model.addAttribute( "count", 3 );
		return "items";
	}

}
