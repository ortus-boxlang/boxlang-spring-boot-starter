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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import jakarta.servlet.http.HttpServletRequest;
import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.web.springboot.view.BoxLangViewResolver;

/**
 * Spring Boot auto-configuration for BoxLang templating support.
 *
 * <h2>What this does</h2>
 * <ul>
 * <li>Initialises the BoxLang {@link BoxRuntime} when the Spring application
 * context starts (via {@link SmartLifecycle}).</li>
 * <li>Shuts the runtime down gracefully when the context stops.</li>
 * <li>Registers a {@link BoxLangViewResolver} bean using the properties bound
 * from {@code application.properties} with the {@code boxlang.*} prefix (see
 * {@link BoxLangProperties}).</li>
 * </ul>
 *
 * <h2>Configuration resolution</h2>
 * The BoxLang runtime is initialised with a {@code boxlang.json} found by the
 * following priority chain:
 * <ol>
 * <li>{@code boxlang.config-path} property (explicit override)</li>
 * <li>{@code classpath:/boxlang.json} auto-detect</li>
 * <li>{@code null} — BoxLang uses built-in defaults</li>
 * </ol>
 */
@AutoConfiguration( after = WebMvcAutoConfiguration.class )
@ConditionalOnWebApplication( type = ConditionalOnWebApplication.Type.SERVLET )
@ConditionalOnClass( HttpServletRequest.class )
@EnableConfigurationProperties( BoxLangProperties.class )
public class BoxLangAutoConfiguration implements SmartLifecycle, ResourceLoaderAware {

	private static final Logger		logger	= LoggerFactory.getLogger( BoxLangAutoConfiguration.class );

	private final BoxLangProperties	properties;
	private ResourceLoader			resourceLoader;
	private volatile boolean		running	= false;

	// -----------------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------------

	/**
	 * Creates the auto-configuration wired with BoxLang properties.
	 *
	 * @param properties the bound {@code boxlang.*} configuration properties
	 */
	public BoxLangAutoConfiguration( BoxLangProperties properties ) {
		this.properties = properties;
	}

	// -----------------------------------------------------------------------
	// ResourceLoaderAware
	// -----------------------------------------------------------------------

	@Override
	public void setResourceLoader( ResourceLoader resourceLoader ) {
		this.resourceLoader = resourceLoader;
	}

	// -----------------------------------------------------------------------
	// SmartLifecycle — BoxRuntime initialisation / shutdown
	// -----------------------------------------------------------------------

	@Override
	public void start() {
		String configPath = resolveConfigPath();
		logger.info( "BoxLang: initialising runtime (config='{}')", configPath != null ? configPath : "<defaults>" );
		BoxRuntime.getInstance( false, configPath );
		this.running = true;
		logger.info( "BoxLang: runtime started" );
	}

	@Override
	public void stop() {
		if ( this.running ) {
			logger.info( "BoxLang: shutting down runtime" );
			try {
				BoxRuntime.getInstance().shutdown( false );
			} catch ( Exception e ) {
				logger.warn( "BoxLang: error during runtime shutdown", e );
			} finally {
				this.running = false;
			}
		}
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	/**
	 * Run very early so the BoxLang runtime is available before any request
	 * arrives.
	 */
	@Override
	public int getPhase() {
		return Integer.MIN_VALUE + 100;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	// -----------------------------------------------------------------------
	// Beans
	// -----------------------------------------------------------------------

	/**
	 * Register the {@link BoxLangViewResolver} using values from
	 * {@link BoxLangProperties}.
	 *
	 * @return a fully configured {@link BoxLangViewResolver}
	 */
	@Bean
	@ConditionalOnMissingBean
	public BoxLangViewResolver boxLangViewResolver() {
		BoxLangViewResolver resolver = new BoxLangViewResolver();
		resolver.setPrefix( this.properties.getPrefix() );
		resolver.setSuffix( this.properties.getSuffix() );
		resolver.setOrder( this.properties.getViewResolverOrder() );
		resolver.setWebRoot( this.properties.getWebRoot() );
		return resolver;
	}

	// -----------------------------------------------------------------------
	// Helpers
	// -----------------------------------------------------------------------

	/**
	 * Determine the BoxLang config file path to use.
	 *
	 * <ol>
	 * <li>Explicit {@code boxlang.config-path} property</li>
	 * <li>Auto-detected {@code classpath:/boxlang.json}</li>
	 * <li>{@code null} (BoxLang built-in defaults)</li>
	 * </ol>
	 *
	 * @return filesystem or classpath path string, or {@code null}
	 */
	private String resolveConfigPath() {
		// 1) Explicit override
		if ( this.properties.getConfigPath() != null && !this.properties.getConfigPath().isBlank() ) {
			return this.properties.getConfigPath();
		}

		// 2) Auto-detect classpath:/boxlang.json
		try {
			Resource autoDetect = this.resourceLoader.getResource( "classpath:/boxlang.json" );
			if ( autoDetect.exists() ) {
				// BoxRuntime.loadConfiguration() calls new File(configPath), so we need a
				// plain filesystem path — NOT a file: URL string.
				try {
					// Works when the classpath resource is an exploded file on disk (the normal dev/test case).
					String path = autoDetect.getFile().getAbsolutePath();
					logger.debug( "BoxLang: auto-detected config at '{}'", path );
					return path;
				} catch ( IOException jarEx ) {
					// Resource is inside a JAR — copy to a temp file so BoxRuntime can read it.
					try ( InputStream in = autoDetect.getInputStream() ) {
						Path tmp = Files.createTempFile( "boxlang-", ".json" );
						tmp.toFile().deleteOnExit();
						Files.copy( in, tmp, StandardCopyOption.REPLACE_EXISTING );
						logger.debug( "BoxLang: copied packaged boxlang.json to temp file '{}'", tmp );
						return tmp.toAbsolutePath().toString();
					}
				}
			}
		} catch ( Exception e ) {
			logger.debug( "BoxLang: could not probe classpath for boxlang.json", e );
		}

		// 3) Defaults
		return null;
	}

}
