# Protocol Formatter

The `ProtocolFormatter<M,R>` interface defines the contract for transforming a protocol into a result of type `R`.
This is the primary extension point for producing custom output representations such as plain text, HTML, XML, data
structures, or anything else a downstream consumer requires. A formatter receives a sequence of typed entries from a
`ProtocolIterator` and assembles them into the desired output format one entry at a time.


## The Formatting Lifecycle

Every formatting operation follows a strict, well-defined sequence of method calls. The protocol infrastructure
drives this sequence automatically; the formatter implementation simply responds to each callback in order:

1. `init(factory, matcher, estimatedGroupDepth)` initializes (or re-initializes) the formatter before any entries
   are processed. The method receives the `ProtocolFactory` that created the protocol, the `MessageMatcher` in
   effect for this formatting pass, and an integer representing the estimated maximum nesting depth of protocol
   groups. This depth is always an upper bound; the actual depth depends on which groups match the current level,
   tags, and visibility configuration.

2. `protocolStart()` signals that iteration over the protocol entries is about to begin. This is the place to emit
   opening markup, allocate output buffers, or perform any setup that depends on the protocol as a whole rather
   than individual entries.

3. For each matching entry in depth-first order, exactly one of the following is called:

    - `message(MessageEntry)` for a regular message or a standalone group header whose group has no visible
      children.
    - `groupStart(GroupStartEntry)` when entering a group that has both a visible header and at least one visible
      child entry.
    - `groupEnd(GroupEndEntry)` when leaving such a group.

4. `protocolEnd()` signals that all entries have been processed. This is the place to emit closing markup, flush
   buffers, or finalize computed results.

5. `getResult()` returns the assembled output of type `R`.

The `protocolStart()`, `protocolEnd()`, `groupStart()`, and `groupEnd()` methods have default no-op implementations
in the interface. Formatters that produce flat output (no structural nesting) can ignore those callbacks entirely and
only implement `init`, `message`, and `getResult`.

```java
// Minimal formatter that collects message texts
// into a newline-separated string
public class SimpleTextFormatter 
    implements ProtocolFormatter<String,String> 
{
  private MessageFormatter<String> msgFmt;
  private StringBuilder sb;

  @Override
  public void init(ProtocolFactory<String> factory, 
                   MessageMatcher matcher, 
                   int estimatedGroupDepth) 
  {
    msgFmt = factory.getMessageFormatter();
    sb = new StringBuilder();
  }

  @Override
  public void message(MessageEntry<String> message) 
  {
    if (!sb.isEmpty())
      sb.append('\n');
    
    sb.append(msgFmt.formatMessage(message));
  }

  @Override
  public String getResult() {
    return sb.toString();
  }
}
```


## Invoking Formatting

There are several equivalent ways to trigger the formatting process. All of them invoke the same lifecycle described
above; the difference is only in how the matcher is supplied.

The most explicit form passes both the formatter and a `MessageMatcher` instance:

```java
// format with a programmatic matcher
String result = protocol.format(
    new SimpleTextFormatter(),
    MessageMatchers.hasTag("ui"));
```

A string-based matcher expression can be used instead of a `MessageMatcher` object. The factory parses the expression
into a matcher before formatting begins:

```java
// format using a matcher expression
String result = protocol.format(
    new SimpleTextFormatter(),
    "tag('ui')");
```

The invocation can also start from the formatter side. `ProtocolFormatter` provides a default `format` method that
delegates back to `Protocol.format`:

```java
// formatter-side invocation
SimpleTextFormatter fmt = new SimpleTextFormatter();
String result = fmt.format(protocol, MessageMatchers.hasTag("ui"));
```

All three approaches are semantically identical.


## ConfiguredProtocolFormatter

A `ConfiguredProtocolFormatter<M, R>` is a `ProtocolFormatter` that carries its own matching logic. Instead of
requiring the caller to supply a matcher at format time, the formatter provides one through its
`getMatcher(ProtocolFactory)` method. This makes the formatter self-contained and is particularly useful for
formatters that always operate on a fixed set of tags or levels.

```java
// formatting with a configured formatter
// (no matcher argument needed)
String tree = protocol.format(TechnicalProtocolFormatter.getInstance());
```

The `ConfiguredProtocolFormatter` interface also adds a convenience `format(Protocol)` default method that calls
`protocol.format(this)`, making the invocation symmetrical from either side.

Implementing `ConfiguredProtocolFormatter` is straightforward. Provide a `getMatcher` method that returns the
appropriate matcher for the formatter's purpose:

```java
public class UiOnlyFormatter
    implements ConfiguredProtocolFormatter<String,String> 
{
  // ... init, message, getResult ...

  @Override
  public MessageMatcher getMatcher(ProtocolFactory<String> factory) {
    return factory.parseMessageMatcher("tag('ui')");
  }
}
```


## Entry Types

