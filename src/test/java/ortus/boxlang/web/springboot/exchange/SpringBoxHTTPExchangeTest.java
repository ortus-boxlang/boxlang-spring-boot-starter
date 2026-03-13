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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ortus.boxlang.web.context.WebRequestBoxContext;
import ortus.boxlang.web.exchange.BoxCookie;

/**
 * Unit tests for {@link SpringBoxHTTPExchange}.
 *
 * Every method is a pure delegation to the underlying Jakarta request/response pair.
 * Tests verify that delegation happens correctly and that the Cookie type-bridging
 * (Jakarta ↔ BoxCookie) preserves all cookie fields.
 */
@ExtendWith( MockitoExtension.class )
class SpringBoxHTTPExchangeTest {

	@Mock
	private HttpServletRequest		request;

	@Mock
	private HttpServletResponse		response;

	private SpringBoxHTTPExchange	exchange;

	@BeforeEach
	void setUp() {
		this.exchange = new SpringBoxHTTPExchange( this.request, this.response );
	}

	// -----------------------------------------------------------------------
	// Context wiring
	// -----------------------------------------------------------------------

	@Test
	void webContext_isNullByDefault() {
		assertThat( this.exchange.getWebContext() ).isNull();
	}

	@Test
	void webContext_setGet_roundTrip() {
		WebRequestBoxContext ctx = mock( WebRequestBoxContext.class );
		this.exchange.setWebContext( ctx );
		assertThat( this.exchange.getWebContext() ).isSameInstanceAs( ctx );
	}

	// -----------------------------------------------------------------------
	// Cookie conversion — getRequestCookies
	// -----------------------------------------------------------------------

	@Test
	void getRequestCookies_whenNull_returnsEmptyArray() {
		when( this.request.getCookies() ).thenReturn( null );
		assertThat( this.exchange.getRequestCookies() ).isEmpty();
	}

	@Test
	void getRequestCookies_whenEmpty_returnsEmptyArray() {
		when( this.request.getCookies() ).thenReturn( new Cookie[ 0 ] );
		assertThat( this.exchange.getRequestCookies() ).isEmpty();
	}

	@Test
	void getRequestCookies_convertsAllFields() {
		Cookie c = new Cookie( "session", "abc123" );
		c.setPath( "/" );
		c.setDomain( "example.com" );
		c.setMaxAge( 3600 );
		c.setSecure( true );
		c.setHttpOnly( true );
		when( this.request.getCookies() ).thenReturn( new Cookie[] { c } );

		BoxCookie[] cookies = this.exchange.getRequestCookies();

		assertThat( cookies ).hasLength( 1 );
		assertThat( cookies[ 0 ].getName() ).isEqualTo( "session" );
		assertThat( cookies[ 0 ].getValue() ).isEqualTo( "abc123" );
		assertThat( cookies[ 0 ].getPath() ).isEqualTo( "/" );
		assertThat( cookies[ 0 ].getDomain() ).isEqualTo( "example.com" );
		assertThat( cookies[ 0 ].getMaxAge() ).isEqualTo( 3600 );
		assertThat( cookies[ 0 ].isSecure() ).isEqualTo( true );
		assertThat( cookies[ 0 ].isHttpOnly() ).isEqualTo( true );
	}

	@Test
	void getRequestCookies_returnsAllCookies() {
		Cookie[] jakartaCookies = {
		    new Cookie( "a", "1" ),
		    new Cookie( "b", "2" ),
		    new Cookie( "c", "3" )
		};
		when( this.request.getCookies() ).thenReturn( jakartaCookies );

		assertThat( this.exchange.getRequestCookies() ).hasLength( 3 );
	}

	// -----------------------------------------------------------------------
	// Cookie lookup — getRequestCookie
	// -----------------------------------------------------------------------

	@Test
	void getRequestCookie_noCookies_returnsNull() {
		when( this.request.getCookies() ).thenReturn( null );
		assertThat( this.exchange.getRequestCookie( "any" ) ).isNull();
	}

