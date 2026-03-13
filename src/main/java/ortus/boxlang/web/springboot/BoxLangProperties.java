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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the BoxLang Spring Boot integration.
 *
 * All properties are prefixed with {@code boxlang} in {@code application.properties}
 * or {@code application.yml}.
 *
 * <h2>Example application.properties</h2>
 *
 * <pre>
 * # Path to boxlang.json (optional — auto-detects classpath:/boxlang.json when absent)
 * boxlang.config-path=classpath:/boxlang.json
 *
 * # Template location prefix (default: classpath:/templates/)
 * boxlang.prefix=classpath:/templates/
 *
 * # Template file extension (default: .bxm)
 * boxlang.suffix=.bxm
 *
 * # View resolver order in the Spring resolver chain (default: Integer.MAX_VALUE - 5)
 * boxlang.view-resolver-order=2147483642
 *
 * # Optional explicit web root for BoxLang mappings
 * boxlang.web-root=
 *
 * # Enable BoxLang debug mode (default: false)
 * boxlang.debug-mode=false
 *
 * # Override the BoxLang runtime home directory (default: null — uses ~/.boxlang)
 * # boxlang.runtime-home=/path/to/boxlang-home
 * </pre>
 */
@ConfigurationProperties( prefix = "boxlang" )
public class BoxLangProperties {

	/**
	 * Explicit path to the {@code boxlang.json} configuration file.
	 *
	 * Accepts:
	 * <ul>
	 * <li>{@code classpath:/boxlang.json} — resource on the classpath</li>
	 * <li>{@code file:/etc/boxlang/boxlang.json} — absolute filesystem path</li>
	 * <li>A bare filesystem path such as {@code /etc/boxlang/boxlang.json}</li>
	 * </ul>
	 *
	 * When {@code null} (the default), the auto-configuration probes
	 * {@code classpath:/boxlang.json} and falls back to BoxLang's built-in defaults.
	 */
	private String	configPath			= null;

	/**
	 * Template resource prefix.
	 * Default: {@code classpath:/templates/}
	 */
	private String	prefix				= "classpath:/templates/";

	/**
	 * Template file name suffix (extension).
	 * Default: {@code .bxm}
	 */
	private String	suffix				= ".bxm";

	/**
	 * Order of the {@link ortus.boxlang.web.springboot.view.BoxLangViewResolver}
	 * in the Spring MVC view resolver chain.
	 *
	 * Lower values have higher priority. The default places BoxLang after most
	 * Spring-provided resolvers so it acts as a fallback but before the plain
	 * {@code InternalResourceViewResolver}.
	 *
	 * Default: {@code Integer.MAX_VALUE - 5} (i.e. {@code 2147483642})
	 */
	private int		viewResolverOrder	= Integer.MAX_VALUE - 5;

	/**
	 * Optional explicit web root directory for BoxLang path mappings.
	 *
	 * When blank, the auto-configuration derives the web root from the servlet
	 * context's real path or the JVM working directory.
	 */
	private String	webRoot				= "";

	/**
	 * Enable BoxLang debug mode.
	 *
	 * When {@code true}, the BoxLang runtime starts in debug mode, which enables
	 * additional logging and diagnostic output.
	 *
	 * Default: {@code false}
	 */
	private boolean	debugMode			= false;

	/**
	 * Override the BoxLang runtime home directory.
	 *
	 * The runtime home is where BoxLang stores its modules, cache, and other
	 * runtime artefacts. When {@code null} (the default), BoxLang uses its
	 * built-in default: {@code ${user.home}/.boxlang}.
	 *
	 * Typical overrides:
	 * <ul>
	 * <li>{@code /app/.boxlang} — fixed path inside a container image</li>
	 * <li>{@code /mnt/boxlang} — volume-mounted path in Kubernetes</li>
	 * </ul>
	 *
	 * Default: {@code null}
	 */
	private String	runtimeHome			= null;

	// -----------------------------------------------------------------------
	// Getters and setters
	// -----------------------------------------------------------------------

	public String getConfigPath() {
		return this.configPath;
	}

	public void setConfigPath( String configPath ) {
		this.configPath = configPath;
	}

	public String getPrefix() {
		return this.prefix;
	}

	public void setPrefix( String prefix ) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return this.suffix;
	}

	public void setSuffix( String suffix ) {
		this.suffix = suffix;
	}

	public int getViewResolverOrder() {
		return this.viewResolverOrder;
	}

	public void setViewResolverOrder( int viewResolverOrder ) {
		this.viewResolverOrder = viewResolverOrder;
	}

	public String getWebRoot() {
		return this.webRoot;
	}

	public void setWebRoot( String webRoot ) {
		this.webRoot = webRoot;
	}

	public boolean isDebugMode() {
		return this.debugMode;
	}

	public void setDebugMode( boolean debugMode ) {
		this.debugMode = debugMode;
	}

	public String getRuntimeHome() {
		return this.runtimeHome;
	}

	public void setRuntimeHome( String runtimeHome ) {
		this.runtimeHome = runtimeHome;
	}

}
