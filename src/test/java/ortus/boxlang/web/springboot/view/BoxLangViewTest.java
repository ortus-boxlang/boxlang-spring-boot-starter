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
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

/**
 * Unit tests for {@link BoxLangView}.
 *
 * {@code BoxLangView.render()} requires a live BoxLang runtime and is covered by
 * the integration tests in the {@code test-app} sub-project. Here we test the
 * parts that are independently verifiable without starting the runtime:
 * content-type contract and constructor invariants (null/blank webRoot fallback).
 */
class BoxLangViewTest {

	// -----------------------------------------------------------------------
	// Content type
	// -----------------------------------------------------------------------

	@Test
	void getContentType_returnsHtmlUtf8() {
		BoxLangView view = new BoxLangView( mock( Resource.class ), "/var/www" );
		assertThat( view.getContentType() ).isEqualTo( "text/html;charset=UTF-8" );
	}

	// -----------------------------------------------------------------------
	// Constructor — webRoot fallback
	// The webRoot field is private; we verify the constructor completes without
	// error and that the view's content type is still correctly reported.
	// -----------------------------------------------------------------------

	@Test
	void constructor_nullWebRoot_doesNotThrow() {
		BoxLangView view = new BoxLangView( mock( Resource.class ), null );
		assertThat( view.getContentType() ).isEqualTo( "text/html;charset=UTF-8" );
	}

	@Test
	void constructor_emptyWebRoot_doesNotThrow() {
		BoxLangView view = new BoxLangView( mock( Resource.class ), "" );
		assertThat( view.getContentType() ).isEqualTo( "text/html;charset=UTF-8" );
	}

	@Test
	void constructor_blankWebRoot_doesNotThrow() {
		BoxLangView view = new BoxLangView( mock( Resource.class ), "   " );
		assertThat( view.getContentType() ).isEqualTo( "text/html;charset=UTF-8" );
	}

	@Test
	void constructor_explicitWebRoot_doesNotThrow() {
		BoxLangView view = new BoxLangView( mock( Resource.class ), "/srv/www/myapp" );
		assertThat( view.getContentType() ).isEqualTo( "text/html;charset=UTF-8" );
	}

}
