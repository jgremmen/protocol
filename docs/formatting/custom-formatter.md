# Custom Formatters

The protocol library ships with formatters for JSON, HTML, and ASCII tree output. When none of these fits the
requirements of a particular integration, a custom formatter can be built from scratch by implementing the
`ProtocolFormatter<M,R>` interface. This page explains the contracts, available base classes, and the information
accessible from each entry type, then walks through complete examples that produce CSV, XML, and Markdown output.


## Implementing ProtocolFormatter

`ProtocolFormatter<M,R>` is the primary interface for converting a protocol into a result of type `R`. The generic
parameter `M` represents the internal message object type used by the protocol factory (commonly `String`), and `R` is
whatever the formatter produces: a `String`, a DOM tree, a byte array, or any other representation.

A minimal implementation requires three methods: `init`, `message`, and `getResult`. The remaining lifecycle methods
(`protocolStart`, `protocolEnd`, `groupStart`, `groupEnd`) have default no-op implementations and only need to be
overridden when the output format requires structural nesting or framing.

```java
// Flat CSV formatter that emits one line per message
// Result: level;timestamp;message\n per line
public class CsvProtocolFormatter
    implements ProtocolFormatter<String,String>
{
  private MessageFormatter<String> msgFmt;
  private StringBuilder csv;

  @Override
  public void init(ProtocolFactory<String> factory, 
                   MessageMatcher matcher, 
                   int estimatedGroupDepth)
  {
    msgFmt = factory.getMessageFormatter();
    csv = new StringBuilder(512);
    csv.append("level;timestamp;message\n");
  }

  @Override
  public void message(MessageEntry<String> message)
  {
    csv.append(message.getLevel())
        .append(';')
        .append(Instant.ofEpochMilli(message.getTimeMillis()))
        .append(';')
        .append(escape(msgFmt.formatMessage(message)))
        .append('\n');
  }

  @Override
  public String getResult() {
    return csv.toString();
  }

  private static String escape(String value) 
  {
    if (value.indexOf(';') >= 0 || value.indexOf('"') >= 0)
      return '"' + value.replace("\"", "\"\"") + '"';

    return value;
  }
}
```

The formatter above ignores groups entirely; group header messages still arrive through the `message` callback (with
`isGroupMessage()` returning `true`), so they appear as regular rows in the CSV output. This is valid behavior for flat
output formats.


## The init Contract

The `init` method is invoked at the start of every formatting operation, before any entries are delivered. Its primary
responsibility is to reset all internal state so the formatter can be reused across multiple `format()` calls without
output from previous runs bleeding into the current result.

The method receives three parameters. The `ProtocolFactory<M>` provides access to the `MessageFormatter<M>` that
converts the internal message representation into displayable text. The `MessageMatcher` is the matcher in effect for
this formatting pass, which is occasionally useful if the formatter needs to display or log which filter was applied.
The `estimatedGroupDepth` integer is an upper bound on the depth of nested groups in the protocol. A value of `0`
means no groups exist. Formatters can use this hint to pre-allocate arrays, indent strings, or stack structures of the
right size. The actual depth may be lower because group visibility settings and matcher filtering can suppress groups
during iteration.

```java
@Override
public void init(ProtocolFactory<String> factory, 
                 MessageMatcher matcher, 
                 int estimatedGroupDepth)
{
  // always clear accumulated output
  this.buffer = new StringBuilder(1024);
  this.msgFmt = factory.getMessageFormatter();

  // pre-allocate indentation table
  this.indents = new String[estimatedGroupDepth + 1];
  this.indents[0] = "";
  for(int i = 1; i <= estimatedGroupDepth; i++)
    this.indents[i] = "  ".repeat(i);
}
```

Because `init()` establishes a clean slate, a single formatter instance can serve sequential formatting requests
without constructing a new object each time. However, formatter instances are not thread-safe. Concurrent formatting
from multiple threads requires either separate instances or external synchronization.


## Working with MessageEntry

A `MessageEntry<M>` carries the full data of a protocol message. It extends `Protocol.Message<M>`, which provides
the level, the internal message object, the creation timestamp, parameter values, associated tags, and an optional
throwable. It also implements `BoundedDepthEntry<M>`, exposing the positional context through `getDepth()`,
`isFirst()`, and `isLast()`.

The `isGroupMessage()` method returns `true` when the entry represents a standalone group header rather than a regular
message. This happens when a group has no visible children (for example because its visibility is set to
`SHOW_HEADER_ONLY` or none of its children match the current filter). In all other respects a group message entry
behaves identically to a regular message. A formatter can choose to render it differently (for example with a distinct
CSS class or an indent) or treat it the same as any other message.

Converting the internal message object `M` into displayable text is done through the `MessageFormatter<M>` obtained
from the factory. The factory's message formatter handles parameter substitution and any locale-aware formatting that
was configured when the protocol was created.

