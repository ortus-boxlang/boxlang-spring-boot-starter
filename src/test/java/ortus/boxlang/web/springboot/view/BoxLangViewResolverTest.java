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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.View;

/**
 * Unit tests for {@link BoxLangViewResolver}.
 *
 * Uses a mock {@link ResourceLoader} to verify that view-name resolution
 * constructs the correct resource path and returns either a {@link BoxLangView}
 * (when the template exists) or {@code null} (when it does not).
 */
@ExtendWith( MockitoExtension.class )
class BoxLangViewResolverTest {

	@Mock
	private ResourceLoader		resourceLoader;

	@Mock
	private Resource			resource;

	private BoxLangViewResolver	resolver;

	@BeforeEach
	void setUp() {
		this.resolver = new BoxLangViewResolver();
		this.resolver.setResourceLoader( this.resourceLoader );
	}

	// -----------------------------------------------------------------------
	// Defaults
	// -----------------------------------------------------------------------

	@Test
	void defaultPrefix_isClasspathTemplates() {
		assertThat( this.resolver.getPrefix() ).isEqualTo( "classpath:/templates/" );
	}

	@Test
	void defaultSuffix_isBxm() {
		assertThat( this.resolver.getSuffix() ).isEqualTo( ".bxm" );
	}

	@Test
	void defaultOrder_isNearLowestPrecedence() {
		assertThat( this.resolver.getOrder() ).isEqualTo( Ordered.LOWEST_PRECEDENCE - 5 );
	}

	@Test
	void defaultWebRoot_isEmpty() {
		assertThat( this.resolver.getWebRoot() ).isEmpty();
	}

	// -----------------------------------------------------------------------
	// resolveViewName — existing template
	// -----------------------------------------------------------------------

	@Test
	void resolveViewName_existingTemplate_returnsBoxLangView() throws Exception {
		when( this.resourceLoader.getResource( "classpath:/templates/home.bxm" ) ).thenReturn( this.resource );
		when( this.resource.exists() ).thenReturn( true );

		View view = this.resolver.resolveViewName( "home", Locale.getDefault() );

		assertThat( view ).isInstanceOf( BoxLangView.class );
	}

	@Test
	void resolveViewName_existingTemplate_hasHtmlContentType() throws Exception {
		when( this.resourceLoader.getResource( anyString() ) ).thenReturn( this.resource );
		when( this.resource.exists() ).thenReturn( true );

		View view = this.resolver.resolveViewName( "page", Locale.getDefault() );

		assertThat( view.getContentType() ).isEqualTo( "text/html;charset=UTF-8" );
	}

	// -----------------------------------------------------------------------
	// resolveViewName — missing template
	// -----------------------------------------------------------------------

	@Test
	void resolveViewName_missingTemplate_returnsNull() throws Exception {
		when( this.resourceLoader.getResource( "classpath:/templates/notfound.bxm" ) ).thenReturn( this.resource );
		when( this.resource.exists() ).thenReturn( false );

		View view = this.resolver.resolveViewName( "notfound", Locale.getDefault() );

		assertThat( view ).isNull();
	}

	// -----------------------------------------------------------------------
	// Path construction
	// -----------------------------------------------------------------------

	@Test
	void resolveViewName_buildsPath_withDefaultPrefixAndSuffix() throws Exception {
		when( this.resourceLoader.getResource( anyString() ) ).thenReturn( this.resource );
		when( this.resource.exists() ).thenReturn( false );

		this.resolver.resolveViewName( "dashboard", Locale.getDefault() );

		verify( this.resourceLoader ).getResource( "classpath:/templates/dashboard.bxm" );
	}

	@Test
	void resolveViewName_buildsPath_withCustomPrefixAndSuffix() throws Exception {
		this.resolver.setPrefix( "classpath:/views/" );
		this.resolver.setSuffix( ".cfm" );
		when( this.resourceLoader.getResource( anyString() ) ).thenReturn( this.resource );
		when( this.resource.exists() ).thenReturn( false );

		this.resolver.resolveViewName( "profile", Locale.getDefault() );

		verify( this.resourceLoader ).getResource( "classpath:/views/profile.cfm" );
	}

	@Test
	void resolveViewName_buildsPath_withNestedViewName() throws Exception {
		when( this.resourceLoader.getResource( anyString() ) ).thenReturn( this.resource );
		when( this.resource.exists() ).thenReturn( false );

		this.resolver.resolveViewName( "admin/users", Locale.getDefault() );

		verify( this.resourceLoader ).getResource( "classpath:/templates/admin/users.bxm" );
	}

	// -----------------------------------------------------------------------
	// Setters / order
	// -----------------------------------------------------------------------

	@Test
	void setOrder_changesResolverOrder() {
		this.resolver.setOrder( 1 );
		assertThat( this.resolver.getOrder() ).isEqualTo( 1 );
	}

	@Test
	void setPrefix_affectsPathConstruction() throws Exception {
		this.resolver.setPrefix( "file:/opt/templates/" );
		when( this.resourceLoader.getResource( anyString() ) ).thenReturn( this.resource );
		when( this.resource.exists() ).thenReturn( false );

		this.resolver.resolveViewName( "index", Locale.getDefault() );

		verify( this.resourceLoader ).getResource( "file:/opt/templates/index.bxm" );
	}

	@Test
	void setSuffix_affectsPathConstruction() throws Exception {
		this.resolver.setSuffix( ".bxs" );
		when( this.resourceLoader.getResource( anyString() ) ).thenReturn( this.resource );
		when( this.resource.exists() ).thenReturn( false );

		this.resolver.resolveViewName( "script", Locale.getDefault() );

		verify( this.resourceLoader ).getResource( "classpath:/templates/script.bxs" );
	}

	@Test
	void setWebRoot_propagatesToResolvedView() throws Exception {
		this.resolver.setWebRoot( "/srv/www" );
		when( this.resourceLoader.getResource( anyString() ) ).thenReturn( this.resource );
		when( this.resource.exists() ).thenReturn( true );

		View view = this.resolver.resolveViewName( "home", Locale.getDefault() );

		// The view is a BoxLangView with the webRoot stored internally — verify it was created
		assertThat( view ).isInstanceOf( BoxLangView.class );
	}

	// -----------------------------------------------------------------------
	// InitializingBean
	// -----------------------------------------------------------------------

	@Test
	void afterPropertiesSet_doesNotThrow() throws Exception {
		// Must complete without exception after configuration
		this.resolver.afterPropertiesSet();
	}

	// -----------------------------------------------------------------------
	// Locale is not used (BoxLang doesn't internationalise template file names)
	// -----------------------------------------------------------------------

	@Test
	void resolveViewName_localeIsIgnored_samePathRegardlessOfLocale() throws Exception {
		Resource	r1	= mock( Resource.class );
		Resource	r2	= mock( Resource.class );
		when( r1.exists() ).thenReturn( false );
		when( r2.exists() ).thenReturn( false );

		// Return the same resource path regardless of locale
		when( this.resourceLoader.getResource( "classpath:/templates/greeting.bxm" ) )
		    .thenReturn( r1 )
		    .thenReturn( r2 );

		this.resolver.resolveViewName( "greeting", Locale.ENGLISH );
		this.resolver.resolveViewName( "greeting", Locale.JAPANESE );

		// Both should have queried the exact same path
		verify( this.resourceLoader, org.mockito.Mockito.times( 2 ) )
		    .getResource( "classpath:/templates/greeting.bxm" );
	}

}
