---
title: 1.6.0 -> 1.7.0
toc_depth: 2
---

# Version [1.7.0](https://github.com/jgremmen/protocol/tree/1.7.0) (2026-08-09)

## Breaking Changes

### Minimum Java Version Raised to 21

The library now requires Java 21 to compile and run. The previous version required Java 11. All modules have been
updated accordingly. Code that depends on this library must be compiled and run with JDK 21 or later.

### Removal of `MessageFormatMessageProcessor`

The class `MessageFormatMessageProcessor` has been removed entirely. Its functionality for parsing inline message
formats has been absorbed into `MessageAccessorMessageProcessor`, which uses the parser fallback mechanism when no
message code is found.

If you previously used `MessageFormatMessageProcessor.INSTANCE` to parse messages directly, switch to creating a
`MessageAccessorMessageProcessor` with parser fallback enabled:

```java
// Before (1.6.0):
var processor = MessageFormatMessageProcessor.INSTANCE;

// After (1.7.0):
var processor = new MessageAccessorMessageProcessor(
    messageSupport.getMessageAccessor(), true);
```

If you only need inline message parsing without a message accessor, there is no direct replacement within the
protocol library. Use the `message-format` library's `MessageFactory` directly and wrap the result in a custom
`MessageProcessor` implementation.

### Removal of `MessageSupportMessageProcessor`

The class `MessageSupportMessageProcessor` has been renamed to `MessageAccessorMessageProcessor`. The API is
identical; only the class name has changed. Update your imports and constructor calls:

```java
// Before (1.6.0):
var processor = new MessageSupportMessageProcessor(
    messageAccessor, true);

// After (1.7.0):
var processor = new MessageAccessorMessageProcessor(
    messageAccessor, true);
```

### Sealed `ProtocolIterator` Hierarchy

All interfaces in the `ProtocolIterator` hierarchy are now `sealed`. This means that external code can no longer
implement `DepthEntry`, `BoundedDepthEntry`, `MessageEntry`, `GroupMessageEntry`, `GroupStartEntry`,
`GroupEndEntry`, `ProtocolStart`, or `ProtocolEnd`. If you have custom implementations of these interfaces, they
will no longer compile. Use pattern matching with `switch` expressions to handle the known subtypes instead.

### `MessageFormatMessageProcessor.INSTANCE` Behavioral Change Before Removal

In a transient commit prior to removal, the `INSTANCE` field was changed from using
`MessageFactory.NO_CACHE_INSTANCE` to `MessageFactory.getSharedInstance()`. This is only relevant if you
accessed this constant via reflection or copied the pattern; the shared instance caches parsed messages.

### Dependency Changes

| Dependency                         | Scope   | Version in 1.6.0 | Version in 1.7.0 |
|------------------------------------|---------|------------------|------------------|
| `de.sayayi.lib:message-format`     | compile | [0.20,0.21)      | [0.12,0.25)      |
| `de.sayayi.lib:antlr4-runtime-ext` | compile | [0.6,0.7)        | [0.7,0.8)        |
| `org.apache.commons:commons-text`  | runtime | 1.13.1           | [1.3,1.16)       |
| `com.google.guava:guava`           | runtime | 33.4.8-jre       | 33.6.0-jre       |
| `org.owasp.encoder:encoder`        | runtime | 1.3.1            | [1.3,1.5)        |
| `org.springframework:spring-web`   | runtime | 5.3.39           | [5.3,7.1)        |

The `message-format` version range has been widened significantly and now accepts versions starting from 0.12. The
`antlr4-runtime-ext` dependency moved to the 0.7.x range, which may require updating that transitive dependency.

## New Features

### `Protocol#get(...)` Methods

Two new methods have been added to the `Protocol` interface for retrieving parameter values previously set via
`Protocol#set(String, Object)`:

```java
// Retrieve a parameter as Object
Object value = protocol.get("myParam");

// Retrieve a parameter with type checking
String name = protocol.get("name", String.class);
```

The typed variant returns `null` if no value has been set or if the stored value is not assignable to the
requested type. These methods are available on protocols and protocol groups alike.

### JSON Protocol Formatter

A new `JsonProtocolFormatter` has been added to the `protocol-core` module. It renders the protocol structure as
a JSON string without requiring any external JSON library.

```java
var formatter = new JsonProtocolFormatter<Message>();
String json = protocol.format(formatter, matcher);
```

The formatter supports both pretty-printed and compact output (controlled via a constructor boolean). Group and
message entries are emitted as JSON objects with properties for level, message text, creation time, message ID,
and severity. Tags are included as a JSON array.

The formatter is customizable through protected methods `decorateGroupEntries(...)`,
`decorateMessageEntries(...)`, `extractTagNames(...)`, and `levelToString(...)` that can be overridden to
control the JSON output structure.

## Bug Fixes

The `JsonProtocolFormatter` did not reset internal state correctly when reused across multiple `format(...)` calls.
The `nameBeforeValue` field retained stale data from the previous formatting run, causing malformed JSON output on
subsequent invocations. The `init(...)` method now explicitly clears this field.

The `HtmlEncoder.getInstance()` method was not thread-safe. Concurrent calls could trigger multiple
`ServiceLoader` lookups or return a partially constructed encoder. The singleton initialization now uses a
`ReentrantLock` with proper double-checked locking to guarantee safe publication.

Subclasses of `HtmlProtocolFormatter` that override the `init(...)` method without calling `super.init(...)` would
silently skip initialization of the base formatter state. The method now carries a `@MustBeInvokedByOverriders`
annotation, causing IDE and static analysis warnings when the super call is omitted.
