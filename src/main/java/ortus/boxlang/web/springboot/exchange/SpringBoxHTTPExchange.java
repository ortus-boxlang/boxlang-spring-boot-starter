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
package ortus.boxlang.web.springboot.exchange;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ortus.boxlang.web.context.WebRequestBoxContext;
import ortus.boxlang.web.exchange.BoxCookie;
import ortus.boxlang.web.exchange.IBoxHTTPExchange;

/**
 * A pure proxy implementation of {@link IBoxHTTPExchange} that delegates every
 * method call directly to the underlying Jakarta {@link HttpServletRequest} and
 * {@link HttpServletResponse} objects provided by Spring Boot / Tomcat.
 *
 * The only non-trivial code in this class is the mandatory type-bridging
 * between Jakarta's {@link Cookie} and BoxLang's {@link BoxCookie} — the two
 * classes are incompatible but carry the same data.
 *
 * No custom logic, no state, no caching — pure delegation.
 */
public class SpringBoxHTTPExchange implements IBoxHTTPExchange {

	// -----------------------------------------------------------------------
	// Fields
	// -----------------------------------------------------------------------

	private final HttpServletRequest	request;
	private final HttpServletResponse	response;

	/**
	 * The BoxLang web context wired to this exchange, set by
	 * {@link WebRequestBoxContext} during its own construction.
	 */
	private WebRequestBoxContext		webContext;

	// -----------------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------------

	/**
	 * Create a new exchange proxy wrapping an existing servlet request/response.
	 *
	 * @param request  the incoming Jakarta servlet request (never null)
	 * @param response the outgoing Jakarta servlet response (never null)
	 */
	public SpringBoxHTTPExchange( HttpServletRequest request, HttpServletResponse response ) {
		this.request	= request;
		this.response	= response;
	}

	// -----------------------------------------------------------------------
	// Context wiring
	// -----------------------------------------------------------------------

	@Override
	public void setWebContext( WebRequestBoxContext context ) {
		this.webContext = context;
	}

	@Override
	public WebRequestBoxContext getWebContext() {
		return this.webContext;
	}

	// -----------------------------------------------------------------------
	// Forward
	// -----------------------------------------------------------------------

	@Override
	public void forward( String URI ) {
		try {
			this.request.getRequestDispatcher( URI ).forward( this.request, this.response );
		} catch ( Exception e ) {
			throw new RuntimeException( "BoxLang forward failed: " + URI, e );
		}
	}

	// -----------------------------------------------------------------------
	// Request methods — pure delegation to HttpServletRequest
	// -----------------------------------------------------------------------

	@Override
	public String getRequestAuthType() {
		return this.request.getAuthType();
	}

	@Override
	public BoxCookie[] getRequestCookies() {
		Cookie[] cookies = this.request.getCookies();
		if ( cookies == null ) {
			return new BoxCookie[ 0 ];
		}
		return Arrays.stream( cookies )
		    .map( this::toBoxCookie )
		    .toArray( BoxCookie[]::new );
	}

	@Override
	public BoxCookie getRequestCookie( String name ) {
		Cookie[] cookies = this.request.getCookies();
		if ( cookies == null ) {
			return null;
		}
		return Arrays.stream( cookies )
		    .filter( c -> c.getName().equalsIgnoreCase( name ) )
		    .map( this::toBoxCookie )
		    .findFirst()
		    .orElse( null );
	}

	@Override
	public Map<String, String[]> getRequestHeaderMap() {
		Map<String, String[]>	map		= new HashMap<>();
		Enumeration<String>		names	= this.request.getHeaderNames();
		if ( names != null ) {
			while ( names.hasMoreElements() ) {
				String		name	= names.nextElement();
				String[]	values	= Collections.list( this.request.getHeaders( name ) ).toArray( new String[ 0 ] );
				map.put( name, values );
			}
		}
		return map;
	}

	@Override
	public String getRequestHeader( String name ) {
		return this.request.getHeader( name );
	}

	@Override
	public String getRequestMethod() {
		return this.request.getMethod();
	}

	@Override
	public String getRequestPathInfo() {
		return this.request.getPathInfo();
	}

	@Override
	public String getRequestPathTranslated() {
		return this.request.getPathTranslated();
	}

