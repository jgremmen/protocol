# Installation


## Java Version

The Protocol library requires Java 21 or later. All modules are compiled with source and target compatibility set to 21,
and the library makes use of language features introduced in that release. Projects that still target an earlier Java
version cannot use this library.


## Modules

The Protocol library is split into three modules. Only `protocol-core` is required; the other two modules provide
additional capabilities that can be pulled in on demand.

**protocol-core** contains the full protocol API: the fluent message builder, protocol groups, severity levels, tags,
message matchers, tag selectors, and the built-in formatters (ASCII tree, JSON). Every project that uses the Protocol
library needs this module.

**protocol-html** adds `HtmlProtocolFormatter`, which renders protocol entries as nested HTML lists with CSS classes for
severity and group depth. It requires exactly one HTML encoding library on the classpath (see the transitive dependencies
section below). Include this module only when HTML output is needed.

**protocol-message-matcher** provides an ANTLR-based parser that converts textual matcher and tag-selector expressions
into their programmatic equivalents. This is useful when matcher expressions need to be externalized into configuration
files or accepted as user input at runtime. If all matching logic is written programmatically through the
`MessageMatchers` factory methods, this module is not needed.


## Dependencies

Add the following to the project build file. Replace `1.7.0` with the desired version.

=== "Gradle (Groovy)"

    ```groovy
    dependencies {
      // required
      implementation 'de.sayayi.lib:protocol-core:1.7.0'

      // optional
      implementation 'de.sayayi.lib:protocol-html:1.7.0'
      implementation 'de.sayayi.lib:protocol-message-matcher:1.7.0'
    }
    ```

=== "Gradle (Kotlin)"

    ```kotlin
    dependencies {
      // required
      implementation("de.sayayi.lib:protocol-core:1.7.0")

      // optional
      implementation("de.sayayi.lib:protocol-html:1.7.0")
      implementation("de.sayayi.lib:protocol-message-matcher:1.7.0")
    }
    ```

=== "Maven"

    ```xml
    <dependencies>
      <!-- required -->
      <dependency>
        <groupId>de.sayayi.lib</groupId>
        <artifactId>protocol-core</artifactId>
        <version>1.7.0</version>
      </dependency>

      <!-- optional -->
      <dependency>
        <groupId>de.sayayi.lib</groupId>
        <artifactId>protocol-html</artifactId>
        <version>1.7.0</version>
      </dependency>

      <!-- optional -->
      <dependency>
        <groupId>de.sayayi.lib</groupId>
        <artifactId>protocol-message-matcher</artifactId>
        <version>1.7.0</version>
      </dependency>
    </dependencies>
    ```


## Transitive Dependencies

**protocol-core** has no mandatory runtime dependencies. It declares an optional dependency on
`de.sayayi.lib:message-format`, which enables integration with the Message Format library for advanced message
resolution and formatting. If that library is not on the classpath, the core module works perfectly fine on its own
using its built-in message processors.

**protocol-html** depends on `protocol-core` and requires exactly one of the following HTML encoding libraries to be
present at runtime. The `HtmlEncoder` service interface detects which library is available and delegates to it
automatically. The supported libraries are Spring Web (`org.springframework:spring-web`), Google Guava
(`com.google.guava:guava`), Apache Commons Text (`org.apache.commons:commons-text`), Unbescape
(`org.unbescape:unbescape`), and OWASP Java Encoder (`org.owasp.encoder:encoder`). All five are declared as optional
in the published POM, so exactly one must be added explicitly.

**protocol-message-matcher** depends on `protocol-core` and pulls in the ANTLR 4 runtime
(`org.antlr:antlr4-runtime`) as well as the ANTLR 4 Runtime Extensions library
(`de.sayayi.lib:antlr4-runtime-ext`). Both are resolved transitively and require no manual configuration.


## JPMS Module Names

All three modules ship with full Java Platform Module System descriptors. The module names are:

| Artifact                       | JPMS Module Name                           |
|--------------------------------|--------------------------------------------|
| `protocol-core`                | `de.sayayi.lib.protocol`                   |
| `protocol-html`                | `de.sayayi.lib.protocol.html`              |
| `protocol-message-matcher`     | `de.sayayi.lib.protocol.message.matcher`   |

A `module-info.java` that declares dependencies on the Protocol library looks like this:

```java
module com.example.myapp 
{
  requires de.sayayi.lib.protocol;

  // only if HTML output is used
  requires de.sayayi.lib.protocol.html;

  // only if expression-based matchers are used
  requires de.sayayi.lib.protocol.message.matcher;
}
```

Projects that do not use the module system can ignore these declarations entirely; the library works equally well on the
unnamed module path (traditional classpath).
