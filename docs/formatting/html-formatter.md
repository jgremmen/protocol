# HTML Formatter

The `HtmlProtocolFormatter` renders a protocol as nested HTML unordered lists. Messages and group headers become
`<li>` elements inside `<ul>` containers, with CSS classes indicating the severity level and nesting depth of each
entry. This formatter lives in the `protocol-html` module, which must be added as a dependency separately from
`protocol-core`.


## Module Dependency

Add the `protocol-html` artifact to the project. The module depends on `protocol-core` transitively, so only a single
dependency declaration is needed if `protocol-core` is not used directly elsewhere.

=== "Gradle (Groovy)"

    ```groovy
    dependencies {
      implementation 'de.sayayi.lib:protocol-html:1.7.0'
    }
    ```

=== "Gradle (Kotlin)"

    ```kotlin
    dependencies {
      implementation("de.sayayi.lib:protocol-html:1.7.0")
    }
    ```

=== "Maven"

    ```xml
    <dependency>
      <groupId>de.sayayi.lib</groupId>
      <artifactId>protocol-html</artifactId>
      <version>1.7.0</version>
    </dependency>
    ```


## HTML Encoding

The formatter automatically encodes all message text for safe inclusion in HTML. Encoding is handled by the
`HtmlEncoder` abstraction, which delegates to whichever HTML escaping library is available on the classpath. Exactly
one of the following libraries must be present at runtime:

- Spring Web (`org.springframework.web.util.HtmlUtils`)
- Google Guava (`com.google.common.html.HtmlEscapers`)
- Apache Commons Text (`org.apache.commons.text.StringEscapeUtils`)
- Unbescape (`org.unbescape.html.HtmlEscape`)
- OWASP Java Encoder (`org.owasp.encoder.Encode`)

The `HtmlEncoder.getInstance()` method resolves the active encoder on first use. It first consults Java's
`ServiceLoader` mechanism for a custom `HtmlEncoder` implementation. If none is registered, it probes the classpath
for the libraries listed above in the order shown and instantiates a bridge to the first one found. If no supported
library is available, an `UnsupportedOperationException` is thrown.

The resolved instance is cached as a singleton protected by a `ReentrantLock`, making the initial resolution
thread-safe. Subsequent calls to `getInstance()` return the cached encoder without locking overhead.

A custom `HtmlEncoder` implementation can be registered via `ServiceLoader` by placing a provider configuration file
at `META-INF/services/de.sayayi.lib.protocol.formatter.html.HtmlEncoder` containing the fully qualified class name
of the implementation. This takes precedence over classpath probing.

!!! warning "Security"
    Never bypass or disable HTML encoding when rendering protocol messages that may contain user-supplied input. The
    encoder prevents cross-site scripting (XSS) by escaping characters such as `<`, `>`, `&`, and `"`.


## Creating and Using the Formatter

The no-argument constructor creates a formatter using the default `HtmlEncoder` resolved from the classpath. A custom
encoder instance can be supplied explicitly through the single-argument constructor.

```java
// default encoder (classpath resolution)
HtmlProtocolFormatter<String> fmt = new HtmlProtocolFormatter<>();

// explicit encoder
HtmlProtocolFormatter<String> fmt =
    new HtmlProtocolFormatter<>(myCustomEncoder);
```

`HtmlProtocolFormatter` is a plain `ProtocolFormatter` and requires a matcher at format time:

```java
String html = protocol.format(
    new HtmlProtocolFormatter<>(),
    MessageMatchers.any());

// or with a matcher expression
String html = protocol.format(
    new HtmlProtocolFormatter<>(),
    "level >= 'info'");
```


## Output Structure

The formatter wraps the entire output in a `<div class="protocol">` element containing a top-level
`<ul class="depth-0">`. Each message becomes a `<li>` with a CSS class reflecting its level (e.g.
`level-info`, `level-error`). The message text is wrapped in a `<span class="message">` element.
Group headers also receive the `group` class on their span.

When a group has visible children, the formatter emits the header as a `<li>` followed by a nested
`<ul class="depth-N group">` containing the child entries. This nesting continues recursively for
deeply nested groups.

Given a protocol with one info message, a group with a warning, and a trailing info message:

```java
Protocol<String> protocol = factory.createProtocol();
protocol
    .info()
    .message("Started");

ProtocolGroup<String> group = protocol
    .createGroup()
    .setGroupMessage("Validation");
group
    .warn()
    .message("Field is empty");

protocol
    .info()
    .message("Done");
```

The formatter produces:

```html
<div class="protocol">
  <ul class="depth-0">
    <li class="level-info">
      <span class="message">Started</span>
    </li>
    <li class="level-warn">
      <span class="group">Validation</span>
    </li>
    <ul class="depth-1 group">
      <li class="level-warn">
        <span class="message">Field is empty</span>
      </li>
    </ul>
    <li class="level-info">
      <span class="message">Done</span>
    </li>
  </ul>
</div>
```

Standalone group headers (groups with no visible children) are rendered as regular `<li>` elements with both
`group-message` and `message` classes on the span, appearing inline with sibling messages rather than opening a
nested list.


## Customization

The formatter exposes a set of protected hook methods that subclasses can override to inject additional CSS classes
or HTML content at specific points in the output. Every hook returns either a CSS class name (or `null` for none)
or an HTML fragment (empty string for none). This design keeps the overall list structure intact while allowing
fine-grained visual customization.


### CSS Class Hooks

Each hook adds a single additional CSS class to a specific element. Returning `null` suppresses the extra class.

