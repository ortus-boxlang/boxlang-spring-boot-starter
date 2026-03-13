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

/**
 * Unit tests for {@link BoxLangProperties}.
 * Verifies default values and setter/getter round-trips.
 */
class BoxLangPropertiesTest {

	// -----------------------------------------------------------------------
	// Defaults
	// -----------------------------------------------------------------------

	@Test
	void defaults_configPath_isNull() {
		BoxLangProperties props = new BoxLangProperties();
		// Null triggers auto-detection of classpath:/boxlang.json at runtime
		assertThat( props.getConfigPath() ).isNull();
	}

	@Test
	void defaults_prefix_isClasspathTemplates() {
		BoxLangProperties props = new BoxLangProperties();
		assertThat( props.getPrefix() ).isEqualTo( "classpath:/templates/" );
	}

	@Test
	void defaults_suffix_isBxm() {
		BoxLangProperties props = new BoxLangProperties();
		assertThat( props.getSuffix() ).isEqualTo( ".bxm" );
	}

	@Test
	void defaults_viewResolverOrder_isNearLowestPrecedence() {
		BoxLangProperties props = new BoxLangProperties();
		assertThat( props.getViewResolverOrder() ).isEqualTo( Integer.MAX_VALUE - 5 );
	}

	@Test
	void defaults_webRoot_isEmpty() {
		BoxLangProperties props = new BoxLangProperties();
		assertThat( props.getWebRoot() ).isEmpty();
	}

	// -----------------------------------------------------------------------
	// Setter / getter round-trips
	// -----------------------------------------------------------------------

	@Test
	void setConfigPath_roundTrip() {
		BoxLangProperties props = new BoxLangProperties();
		props.setConfigPath( "/etc/boxlang/boxlang.json" );
		assertThat( props.getConfigPath() ).isEqualTo( "/etc/boxlang/boxlang.json" );
	}

	@Test
	void setPrefix_roundTrip() {
		BoxLangProperties props = new BoxLangProperties();
		props.setPrefix( "classpath:/views/" );
		assertThat( props.getPrefix() ).isEqualTo( "classpath:/views/" );
	}

	@Test
	void setSuffix_roundTrip() {
		BoxLangProperties props = new BoxLangProperties();
		props.setSuffix( ".cfm" );
		assertThat( props.getSuffix() ).isEqualTo( ".cfm" );
	}

	@Test
	void setViewResolverOrder_roundTrip() {
		BoxLangProperties props = new BoxLangProperties();
		props.setViewResolverOrder( 42 );
		assertThat( props.getViewResolverOrder() ).isEqualTo( 42 );
	}

	@Test
	void setWebRoot_roundTrip() {
		BoxLangProperties props = new BoxLangProperties();
		props.setWebRoot( "/var/www/myapp" );
		assertThat( props.getWebRoot() ).isEqualTo( "/var/www/myapp" );
	}

	@Test
	void setConfigPath_toNull_clearsValue() {
		BoxLangProperties props = new BoxLangProperties();
		props.setConfigPath( "/some/path" );
		props.setConfigPath( null );
		assertThat( props.getConfigPath() ).isNull();
	}

}