	@Test
	void getRequestCookie_found_returnsBoxCookie() {
		when( this.request.getCookies() ).thenReturn( new Cookie[] { new Cookie( "token", "xyz" ) } );

		BoxCookie result = this.exchange.getRequestCookie( "token" );

		assertThat( result ).isNotNull();
		assertThat( result.getValue() ).isEqualTo( "xyz" );
	}

	@Test
	void getRequestCookie_caseInsensitiveMatch() {
		when( this.request.getCookies() ).thenReturn( new Cookie[] { new Cookie( "TOKEN", "val" ) } );
		assertThat( this.exchange.getRequestCookie( "token" ) ).isNotNull();
	}

	@Test
	void getRequestCookie_notFound_returnsNull() {
		when( this.request.getCookies() ).thenReturn( new Cookie[] { new Cookie( "other", "v" ) } );
		assertThat( this.exchange.getRequestCookie( "missing" ) ).isNull();
	}

	// -----------------------------------------------------------------------
	// Request header map
	// -----------------------------------------------------------------------

	@Test
	void getRequestHeaderMap_delegatesToRequest() {
		when( this.request.getHeaderNames() ).thenReturn( Collections.enumeration( List.of( "Accept", "X-App" ) ) );
		when( this.request.getHeaders( "Accept" ) ).thenReturn( Collections.enumeration( List.of( "application/json" ) ) );
		when( this.request.getHeaders( "X-App" ) ).thenReturn( Collections.enumeration( List.of( "test", "demo" ) ) );

		Map<String, String[]> map = this.exchange.getRequestHeaderMap();

		assertThat( map ).containsKey( "Accept" );
		assertThat( map.get( "Accept" ) ).asList().containsExactly( "application/json" );
		assertThat( map ).containsKey( "X-App" );
		assertThat( map.get( "X-App" ) ).asList().containsExactly( "test", "demo" );
	}

	@Test
	void getRequestHeaderMap_nullHeaderNames_returnsEmptyMap() {
		when( this.request.getHeaderNames() ).thenReturn( null );
		assertThat( this.exchange.getRequestHeaderMap() ).isEmpty();
	}

	@Test
	void getRequestHeader_delegatesToRequest() {
		when( this.request.getHeader( "Authorization" ) ).thenReturn( "Bearer token123" );
		assertThat( this.exchange.getRequestHeader( "Authorization" ) ).isEqualTo( "Bearer token123" );
	}

	// -----------------------------------------------------------------------
	// Request method / URI / URL / query string
	// -----------------------------------------------------------------------

	@Test
	void getRequestMethod_delegatesToRequest() {
		when( this.request.getMethod() ).thenReturn( "POST" );
		assertThat( this.exchange.getRequestMethod() ).isEqualTo( "POST" );
	}

	@Test
	void getRequestURI_delegatesToRequest() {
		when( this.request.getRequestURI() ).thenReturn( "/api/users" );
		assertThat( this.exchange.getRequestURI() ).isEqualTo( "/api/users" );
	}

	@Test
	void getRequestURL_delegatesToRequest() {
		StringBuffer url = new StringBuffer( "http://example.com/api/users" );
		when( this.request.getRequestURL() ).thenReturn( url );
		assertThat( this.exchange.getRequestURL() ).isSameInstanceAs( url );
	}

	@Test
	void getRequestQueryString_delegatesToRequest() {
		when( this.request.getQueryString() ).thenReturn( "page=1&size=20" );
		assertThat( this.exchange.getRequestQueryString() ).isEqualTo( "page=1&size=20" );
	}

	// -----------------------------------------------------------------------
	// getRequestURLMap (custom query-string parser)
	// -----------------------------------------------------------------------

	@Test
	void getRequestURLMap_nullQueryString_returnsEmptyMap() {
		when( this.request.getQueryString() ).thenReturn( null );
		assertThat( this.exchange.getRequestURLMap() ).isEmpty();
	}

