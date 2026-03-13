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
package ortus.boxlang.web.springboot;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import ortus.boxlang.web.springboot.view.BoxLangViewResolver;

/**
 * Unit tests for {@link BoxLangAutoConfiguration}.
 *
 * These tests verify the configuration class in isolation — lifecycle state
 * transitions, phase ordering, and that the {@link BoxLangViewResolver} bean
 * is built correctly from the bound {@link BoxLangProperties}.
 *
 * Full lifecycle (BoxRuntime start/stop) and Spring conditional wiring
 * ({@code @ConditionalOnMissingBean}, {@code @ConditionalOnWebApplication})
 * are covered by the integration tests in the {@code test-app} sub-project.
 */
class BoxLangAutoConfigurationTest {

	/** Creates a configuration instance with an injected DefaultResourceLoader. */
	private BoxLangAutoConfiguration configWith( BoxLangProperties props ) {
		BoxLangAutoConfiguration config = new BoxLangAutoConfiguration( props );
		config.setResourceLoader( new DefaultResourceLoader() );
		return config;
	}

	// -----------------------------------------------------------------------
	// SmartLifecycle — initial state
	// -----------------------------------------------------------------------

	@Test
	void isRunning_isFalseBeforeStart() {
		BoxLangAutoConfiguration config = configWith( new BoxLangProperties() );
		assertThat( config.isRunning() ).isFalse();
	}

	@Test
	void isAutoStartup_isAlwaysTrue() {
		BoxLangAutoConfiguration config = configWith( new BoxLangProperties() );
		assertThat( config.isAutoStartup() ).isTrue();
	}

	@Test
	void getPhase_isEarlyInitPhase() {
		// Must run before most other SmartLifecycle beans so the BoxRuntime is ready
		BoxLangAutoConfiguration config = configWith( new BoxLangProperties() );
		assertThat( config.getPhase() ).isEqualTo( Integer.MIN_VALUE + 100 );
	}

	// -----------------------------------------------------------------------
	// BoxLangViewResolver bean creation
	// -----------------------------------------------------------------------

	@Test
	void boxLangViewResolver_isNotNull() {
		BoxLangAutoConfiguration config = configWith( new BoxLangProperties() );
		assertThat( config.boxLangViewResolver() ).isNotNull();
	}

	@Test
	void boxLangViewResolver_usesDefaultOrder() {
		BoxLangAutoConfiguration	config		= configWith( new BoxLangProperties() );
		BoxLangViewResolver			resolver	= config.boxLangViewResolver();
		assertThat( resolver.getOrder() ).isEqualTo( Integer.MAX_VALUE - 5 );
	}

	@Test
	void boxLangViewResolver_usesDefaultPrefix() {
		BoxLangAutoConfiguration	config		= configWith( new BoxLangProperties() );
		BoxLangViewResolver			resolver	= config.boxLangViewResolver();
		assertThat( resolver.getPrefix() ).isEqualTo( "classpath:/templates/" );
	}

	@Test
	void boxLangViewResolver_usesDefaultSuffix() {
		BoxLangAutoConfiguration	config		= configWith( new BoxLangProperties() );
		BoxLangViewResolver			resolver	= config.boxLangViewResolver();
		assertThat( resolver.getSuffix() ).isEqualTo( ".bxm" );
	}

	@Test
	void boxLangViewResolver_usesDefaultWebRoot() {
		BoxLangAutoConfiguration	config		= configWith( new BoxLangProperties() );
		BoxLangViewResolver			resolver	= config.boxLangViewResolver();
		assertThat( resolver.getWebRoot() ).isEmpty();
	}

	@Test
	void boxLangViewResolver_propagatesCustomPrefix() {
		BoxLangProperties props = new BoxLangProperties();
		props.setPrefix( "classpath:/myviews/" );
		BoxLangAutoConfiguration	config		= configWith( props );

		BoxLangViewResolver			resolver	= config.boxLangViewResolver();

		assertThat( resolver.getPrefix() ).isEqualTo( "classpath:/myviews/" );
	}

	@Test
	void boxLangViewResolver_propagatesCustomSuffix() {
		BoxLangProperties props = new BoxLangProperties();
		props.setSuffix( ".cfm" );
		BoxLangAutoConfiguration	config		= configWith( props );

		BoxLangViewResolver			resolver	= config.boxLangViewResolver();

		assertThat( resolver.getSuffix() ).isEqualTo( ".cfm" );
	}

	@Test
	void boxLangViewResolver_propagatesCustomOrder() {
		BoxLangProperties props = new BoxLangProperties();
		props.setViewResolverOrder( 5 );
		BoxLangAutoConfiguration	config		= configWith( props );

		BoxLangViewResolver			resolver	= config.boxLangViewResolver();

		assertThat( resolver.getOrder() ).isEqualTo( 5 );
	}

	@Test
	void boxLangViewResolver_propagatesCustomWebRoot() {
		BoxLangProperties props = new BoxLangProperties();
		props.setWebRoot( "/opt/myapp/www" );
		BoxLangAutoConfiguration	config		= configWith( props );

		BoxLangViewResolver			resolver	= config.boxLangViewResolver();

		assertThat( resolver.getWebRoot() ).isEqualTo( "/opt/myapp/www" );
	}

	@Test
	void boxLangViewResolver_allPropertiesSetTogether() {
		BoxLangProperties props = new BoxLangProperties();
		props.setPrefix( "file:/views/" );
		props.setSuffix( ".bxm" );
		props.setViewResolverOrder( 10 );
		props.setWebRoot( "/var/www" );
		BoxLangAutoConfiguration	config		= configWith( props );

		BoxLangViewResolver			resolver	= config.boxLangViewResolver();

		assertThat( resolver.getPrefix() ).isEqualTo( "file:/views/" );
		assertThat( resolver.getSuffix() ).isEqualTo( ".bxm" );
		assertThat( resolver.getOrder() ).isEqualTo( 10 );
		assertThat( resolver.getWebRoot() ).isEqualTo( "/var/www" );
	}

}
