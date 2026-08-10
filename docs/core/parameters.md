# Parameters

Parameters supply dynamic values to protocol messages at formatting time. They bridge the gap between a static message
template (such as `"Processed {0} of {1} records"`) and the concrete values that should appear in the final output.
The protocol library distinguishes between two scopes of parameters: protocol-level parameters that apply to all
messages within a protocol or group, and message-level parameters that are specific to a single message.


## Protocol-Level Parameters

Protocol-level parameters are set directly on a `Protocol` or `ProtocolGroup` instance via the `set(String, Object)`
method and its typed overloads for `boolean`, `int`, `long`, `float`, and `double`. Once set, a protocol-level
parameter is available to every message and group that belongs to that protocol. This makes protocol-level parameters
ideal for shared context values such as a batch identifier, a user name, or an operation timestamp.

```java
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.factory.StringProtocolFactory;

StringProtocolFactory factory =
    StringProtocolFactory.createJavaMessageFormatFactory();
Protocol<String> protocol = factory.createProtocol();

// set shared context values
protocol.set("batchId", "B-2026-08");
protocol.set("retryCount", 3);
protocol.set("dryRun", true);
protocol.set("progress", 0.75);
```

The `set` method returns the protocol instance, so calls can be chained if multiple parameters need to be established
in sequence.

```java
protocol
    .set("tenant", "acme-corp")
    .set("region", "eu-west")
    .set("version", 2L);
```

Protocol-level parameters are retrieved with `get(String)` or the type-safe `get(String, Class)`. The untyped variant
returns an `Object` (or `null` if the parameter has not been set). The typed variant returns the value only if it is
assignable to the specified class; otherwise it returns `null`.

```java
// untyped retrieval
Object batchId = protocol.get("batchId");
// batchId == "B-2026-08"

// type-safe retrieval
String id = protocol.get("batchId", String.class);
// id == "B-2026-08"

Integer retries = protocol.get("retryCount", Integer.class);
// retries == 3

// type mismatch returns null
String wrong = protocol.get("retryCount", String.class);
// wrong == null
```


### Inheritance in Groups

When a `ProtocolGroup` is created from a protocol, the group's internal parameter map is linked to the parent
protocol's parameter map. A parameter lookup on the group first checks the group's own parameters and then falls
through to the parent. This means that protocol-level parameters set on the parent are automatically visible inside
child groups without any additional configuration.

```java
protocol.set("tenant", "acme-corp");

var group = protocol.createGroup("validation");

// inherited from parent
String tenant = group.get("tenant", String.class);
// tenant == "acme-corp"

// override in the group
group.set("tenant", "beta-inc");
String overridden = group.get("tenant", String.class);
// overridden == "beta-inc"

// parent is unchanged
String parentTenant = protocol.get("tenant", String.class);
// parentTenant == "acme-corp"
```

Setting a parameter on a group does not modify the parent. The override exists only within the group's scope and is
visible to any nested sub-groups created from that group.


## Message-Level Parameters

Message-level parameters are attached to a specific message through the `MessageParameterBuilder` returned by
`message()`. The `with(String, Object)` method and its typed overloads for `boolean`, `int`, `long`, `float`, and
`double` associate a named value with the message. Multiple calls can be chained, and `with(Map)` sets several
parameters at once.

```java
protocol
    .info()
    .message("Imported {0} records from {1}")
    .with("0", 1500)
    .with("1", "orders.csv");

protocol
    .warn()
    .message("Retry {0} of {1} failed")
    .with("0", 3)
    .with("1", 5);
```

```java
import java.util.Map;

// bulk assignment with a map
protocol
    .info()
    .message("User {0} ({1}) logged in")
    .with(Map.of("0", "jdoe", "1", "admin"));
```

If a parameter with the same name is set more than once on the same message, the last value wins. This applies both
within chained `with()` calls and between `with(Map)` and individual `with()` calls.

```java
protocol
    .info()
    .message("Status: {0}")
    .with("0", "pending")
    .with("0", "confirmed");
// parameter "0" == "confirmed"
```


