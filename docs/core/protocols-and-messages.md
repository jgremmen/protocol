# Protocols and Messages

A `Protocol` is the central container in the Protocol library. It collects structured messages that accumulate during
a business operation, such as an import job, a validation pass, or a multi-step workflow. Rather than scattering
diagnostic output across log files and return values, a protocol keeps every message in one place. The messages can
later be filtered, counted, and formatted into different representations for different audiences.


## The Protocol Interface

The `Protocol<M>` interface is generic. The type parameter `M` represents the internal message format that the factory
uses to store message content. For most applications `M` is simply `String`, meaning the text passed to `message()` is
stored as-is. Other setups map the message string to a resource bundle key, a parsed template, or an object from the
Message Format library. The choice of `M` is determined by the `ProtocolFactory` that creates the protocol; application
code that only records and reads messages rarely needs to interact with `M` directly.


## Creating a Protocol

Every protocol is created through a `ProtocolFactory`. The factory is typically instantiated once during application
startup and reused whenever a new operation requires its own protocol.

```java
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.factory.StringProtocolFactory;

// create the factory once
StringProtocolFactory factory =
    StringProtocolFactory.createJavaMessageFormatFactory();

// create a protocol per operation
Protocol<String> protocol = factory.createProtocol();
```

Each call to `createProtocol()` returns a fresh, empty protocol. Every protocol receives a unique integer ID,
assigned by a global atomic counter in the factory infrastructure. The ID can be retrieved with `getId()` and is
useful for correlating protocols in logs or diagnostics.

```java
int id = protocol.getId();
// e.g. 1, 2, 3, … — unique across all
// protocols created by any factory
```


## Adding Messages

Messages are added through a fluent builder chain. The chain starts by selecting a severity level, then optionally
attaches tags and a throwable, and ends by specifying the message text. The general shape of the chain is:

**level → tags → throwable → message → parameters**

The `Protocol` interface provides convenience methods for the four standard severity levels (`debug()`, `info()`,
`warn()`, `error()`) and a generic `add(Level)` method for custom levels. Each of these returns a
`ProtocolMessageBuilder` that collects metadata before the message is committed.

```java
// info-level message with a tag
protocol
    .info()
    .forTag("ui")
    .message("Upload complete");

// error with an exception attached
protocol
    .error(new IOException("disk full"))
    .forTag("ops")
    .message("Storage write failed");

// custom level (severity 250)
protocol
    .add(() -> 250)
    .message("Custom severity event");
```

Calling `message(...)` finalizes the builder and adds the entry to the protocol. The string passed to `message()` is
handed to the factory's `MessageProcessor`, which converts it into the internal representation `M`. If the processor
uses `java.text.MessageFormat`, parameter placeholders like `{0}` can be included in the message text.


### The Builder Steps in Detail

The builder steps can be combined in any order before `message()` is called, with the constraint that `message()` must
come last.

`forTag(String)` and `forTags(String...)` associate one or more tags with the message. Tags are covered in depth in the
[Tags](tags.md) section.

`withThrowable(Throwable)` attaches an exception to the message. A common shorthand for error-level messages with a
throwable is the `error(Throwable)` method, which is equivalent to `add(Level.Shared.ERROR).withThrowable(throwable)`.

`message(String)` specifies the message text and commits the entry. It returns a `MessageParameterBuilder` that allows
parameter values to be attached.


### Message Parameters

The `MessageParameterBuilder` returned by `message()` supports `with(String, Object)` and its typed overloads for
`boolean`, `int`, `long`, `float`, and `double`. Parameter names are free-form strings, though alphanumeric names
matching `\p{Alnum}\p{Graph}*` are recommended. Multiple `with()` calls can be chained, and `with(Map)` sets several
parameters at once.

```java
protocol
    .info()
    .message("Processed {0} of {1} records")
    .with("0", 42)
    .with("1", 100);
```

Because `MessageParameterBuilder` also implements `Protocol`, the next message can be chained directly after the
parameter calls without obtaining a separate reference to the protocol.

