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

## 👋 Welcome to the BoxLang Spring Boot Starter

The **BoxLang Spring Boot Starter** is a zero-configuration Spring Boot auto-configuration library that integrates the [BoxLang](https://www.boxlang.io) dynamic JVM language as a view engine inside any Spring Boot 3 web application. Write your templates in BoxLang's expressive `.bxm` markup syntax and let Spring MVC route requests to them — no boilerplate required. You can also leverage any BoxLang features and libraries directly from your Java controllers, services, or any Spring-managed bean by just talking to the `BoxRuntime` API.

```java
BoxRuntime boxlang = BoxRuntime.getInstance();
```

You can find all the documentation about the BoxLang templating language here: https://boxlang.ortusbooks.com/boxlang-language/templating-language

---

## 📖 Table of Contents

- [What is BoxLang?](#-what-is-boxlang)
- [Features](#-features)
- [Requirements](#-requirements)
- [Installation](#-installation)
- [Quick Start](#-quick-start)
- [Configuration](#%EF%B8%8F-configuration)
- [BoxLang Configuration (boxlang.json)](#-boxlang-configuration-boxlangjson)
- [Template Scopes & Model Integration](#-template-scopes--model-integration)
- [How It Works](#-how-it-works)
- [Project Structure](#-project-structure)
- [Development Setup](#-development-setup)
- [Hot-Reloading Templates](#-hot-reloading-templates)
- [Building from Source](#-building-from-source)
- [Running the Test Application](#-running-the-test-application)
- [Running Tests](#-running-tests)
- [IDE Tooling](#-ide-tooling)
- [License](#-license)
- [Open-Source & Professional Support](#-open-source--professional-support)

---

## 🌐 What is BoxLang?

**BoxLang** is a modern dynamic JVM language that can be deployed on multiple runtimes: operating system (Windows/Mac/*nix/Embedded), web server, lambda, iOS, Android, web assembly, and more. **BoxLang** combines many features from different programming languages, including Java, ColdFusion, Python, Ruby, Go, and PHP, to provide developers with a modern and expressive syntax.

**BoxLang** has been designed to be a highly adaptable and dynamic language to take advantage of all the modern features of the JVM:

- 🚀 Rapid application development (RAD) scripting language and middleware
- 💡 Dynamic, modular, lightweight, and fast
- ☕ 100% interoperable with Java
- 🎨 Modern, functional, and fluent
- 🌍 Multi-runtime deployment:
  - Native OS Binaries (CLI Tooling, compilers, etc.)
  - MiniServer
  - Servlet Containers — CommandBox/Tomcat/Jetty/JBoss
  - JSR223 Scripting Engines
  - AWS Lambda
  - Microsoft Azure Functions *(Coming Soon)*
  - Android/iOS Devices *(Coming Soon)*
  - Web Assembly *(Coming Soon)*
- 🔩 Compiles down to Java ByteCode
- 🔄 Backward compatible with the ColdFusion/CFML language

📚 Full documentation: https://boxlang.ortusbooks.com/

---

## ✨ Features

- ⚙️ **Zero-configuration auto-configuration** — drop the JAR on the classpath and Spring Boot wires everything automatically via `BoxLangAutoConfiguration`.
- 🖼️ **BoxLang View Resolver** — a `BoxLangViewResolver` resolves logical view names (e.g. `"home"`) to BoxLang `.bxm` templates (e.g. `classpath:/templates/home.bxm`).
- 🌐 **Full web scopes** — templates have access to the complete set of BoxLang web scopes: `URL`, `Form`, `CGI`, `Cookie`, and `Request`.
- 🔗 **Spring Model integration** — every attribute added to the Spring `Model` is automatically injected into the BoxLang `variables` scope, accessible as `#variables.myKey#` in the template.
- 🔄 **Lifecycle-managed runtime** — the `BoxRuntime` starts early in the application lifecycle and shuts down gracefully when the context stops, with no manual wiring needed.
- 🛠️ **Configurable via `application.properties`** — all settings are controlled through the `boxlang.*` property namespace; sensible defaults require no changes for basic use.
- 📄 **Custom `boxlang.json` support** — supply your own BoxLang configuration file via classpath, file URI, or absolute path.
- 🔀 **Pluggable resolver order** — configure the view resolver's position in the Spring MVC resolver chain so BoxLang can coexist with Thymeleaf, FreeMarker, or any other view technology.
- 🏷️ **Spring Boot 3 / Jakarta EE ready** — built against Spring Boot 3.x and the `jakarta.*` namespace.

---

## 📋 Requirements

| Dependency | Version |
|---|---|
| ☕ Java | 21+ |
| 🍃 Spring Boot | 3.4.x+ |
| 🥊 BoxLang | 1.11.0+ |

> **Note:** Make sure `JAVA_HOME` points to a JDK 21+ installation before building or running.

---

## 📦 Installation

### Gradle

```groovy
dependencies {
    implementation 'io.boxlang:boxlang-spring-boot-starter:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>io.boxlang</groupId>
    <artifactId>boxlang-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

> ✅ No `@EnableBoxLang` annotation or manual bean registration is required. Spring Boot's auto-configuration mechanism detects the starter on the classpath and configures everything automatically.

---

## 🚀 Quick Start

Get a BoxLang-powered Spring Boot application running in minutes.

### Step 1 — Add the dependency

Add `boxlang-spring-boot-starter` to your project as shown in the [Installation](#-installation) section.

### Step 2 — Create a Spring MVC controller

```java
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping( "/" )
    public String home( Model model ) {
        model.addAttribute( "title", "Hello from BoxLang + Spring Boot!" );
        model.addAttribute( "framework", "Spring Boot 3" );
        return "home"; // resolves to classpath:/templates/home.bxm
    }

    @GetMapping( "/greeting" )
    public String greeting(
        @RequestParam( name = "name", defaultValue = "World" ) String name,
        Model model
    ) {
        model.addAttribute( "name", name );
        model.addAttribute( "message", "Welcome, " + name + "!" );
        return "greeting"; // resolves to classpath:/templates/greeting.bxm
    }

    @GetMapping( "/items" )
    public String items( Model model ) {
        model.addAttribute( "items", java.util.List.of( "Apple", "Banana", "Cherry" ) );
        model.addAttribute( "count", 3 );
        return "items"; // resolves to classpath:/templates/items.bxm
    }
}
```

### Step 3 — Create BoxLang templates

Place `.bxm` templates under `src/main/resources/templates/`:

**`src/main/resources/templates/home.bxm`**

```html
<bx:output>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>#variables.title#</title>
</head>
<body>
    <!-- Variables are available directly or via the variables scope -->
    <h1>#title#</h1>
    <p>Framework: #framework#</p>
    <p>Rendered at: #dateTimeFormat( now(), "full" )#</p>

    <ul>
        <li><a href="/greeting">Default greeting</a></li>
        <li><a href="/greeting?name=Developer">Greeting with name</a></li>
        <li><a href="/items">Items list</a></li>
    </ul>
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
    <!-- Always encode user input to prevent XSS -->
    <p>Greeting for: <strong>#encodeForHTML( variables.name )#</strong></p>
    <p><a href="/">&larr; Back to home</a></p>
</body>
</html>
</bx:output>
```

**`src/main/resources/templates/items.bxm`**

```html
<bx:output>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Items List</title>
</head>
<body>
    <h1>Items (#variables.count#)</h1>
    <ul>
        <bx:loop array="#variables.items#" item="item">
            <li>#encodeForHTML( item )#</li>
        </bx:loop>
    </ul>
    <p><a href="/">&larr; Back to home</a></p>
</body>
</html>
</bx:output>
```

> 💡 Spring attributes added via `model.addAttribute(...)` are available inside templates as `#variables.key#` or just `#key#` for convenience. You can use any BoxLang built-in functions, tags, and features.

### Step 4 — Run the application

```bash
# Gradle
./gradlew bootRun

# Maven
mvn spring-boot:run
```

Open your browser at **http://localhost:8080** and you should see your BoxLang-rendered page! 🎉

---

## ⚙️ Configuration

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

# Optional: adjust resolver priority when mixing with other view technologies
# boxlang.view-resolver-order=1

# Optional: enable BoxLang debug mode
# boxlang.debug-mode=true

# Optional: override the BoxLang runtime home (default: ~/.boxlang)
# boxlang.runtime-home=/app/.boxlang

# Logging
logging.level.ortus.boxlang=WARN
logging.level.org.springframework.web=INFO
```

### Example `application.yml`

```yaml
boxlang:
  prefix: classpath:/templates/
  suffix: .bxm
  # config-path: classpath:/boxlang.json
  # view-resolver-order: 1
  # debug-mode: false
  # runtime-home: /app/.boxlang

logging:
  level:
    io.boxlang: WARN
    org.springframework.web: INFO
```

---

## 📄 BoxLang Configuration (`boxlang.json`)

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

📚 Full `boxlang.json` documentation: https://boxlang.ortusbooks.com/getting-started/configuration

---

## 🌐 Template Scopes & Model Integration

BoxLang templates rendered through the view engine have access to all standard BoxLang web scopes:

| Scope | Description |
|---|---|
| `variables` | Contains all Spring `Model` attributes plus template-local variables |
| `url` | Query string parameters from the HTTP request |
| `form` | Form POST data |
| `cgi` | CGI/server environment variables |
| `cookie` | HTTP cookies |
| `request` | Request-scoped storage (per HTTP request) |

**Accessing Spring Model attributes:**

```html
<bx:output>
    <!-- Both forms are equivalent -->
    <p>#variables.title#</p>
    <p>#title#</p>

    <!-- Access URL query params -->
    <p>Page: #url.page ?: 1#</p>

    <!-- Access cookies -->
    <p>Theme: #cookie.theme ?: "light"#</p>
</bx:output>
```

> 🔒 **Security tip:** Always use `encodeForHTML()` when outputting user-supplied values (e.g., URL or form parameters) to prevent XSS attacks.

---

## 🔍 How It Works

1. **Auto-configuration** — `BoxLangAutoConfiguration` is registered via `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` and activates on any Servlet-based Spring web application.
2. **Runtime lifecycle** — the `BoxRuntime` is started via `SmartLifecycle` at phase `Integer.MIN_VALUE + 100` (very early), ensuring it is ready before the first HTTP request arrives. It shuts down gracefully when the application context stops.
3. **View resolution** — `BoxLangViewResolver` resolves logical view names to resources using the configured `prefix` + `viewName` + `suffix` pattern. If the resource does not exist, it returns `null` and Spring continues to the next resolver in the chain, enabling seamless coexistence with other view technologies.
4. **Template rendering** — `BoxLangView` wraps the `HttpServletRequest` and `HttpServletResponse` in a `SpringBoxHTTPExchange`, constructs a `WebRequestBoxContext` (which exposes all BoxLang web scopes), injects the Spring `Model` map into the `variables` scope, executes the template, and flushes the output buffer to the servlet response.

---

## 📁 Project Structure

```
boxlang-spring-boot-starter/
├── src/
│   ├── main/
│   │   ├── java/ortus/boxlang/web/springboot/
│   │   │   ├── BoxLangAutoConfiguration.java     # Spring Boot auto-configuration entry point
│   │   │   ├── BoxLangProperties.java            # Bound configuration properties (boxlang.*)
│   │   │   ├── exchange/
│   │   │   │   └── SpringBoxHTTPExchange.java    # Adapts Servlet request/response to BoxLang's HTTP exchange interface
│   │   │   └── view/
│   │   │       ├── BoxLangViewResolver.java      # ViewResolver that maps view names to .bxm templates
│   │   │       └── BoxLangView.java              # View implementation that executes BoxLang templates
│   │   └── resources/
│   │       └── META-INF/spring/
│   │           └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│   └── test/
│       └── java/ortus/boxlang/web/springboot/   # JUnit 5 unit & integration tests
├── test-app/                                      # Runnable Spring Boot integration app
│   └── src/main/
│       ├── java/.../HomeController.java           # Example controllers
│       └── resources/
│           ├── application.properties
│           ├── application-dev.properties         # Dev profile (hot-reload enabled)
│           ├── boxlang.json                       # BoxLang runtime configuration
│           └── templates/                         # Example .bxm templates
│               ├── home.bxm
│               ├── greeting.bxm
│               └── items.bxm
├── build.gradle
├── gradle.properties
└── settings.gradle
```

---

## 💻 Development Setup

Follow these steps to get a local development environment up and running.

### Prerequisites

- ☕ **JDK 21+** — verify with `java -version`
- 🐘 **Gradle wrapper** is included — no separate Gradle installation required

### 1. Clone the repository

```bash
git clone https://github.com/ortus-boxlang/boxlang-spring-boot-starter.git
cd boxlang-spring-boot-starter
```

### 2. Build the library

```bash
./gradlew build
```

This compiles the source, downloads BoxLang JARs, runs all tests, and produces the output JAR in `build/libs/`.

### 3. Run the included test application

```bash
cd test-app
../gradlew bootRun
```

Or from the project root:

```bash
./gradlew :test-app:bootRun
```

Then open **http://localhost:8080** in your browser. You should see:

- **`/`** — Home page rendered by `home.bxm`
- **`/greeting`** — Default greeting rendered by `greeting.bxm`
- **`/greeting?name=YourName`** — Personalised greeting
- **`/items`** — Item list rendered by `items.bxm`

### JVM Arguments

The test application requires specific JVM flags for BoxLang reflection access. These are pre-configured in `test-app/build.gradle`:

```groovy
bootRun {
    jvmArgs = [
        '--add-opens', 'java.base/java.lang=ALL-UNNAMED',
        '--add-opens', 'java.base/java.lang.reflect=ALL-UNNAMED'
    ]
}
```

If you run the JAR directly or from an IDE, add the same flags to your run configuration:

```bash
java --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
     -jar test-app/build/libs/test-app-1.0.0.jar
```

---

## 🔥 Hot-Reloading Templates

By default, `boxlang.prefix` points at `classpath:/templates/`, which resolves to the compiled output directory (`build/resources/main/templates/`). Editing a `.bxm` source file has no effect until Gradle copies the updated resource — meaning a restart is usually required.

**To get instant hot-reload without a restart**, switch the prefix to a `file:` path that points directly at the source tree using a Spring dev profile.

### 1. Create `src/main/resources/application-dev.properties`

```properties
# Load templates directly from the source tree — edits take effect on the next request
boxlang.prefix=file:src/main/resources/templates/

# Enable BoxLang debug mode in development
boxlang.debug-mode=true

# Verbose logging in development
logging.level.ortus.boxlang=DEBUG
logging.level.org.springframework.web=DEBUG
```

> The path `file:src/main/resources/templates/` is relative to the JVM working directory. When you run `./gradlew bootRun` from the project root, the working directory is the project folder, so the path resolves correctly.

### 2. Activate the dev profile

```bash
# Pass it as a command-line argument
./gradlew bootRun --args='--spring.profiles.active=dev'

# Or set it permanently during development
echo 'spring.profiles.active=dev' >> src/main/resources/application.properties
```

> ⚠️ **Keep the `classpath:` prefix for production.** The `file:` path works for local development but must not be used in packaged JARs or containers where the source tree is not present.

---

## 🔨 Building from Source

```bash
# Compile and package
./gradlew build

# Run all unit and integration tests
./gradlew test

# Build without running tests
./gradlew build -x test

# View the test report
open build/reports/tests/test/index.html

# Publish to local Maven repository (~/.m2)
./gradlew publishToMavenLocal
```

---

## 🧪 Running the Test Application

The `test-app/` directory contains a fully functional Spring Boot application that exercises all starter features end-to-end.

```bash
# From the project root
./gradlew :test-app:bootRun

# With dev profile (hot-reload enabled)
./gradlew :test-app:bootRun --args='--spring.profiles.active=dev'
```

**Available endpoints:**

| Method | URL | Template |
|---|---|---|
| `GET` | `http://localhost:8080/` | `templates/home.bxm` |
| `GET` | `http://localhost:8080/greeting` | `templates/greeting.bxm` |
| `GET` | `http://localhost:8080/greeting?name=Alice` | `templates/greeting.bxm` |
| `GET` | `http://localhost:8080/items` | `templates/items.bxm` |

---

## ✅ Running Tests

```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "ortus.boxlang.web.springboot.BoxLangAutoConfigurationTest"

# Run tests for the test-app
./gradlew :test-app:test

# Continuous test execution (re-runs on file changes)
./gradlew test --continuous
```

Test reports are generated at `build/reports/tests/test/index.html`.

---

## 🛠️ IDE Tooling

For the best BoxLang authoring experience, install the official **BoxLang IDE Extension**:

- 🔌 **VS Code**: [BoxLang VSCode Extension](https://marketplace.visualstudio.com/items?itemName=ortus-solutions.vscode-boxlang)
- 🔌 **IntelliJ IDEA**: Available via JetBrains Marketplace

Features include syntax highlighting, code completion, formatting, and debugging support for `.bxm` and `.bx` files.

📖 Full IDE setup guide: https://boxlang.ortusbooks.com/getting-started/ide-tooling

---

## 📜 License

Apache License, Version 2.0.

---

## 💼 Open-Source & Professional Support

This project is a professional open source project and is available as **FREE** and open source to use. Ortus Solutions, Corp provides commercial support, training, and commercial subscriptions which include:

- 🎯 Professional Support and Priority Queuing
- 🖥️ Remote Assistance and Troubleshooting
- 🚀 New Feature Requests and Custom Development
- 📋 Custom SLAs
- 🔄 Application Modernization and Migration Services
- 📊 Performance Audits
- 🧩 Enterprise Modules and Integrations
- 🌟 Much More

Visit us at [BoxLang.io Plans](https://boxlang.io/plans) for more information.

## ❤️ Ortus Sponsors

BoxLang is a professional open-source project and it is completely funded by the [community](https://patreon.com/ortussolutions) and [Ortus Solutions, Corp](https://www.ortussolutions.com). Ortus Patreons get many benefits like a cfcasts account, a FORGEBOX Pro account and so much more. If you are interested in becoming a sponsor, please visit our patronage page: [https://patreon.com/ortussolutions](https://patreon.com/ortussolutions)

## 🙏 THE DAILY BREAD

> "I am the way, and the truth, and the life; no one comes to the Father, but by me (JESUS)" Jn 14:1-12