### Overriding Protocol-Level Parameters

Message-level parameters take precedence over protocol-level parameters when both define the same name. The merged
parameter map seen by the `MessageFormatter` during formatting contains the union of protocol-level and message-level
parameters, with message-level values shadowing any equally-named protocol-level values.

```java
protocol.set("env", "production");

protocol
    .info()
    .message("Running in {0}")
    .with("0", protocol.get("env"));
// formatted: "Running in production"

// message-level override
protocol
    .info()
    .message("Running in {0}")
    .with("0", "staging");
// formatted: "Running in staging"
```


## Supported Value Types

Both `set()` (protocol-level) and `with()` (message-level) accept any `Object` as a parameter value. Typed overloads
exist for the six primitive-adjacent types: `boolean`, `int`, `long`, `float`, `double`, and `Object`. The actual
interpretation of the value depends on the `MessageFormatter` in use. `JavaMessageFormatFormatter` passes values to
`java.text.MessageFormat`, which supports `Number`, `Date`, and `String` natively.
`JavaStringFormatFormatter` passes values to `String.format`, where the format specifier in the message determines how
the value is rendered. The message-format library's `MessageFormatFormatter` can handle any type through its own type
system and data converters.

```java
// various types with JavaMessageFormatFormatter
protocol
    .info()
    .message("{0} items at {1,number,currency}")
    .with("0", 42)
    .with("1", 9.99);
// formatted: "42 items at $9.99"

protocol
    .info()
    .message("Active: {0}")
    .with("0", true);
// formatted: "Active: true"

protocol
    .info()
    .message("Size: {0,number,#,###} bytes")
    .with("0", 1048576L);
// formatted: "Size: 1,048,576 bytes"
```


## How Parameters Are Used During Formatting

When a protocol message is formatted, the `MessageFormatter` receives a `GenericMessage<M>` that exposes the merged
parameter map through `getParameterValues()`. This map is an unmodifiable `Map<String, Object>` containing all
parameters from both the protocol level and the message level, with message-level values taking priority.

For the indexed formatters (`JavaMessageFormatFormatter` and `JavaStringFormatFormatter`), parameter names must be
numeric strings representing array indices. The formatter collects these into an `Object[]` where the parameter named
`"0"` occupies index 0, `"1"` occupies index 1, and so on. The maximum supported index is 31; parameter names that
are not valid integers or exceed 31 are silently skipped. Missing indices result in `null` values in the array.

```java
// indices 0 and 2 are set, index 1 is null
protocol
    .info()
    .message("{0} / {1} / {2}")
    .with("0", "A")
    .with("2", "C");
// formatted: "A / null / C"
```

For the message-format library's `MessageFormatFormatter`, parameters are passed by name directly to the formatting
engine. There is no index restriction; any valid parameter name can be referenced in the message template.


## Parameter Naming

Parameter names must have a length of at least one character. Although there are no hard restrictions enforced by the
API, the recommended convention is that parameter names match the regular expression `\p{Alnum}\p{Graph}*`. This means
the name starts with an alphanumeric character and may be followed by any printable non-whitespace characters.

For the indexed formatters, parameter names are typically just the digits `"0"` through `"31"` corresponding to
placeholder positions. For the message-format library, names can be descriptive identifiers like `"count"`,
`"userName"`, or `"order.total"`.


## The ParameterMap Utility

Internally, the protocol library uses `ParameterMap` to store and resolve parameters. This class maintains a sorted
array of parameter entries and supports optional parent chaining for the inheritance mechanism described above.
Application code does not interact with `ParameterMap` directly under normal circumstances, but it is part of the
public API in the `de.sayayi.lib.protocol.util` package for advanced use cases such as building custom formatters or
protocol entry adapters.

Key characteristics of `ParameterMap` include sorted storage by parameter name for efficient binary-search lookups,
parent chaining where lookups fall through to the parent when a local entry is not found, and a merged iterator that
returns entries from both the local map and the parent with local entries shadowing parent entries of the same name.