```java
// chaining two messages in one statement
protocol
    .info()
    .forTag("audit")
    .message("Batch {0} started")
    .with("0", "B-001")
    .warn()
    .forTag("validation")
    .message("Missing field {0}")
    .with("0", "email");
```

Protocol-level and message-level parameters are covered in detail in the [Parameters](parameters.md) section.


## Message Identity

Every message stored in a protocol carries a message ID and a creation timestamp. The message ID is produced by the
factory's `MessageProcessor` when `processMessage(String)` is called. The default `StringMessageProcessor` generates
a random UUID for each message. Other processors, such as `ResourceBundleMessageProcessor`, can use the message string
itself as the ID, making it possible to look up messages by a known key.

The creation timestamp records the wall-clock time at which the message was added, measured in milliseconds since the
Unix epoch. Both values are accessible through the `GenericMessage` interface.

```java
import de.sayayi.lib.protocol.Protocol.MessageParameterBuilder;

MessageParameterBuilder<String> builder = protocol
    .info()
    .message("Step completed");

String messageId = builder.getMessageId();
long createdAt = builder.getTimeMillis();
```


## Protocol-Level Parameters

In addition to message-level parameters set via `with()`, a protocol itself can carry parameters. These are set with
`set(String, Object)` and retrieved with `get(String)` or the type-safe `get(String, Class)`. Protocol-level parameters
are inherited by all messages and groups added to the protocol, so they act as shared context values.

```java
protocol.set("batchId", "B-2026-08");
protocol.set("retryCount", 3);

protocol
    .info()
    .message("Batch {0} attempt {1}")
    .with("0", protocol.get("batchId"))
    .with("1", protocol.get("retryCount"));

// type-safe retrieval
String batchId = protocol.get("batchId", String.class);
Integer retries = protocol.get("retryCount", Integer.class);
// returns null if the type does not match
String wrong = protocol.get("retryCount", String.class);
```


## Protocol Hierarchy

Protocols can form a parent-child hierarchy through groups. When `createGroup()` or `createGroup(name)` is called on a
protocol, the resulting `ProtocolGroup` is a child of that protocol. The group itself is also a `Protocol`, so it
supports the same message-building and querying operations.

```java
import de.sayayi.lib.protocol.ProtocolGroup;

ProtocolGroup<String> child = protocol.createGroup("validation");

// navigate the hierarchy
Protocol<String> parent = child.getParent();
// parent == protocol

Protocol<String> root = child.getRootProtocol();
// root == protocol (same as parent here)
```

`getParent()` returns the immediate parent protocol, or `null` for a root protocol. `getRootProtocol()`, available only
on `ProtocolGroup`, walks up the chain and returns the top-level protocol. Groups can be nested arbitrarily deep, and
each level inherits the parameters and tag propagation rules of its ancestors.

Groups, their visibility settings, and nesting behavior are covered in the [Groups](groups.md) section.


## Thread Safety

Protocol instances are **not** thread-safe. All message-building, querying, and formatting operations on a single
protocol must happen on the same thread. Concurrent modification leads to undefined behavior.

A common pattern for multi-threaded workloads is to create a separate `ProtocolGroup` for each thread from a shared
parent protocol. Each thread writes exclusively to its own group, and the parent protocol is not used for formatting
or querying until all threads have finished. This avoids synchronization while still collecting all results under a
single root.

```java
// parent protocol, created on the main thread
Protocol<String> protocol = factory.createProtocol();

// each worker thread gets its own group
ProtocolGroup<String> threadGroup =
    protocol.createGroup("worker-" + threadId);

// safe: each thread writes only to its group
threadGroup
    .info()
    .message("Processing item {0}")
    .with("0", itemId);

// after all threads complete, format on the
// main thread
String tree = protocol.toStringTree();
```

This works because group creation itself is a lightweight operation and groups maintain their own internal entry list.
As long as the parent protocol is not read while worker threads are still writing, no data races occur.
