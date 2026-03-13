# ⚡︎ BoxLang Spring Boot Starter

```
|:------------------------------------------------------:|
| ⚡︎ B o x L a n g ⚡︎
| Dynamic : Modular : Productive
|:------------------------------------------------------:|
```

<blockquote>
	Copyright Since 2023 by Ortus Solutions, Corp
	<br>
	<a href="https://www.boxlang.io">www.boxlang.io</a> |
	<a href="https://www.ortussolutions.com">www.ortussolutions.com</a>
</blockquote>

<p>&nbsp;</p>

## Welcome to the BoxLang Spring Boot Starter

The **BoxLang Spring Boot Starter** is a zero-configuration Spring Boot auto-configuration library that integrates the [BoxLang](https://www.boxlang.io) dynamic JVM language as a view engine inside any Spring Boot 3 web application. Write your templates in BoxLang's expressive `.bxm` markup syntax and let Spring MVC route requests to them — no boilerplate required.  You can also leverage any BoxLang features and libraries directly from your Java controllers, services, or any Spring-managed bean by just talking to the `BoxRuntime` API.

```
boxlang = BoxRuntime.getInstance();
```

## What is BoxLang?

**BoxLang** is a modern dynamic JVM language that can be deployed on multiple runtimes: operating system (Windows/Mac/*nix/Embedded), web server, lambda, iOS, android, web assembly, and more. **BoxLang** combines many features from different programming languages, including Java, ColdFusion, Python, Ruby, Go, and PHP, to provide developers with a modern and expressive syntax.

**BoxLang** has been designed to be a highly adaptable and dynamic language to take advantage of all the modern features of the JVM and was designed with several goals in mind:

* Be a rapid application development (RAD) scripting language and middleware.
* Unstagnate the dynamic language ecosystem in Java.
* Be dynamic, modular, lightweight, and fast.
* Be 100% interoperable with Java.
* Be modern, functional, and fluent (Think mixing CFML, Node, Kotlin, Java, and Clojure)
* Be able to support multiple runtimes and deployment targets:
  * Native OS Binaries (CLI Tooling, compilers, etc.)
  * MiniServer
  * Servlet Containers - CommandBox/Tomcat/Jetty/JBoss
  * JSR223 Scripting Engines
  * AWS Lambda
  * Microsoft Azure Functions (Coming Soon)
  * Android/iOS Devices (Coming Soon)
  * Web assembly (Coming Soon)
* Compile down to Java ByteCode
* Allow backward compatibility with the existing ColdFusion/CFML language.
* Great IDE, Debugger and Tooling: https://boxlang.ortusbooks.com/getting-started/ide-tooling
* Scripting (Any OS and Shebang) and REPL capabilities

You can find our docs here: https://boxlang.ortusbooks.com/

## Features

* **Zero-configuration auto-configuration** — drop the JAR on the classpath and Spring Boot wires everything automatically via `BoxLangAutoConfiguration`.
* **BoxLang View Resolver** — a `BoxLangViewResolver` resolves logical view names (e.g. `"home"`) to BoxLang `.bxm` templates (e.g. `classpath:/templates/home.bxm`).
* **Full web scopes** — templates have access to the complete set of BoxLang web scopes: `URL`, `Form`, `CGI`, `Cookie`, and `Request`.
* **Spring Model integration** — every attribute added to the Spring `Model` is automatically injected into the BoxLang `variables` scope, accessible as `#variables.myKey#` in the template.
* **Lifecycle-managed runtime** — the `BoxRuntime` starts early in the application lifecycle and shuts down gracefully when the context stops, with no manual wiring needed.
* **Configurable via `application.properties`** — all settings are controlled through the `boxlang.*` property namespace; sensible defaults require no changes for basic use.
* **Custom `boxlang.json` support** — supply your own BoxLang configuration file via classpath, file URI, or absolute path.
* **Pluggable resolver order** — configure the view resolver's position in the Spring MVC resolver chain so BoxLang can coexist with Thymeleaf, FreeMarker, or any other view technology.
* **Spring Boot 3 / Jakarta EE ready** — built against Spring Boot 3.x and the `jakarta.*` namespace.

## Requirements

| Dependency | Version |
|---|---|
| Java | 21+ |
| Spring Boot | 3.4.x+ |
| BoxLang | 1.11.0+ |

## Installation

### Gradle

```groovy
dependencies {
    implementation 'ortus.boxlang:boxlang-spring-boot-starter:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>ortus.boxlang</groupId>
    <artifactId>boxlang-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

No `@EnableBoxLang` annotation or manual bean registration is required. Spring Boot's auto-configuration mechanism detects the starter on the classpath and configures everything automatically.

## Quick Start

### 1. Add the dependency

Add `boxlang-spring-boot-starter` to your project as shown in the [Installation](#installation) section.

### 2. Create a controller

```java
@Controller
public class HomeController {

    @GetMapping( "/" )
    public String home( Model model ) {
        model.addAttribute( "title", "Hello from BoxLang + Spring Boot!" );
        model.addAttribute( "framework", "Spring Boot 3" );
        return "home"; // resolves to classpath:/templates/home.bxm
    }

    @GetMapping( "/greeting" )
    public String greeting( @RequestParam( defaultValue = "World" ) String name, Model model ) {
        model.addAttribute( "name", name );
        model.addAttribute( "message", "Hello, " + name + "!" );
        return "greeting"; // resolves to classpath:/templates/greeting.bxm
    }
}
```

### 3. Create BoxLang templates

Place `.bxm` templates under `src/main/resources/templates/`:

**`src/main/resources/templates/home.bxm`**

```html
<bx:output>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>#variables.title#</title>
</head>
<body>
	<!-- You can use the variable without the #variables prefix, if you want, for less verbosity -->
    <h1>#title#</h1>
    <p>Framework: #framework#</p>
</body>
</html>
</bx:output>
```

**`src/main/resources/templates/greeting.bxm`**

```html
<bx:output>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Greeting</title>
</head>
<body>
    <h1>#variables.message#</h1>
    <p>You requested a greeting for: <strong>#encodeForHTML( variables.name )#</strong></p>
</body>
</html>
</bx:output>
```

Spring attributes added via `model.addAttribute(...)` are available inside templates as `#variables.key#` or just `#key#` for convenience. You can also use any BoxLang functions or features as needed.

### 4. Run the application

```bash
./gradlew bootRun
# or
mvn spring-boot:run
```

## Configuration

All properties use the `boxlang.*` prefix in `application.properties` or `application.yml`.

| Property | Default | Description |
|---|---|---|
| `boxlang.prefix` | `classpath:/templates/` | Resource prefix prepended to every view name when resolving templates. |
| `boxlang.suffix` | `.bxm` | File extension appended to the view name. |
| `boxlang.config-path` | *(auto-detect)* | Explicit path to `boxlang.json`. Accepts `classpath:`, `file:`, or a bare filesystem path. |
| `boxlang.view-resolver-order` | `2147483642` (`Integer.MAX_VALUE - 5`) | Order of the `BoxLangViewResolver` in the Spring MVC resolver chain. Lower = higher priority. |
| `boxlang.web-root` | *(derived)* | Explicit web root directory for BoxLang path mappings. Defaults to the servlet context real path or JVM working directory. |
| `boxlang.debug-mode` | `false` | Enable BoxLang debug mode for additional logging and diagnostic output. |
| `boxlang.runtime-home` | `null` (uses `~/.boxlang`) | Override the BoxLang runtime home directory where modules and cache are stored. Useful for containerized deployments (e.g. `/app/.boxlang`). |

### Example `application.properties`

```properties
# Template location (default shown — only override when needed)
boxlang.prefix=classpath:/templates/
boxlang.suffix=.bxm

# Optional: explicit path to a custom boxlang.json
# boxlang.config-path=classpath:/boxlang.json

# Optional: adjust resolver priority
# boxlang.view-resolver-order=1

# Optional: enable BoxLang debug mode
# boxlang.debug-mode=true

# Optional: override the BoxLang runtime home (default: ~/.boxlang)
# boxlang.runtime-home=/app/.boxlang
```

### Example `application.yml`

```yaml
boxlang:
  prefix: classpath:/templates/
  suffix: .bxm
  # config-path: classpath:/boxlang.json
  # view-resolver-order: 1
```

## BoxLang Configuration (`boxlang.json`)

If no `boxlang.config-path` is set, the auto-configuration probes for `classpath:/boxlang.json` automatically. If that file is absent, BoxLang starts with its built-in defaults.

Place `boxlang.json` at `src/main/resources/boxlang.json` to customise language behaviour:

```json
{
    "debugMode": false,
    "locale": "en_US",
    "timezone": "UTC",
    "logging": {
        "logsDirectory": "./logs",
        "level": "WARN"
    }
}
```

Full `boxlang.json` documentation: https://boxlang.ortusbooks.com/configuration

## How It Works

1. **Auto-configuration** — `BoxLangAutoConfiguration` is registered via `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` and activates on any Servlet-based Spring web application.
2. **Runtime lifecycle** — the `BoxRuntime` is started via `SmartLifecycle` at phase `Integer.MIN_VALUE + 100` (very early), ensuring it is ready before the first HTTP request arrives. It shuts down gracefully when the application context stops.
3. **View resolution** — `BoxLangViewResolver` resolves logical view names to resources using the configured `prefix` + `viewName` + `suffix` pattern. If the resource does not exist, it returns `null` and Spring continues to the next resolver in the chain.
4. **Template rendering** — `BoxLangView` wraps the `HttpServletRequest` and `HttpServletResponse` in a `SpringBoxHTTPExchange`, constructs a `WebRequestBoxContext` (which exposes all BoxLang web scopes), injects the Spring `Model` map into the `variables` scope, executes the template, and flushes the output buffer to the servlet response.

## Project Structure

```
boxlang-spring-boot-starter/
├── src/main/java/ortus/boxlang/web/springboot/
│   ├── BoxLangAutoConfiguration.java   # Spring Boot auto-configuration entry point
│   ├── BoxLangProperties.java          # Bound configuration properties (boxlang.*)
│   ├── exchange/
│   │   └── SpringBoxHTTPExchange.java  # Adapts Servlet request/response to BoxLang's HTTP exchange interface
│   └── view/
│       ├── BoxLangViewResolver.java    # ViewResolver that maps view names to .bxm templates
│       └── BoxLangView.java            # View implementation that executes BoxLang templates
├── src/main/resources/
│   └── META-INF/spring/
│       └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
└── test-app/                           # Runnable integration test application
    └── src/main/
        ├── java/.../HomeController.java
        └── resources/templates/        # Example .bxm templates
```

## Building from Source

```bash
# Build the library
./gradlew build

# Run tests
./gradlew test

# Run the test application
cd test-app && ./gradlew bootRun
```

## License

Apache License, Version 2.0.

## Open-Source & Professional Support

This project is a professional open source project and is available as FREE and open source to use.  Ortus Solutions, Corp provides commercial support, training and commercial subscriptions which include the following:

* Professional Support and Priority Queuing
* Remote Assistance and Troubleshooting
* New Feature Requests and Custom Development
* Custom SLAs
* Application Modernization and Migration Services
* Performance Audits
* Enterprise Modules and Integrations
* Much More

Visit us at [BoxLang.io Plans](https://boxlang.io/plans) for more information.

## Ortus Sponsors

BoxLang is a professional open-source project and it is completely funded by the [community](https://patreon.com/ortussolutions) and [Ortus Solutions, Corp](https://www.ortussolutions.com). Ortus Patreons get many benefits like a cfcasts account, a FORGEBOX Pro account and so much more. If you are interested in becoming a sponsor, please visit our patronage page: [https://patreon.com/ortussolutions](https://patreon.com/ortussolutions)

### THE DAILY BREAD

> "I am the way, and the truth, and the life; no one comes to the Father, but by me (JESUS)" Jn 14:1-12
