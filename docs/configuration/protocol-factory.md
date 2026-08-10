# Factories

The `ProtocolFactory` interface is the entry point for creating `Protocol` instances. A factory encapsulates two
strategies: how to convert the string that application code passes to `message()` into an internal representation, and
how to render that internal representation back into a displayable string when formatting takes place. These two
responsibilities are handled by a `MessageProcessor` and a `MessageFormatter` respectively. Both components are shared
across all protocols created by the same factory, so configuring the factory once during application startup is
sufficient for the entire lifecycle of the application.


## The M Type Parameter

Every `ProtocolFactory<M>` is parameterized by `M`, the type of the internal message representation. This type flows
through the entire protocol pipeline: the factory creates `Protocol<M>` instances, messages are stored as `M` objects
internally, and the `MessageFormatter<M>` knows how to turn an `M` into a formatted `String` at output time.

For the majority of use cases, `M` is simply `String`. The text passed to `message()` is stored as-is, and the
formatter applies parameter substitution at format time. More advanced setups use `M = Message` (from the
message-format library) where the internal representation is a parsed, structured message template. The choice of `M`
is entirely determined by the factory configuration. Application code that records and reads messages rarely needs to
interact with `M` directly.


## GenericProtocolFactory

`GenericProtocolFactory<M>` is the base implementation of `ProtocolFactory`. It combines a `MessageProcessor<M>` and a
`MessageFormatter<M>` into a working factory that can create protocol instances. Additionally, it detects a
`ProtocolMessageMatcher` implementation via the `ServiceLoader` mechanism, which enables expression-based matching
(the `matches(String)` and `propagate(String)` methods on protocols).

```java
import de.sayayi.lib.protocol.factory.GenericProtocolFactory;
import de.sayayi.lib.protocol.message.processor
    .ResourceBundleMessageProcessor;
import de.sayayi.lib.protocol.message.formatter
    .JavaMessageFormatFormatter;
import java.util.ResourceBundle;
import java.util.Locale;

ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.US);

var factory = new GenericProtocolFactory<>(
    new ResourceBundleMessageProcessor(bundle),
    new JavaMessageFormatFormatter(Locale.US)
);

var protocol = factory.createProtocol();
protocol
    .info()
    .message("import.started");
```

When the `protocol-message-matcher` module is on the classpath, the `ServiceLoader` automatically discovers the
matcher implementation and registers it with the factory. If the module is not present, expression-based matching
throws a `MessageMatcherException` at runtime. A custom matcher can be injected explicitly through the three-argument
constructor or via `setMessageMatcher()`.

```java
import de.sayayi.lib.protocol.ProtocolMessageMatcher;

ProtocolMessageMatcher customMatcher = ...;

// explicit matcher via constructor
var factory = new GenericProtocolFactory<>(
    processor, formatter, customMatcher);

// or override after construction
factory.setMessageMatcher(customMatcher);
```


## StringProtocolFactory

`StringProtocolFactory` is a convenience subclass of `GenericProtocolFactory<String>` that uses
`StringMessageProcessor` as its processor and provides static factory methods for the three most common formatting
strategies. It is the fastest way to get a working protocol factory when messages are simple text strings.


### createPlainTextFactory

`createPlainTextFactory()` creates a factory that stores messages as-is and returns them without any parameter
substitution during formatting. Messages are treated as plain text literals.

```java
import de.sayayi.lib.protocol.factory.StringProtocolFactory;

var factory = StringProtocolFactory.createPlainTextFactory();

var protocol = factory.createProtocol();
protocol
    .info()
    .message("Import complete");
// formatted: "Import complete"

// placeholders are NOT expanded
protocol
    .info()
    .message("Count: {0}")
    .with("0", 42);
// formatted: "Count: {0}" (literal output)
```


### createJavaMessageFormatFactory

`createJavaMessageFormatFactory()` creates a factory that formats messages using `java.text.MessageFormat`. This is
the most commonly used factory for applications that need simple indexed parameter substitution.

```java
var factory = StringProtocolFactory.createJavaMessageFormatFactory();

var protocol = factory.createProtocol();
protocol
    .info()
    .message("Imported {0} records from {1}")
    .with("0", 500)
    .with("1", "orders.csv");
// formatted: "Imported 500 records from orders.csv"
```


### createJavaStringFormatFactory

`createJavaStringFormatFactory()` creates a factory that formats messages using `String.format`. Messages use the
familiar `%s`, `%d`, `%f` placeholder syntax from Java's format strings.

```java
var factory = StringProtocolFactory.createJavaStringFormatFactory();

var protocol = factory.createProtocol();
protocol
    .warn()
    .message("Disk usage at %d%% (%s)")
    .with("0", 92)
    .with("1", "/data");
// formatted: "Disk usage at 92% (/data)"
```


## Creating Custom Factories

For scenarios where none of the built-in processors or formatters fit, a custom factory can be assembled from any
combination of `MessageProcessor` and `MessageFormatter`. Both are single-method interfaces, so lambda expressions or
small classes are sufficient.

The following example creates a factory that prepends a timestamp to every formatted message. The processor stores the
raw string, while the formatter adds a prefix during rendering.

```java
import de.sayayi.lib.protocol.Protocol.GenericMessage;
import de.sayayi.lib.protocol.ProtocolFactory.MessageFormatter;
import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor;
import de.sayayi.lib.protocol.factory.GenericProtocolFactory;
import de.sayayi.lib.protocol.message.GenericMessageWithId;
import java.time.Instant;

MessageProcessor<String> processor = message ->
    new GenericMessageWithId<>(message);

MessageFormatter<String> formatter = msg -> {
  long millis = msg.getTimeMillis();
  var ts = Instant.ofEpochMilli(millis);
  return "[" + ts + "] " + msg.getMessage();
};

var factory = new GenericProtocolFactory<>(processor, formatter);

var protocol = factory.createProtocol();
protocol
    .info()
    .message("Job started");
// formatted: "[2026-08-10T19:08:15Z] Job started"
```

When implementing a custom `MessageProcessor`, the `getIdFromMessage(M)` method can be overridden to control how
message IDs are derived from already-processed messages. The default implementation generates a random UUID, but a
custom processor might return a hash, a sequence number, or a domain-specific identifier.


## The DEFAULT_TAG_NAME Constant

`ProtocolFactory.DEFAULT_TAG_NAME` holds the string `default`. Every message implicitly carries this tag, as
described in the [Tags](../core/tags.md) section. The constant is useful when defining tag selectors, propagation rules, or
matchers that need to reference the default tag programmatically rather than through a string literal.
