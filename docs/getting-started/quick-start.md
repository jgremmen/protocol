# Quick Start

This page walks through the complete lifecycle of a protocol: creating a factory, obtaining a protocol instance,
recording messages, organizing them into groups, and rendering the result. The goal is to build working familiarity with
the API before diving into individual concepts in the sections that follow.


## The Pipeline

Every interaction with the Protocol library follows four stages. First, a **factory** is created once and reused across
the application. It determines how message strings are stored internally and how they are rendered back into displayable
text. Second, the factory produces a **protocol** instance that acts as the container for all messages recorded during a
particular operation. Third, messages are added to the protocol (or to groups within it) through a fluent builder API
that attaches a severity level, optional tags, and optional parameters to each message. Fourth, the collected messages
are **formatted** into a desired output representation such as an ASCII tree or JSON.


## Minimal Example

The simplest possible usage records a few messages without any tags or groups and renders the result as an ASCII tree.

```java
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.factory.StringProtocolFactory;

StringProtocolFactory factory =
    StringProtocolFactory.createPlainTextFactory();

Protocol<String> protocol = factory.createProtocol();

protocol.debug().message("Initializing module");
protocol.info().message("Configuration loaded");
protocol.warn().message("Deprecated API in use");
protocol.error().message("Connection refused");

String tree = protocol.toStringTree();
// Renders:
// ■──Initializing module  {level=DEBUG,tags=[default]}
// │
// ├──Configuration loaded  {level=INFO,tags=[default]}
// │
// ├──Deprecated API in use  {level=WARN,tags=[default]}
// │
// └──Connection refused  {level=ERROR,tags=[default]}
```

The factory created here uses `createPlainTextFactory()`, which stores messages verbatim without any parameter
substitution. Each call to `debug()`, `info()`, `warn()`, or `error()` returns a builder that expects a `message()`
call to finalize the entry. The `toStringTree()` method delegates to the `TechnicalProtocolFormatter`, which renders
every message with its level and tags in a tree structure. Because no explicit tags were assigned, each message carries
only the implicit `default` tag that the library assigns automatically.


## Parameter Substitution

When messages contain variable parts, parameter placeholders allow injecting values at format time. The
`createJavaMessageFormatFactory()` uses `java.text.MessageFormat` syntax where parameters are referenced by numeric
index.

```java
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.factory.StringProtocolFactory;

StringProtocolFactory factory =
    StringProtocolFactory.createJavaMessageFormatFactory();

Protocol<String> protocol = factory.createProtocol();

protocol
    .info()
    .message("Processing batch {0} of {1}")
    .with("0", 3)
    .with("1", 10);

protocol
    .warn()
    .message("Skipped {0} invalid records")
    .with("0", 17);

String tree = protocol.toStringTree();
// Renders:
// ■──Processing batch 3 of 10  {level=INFO,...}
// │
// └──Skipped 17 invalid records  {level=WARN,...}
```

The `with()` method on the `MessageParameterBuilder` attaches parameter values that get substituted when the message is
formatted. Parameters are keyed by name (here the numeric index `"0"`, `"1"` matching the `MessageFormat` placeholders).
The builder returns itself, so multiple `with()` calls can be chained. After calling `message(...)`, the builder also
exposes the full `Protocol` interface, allowing the next message to be added immediately in a fluent chain.


## Tags

Tags categorize messages for different audiences or downstream consumers. A message can carry multiple tags, and tags
are used as filter criteria when formatting or querying the protocol. Tags are free-form strings, so no upfront
registration is required.

```java
protocol
    .info()
    .forTag("ui")
    .message("Upload complete");

protocol
    .error()
    .forTags("ui", "ops")
    .message("Storage quota exceeded");

protocol
    .debug()
    .forTag("audit")
    .message("Request payload logged");
```

The `forTag()` method assigns a single tag, while `forTags()` accepts multiple. Messages without an explicit tag still
receive the implicit `default` tag. Tags become powerful when combined with matchers during formatting or querying, as
covered in the [Tags](../core/tags.md) and [Querying](../core/querying.md) sections.


## Groups

Groups organize related messages into a named, hierarchical scope. A group can carry its own header message and
parameters, and its entries appear indented when rendered as a tree. Groups are created from the protocol (or from
another group for nesting) and act as a `Protocol` themselves, meaning messages are added to them in exactly the same
way.

```java
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.factory.StringProtocolFactory;

StringProtocolFactory factory =
    StringProtocolFactory.createJavaMessageFormatFactory();

Protocol<String> protocol = factory.createProtocol();

protocol
    .info()
    .message("Import started");

ProtocolGroup<String> orderGroup = protocol
    .createGroup("order-4711")
    .setGroupMessage("Order {0}")
    .with("0", 4711);

orderGroup
    .info()
    .message("Parsing line items");
orderGroup
    .warn()
    .message("Quantity {0} exceeds limit")
    .with("0", 500);

protocol
    .info()
    .message("Import finished");

String tree = protocol.toStringTree();
// Renders:
// ■──Import started  {level=INFO,tags=[default]}
// │
// ├──Order 4711  {level=INFO,tags=[default]}
// │  │
// │  ├──Parsing line items  {level=INFO,...}
// │  │
// │  └──Quantity 500 exceeds limit  {level=WARN,...}
// │
// └──Import finished  {level=INFO,tags=[default]}
```