	@Test
	void getRequestURLMap_emptyQueryString_returnsEmptyMap() {
		when( this.request.getQueryString() ).thenReturn( "" );
		assertThat( this.exchange.getRequestURLMap() ).isEmpty();
	}

	@Test
	void getRequestURLMap_parsesSimplePairs() {
		when( this.request.getQueryString() ).thenReturn( "name=Alice&role=admin" );

		Map<String, String[]> map = this.exchange.getRequestURLMap();

		assertThat( map.get( "name" ) ).asList().containsExactly( "Alice" );
		assertThat( map.get( "role" ) ).asList().containsExactly( "admin" );
	}

	@Test
	void getRequestURLMap_decodesUrlEncoding() {
		when( this.request.getQueryString() ).thenReturn( "q=Hello%20World" );

		Map<String, String[]> map = this.exchange.getRequestURLMap();

		assertThat( map.get( "q" ) ).asList().containsExactly( "Hello World" );
	}

	@Test
	void getRequestURLMap_keyWithNoValue_storesEmptyString() {
		when( this.request.getQueryString() ).thenReturn( "flag" );

		Map<String, String[]> map = this.exchange.getRequestURLMap();

		assertThat( map ).containsKey( "flag" );
		assertThat( map.get( "flag" ) ).asList().containsExactly( "" );
	}

	// -----------------------------------------------------------------------
	// Server / network info
	// -----------------------------------------------------------------------

	@Test
	void getRequestServerName_delegatesToRequest() {
		when( this.request.getServerName() ).thenReturn( "api.example.com" );
		assertThat( this.exchange.getRequestServerName() ).isEqualTo( "api.example.com" );
	}

	@Test
	void getRequestServerPort_delegatesToRequest() {
		when( this.request.getServerPort() ).thenReturn( 8443 );
		assertThat( this.exchange.getRequestServerPort() ).isEqualTo( 8443 );
	}

	@Test
	void getRequestScheme_delegatesToRequest() {
		when( this.request.getScheme() ).thenReturn( "https" );
		assertThat( this.exchange.getRequestScheme() ).isEqualTo( "https" );
	}

	@Test
	void isRequestSecure_delegatesToRequest() {
		when( this.request.isSecure() ).thenReturn( true );
		assertThat( this.exchange.isRequestSecure() ).isTrue();
	}

	@Test
	void getRequestRemoteAddr_delegatesToRequest() {
		when( this.request.getRemoteAddr() ).thenReturn( "192.168.1.10" );
		assertThat( this.exchange.getRequestRemoteAddr() ).isEqualTo( "192.168.1.10" );
	}

	@Test
	void getRequestRemoteHost_delegatesToRequest() {
		when( this.request.getRemoteHost() ).thenReturn( "client.internal" );
		assertThat( this.exchange.getRequestRemoteHost() ).isEqualTo( "client.internal" );
	}

	@Test
	void getRequestLocale_delegatesToRequest() {
		when( this.request.getLocale() ).thenReturn( Locale.FRENCH );
		assertThat( this.exchange.getRequestLocale() ).isEqualTo( Locale.FRENCH );
	}

	@Test
	void getRequestProtocol_delegatesToRequest() {
		when( this.request.getProtocol() ).thenReturn( "HTTP/1.1" );
		assertThat( this.exchange.getRequestProtocol() ).isEqualTo( "HTTP/1.1" );
	}

	// -----------------------------------------------------------------------
	// Request attributes
	// -----------------------------------------------------------------------

	@Test
	void setRequestAttribute_delegatesToRequest() {
		this.exchange.setRequestAttribute( "myKey", "myValue" );
		verify( this.request ).setAttribute( "myKey", "myValue" );
	}

	@Test
	void getRequestAttribute_delegatesToRequest() {
		when( this.request.getAttribute( "myKey" ) ).thenReturn( "stored" );
		assertThat( this.exchange.getRequestAttribute( "myKey" ) ).isEqualTo( "stored" );
	}