The entries delivered to a formatter carry different information depending on their type.


### MessageEntry

A `MessageEntry<M>` represents a single protocol message. It extends `Protocol.Message<M>`, giving access to the
full message data: level, internal message object, tags, throwable, creation timestamp, and parameter values. It also
implements `BoundedDepthEntry<M>`, which provides structural context through `getDepth()`, `isFirst()`, and
`isLast()`.

The `isGroupMessage()` method distinguishes regular messages from standalone group headers. A group header appears as
a `MessageEntry` (with `isGroupMessage()` returning `true`) when the group has no visible child entries, for example
because visibility is set to `SHOW_HEADER_ONLY` or no children match the current matcher. In all other respects the
entry behaves identically to a regular message.

```java
@Override
public void message(MessageEntry<String> message) 
{
  Level level = message.getLevel();
  String text = msgFmt.formatMessage(message);
  Set<String> tags = message.getTagNames();
  Throwable ex = message.getThrowable();
  int depth = message.getDepth();
  boolean first = message.isFirst();
  boolean last = message.isLast();
  boolean groupMsg = message.isGroupMessage();

  // use these values to build the output
}
```


### GroupStartEntry

A `GroupStartEntry<M>` marks the beginning of a group that has both a visible header message and at least one visible
child entry. It extends `BoundedDepthEntry<M>` (providing `getDepth()`, `isFirst()`, `isLast()`) and
`Protocol.Group<M>` (providing `getName()` and `getGroupMessage()`).

The `getGroupMessage()` method returns a `GenericMessageWithLevel<M>` containing the group header's level, message
content, timestamp, and parameter values. The `getMessageCount()` method returns the number of visible direct child
messages at the same depth, which is always at least 1.

```java
@Override
public void groupStart(GroupStartEntry<String> group)
{
  String header = msgFmt.formatMessage(group.getGroupMessage());
  int childCount = group.getMessageCount();
  int depth = group.getDepth();

  // emit opening structure
  // (e.g. open a list, increase indent)
}
```


### GroupEndEntry

A `GroupEndEntry<M>` signals the end of the most recently opened group. It provides only `getDepth()`, which matches
the depth of the corresponding `GroupStartEntry`. Use this callback to emit closing markup or decrease indentation.

```java
@Override
public void groupEnd(GroupEndEntry<String> groupEnd) 
{
  int depth = groupEnd.getDepth();

  // emit closing structure
}
```


## Depth Semantics

The depth value on each entry reflects its position in the protocol hierarchy. Root-level messages and groups have a
depth of 0. Each `GroupStartEntry` increases the depth for its child entries by one; the corresponding `GroupEndEntry`
returns to the previous depth. This allows formatters to produce indented or nested output without maintaining their
own depth counter.

Consider a protocol with one top-level message, a group containing two messages, and another top-level message. The
entries delivered to the formatter would have the following depths:

| Entry              | Depth |
|--------------------|-------|
| message "A"        | 0     |
| groupStart "Order" | 1     |
| message "B"        | 1     |
| message "C"        | 1     |
| groupEnd           | 1     |
| message "D"        | 0     |


## Positional Flags

`MessageEntry` and `GroupStartEntry` both implement `BoundedDepthEntry`, which exposes `isFirst()` and `isLast()`.
These flags indicate whether the entry is the first or last sibling at its depth level. They are useful for formatters
that need to emit separators between entries but not before the first or after the last. A single entry that is both
first and last (the only sibling at its depth) will have both flags set to `true`.

```java
@Override
public void message(MessageEntry<String> message) 
{
  if (!message.isFirst())
    sb.append(",\n");  // separator before all but first

  sb.append(msgFmt.formatMessage(message));

  if (message.isLast())
    sb.append('\n');   // trailing newline after last
}
```


## Reusability

A `ProtocolFormatter` instance can be reused across multiple `format()` calls. The `init()` method is invoked at the
start of every formatting operation and must reset all internal state accumulated from previous invocations. Failing
to do so causes output from earlier runs to bleed into later results.

The `init()` method receives an `estimatedGroupDepth` parameter that represents the upper bound on group nesting. A
value of 0 means the protocol contains no groups. Formatters can use this hint to pre-allocate data structures (such
as prefix arrays or indent strings) of the appropriate size, avoiding resize overhead during iteration.

```java
@Override
public void init(ProtocolFactory<String> factory, 
                 MessageMatcher matcher, 
                 int estimatedGroupDepth)
{
  // reset all state for a clean run
  this.msgFmt = factory.getMessageFormatter();
  this.sb = new StringBuilder(256);
  this.indents = new String[estimatedGroupDepth + 1];
  this.indents[0] = "";
}
```

Because `init()` establishes a clean slate, a single formatter instance can safely serve sequential formatting
requests. However, `ProtocolFormatter` instances are not thread-safe. Concurrent formatting from multiple threads
requires either separate instances or external synchronization.