The string passed to `createGroup("order-4711")` assigns a unique name that can later be used to retrieve the group via
`protocol.getGroupByName("order-4711")`. The `setGroupMessage(...)` call defines the header line that appears as the
group node in the tree, and `with()` provides the parameter values for that header. Detailed coverage of group
visibility, level limits, and nesting is available in the [Groups](../core/groups.md) section.


## Formatting as JSON

Besides the ASCII tree, the library ships a `JsonProtocolFormatter` that produces a JSON array of message objects.

```java
import de.sayayi.lib.protocol.formatter.JsonProtocolFormatter;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.any;

String json = protocol.format(new JsonProtocolFormatter<>(), any());
```

The `format()` method accepts any `ProtocolFormatter` together with a `MessageMatcher` that determines which entries
are included. The `any()` matcher includes everything. The resulting JSON contains objects with `level`, `message`,
`group`, `tags`, `creation-time`, and `message-id` properties. Group entries nest their children in a `messages` array.
The [JSON Formatter](../formatting/json-formatter.md) and [Technical Formatter](../formatting/technical-formatter.md) 
sections cover both formatters in detail.


## Querying

Before formatting, it is often useful to check whether a protocol contains entries of interest or to count them. The
`ProtocolQueryable` interface, implemented by every protocol and group, provides `matches()` and
`getVisibleEntryCount()` for this purpose.

```java
import static de.sayayi.lib.protocol.matcher.MessageMatchers.isError;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.isWarn;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.hasTag;

boolean hasErrors = protocol.matches(isError());
int warningCount = protocol
    .getVisibleEntryCount(hasTag("ui").and(isWarn()));
```

Matchers compose through `and()` and `or()` methods, allowing arbitrarily complex filter expressions. The
[Querying](../core/querying.md) and [Programmatic Matchers](../matcher/programmatic-matchers.md)
sections explore matcher composition in depth.


## Complete Example

The following example combines all concepts into a realistic scenario: an order import service that processes multiple
orders, records validation findings for different audiences, attaches exceptions, and renders the protocol both as a
diagnostic tree and as filtered JSON for the UI layer.

```java
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.factory.StringProtocolFactory;
import de.sayayi.lib.protocol.formatter.JsonProtocolFormatter;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.*;

StringProtocolFactory factory =
    StringProtocolFactory.createJavaMessageFormatFactory();

Protocol<String> protocol = factory.createProtocol();

// top-level status
protocol
    .info()
    .forTag("ui")
    .message("Order import started");

// --- first order group ---
ProtocolGroup<String> order1 = protocol
    .createGroup("order-1001")
    .setGroupMessage("Order {0}")
    .with("0", 1001);

order1
    .info()
    .forTag("audit")
    .message("Received {0} line items")
    .with("0", 5);

order1
    .debug()
    .forTag("ops")
    .message("Mapped to warehouse region {0}")
    .with("0", "EU-WEST");

order1
    .warn()
    .forTags("ui", "validation")
    .message("Delivery date {0} is in the past")
    .with("0", "2025-12-01");

order1
    .info()
    .forTag("ui")
    .message("Order accepted with warnings");

// --- second order group ---
ProtocolGroup<String> order2 = protocol
    .createGroup("order-1002")
    .setGroupMessage("Order {0}")
    .with("0", 1002);

order2
    .info()
    .forTag("audit")
    .message("Received {0} line items")
    .with("0", 3);

order2
    .error(new IllegalStateException("constraint"))
    .forTags("ui", "ops")
    .message("Duplicate order number detected");

order2.warn()
    .forTag("validation")
    .message("Customer {0} flagged for review")
    .with("0", "CUST-887");

order2.error(new RuntimeException("timeout"))
    .forTag("ops")
    .message("Persistence failed after {0}ms")
    .with("0", 3000);

// top-level status
protocol.info()
    .forTag("ui")
    .message("Order import completed");

protocol.warn()
    .forTag("ops")
    .message("{0} order(s) require attention")
    .with("0", 1);

// --- formatting ---

// full diagnostic tree (all messages)
String diagnosticTree = protocol.toStringTree();

// JSON for the UI (only messages tagged "ui")
String uiJson = protocol.format(
    new JsonProtocolFormatter<>(), hasTag("ui"));

// count errors across all groups
int errorCount = protocol.getVisibleEntryCount(isError());

// check if any validation issues exist
boolean hasValidationFindings = protocol.matches(hasTag("validation"));
```

This protocol contains two groups with a total of ten messages spread across four tags (ui, audit, ops, validation)
and four severity levels. The `toStringTree()` call produces the full hierarchy for developers and operators. The
filtered JSON call extracts only messages relevant to the end user. The query methods provide quick counts and existence
checks without iterating manually.

The following sections cover each concept in depth: [Levels](../core/levels.md),
[Tags](../core/tags.md), [Groups](../core/groups.md),
[Parameters](../core/parameters.md), and [Factories](../configuration/protocol-factory.md).