```java
@Override
public void message(MessageEntry<String> message)
{
  String text = msgFmt.formatMessage(message);
  Level level = message.getLevel();
  long millis = message.getTimeMillis();
  Set<String> tags = message.getTagNames();
  Throwable ex = message.getThrowable();
  int depth = message.getDepth();
  boolean isGroup = message.isGroupMessage();

  // access parameter values if needed
  Map<String, Object> params = message.getParameterValues();

  // depth/position for structural output
  boolean firstAtDepth = message.isFirst();
  boolean lastAtDepth = message.isLast();
}
```


## Working with GroupStartEntry and GroupEndEntry

When a group has both a visible header message and at least one visible child entry, the formatter receives a
`groupStart` call followed by the child entries and finally a `groupEnd` call. This bracketing structure allows
formatters to emit opening and closing markup or to maintain an indentation level.

`GroupStartEntry<M>` provides the group header through `getGroupMessage()`, which returns a `GenericMessageWithLevel`
with the same accessors as a regular message (level, text, timestamp, parameters). It also provides `getName()` for
the optional group name and `getMessageCount()` for the number of direct visible children at the same depth. The
positional methods `getDepth()`, `isFirst()`, and `isLast()` behave the same way as on a `MessageEntry`.

`GroupEndEntry<M>` is minimal: it provides only `getDepth()`, which matches the depth of the corresponding
`GroupStartEntry`. It signals that all children have been delivered and the formatter should close whatever structural
element it opened.

```java
private int currentDepth = 0;

@Override
public void groupStart(GroupStartEntry<String> group)
{
  String header = msgFmt.formatMessage(group.getGroupMessage());
  String name = group.getName();
  int childCount = group.getMessageCount();
  currentDepth = group.getDepth();

  buffer
      .append(indents[currentDepth - 1])
      .append("+ ").append(header)
      .append(" (").append(childCount)
      .append(" entries)\n");
}

@Override
public void groupEnd(GroupEndEntry<String> groupEnd) {
  currentDepth = groupEnd.getDepth() - 1;
}
```

Note that `getDepth()` on a `GroupStartEntry` returns the depth of the children inside the group, not the depth of
the header itself. The header logically belongs to the parent level (one less than `getDepth()`), which is why the
indentation in the example above uses `indents[currentDepth - 1]`.


## Implementing ConfiguredProtocolFormatter

A `ConfiguredProtocolFormatter<M, R>` is a `ProtocolFormatter` that carries its own `MessageMatcher`, making it
self-contained. Instead of requiring callers to supply a matcher at format time, the formatter provides one through
`getMatcher(ProtocolFactory)`. This is useful when a formatter always operates on a specific set of levels or tags
and should not be invoked with an arbitrary filter.

```java
// Formatter that always shows errors and warnings only
public class ErrorWarningCsvFormatter
    extends CsvProtocolFormatter
    implements ConfiguredProtocolFormatter<String, String>
{
  @Override
  public MessageMatcher getMatcher(ProtocolFactory<String> factory) {
    return factory.parseMessageMatcher("level >= 'warn'");
  }
}
```

Callers invoke a configured formatter without specifying a matcher:

```java
String csv = protocol.format(new ErrorWarningCsvFormatter());
```


## Extending AbstractTreeProtocolFormatter

The `AbstractTreeProtocolFormatter<M>` base class handles the complexity of rendering a protocol as an indented tree
with box-drawing characters. It manages prefix arrays, depth tracking, and connector selection (`■──` for the root
node, `├──` for intermediate siblings, `└──` for the last sibling, and `│` for vertical continuations). Subclasses
only need to override a single method, `format(GenericMessageWithLevel<M>)`, to control what text appears after the
tree connector on each line.

The base class already implements `init`, `message`, `groupStart`, and `getResult`. It obtains the factory's
`MessageFormatter` during initialization and delegates to it in its own `format` method. This means the simplest
possible tree formatter is an empty subclass with no overrides:

```java
// Produces a tree with plain message text
public class PlainTreeFormatter
    extends AbstractTreeProtocolFormatter<String> {
}
```

Overriding `format` allows appending metadata or transforming the message label:

```java
// Tree that shows the level before each message
public class LevelPrefixedTreeFormatter
    extends AbstractTreeProtocolFormatter<String>
{
  @Override
  protected String format(GenericMessageWithLevel<String> message) {
    return "[" + message.getLevel() + "] " + super.format(message);
  }
}
```

The `AbstractTreeProtocolFormatter` always produces a `String` result. It uses an internal `StringBuilder` that is
cleared on each `init` call. Combining it with `ConfiguredProtocolFormatter` (as `TechnicalProtocolFormatter` does)
creates a self-contained tree formatter that can be passed to `Protocol.format()` without a matcher argument.


## Complete Example: XML Formatter

The following example demonstrates a complete formatter that produces well-formed XML output, handling both messages
and nested groups. It uses `protocolStart` and `protocolEnd` for the document framing, and `groupStart`/`groupEnd`
for nested elements.

