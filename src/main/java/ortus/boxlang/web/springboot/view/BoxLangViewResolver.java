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

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * Spring MVC {@link ViewResolver} that maps view names to BoxLang {@code .bxm}
 * templates found on the classpath (or filesystem).
 *
 * <h2>Resolution algorithm</h2>
 *
 * <pre>
 *   resolvedPath = prefix + viewName + suffix
 *   e.g.  "classpath:/templates/" + "home" + ".bxm"
 *       → classpath:/templates/home.bxm
 * </pre>
 *
 * <p>
 * If the computed resource exists it is wrapped in a {@link BoxLangView} and
 * returned. If it does not exist this method returns {@code null}, allowing the
 * Spring resolver chain to continue to the next resolver.
 * </p>
 *
 * <p>
 * Registered automatically by {@link ortus.boxlang.web.springboot.BoxLangAutoConfiguration}.
 * Override any property via {@code application.properties} using the {@code boxlang.*}
 * prefix (see {@link ortus.boxlang.web.springboot.BoxLangProperties}).
 * </p>
 */
public class BoxLangViewResolver implements ViewResolver, Ordered, InitializingBean, ResourceLoaderAware {

	private static final Logger	logger	= LoggerFactory.getLogger( BoxLangViewResolver.class );

	// -----------------------------------------------------------------------
	// Configuration properties
	// -----------------------------------------------------------------------

	/** Resource prefix — default {@code classpath:/templates/} */
	private String				prefix	= "classpath:/templates/";

	/** Resource suffix / file extension — default {@code .bxm} */
	private String				suffix	= ".bxm";

	/** Position in the resolver chain — lower = higher priority. */
	private int					order	= Ordered.LOWEST_PRECEDENCE - 5;

	/**
	 * BoxLang web root directory.
	 * Blank means "derive from working directory" (handled inside {@link BoxLangView}).
	 */
	private String				webRoot	= "";

	/** Injected by Spring — used to resolve classpath and file resources. */
	private ResourceLoader		resourceLoader;

	// -----------------------------------------------------------------------
	// ResourceLoaderAware
	// -----------------------------------------------------------------------

	@Override
	public void setResourceLoader( ResourceLoader resourceLoader ) {
		this.resourceLoader = resourceLoader;
	}

	// -----------------------------------------------------------------------
	// InitializingBean
	// -----------------------------------------------------------------------

	@Override
	public void afterPropertiesSet() {
		logger.info( "BoxLangViewResolver initialised — prefix='{}' suffix='{}'", this.prefix, this.suffix );
	}

	// -----------------------------------------------------------------------
	// ViewResolver
	// -----------------------------------------------------------------------

	/**
	 * Resolve a logical view name to a {@link BoxLangView}.
	 *
	 * @param viewName the logical view name returned by a Spring controller
	 * @param locale   the current request locale (unused by BoxLang templates)
	 *
	 * @return a {@link BoxLangView} if the template resource exists, {@code null} otherwise
	 */
	@Override
	public View resolveViewName( String viewName, Locale locale ) throws Exception {
		String		path		= this.prefix + viewName + this.suffix;
		Resource	resource	= this.resourceLoader.getResource( path );

		if ( !resource.exists() ) {
			logger.debug( "BoxLangViewResolver: no template found at '{}', skipping", path );
			return null;
		}

		logger.debug( "BoxLangViewResolver: resolved '{}' → '{}'", viewName, path );
		return new BoxLangView( resource, this.webRoot );
	}

	// -----------------------------------------------------------------------
	// Ordered
	// -----------------------------------------------------------------------

	@Override
	public int getOrder() {
		return this.order;
	}

	// -----------------------------------------------------------------------
	// Setters
	// -----------------------------------------------------------------------

	public void setPrefix( String prefix ) {
		this.prefix = prefix;
	}

	public void setSuffix( String suffix ) {
		this.suffix = suffix;
	}

	public void setOrder( int order ) {
		this.order = order;
	}

	public void setWebRoot( String webRoot ) {
		this.webRoot = webRoot;
	}

	// -----------------------------------------------------------------------
	// Getters (useful for testing / diagnostics)
	// -----------------------------------------------------------------------

	public String getPrefix() {
		return this.prefix;
	}

	public String getSuffix() {
		return this.suffix;
	}

	public String getWebRoot() {
		return this.webRoot;
	}

}
