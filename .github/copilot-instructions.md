# BoxLang Spring Boot Starter — Copilot Instructions

## Project Overview

A **Spring Boot 3 auto-configuration starter** that integrates the [BoxLang](https://www.boxlang.io) dynamic JVM language as a view engine (`.bxm` templates) in Spring MVC applications.

- Root project (`boxlang-spring-boot-starter`) — the published library
- Sub-project (`:test-app`) — a runnable Spring Boot app used for end-to-end integration testing

---

## Build & Test Commands

```bash
# Compile and package the library
./gradlew build

# Run all unit tests
./gradlew test

# Build without running tests
./gradlew build -x test

# Run the test application (port 8080)
./gradlew :test-app:bootRun

# Run the test app with dev profile (hot-reload templates)
./gradlew :test-app:bootRun --args='--spring.profiles.active=dev'

# Run integration tests for the test app
./gradlew :test-app:test

# Format source code (Eclipse Java formatter)
./gradlew spotlessApply

# Download BoxLang and web-support JARs to src/test/resources/libs/
./gradlew downloadBoxLang

# Publish to Maven Central
./gradlew publish

# Version bumping
./gradlew bumpMajorVersion   # or bumpMinorVersion, bumpPatchVersion, bumpBetaVersion, bumpRCVersion
```

---

## Architecture

### Key Classes

| Class | Package | Role |
|---|---|---|
| `BoxLangAutoConfiguration` | `ortus.boxlang.web.springboot` | Auto-configuration entry point. Implements `SmartLifecycle` (phase `MIN_VALUE + 100`) to start/stop `BoxRuntime`. Registers `BoxLangViewResolver` bean. |
| `BoxLangProperties` | `ortus.boxlang.web.springboot` | `@ConfigurationProperties(prefix = "boxlang")` POJO. Defaults: `prefix=classpath:/templates/`, `suffix=.bxm`, `debugMode=false`. |
| `BoxLangViewResolver` | `ortus.boxlang.web.springboot.view` | Spring MVC `ViewResolver`. Resolves `viewName` → `prefix + viewName + suffix`. Returns `null` if template missing (chain continues). Ordered at `MAX_VALUE - 5`. |
| `BoxLangView` | `ortus.boxlang.web.springboot.view` | Spring `View` implementation. Wraps servlet request/response in `SpringBoxHTTPExchange`, builds `WebRequestBoxContext`, injects Spring `Model` into BoxLang `variables` scope, executes `.bxm` template. |
| `SpringBoxHTTPExchange` | `ortus.boxlang.web.springboot.exchange` | Stateless proxy implementing `IBoxHTTPExchange`. Delegates all HTTP methods to Jakarta `HttpServletRequest`/`HttpServletResponse`. Handles Jakarta ↔ BoxLang `Cookie` type-bridging. |

### Auto-Configuration Registration

`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
→ registers `ortus.boxlang.web.springboot.BoxLangAutoConfiguration`

### Template Rendering Flow

1. Controller returns logical view name (e.g., `"home"`)
2. `BoxLangViewResolver` builds path: `classpath:/templates/home.bxm`
3. `BoxLangView` wraps request/response in `SpringBoxHTTPExchange`
4. `WebRequestBoxContext` exposes web scopes: `url`, `form`, `cgi`, `cookie`, `request`
5. Spring `Model` attributes are merged into BoxLang `variables` scope
6. `BoxRuntime.executeTemplate()` renders the `.bxm` file to the response

---

## Dependency Resolution (Important)

The BoxLang JARs are **not on Maven Central** — the build uses a conditional resolution strategy:

```groovy
// If sibling monorepo projects are present (local development):
if ( file( '../boxlang/build/libs/boxlang-<version>.jar' ).exists() ) {
    implementation files( '../boxlang/build/libs/boxlang-<version>.jar' )
    implementation files( '../boxlang-web-support/build/libs/boxlang-web-support-<version>.jar' )
} else {
    // Use pre-downloaded JARs (CI/CD or fresh checkout)
    implementation files( 'src/test/resources/libs/boxlang-<version>.jar' )
    implementation files( 'src/test/resources/libs/boxlang-web-support-<version>.jar' )
}
```

If the JARs are missing, run `./gradlew downloadBoxLang` first.

---

## Versions (`gradle.properties`)

```properties
version=1.0.0
group=ortus.boxlang
jdkVersion=21
boxlangVersion=1.11.0
springBootVersion=3.4.3
```

On the `development` branch, versions automatically become `-snapshot` variants.

---

## Conventions

### Templates

- File extension: **`.bxm`** (BoxLang Markup)
- Default location: `src/main/resources/templates/`
- Variable access: `#variables.attributeName#` or shorthand `#attributeName#`
- Always use `encodeForHTML()` for user-supplied values to prevent XSS
- Wrap output in `<bx:output>...</bx:output>` to enable expression interpolation
- Optional `Application.bx` descriptor in the templates directory (lifecycle callbacks)

### Web Scopes in Templates

| Scope | Contents |
|---|---|
| `variables` | Spring `Model` attributes + template-local variables |
| `url` | Query string parameters |
| `form` | POST form data |
| `cgi` | Server/CGI environment |
| `cookie` | HTTP cookies |
| `request` | Request-scoped storage |

### Configuration Properties Prefix

All properties use the `boxlang.*` namespace in `application.properties` / `application.yml`.

### Code Formatting

Source is formatted with **Eclipse Java formatter** via Spotless. Run `./gradlew spotlessApply` before committing. The style config is `.ortus-java-style.xml` at the project root.

---

## JVM Arguments

BoxLang requires reflective access to JDK internals. Always add these when running the app outside Gradle (e.g., from an IDE or `java -jar`):

```
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.lang.reflect=ALL-UNNAMED
```

These are pre-configured in `test-app/build.gradle`'s `bootRun` block.

---

## Testing

- Framework: **JUnit 5 (Jupiter)** + **Mockito** + **Google Truth**
- Unit tests: `src/test/java/ortus/boxlang/web/springboot/`
- Integration tests: `test-app/src/test/java/` — starts a full Spring Boot context on a random port via `@SpringBootTest(webEnvironment = RANDOM_PORT)`
- Test reports: `build/reports/tests/test/index.html`

---

## Project Structure

```
boxlang-spring-boot-starter/
├── .github/
│   └── copilot-instructions.md
├── src/
│   ├── main/java/ortus/boxlang/web/springboot/
│   │   ├── BoxLangAutoConfiguration.java
│   │   ├── BoxLangProperties.java
│   │   ├── exchange/SpringBoxHTTPExchange.java
│   │   └── view/{BoxLangViewResolver,BoxLangView}.java
│   ├── main/resources/META-INF/spring/        # Auto-config imports
│   └── test/java/...                           # Unit tests
├── test-app/                                   # Integration test app
│   └── src/main/resources/
│       ├── application.properties
│       ├── application-dev.properties          # dev profile: file: prefix for hot-reload
│       ├── boxlang.json
│       └── templates/{home,greeting,items}.bxm
├── build.gradle
├── gradle.properties
└── settings.gradle                             # includes ':test-app'
```