```java
public class StyledFormatter extends HtmlProtocolFormatter<String>
{
  @Override
  protected String protocolStartDivClass() 
  {
    // added to the outer <div>
    return "my-protocol-widget";
  }

  @Override
  protected String protocolStartUlClass() 
  {
    // added to the top-level <ul>
    return "styled-list";
  }

  @Override
  protected String messageLiClass(MessageEntry<String> message)
  {
    // added to each message <li>
    return message.isLast() ? "last-entry" : null;
  }

  @Override
  protected String messageSpanClass(MessageEntry<String> message)
  {
    // added to the message text <span>
    return null;
  }

  @Override
  protected String groupHeaderLiClass(
      GenericMessageWithLevel<String> message)
  {
    // added to the group header <li>
    return "group-header";
  }

  @Override
  protected String groupHeaderLiSpanClass(
      GenericMessageWithLevel<String> message)
  {
    // added to the group header text <span>
    return null;
  }

  @Override
  protected String groupStartUlClass(GroupStartEntry<String> group)
  {
    // added to the group's child <ul>
    return null;
  }
}
```


### HTML Prefix and Suffix Hooks

These hooks inject arbitrary HTML before or after the text span of messages and group headers. They are useful for
inserting icons, badges, timestamps, or other visual elements.

```java
public class TimestampedFormatter extends HtmlProtocolFormatter<String>
{
  @Override
  protected String messagePrefixHtml(MessageEntry<String> message)
  {
    // insert a timestamp before the message text
    return "<time>" + Instant.ofEpochMilli(
        message.getTimeMillis()) + "</time> ";
  }

  @Override
  protected String messageSuffixHtml(MessageEntry<String> message)
  {
    // nothing after the message text
    return "";
  }

  @Override
  protected String groupHeaderPrefixHtml(
      GenericMessageWithLevel<String> message)
  {
    return "<time>" + Instant.ofEpochMilli(
        message.getTimeMillis()) + "</time> ";
  }

  @Override
  protected String groupHeaderSuffixHtml(
      GenericMessageWithLevel<String> message) {
    return "";
  }
}
```


### Level-to-CSS Mapping

The `levelToHtmlClass(Level)` method converts a level to the CSS class suffix used on `<li>` elements. The default
implementation returns the lowercased level name (producing classes like `level-info`, `level-debug`). Override this
to map levels to custom class names, for example to consolidate multiple severity levels under a single visual style.

```java
@Override
protected String levelToHtmlClass(Level level) 
{
  if (Level.compare(level, Level.Shared.ERROR) >= 0)
    return "danger";

  if (Level.compare(level, Level.Shared.WARN) >= 0)
    return "warning";

  return "normal";
}
// produces: <li class="level-danger">, etc.
```


## HtmlProtocolFormatter.WithFontAwesome

The `WithFontAwesome` inner class extends `HtmlProtocolFormatter` to prefix each list item with a Font Awesome icon
selected by severity level. It automatically adds the `fa-ul` class to all `<ul>` elements and inserts
`<span class="fa-li"><i class="..."></i></span>` before every message and group header.

The constructor accepts a `Map<Level, String>` that maps severity levels to Font Awesome CSS class names. Icon
selection works by finding the closest matching level: if an exact match exists it is used, otherwise the map is
traversed in descending severity order and the first entry whose level is equal to or below the message level is
selected.

Two pre-built icon maps are provided as static constants:

| Constant                 | Font Awesome Version | Example Classes                      |
|--------------------------|----------------------|--------------------------------------|
| `FA4_LEVEL_ICON_CLASSES` | 4.x                  | `fa fa-times`, `fa fa-info-circle`   |
| `FA5_LEVEL_ICON_CLASSES` | 5.x                  | `fas fa-times`, `fas fa-info-circle` |

The default icon assignments map `ERROR` to a times (×) icon, `WARN` to an exclamation triangle, `INFO` to an
info circle, `DEBUG` to a puzzle piece, and `LOWEST` to a wrench.

```java
// Font Awesome 5 formatter
HtmlProtocolFormatter<String> faFmt =
    new HtmlProtocolFormatter.WithFontAwesome<>(
        HtmlProtocolFormatter.WithFontAwesome.FA5_LEVEL_ICON_CLASSES);

String html = protocol.format(faFmt, "level >= 'debug'");
```

Custom icon mappings can be supplied by constructing a map with the desired levels and class names:

```java
Map<Level,String> icons = new TreeMap<>(Level.SORT_DESCENDING);
icons.put(Level.Shared.ERROR, "fas fa-bomb");
icons.put(Level.Shared.WARN, "fas fa-bell");
icons.put(Level.Shared.INFO, "fas fa-check");
icons.put(Level.Shared.DEBUG, "fas fa-bug");
icons.put(Level.Shared.LOWEST, "fas fa-cog");

HtmlProtocolFormatter<String> fmt =
    new HtmlProtocolFormatter.WithFontAwesome<>(icons);
```

Include the appropriate Font Awesome stylesheet in the HTML page for the icons to render. For Font Awesome 5:

```html
<link rel="stylesheet"
      href="https://use.fontawesome.com/releases/v5.6.3/css/all.css"
      integrity="sha384-UHRtZLI+pbxtHCWp1t77Bi1L4ZtiqrqD80Kn4Z8NTSRyMA2Fd33n5dQ8lWUE00s/"
      crossorigin="anonymous">
```

The `WithFontAwesome` class can be further subclassed. Override `htmlPart(String)` to change how the icon element is
rendered, or override `getIconClassName(GenericMessageWithLevel)` to implement entirely custom icon selection logic.


## Thread Safety

`HtmlProtocolFormatter` is not thread-safe. The internal `StringBuilder` is cleared on each `init()` call, making
sequential reuse safe. Concurrent formatting from multiple threads requires separate formatter instances or external
synchronization.
