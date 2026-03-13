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
package ortus.boxlang.web.springboot.view;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.RequestBoxContext;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.web.context.WebRequestBoxContext;
import ortus.boxlang.web.springboot.exchange.SpringBoxHTTPExchange;

import org.springframework.core.io.Resource;
import org.springframework.web.servlet.View;

/**
 * A Spring MVC {@link View} that renders a BoxLang markup template ({@code .bxm}).
 *
 * <h2>Rendering flow</h2>
 * <ol>
 * <li>Wrap the incoming {@link HttpServletRequest}/{@link HttpServletResponse} in a
 * {@link SpringBoxHTTPExchange} — a pure proxy with no added logic.</li>
 * <li>Construct a {@link WebRequestBoxContext} on top of the BoxLang runtime context,
 * which makes the full set of web scopes (URL, Form, CGI, Cookie, Request) available
 * inside the template.</li>
 * <li>Inject every entry from the Spring model {@code Map} into the BoxLang
 * {@code variables} scope so templates access them as {@code #variables.myKey#}.</li>
 * <li>Execute the template via {@link BoxRuntime#executeTemplate}.</li>
 * <li>Flush the BoxLang output buffer to the servlet response and shut down the
 * request context.</li>
 * </ol>
 */
public class BoxLangView implements View {

	// -----------------------------------------------------------------------
	// Fields
	// -----------------------------------------------------------------------

	private static final String	CONTENT_TYPE	= "text/html;charset=UTF-8";

	/**
	 * The Spring resource pointing to the template file.
	 * Resolved by {@link BoxLangViewResolver} via the classpath or filesystem.
	 */
	private final Resource		templateResource;

	/**
	 * The BoxLang web root (filesystem path).
	 * Used by {@link WebRequestBoxContext} for BoxLang path mappings.
	 * Defaults to the JVM working directory when blank.
	 */
	private final String		webRoot;

	// -----------------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------------

	/**
	 * Creates a BoxLangView for the given template resource.
	 *
	 * @param templateResource Spring resource for the {@code .bxm} file (never null)
	 * @param webRoot          filesystem web root; may be empty/null — defaults to "."
	 */
	public BoxLangView( Resource templateResource, String webRoot ) {
		this.templateResource	= templateResource;
		this.webRoot			= ( webRoot != null && !webRoot.isBlank() ) ? webRoot : ".";
	}

	// -----------------------------------------------------------------------
	// View
	// -----------------------------------------------------------------------

	@Override
	public String getContentType() {
		return CONTENT_TYPE;
	}

	/**
	 * Render the BoxLang template, injecting all Spring model attributes as
	 * BoxLang {@code variables} scope entries.
	 *
	 * @param model    Spring model attributes (may be null)
	 * @param request  the current HTTP servlet request
	 * @param response the current HTTP servlet response
	 */
	@Override
	public void render( Map<String, ?> model, HttpServletRequest request, HttpServletResponse response ) throws Exception {
		// Ensure UTF-8 content type is set before any output is written
		if ( !response.isCommitted() ) {
			response.setContentType( CONTENT_TYPE );
		}

		SpringBoxHTTPExchange	exchange	= new SpringBoxHTTPExchange( request, response );
		WebRequestBoxContext	context		= new WebRequestBoxContext(
		    BoxRuntime.getInstance().getRuntimeContext(),
		    exchange,
		    this.webRoot
		);

		// Register as the current request context for this thread
		RequestBoxContext.setCurrent( context );

		try {
			// --- Inject Spring model → BoxLang variables scope ---
			if ( model != null && !model.isEmpty() ) {
				IScope variables = context.getScopeNearby( Key.variables );
				model.forEach( ( k, v ) -> variables.put( Key.of( k ), v ) );
			}

			// --- Execute the .bxm template ---
			BoxRuntime.getInstance().executeTemplate( this.templateResource.getURL(), context );

			// --- Flush BoxLang output buffer → servlet response writer ---
			context.flushBuffer( true );

		} finally {
			context.shutdown();
			RequestBoxContext.removeCurrent();
		}
	}

}