	@Override
	public String getRequestContextPath() {
		return this.request.getContextPath();
	}

	@Override
	public String getRequestQueryString() {
		return this.request.getQueryString();
	}

	@Override
	public String getRequestRemoteUser() {
		return this.request.getRemoteUser();
	}

	@Override
	public java.security.Principal getRequestUserPrincipal() {
		return this.request.getUserPrincipal();
	}

	@Override
	public String getRequestURI() {
		return this.request.getRequestURI();
	}

	@Override
	public StringBuffer getRequestURL() {
		return this.request.getRequestURL();
	}

	@Override
	public Object getRequestAttribute( String name ) {
		return this.request.getAttribute( name );
	}

	@Override
	public Map<String, Object> getRequestAttributeMap() {
		Map<String, Object>	map		= new HashMap<>();
		Enumeration<String>	names	= this.request.getAttributeNames();
		while ( names.hasMoreElements() ) {
			String name = names.nextElement();
			map.put( name, this.request.getAttribute( name ) );
		}
		return map;
	}

	@Override
	public String getRequestCharacterEncoding() {
		return this.request.getCharacterEncoding();
	}

	@Override
	public long getRequestContentLength() {
		return this.request.getContentLengthLong();
	}

	@Override
	public String getRequestContentType() {
		return this.request.getContentType();
	}

	@Override
	public Map<String, String[]> getRequestFormMap() {
		return this.request.getParameterMap();
	}

	/**
	 * File uploads not managed by this exchange; returns empty array.
	 * If multipart handling is required, configure Spring's multipart resolver.
	 */
	@Override
	public FileUpload[] getUploadData() {
		return new FileUpload[ 0 ];
	}

	@Override
	public Map<String, String[]> getRequestURLMap() {
		// For a standard GET, URL params are the same as request parameters
		String queryString = this.request.getQueryString();
		if ( queryString == null || queryString.isEmpty() ) {
			return new HashMap<>();
		}
		Map<String, String[]> map = new HashMap<>();
		for ( String pair : queryString.split( "&" ) ) {
			String[]	parts	= pair.split( "=", 2 );
			String		key		= java.net.URLDecoder.decode( parts[ 0 ], java.nio.charset.StandardCharsets.UTF_8 );
			String		value	= parts.length > 1 ? java.net.URLDecoder.decode( parts[ 1 ], java.nio.charset.StandardCharsets.UTF_8 ) : "";
			map.merge( key, new String[] { value }, ( existing, newVal ) -> {
				String[] merged = new String[ existing.length + 1 ];
				System.arraycopy( existing, 0, merged, 0, existing.length );
				merged[ existing.length ] = newVal[ 0 ];
				return merged;
			} );
		}
		return map;
	}

	@Override
	public String getRequestProtocol() {
		return this.request.getProtocol();
	}

	@Override
	public String getRequestScheme() {
		return this.request.getScheme();
	}

	@Override
	public String getRequestServerName() {
		return this.request.getServerName();
	}

	@Override
	public int getRequestServerPort() {
		return this.request.getServerPort();
	}

	@Override
	public Object getRequestBody() {
		try {
			return this.request.getInputStream().readAllBytes();
		} catch ( IOException e ) {
			throw new RuntimeException( "Failed to read request body", e );
		}
	}

	@Override
	public String getRequestRemoteAddr() {
		return this.request.getRemoteAddr();
	}

	@Override
	public String getRequestRemoteHost() {
		return this.request.getRemoteHost();
	}

	@Override
	public void setRequestAttribute( String name, Object o ) {
		this.request.setAttribute( name, o );
	}

	@Override
	public void removeRequestAttribute( String name ) {
		this.request.removeAttribute( name );
	}

	@Override
	public Locale getRequestLocale() {
		return this.request.getLocale();
	}

	@Override
	public Enumeration<Locale> getRequestLocales() {
		return this.request.getLocales();
	}

	@Override
	public boolean isRequestSecure() {
		return this.request.isSecure();
	}

	@Override
	public int getRequestRemotePort() {
		return this.request.getRemotePort();
	}

	@Override
	public String getRequestLocalName() {
		return this.request.getLocalName();
	}

	@Override
	public String getRequestLocalAddr() {
		return this.request.getLocalAddr();
	}