```java
public class XmlProtocolFormatter
    implements ProtocolFormatter<String,String>
{
  private MessageFormatter<String> msgFmt;
  private StringBuilder xml;
  private String[] indents;

  @Override
  public void init(ProtocolFactory<String> factory, 
                   MessageMatcher matcher, 
                   int estimatedGroupDepth)
  {
    msgFmt = factory.getMessageFormatter();
    xml = new StringBuilder(1024);
    indents = new String[estimatedGroupDepth + 2];
    for(int i = 0; i < indents.length; i++)
      indents[i] = "  ".repeat(i + 1);
  }

  @Override
  public void protocolStart() {
    xml.append("<protocol>\n");
  }

  @Override
  public void protocolEnd() {
    xml.append("</protocol>\n");
  }

  @Override
  public void message(MessageEntry<String> message)
  {
    String indent = indents[message.getDepth()];
    xml.append(indent)
        .append("<message level=\"")
        .append(escapeAttr(message.getLevel().toString()))
        .append("\">");
    xml.append(escapeText(msgFmt.formatMessage(message)));
    xml.append("</message>\n");
  }

  @Override
  public void groupStart(
      GroupStartEntry<String> group)
  {
    int depth = group.getDepth();
    String indent = indents[depth - 1];
    var header = group.getGroupMessage();

    xml.append(indent)
        .append("<group level=\"")
        .append(escapeAttr(header.getLevel().toString()))
        .append("\"");

    String name = group.getName();
    if (name != null)
      xml.append(" name=\"")
          .append(escapeAttr(name))
          .append("\"");

    xml.append(" header=\"")
        .append(escapeAttr(msgFmt.formatMessage(header)))
        .append("\">\n");
  }

  @Override
  public void groupEnd(GroupEndEntry<String> end)
  {
    xml.append(indents[end.getDepth() - 1])
        .append("</group>\n");
  }

  @Override
  public String getResult() {
    return xml.toString();
  }

  private static String escapeText(String s) 
  {
    return s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;");
  }

  private static String escapeAttr(String s) {
    return escapeText(s).replace("\"", "&quot;");
  }
}
```

Given a protocol with an info message, a named group containing a warning, and a trailing error, invoking the
formatter produces:

```xml
<protocol>
  <message level="INFO">Session started</message>
  <group level="WARN" name="validation" header="Validation">
    <message level="WARN">
      Missing required field
    </message>
  </group>
  <message level="ERROR">
    Process failed unexpectedly
  </message>
</protocol>
```


## Complete Example: Markdown Formatter

This formatter produces a Markdown document with headings for groups and bullet lists for messages. It uses the depth
to determine heading levels, making it suitable for rendering in documentation tools or issue trackers.

```java
public class MarkdownProtocolFormatter
    implements ProtocolFormatter<String,String>
{
  private MessageFormatter<String> msgFmt;
  private StringBuilder md;
  private int depth;

  @Override
  public void init(ProtocolFactory<String> factory, 
                   MessageMatcher matcher, 
                   int estimatedGroupDepth)
  {
    msgFmt = factory.getMessageFormatter();
    md = new StringBuilder(512);
    depth = 0;
  }

  @Override
  public void message(MessageEntry<String> msg)
  {
    String prefix = "  ".repeat(depth) + "- ";
    String level = msg.getLevel().toString();
    String text = msgFmt.formatMessage(msg);

    md.append(prefix)
        .append("**").append(level).append("** ")
        .append(text).append('\n');
  }

  @Override
  public void groupStart(GroupStartEntry<String> group)
  {
    depth = group.getDepth();
    String heading = "#".repeat(Math.min(depth + 1, 6));
    String text = msgFmt.formatMessage(group.getGroupMessage());

    md.append('\n').append(heading)
        .append(' ').append(text)
        .append("\n\n");
  }

  @Override
  public void groupEnd(GroupEndEntry<String> groupEnd)
  {
    depth = groupEnd.getDepth() - 1;
    md.append('\n');
  }

  @Override
  public String getResult() {
    return md.toString();
  }
}
```

For a protocol with a top-level info message, a group named "Validation" containing a warning and a debug message,
and a trailing error, the output looks like:

```markdown
- **INFO** Session started

# Validation

- **WARN** Field is empty
- **DEBUG** Checking constraints

- **ERROR** Process failed
```


## Performance Considerations

Formatters that produce string output benefit from sizing the initial `StringBuilder` capacity based on expected
protocol size. The `estimatedGroupDepth` parameter in `init` is a useful indicator of protocol complexity; larger
depths often correlate with more entries overall. Allocating a generous initial capacity (512 or 1024 characters for
typical protocols, more for very deep or message-heavy protocols) avoids internal array resizing during iteration.

For formatters that build structured output (such as a DOM tree or a collection), pre-allocating collections with a
capacity derived from the group depth is similarly beneficial. However, the actual number of entries is not known until
iteration completes, so any pre-allocation is inherently an estimate.

Avoid performing expensive operations (such as file I/O or network calls) inside `message` or `groupStart` callbacks.
These methods are called once per matching entry, and a large protocol may contain thousands of entries. If the output
target is a stream or writer, buffer internally and flush in `protocolEnd` or after `getResult`.