	@Test
	void removeRequestAttribute_delegatesToRequest() {
		this.exchange.removeRequestAttribute( "myKey" );
		verify( this.request ).removeAttribute( "myKey" );
	}

	// -----------------------------------------------------------------------
	// Form / upload
	// -----------------------------------------------------------------------

	@Test
	void getRequestFormMap_delegatesToRequest() {
		Map<String, String[]> params = Map.of( "email", new String[] { "user@example.com" } );
		when( this.request.getParameterMap() ).thenReturn( params );
		assertThat( this.exchange.getRequestFormMap() ).isSameInstanceAs( params );
	}

	@Test
	void getUploadData_returnsEmptyArray() {
		assertThat( this.exchange.getUploadData() ).isEmpty();
	}

	// -----------------------------------------------------------------------
	// Response delegation
	// -----------------------------------------------------------------------

	@Test
	void setResponseHeader_delegatesToResponse() {
		this.exchange.setResponseHeader( "Cache-Control", "no-cache" );
		verify( this.response ).setHeader( "Cache-Control", "no-cache" );
	}

	@Test
	void addResponseHeader_delegatesToResponse() {
		this.exchange.addResponseHeader( "X-Frame-Options", "DENY" );
		verify( this.response ).addHeader( "X-Frame-Options", "DENY" );
	}

	@Test
	void setResponseStatus_delegatesToResponse() {
		this.exchange.setResponseStatus( 201 );
		verify( this.response ).setStatus( 201 );
	}

	@Test
	void getResponseStatus_delegatesToResponse() {
		when( this.response.getStatus() ).thenReturn( 404 );
		assertThat( this.exchange.getResponseStatus() ).isEqualTo( 404 );
	}

	@Test
	void isResponseStarted_delegatesToResponse() {
		when( this.response.isCommitted() ).thenReturn( true );
		assertThat( this.exchange.isResponseStarted() ).isTrue();
	}

	@Test
	void getResponseWriter_delegatesToResponse() throws Exception {
		PrintWriter writer = new PrintWriter( new StringWriter() );
		when( this.response.getWriter() ).thenReturn( writer );
		assertThat( this.exchange.getResponseWriter() ).isSameInstanceAs( writer );
	}

	@Test
	void getResponseHeader_delegatesToResponse() {
		when( this.response.getHeader( "X-Custom" ) ).thenReturn( "headerVal" );
		assertThat( this.exchange.getResponseHeader( "X-Custom" ) ).isEqualTo( "headerVal" );
	}

	// -----------------------------------------------------------------------
	// Cookie type-bridging — addResponseCookie
	// -----------------------------------------------------------------------

	@Test
	void addResponseCookie_bridgesBoxCookieToJakarta() {
		BoxCookie bc = new BoxCookie( "pref", "dark", false );
		bc.setPath( "/app" );
		bc.setDomain( "example.com" );
		bc.setMaxAge( 86400 );
		bc.setSecure( true );
		bc.setHttpOnly( true );

		this.exchange.addResponseCookie( bc );

		verify( this.response ).addCookie( argThat( c -> c.getName().equals( "pref" )
		    && c.getValue().equals( "dark" )
		    && "/app".equals( c.getPath() )
		    && "example.com".equals( c.getDomain() )
		    && c.getMaxAge() == 86400
		    && c.getSecure()
		    && c.isHttpOnly() ) );
	}

	// -----------------------------------------------------------------------
	// Forward
	// -----------------------------------------------------------------------

	@Test
	void forward_callsRequestDispatcher() throws Exception {
		RequestDispatcher dispatcher = mock( RequestDispatcher.class );
		when( this.request.getRequestDispatcher( "/other/path" ) ).thenReturn( dispatcher );

		this.exchange.forward( "/other/path" );

		verify( dispatcher ).forward( this.request, this.response );
	}

}