	@Override
	public int getRequestLocalPort() {
		return this.request.getLocalPort();
	}

	// -----------------------------------------------------------------------
	// Response methods — pure delegation to HttpServletResponse
	// -----------------------------------------------------------------------

	@Override
	public boolean isResponseStarted() {
		return this.response.isCommitted();
	}

	@Override
	public void addResponseCookie( BoxCookie cookie ) {
		this.response.addCookie( toServletCookie( cookie ) );
	}

	@Override
	public void setResponseHeader( String name, String value ) {
		this.response.setHeader( name, value );
	}

	@Override
	public void addResponseHeader( String name, String value ) {
		this.response.addHeader( name, value );
	}

	@Override
	public void setResponseStatus( int sc ) {
		this.response.setStatus( sc );
	}

	@Override
	public void setResponseStatus( int sc, String sm ) {
		this.response.setStatus( sc );
	}

	@Override
	public int getResponseStatus() {
		return this.response.getStatus();
	}

	@Override
	public String getResponseHeader( String name ) {
		return this.response.getHeader( name );
	}

	@Override
	public Map<String, String[]> getResponseHeaderMap() {
		Map<String, String[]> map = new HashMap<>();
		for ( String name : this.response.getHeaderNames() ) {
			map.put( name, this.response.getHeaders( name ).toArray( new String[ 0 ] ) );
		}
		return map;
	}

	@Override
	public PrintWriter getResponseWriter() {
		try {
			return this.response.getWriter();
		} catch ( IOException e ) {
			throw new RuntimeException( "Failed to get response writer", e );
		}
	}

	@Override
	public void sendResponseBinary( byte[] data ) {
		try {
			this.response.getOutputStream().write( data );
			this.response.getOutputStream().flush();
		} catch ( IOException e ) {
			throw new RuntimeException( "Failed to send binary response", e );
		}
	}

	@Override
	public void sendResponseFile( File file ) {
		try {
			this.response.getOutputStream().write( java.nio.file.Files.readAllBytes( file.toPath() ) );
			this.response.getOutputStream().flush();
		} catch ( IOException e ) {
			throw new RuntimeException( "Failed to send file response: " + file.getAbsolutePath(), e );
		}
	}

	@Override
	public void flushResponseBuffer() {
		try {
			this.response.flushBuffer();
		} catch ( IOException e ) {
			throw new RuntimeException( "Failed to flush response buffer", e );
		}
	}

	@Override
	public void resetResponseBuffer() {
		this.response.resetBuffer();
	}

	@Override
	public void reset() {
		this.response.reset();
	}

	// -----------------------------------------------------------------------
	// Type bridging helpers — the only non-trivial code in this class
	// -----------------------------------------------------------------------

	/**
	 * Convert an incoming Jakarta {@link Cookie} to a BoxLang {@link BoxCookie}.
	 * Data copied field-by-field; no logic added.
	 *
	 * @param c the Jakarta cookie
	 *
	 * @return an equivalent BoxCookie
	 */
	private BoxCookie toBoxCookie( Cookie c ) {
		BoxCookie bc = new BoxCookie( c.getName(), c.getValue(), false );
		bc.setPath( c.getPath() );
		bc.setDomain( c.getDomain() );
		bc.setMaxAge( c.getMaxAge() );
		bc.setSecure( c.getSecure() );
		bc.setHttpOnly( c.isHttpOnly() );
		return bc;
	}

	/**
	 * Convert a BoxLang {@link BoxCookie} to a Jakarta {@link Cookie}.
	 * Data copied field-by-field; no logic added.
	 *
	 * @param bc the BoxCookie
	 *
	 * @return an equivalent Jakarta Cookie
	 */
	private Cookie toServletCookie( BoxCookie bc ) {
		Cookie c = new Cookie( bc.getName(), bc.getEncodedValue() );
		if ( bc.getPath() != null )
			c.setPath( bc.getPath() );
		if ( bc.getDomain() != null )
			c.setDomain( bc.getDomain() );
		if ( bc.getMaxAge() != null )
			c.setMaxAge( bc.getMaxAge() );
		c.setSecure( bc.isSecure() );
		c.setHttpOnly( bc.isHttpOnly() );
		return c;
	}

}
