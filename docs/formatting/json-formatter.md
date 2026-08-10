# JSON Formatter

The `JsonProtocolFormatter` renders a protocol as a JSON document without requiring an external JSON library on the
classpath. It produces a JSON array where each element is an object representing either a message or a group. Groups
contain a nested `messages` array holding their child entries, preserving the full protocol hierarchy in a single
self-contained structure. This makes the output suitable for logging pipelines, REST responses, diagnostic endpoints,
or any consumer that expects structured data.


## Creating a Formatter

The no-argument constructor creates a formatter that produces indented (pretty-printed) output. Pass `false` to the
constructor to generate compact JSON without whitespace or line breaks.

```java
// pretty-printed output (default)
JsonProtocolFormatter<String> pretty = new JsonProtocolFormatter<>();

// compact output
JsonProtocolFormatter<String> compact = new JsonProtocolFormatter<>(false);
```

`JsonProtocolFormatter` is a plain `ProtocolFormatter`, not a `ConfiguredProtocolFormatter`. A matcher must be
supplied when invoking `format()`:

```java
String json = protocol.format(
    new JsonProtocolFormatter<>(),
    MessageMatchers.any());

// or with a matcher expression
String json = protocol.format(
    new JsonProtocolFormatter<>(false),
    "level >= 'info'");
```


## Output Structure

The top-level structure is a JSON array. Each element is an object with the following default properties:

| Property         | Type    | Description                                                     |
|------------------|---------|-----------------------------------------------------------------|
| `level`          | string  | Lowercased level name (e.g. `"info"`, `"error"`)                |
| `message`        | string  | Formatted message text                                          |
| `group`          | boolean | `true` for group entries, `false` for regular messages          |
| `group-message`  | boolean | Whether this entry is a standalone group header                 |
| `creation-time`  | string  | ISO-8601 instant of message creation                            |
| `message-id`     | number  | Internal message identifier                                     |
| `level-severity` | number  | Numeric severity value of the level                             |
| `throwable`      | boolean | Whether the message has an associated exception (messages only) |
| `tags`           | array   | Tag names (excluding `"default"`), omitted if no explicit tags  |
| `name`           | string  | Group name, present only on named groups                        |
| `messages`       | array   | Child entries (groups only)                                     |

Group objects contain all properties listed above except `throwable` and `tags`, and add a `messages` array that
holds the child entries of the group using the same object format recursively.

Given a simple protocol:

```java
Protocol<String> protocol = factory.createProtocol();

protocol
    .info()
    .forTag("audit")
    .message("Session started");

ProtocolGroup<String> group = protocol
    .createGroup()
    .setGroupMessage("Validation");
group
    .warn()
    .forTag("validation")
    .message("Missing required field");

protocol
    .info()
    .forTag("audit")
    .message("Session ended");
```

Formatting with `new JsonProtocolFormatter<>()` and `MessageMatchers.any()` produces (with properties sorted
alphabetically as the formatter uses a `TreeMap`):

```json
[
  {
    "creation-time": "2026-08-12T10:00:00.123Z",
    "group": false,
    "group-message": false,
    "level": "info",
    "level-severity": 500,
    "message": "Session started",
    "message-id": 1,
    "tags": [ "audit" ],
    "throwable": false
  },
  {
    "creation-time": "2026-08-12T10:00:00.200Z",
    "group": true,
    "group-message": true,
    "level": "warn",
    "level-severity": 700,
    "message": "Validation",
    "messages": [
      {
        "creation-time": "2026-08-12T10:00:00.250Z",
        "group": false,
        "group-message": false,
        "level": "warn",
        "level-severity": 700,
        "message": "Missing required field",
        "message-id": 3,
        "tags": [ "validation" ],
        "throwable": false
      }
    ],
    "name": "process-2"
  },
  {
    "creation-time": "2026-08-12T10:00:00.300Z",
    "group": false,
    "group-message": false,
    "level": "info",
    "level-severity": 500,
    "message": "Session ended",
    "message-id": 4,
    "tags": [ "audit" ],
    "throwable": false
  }
]
```

The compact formatter produces the same structure without indentation or line breaks.


## String Escaping

The formatter escapes all string values to produce ASCII-safe JSON output. Standard JSON escape sequences are used for
control characters (`\n`, `\t`, `\r`, `\b`, `\f`), backslash (`\\`), and double quotes (`\"`). Characters that could
interfere with HTML embedding (`<`, `>`, `&`, `=`) are escaped as Unicode sequences (`\u003c`, `\u003e`, `\u0026`,
`\u003d`). Any character outside the printable ASCII range is written as a `\u` escape. This ensures the output can be
safely embedded in HTML pages or transmitted over ASCII-only channels without additional post-processing.


## Customization

The formatter provides four protected methods that subclasses can override to tailor the JSON output. Entry values in
the decoration maps are limited to `String`, `Number`, and `Boolean`; arrays and complex objects are not converted
properly by the internal serializer.


### decorateMessageEntries

Called for every message entry after the base properties (`level`, `message`, `group`) have been placed in the map.
The default implementation adds `message-id`, `creation-time`, `group-message`, `level-severity`, and `throwable`.
Override this method to add, remove, or replace properties on message objects.

```java
// add a "time" property with a local time format
// and remove the internal message-id
public class CustomJsonFormatter
    extends JsonProtocolFormatter<String>
{
  @Override
  protected void decorateMessageEntries(
      MessageEntry<String> message,
      Map<String,Object> messageEntries)
  {
    super.decorateMessageEntries(message, messageEntries);

    messageEntries.remove("message-id");
    messageEntries.put("time",
        LocalTime.ofInstant(
            Instant.ofEpochMilli(message.getTimeMillis()),
            ZoneId.systemDefault()
        ).toString());
  }
}
```


### decorateGroupEntries

Called for every group start entry after the base properties have been placed in the map. The default implementation
adds `name` (if set), `group-message`, `creation-time`, `message-id`, and `level-severity`. Override this method to
customize group object properties in the same way as message entries.

```java
@Override
protected void decorateGroupEntries(
    GroupStartEntry<String> group,
    Map<String,Object> groupEntries)
{
  super.decorateGroupEntries(group, groupEntries);

  // add the number of child messages
  groupEntries.put("child-count", group.getMessageCount());
}
```


### extractTagNames

Controls which tags appear in the `tags` array of message objects. The default implementation returns all tag names
except the implicit `"default"` tag, or `null` if no explicit tags exist (which omits the `tags` property entirely).
Return `null` to suppress the property, or return a custom set to filter or transform tag names.

```java
@Override
protected Set<String> extractTagNames(MessageEntry<String> message)
{
  // include all tags, including "default"
  return message.getTagNames();
}
```


### levelToString

Converts a `Level` to its string representation for the `level` property. The default implementation returns the
lowercased level name. Override this to use custom level labels.

```java
@Override
protected String levelToString(Level level) {
  return level.toString().toUpperCase();
}
```


## Thread Safety

`JsonProtocolFormatter` is not thread-safe. It maintains a `StringBuilder` and internal state stack that are
reset on each `init()` call. Concurrent formatting from multiple threads requires either a new instance per
thread or external synchronization around the `format()` call. For sequential use, a single instance can be
reused safely because `init()` clears all prior state.
