# Protocol

Protocol is a Java library for collecting, structuring, querying, and formatting messages produced during business operations.

It is designed for multi-step flows where one operation can produce mixed outcomes: validation details, technical diagnostics, warnings, and hard errors. Messages can be tagged for target audiences, grouped into nested execution scopes, filtered with composable matchers, and rendered as plain text trees, JSON, or HTML.

## Modules

| Module | Purpose |
| --- | --- |
| `protocol-core` | Core API: protocol model, fluent message builders, groups, matchers, and formatters (`TechnicalProtocolFormatter`, `JsonProtocolFormatter`) |
| `protocol-html` | HTML formatter (`HtmlProtocolFormatter` and `HtmlProtocolFormatter.WithFontAwesome`) |
| `protocol-message-matcher` | Parser for textual matcher/tag-selector expressions used by `parseMessageMatcher(...)` and `parseTagSelector(...)` |

## Dependency

```gradle
dependencies {
  implementation("de.sayayi.lib:protocol-core:<version>")
  implementation("de.sayayi.lib:protocol-html:<version>") // optional
  implementation("de.sayayi.lib:protocol-message-matcher:<version>") // optional
}
```

```xml
<dependencies>
  <dependency>
    <groupId>de.sayayi.lib</groupId>
    <artifactId>protocol-core</artifactId>
    <version>${protocol.version}</version>
  </dependency>
  <dependency>
    <groupId>de.sayayi.lib</groupId>
    <artifactId>protocol-html</artifactId>
    <version>${protocol.version}</version>
  </dependency>
  <dependency>
    <groupId>de.sayayi.lib</groupId>
    <artifactId>protocol-message-matcher</artifactId>
    <version>${protocol.version}</version>
  </dependency>
</dependencies>
```

## Example: collect structured operation output

```java
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.factory.StringProtocolFactory;

Protocol<String> protocol = StringProtocolFactory.createJavaMessageFormatFactory().createProtocol();

protocol.info().forTag("api").message("Import started");

var orderGroup = protocol.createGroup("order-4711")
    .setGroupMessage("Order {0}")
    .with("0", 4711);

orderGroup.warn().forTag("validation").message("Missing value for field {0}").with("0", "deliveryDate");
orderGroup.error(new IllegalStateException("db timeout")).forTag("ops").message("Persistence failed");

protocol.info().forTag("api").message("Import finished");
```

## Example: filter and render

```java
import de.sayayi.lib.protocol.formatter.JsonProtocolFormatter;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.hasTag;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.isWarn;

int warningCount = protocol.getVisibleEntryCount(isWarn().and(hasTag("validation")));

String json = protocol.format(new JsonProtocolFormatter<>(), hasTag("api").or(hasTag("ops")));
String tree = protocol.toStringTree(); // ASCII tree with levels/tags
```

## Example: expression-based matching

Requires `protocol-message-matcher`.

```java
String filtered = protocol.format(new JsonProtocolFormatter<>(), "warn and any-of(validation,ops)");
```

## Requirements

- Java 21+
- `protocol-html`: one supported HTML encoder library on the classpath (Spring Web, Guava, Commons Text, Unbescape, or OWASP Encoder)
- `protocol-message-matcher`: ANTLR runtime (resolved transitively)
