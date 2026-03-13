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

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.application.BaseApplicationListener;
import ortus.boxlang.runtime.context.RequestBoxContext;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.exceptions.AbortException;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;
import ortus.boxlang.runtime.types.exceptions.MissingIncludeException;
import ortus.boxlang.web.context.WebRequestBoxContext;
import ortus.boxlang.web.handlers.WebErrorHandler;
import ortus.boxlang.web.springboot.exchange.SpringBoxHTTPExchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger	logger			= LoggerFactory.getLogger( BoxLangView.class );

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

	/**
	 * The BoxLang web runtime
	 */
	private final BoxRuntime	runtime;

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
		this.runtime			= BoxRuntime.getInstance();
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

		SpringBoxHTTPExchange	exchange			= new SpringBoxHTTPExchange( request, response );
		WebRequestBoxContext	context				= new WebRequestBoxContext(
		    runtime.getRuntimeContext(),
		    exchange,
		    this.webRoot
		);

		// Resolve the target view path
		String					resolvedViewPath	= resolveTemplatePath();
		Throwable				errorToHandle		= null;

		// Register as the current request context for this thread
		// Set threading Context and prep for request
		BaseApplicationListener	appListener			= initializeApplicationListener( context, resolvedViewPath );
		RequestBoxContext.setCurrent( context.getParentOfType( RequestBoxContext.class ) );

		try {
			// --- Inject Spring model → BoxLang variables scope ---
			if ( model != null && !model.isEmpty() ) {
				IScope variables = context.getScopeNearby( Key.variables );
				model.forEach( ( k, v ) -> variables.put( Key.of( k ), v ) );
			}

			// --- Execute the Listener onRequestStart method ---
			appListener.onRequestStart(
			    context,
			    new Object[] { resolvedViewPath, model, context }
			);

			// --- Execute the .bxm template ---
			runtime.executeTemplate( resolvedViewPath, context );

			// Any unhandled exceptions in the request, will skip onRequestEnd
			// This includes aborts, custom exceptions, and missing file includes
			appListener.onRequestEnd( context, new Object[] { resolvedViewPath, model, context } );

			// --- Flush BoxLang output buffer → servlet response writer ---
			context.flushBuffer( true );

		}
		// --- Handle AbortException separately to invoke onAbort listener method ---
		catch ( AbortException e ) {
			// We'll handle it below
			errorToHandle = e;
		}
		// --- Handle MissingIncludeException separately to allow custom handling via onMissingTemplate listener method without treating it as a full error
		catch ( MissingIncludeException e ) {
			try {
				// A return of true means the error has been "handled". False means the default
				// error handling should be used
				if ( appListener == null
				    || !appListener.onMissingTemplate( context, new Object[] { e.getMissingFileName() } ) ) {
					// If the Application listener didn't "handle" it, then let the default handling
					// kick in below
					errorToHandle = e;
				}
			} catch ( Throwable t ) {
				// Opps, an error while handling the missing template error
				errorToHandle = t;
			}

			if ( context != null ) {
				context.flushBuffer( false );
			}
		}
		// --- Handle any other exceptions that occurred during template execution or onRequestStart ---
		catch ( Throwable e ) {
			errorToHandle = e;
		}
		/**
		 * --------------------------------------------------------------------------------
		 * DEAL WITH ALL THE ERRORS HERE
		 * --------------------------------------------------------------------------------
		 * Finally, invoke onRequestEnd and handle any errors that occur there or during error handling ---
		 */
		finally {
			if ( context != null ) {
				context.flushBuffer( false );
			}

			// Was there an error produced above
			if ( errorToHandle != null ) {

				// If the error to handle is an abort, then take care of it
				if ( errorToHandle instanceof AbortException e ) {

					if ( appListener != null ) {
						try {
							appListener.onAbort( context, new Object[] { resolvedViewPath, model, context } );
						} catch ( AbortException aae ) {
							if ( aae.getCause() != null ) {
								errorToHandle = aae.getCause();
							}
						} catch ( Throwable ae ) {
							// Opps, an error while handling onAbort
							errorToHandle = ae;
						}
					}

					if ( context != null ) {
						context.flushBuffer( true );
					}

					if ( e.getCause() != null ) {
						errorToHandle = e.getCause();
					}
				}

				// This could still run EVEN IF the error above WAS an abort, as the onAbort could have thrown an error or the abort could have specified a
				// custom error to throw in its cause.
				if ( ! ( errorToHandle instanceof AbortException ) ) {
					// Log it to the exception logs no matter what
					BoxRuntime.getInstance()
					    .getLoggingService()
					    .getLogger( "exception" )
					    .error( errorToHandle.getMessage(), errorToHandle );

					try {
						// A return of true means the error has been "handled". False means the default
						// error handling should be used
						if ( appListener == null || !appListener.onError( context, new Object[] { errorToHandle, "" } ) ) {
							WebErrorHandler.handleError( errorToHandle, exchange, context, null, null );
						}
						// This is a failsafe in case the onError blows up.
					} catch ( AbortException ae ) {
						// If we abort during our onError, it's prolly too late to output a custom exception, so we'll ignore that logic in this path.
					} catch ( Throwable t ) {
						WebErrorHandler.handleError( t, exchange, context, null, null );
					}

				}

			}

			if ( context != null ) {
				context.flushBuffer( true );
			} else {
				exchange.flushResponseBuffer();
			}

			// Clean up the request context to prevent any thread-local leaks
			context.shutdown();
			RequestBoxContext.removeCurrent();
		}
	}

	// -----------------------------------------------------------------------
	// Helpers
	// -----------------------------------------------------------------------

	/**
	 * Initializes the application listener by loading the application descriptor from the specified request string.
	 *
	 * @param context       The web request context to use for loading the application descriptor
	 * @param requestString The request string that may contain the path to the application descriptor
	 *
	 * @return The initialized BaseApplicationListener
	 *
	 * @throws BoxRuntimeException if there is an error loading the application descriptor
	 */
	private static BaseApplicationListener initializeApplicationListener( WebRequestBoxContext context, String requestString ) {
		try {
			logger.debug( "Loading application descriptor for request: [{}]", requestString );
			// A scheme-less URI (e.g. a raw filesystem path) makes ApplicationService treat
			// the template as relative, causing mappingPath() to return null → NPE.
			// Convert to a proper file: URI so isAbsolute() returns true.
			URI requestUri = new URI( requestString );
			if ( requestUri.getScheme() == null ) {
				requestUri = java.nio.file.Path.of( requestString ).toUri();
			}
			context.loadApplicationDescriptor( requestUri );
			BaseApplicationListener appListener = context.getApplicationListener();
			logger.debug(
			    "Application descriptor loaded successfully. Descriptor set to: [{}] for application: [{}]", appListener.getBaseTemplatePath(),
			    appListener.getAppName()
			);
			return appListener;
		} catch ( Exception e ) {
			throw new BoxRuntimeException( "Failed to load application descriptor for request: [" + requestString + "]. " + e.getMessage(), e );
		}
	}

	/**
	 * Resolve the template resource to a path string suitable for
	 * {@link BoxRuntime#executeTemplate(String, ortus.boxlang.runtime.context.IBoxContext)}.
	 *
	 * <p>
	 * {@code BoxRuntime.executeTemplate(URL)} calls {@code Path.of(url.toURI())}
	 * internally, which throws {@code IllegalArgumentException: URI is not hierarchical}
	 * for relative {@code file:} URLs produced when {@code boxlang.prefix} is set to a
	 * relative filesystem path (e.g. {@code file:src/main/resources/templates/} in dev).
	 * </p>
	 *
	 * <p>
	 * When the resource is on disk (exploded classpath or {@code file:} prefix) we
	 * resolve it to an absolute path. For resources inside a packaged JAR the URL
	 * approach works fine so we fall back to {@code getURL().toExternalForm()}.
	 * </p>
	 *
	 * @return absolute path or external URL string for the template
	 *
	 * @throws IOException if the resource cannot be accessed
	 */
	private String resolveTemplatePath() throws IOException {
		try {
			// Works for file: resources and exploded classpath (dev / bootRun)
			return this.templateResource.getFile().getAbsolutePath();
		} catch ( IOException jarEx ) {
			// Resource is inside a packaged JAR — the URL form works fine with BoxRuntime
			return this.templateResource.getURL().toExternalForm();
		}
	}

}
