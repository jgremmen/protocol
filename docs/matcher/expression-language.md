# Expression Language

The expression language is a text-based DSL for defining message matchers and tag selectors without Java code. Instead
of importing factory methods and composing `Junction` chains programmatically, a matcher can be expressed as a compact
string such as `"warn and tag(ops)"` or `"any-of(ui, support)"`. The library parses the string at runtime and produces
the same `MessageMatcher` or `TagSelector` instances that the programmatic API would create.

Expression support requires the `protocol-message-matcher` module on the classpath. The module registers itself via
`ServiceLoader`, so no manual configuration is needed. If the module is absent, any attempt to parse an expression
throws a `MessageMatcherException`.


## Parsing Entry Points

Expressions are parsed through the `ProtocolMessageMatcher` interface, which `ProtocolFactory` extends. Two dedicated
methods are available on the factory:

```java
// parse a full message matcher expression
MessageMatcher matcher = factory
    .parseMessageMatcher("error and throwable");

// parse a tag selector expression
TagSelector selector = factory
    .parseTagSelector("any-of(ui, ops)");
```

Several convenience methods on `Protocol` accept expression strings directly, eliminating the need to call the factory
explicitly:

```java
// query with an expression string
boolean hasProblems =
    protocol.matches("warn and tag(ops)");

// format with an expression string
String output = protocol.format(
    formatter, "error or tag(audit)");

// tag propagation with an expression string
protocol.propagate("validation").to("ui");
```

These convenience overloads delegate to the factory internally.


## Message Matcher Expressions

A message matcher expression combines atoms and logical operators into a predicate that can evaluate any aspect of a
protocol message: its level, tags, parameters, throwable, message ID, and structural position.


### Boolean Atoms

The two simplest atoms are `any` and `none`. `any` matches every message, equivalent to `MessageMatchers.any()`.
`none` matches no message.

```java
// matches all messages
boolean all = protocol.matches("any");
// all == true (assuming protocol is non-empty)

// matches nothing
boolean nothing = protocol.matches("none");
// nothing == false
```


### Level Atoms and Functions

The four predefined level keywords `debug`, `info`, `warn`, and `error` create matchers that accept messages whose
effective level is at least as severe as the named level. This follows the same "at least" semantics as the
programmatic `isDebug()`, `isInfo()`, `isWarn()`, and `isError()` methods: `warn` matches both `WARN` and `ERROR`
messages, while `debug` matches all four standard levels.

```java
// WARN and ERROR messages
boolean hasWarnings = protocol.matches("warn");

// only ERROR messages
boolean hasErrors = protocol.matches("error");
```

The `level(name)` function extends level matching to custom levels and to levels known only by name. The argument is a
bare identifier or a quoted string that is resolved by a level resolver. The four built-in names `debug`, `info`,
`warn`, and `error` are always recognized, even without a level resolver.

```java
// equivalent to the bare "info" keyword
protocol.matches("level(info)");

// custom level (requires a level resolver configured on the parser)
protocol.matches("level(notice)");
```

The `between(level, level)` function restricts matching to a closed severity range. Both boundaries are inclusive.
This is the only way to select an exact level or a narrow band without including everything above it. Each argument
can be a predefined level keyword, a bare identifier, or a quoted string.

```java
// only INFO messages, excluding WARN and ERROR
protocol.matches("between(info, info)");

// INFO through WARN, but not DEBUG or ERROR
protocol.matches("between(info, warn)");

// custom range from TRACE to INFO
// (requires a level resolver for "trace")
protocol.matches("between(trace, info)");
```


### Level Resolver

When the expression language encounters a level name that is not one of the four built-in keywords (`debug`, `info`,
`warn`, `error`), it delegates to a level resolver to translate that name into a `Level` instance. Without a level
resolver, any unrecognized name causes a parser error. The level resolver is a `Function<String, Level>` that receives
the level name as it appears in the expression and returns the corresponding `Level`, or `null` if the name is
unknown.

The default parser instance that the `ServiceLoader` discovers has no level resolver configured, so only the four
built-in level names are available out of the box. To support custom levels in expressions, create a
`MessageMatcherParser` with a level resolver and register it on the factory.

```java
import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.matcher.parser.MessageMatcherParser;

Level trace = () -> 50;
Level notice = () -> 250;
Level critical = () -> 500;

MessageMatcherParser parser =
    new MessageMatcherParser(
        null,
        name -> switch(name) {
          case "trace" -> trace;
          case "notice" -> notice;
          case "critical" -> critical;
          default -> null;
        }
    );

factory.setMessageMatcher(parser);
```

After this setup, expressions like `level(trace)`, `level(notice)`, or `between(notice, critical)` work throughout
the factory's parsing methods and all convenience methods on `Protocol` that accept expression strings.

```java
// matches severity >= 250 (notice, WARN, ERROR,
// critical, and anything above)
protocol.matches("level(notice)");

// matches severity in range [50, 200]
// (trace through INFO)
protocol.matches("between(trace, info)");

// combine with other atoms
protocol.matches("level(critical) and tag(ops)");
```

If the resolver returns `null` for a given name and the name also does not match any of the four built-in levels, the
parser throws a `MessageMatcherParserException`. This makes typos in level names fail fast rather than silently
matching nothing.

The first argument to the `MessageMatcherParser` constructor is an optional `ClassLoader` that the parser uses to
resolve throwable class names in `throwable(qualified.ClassName)` expressions. Passing `null` uses the default class
loading behavior via `Class.forName()`.


### Tag Atoms

A bare identifier in a message matcher expression is interpreted as a tag name. Writing `ui` is equivalent to
`tag(ui)`, and both match messages that carry the tag `ui`. The `tag(name)` function accepts both identifiers and
quoted strings, which is necessary when the tag name contains characters that are not valid in identifiers (such as
dots or spaces).

```java
// bare identifier — matches tag "ops"
protocol.matches("ops");

// explicit tag function — same effect
protocol.matches("tag(ops)");

// quoted tag name with special characters
protocol.matches("tag('my.tag')");
```

Three set-oriented functions test multiple tags at once. `any-of(tag, tag, ...)` matches when the message carries at
least one of the listed tags. `all-of(tag, tag, ...)` matches when the message carries every listed tag.
`none-of(tag, tag, ...)` matches when the message carries none of the listed tags. Each function requires at least two
arguments. Tag names in the list can be identifiers or quoted strings.

```java
// at least one of "ops" or "support"
protocol.matches("any-of(ops, support)");

// both "audit" and "compliance"
protocol.matches("all-of(audit, compliance)");

// exclude internal and debug-trace
protocol.matches("none-of(internal, debug-trace)");
```


### Throwable Atoms

`throwable` matches any message that has a throwable associated with it. `throwable(qualified.ClassName)` narrows the
check to throwables that are instances of the given class. The class name must be a fully qualified Java class name
that resolves to a `Throwable` subclass on the classpath.

```java
// any message with a throwable
protocol.matches("throwable");

// only IOExceptions (including subclasses)
protocol.matches("throwable(java.io.IOException)");
```


### Parameter Atoms

`has-param('name')` matches messages that have a parameter with the given name in their parameter map, regardless of
its value (including `null`). `has-param-value('name')` is stricter: it matches only when the parameter exists and its
value is not `null`. The parameter name must be a quoted string.

```java
// parameter "orderId" exists
protocol.matches("has-param('orderId')");

// parameter "orderId" exists and is non-null
protocol.matches("has-param-value('orderId')");
```

Value-based parameter matching (comparing against a specific value) is not available in the expression language. For
that, the programmatic `hasParamValue(String, Object)` method must be used.


### Message ID Atom

`message('id')` matches messages whose ID equals the given string. The argument must be a quoted string.

```java
protocol.matches("message('ERR-CONN-TIMEOUT')");
```


### Structure Atoms

`in-group` matches any message that resides inside a protocol group. `in-group('name')` restricts the match to groups
with the given name. `in-group-regex('pattern')` matches groups whose name satisfies the given regular expression.
`in-root` matches messages that belong directly to the root protocol. Group name arguments must be quoted strings.

```java
// inside any group
protocol.matches("in-group");

// in a group named "validation"
protocol.matches("in-group('validation')");

// in groups named "batch-" followed by digits
protocol.matches("in-group-regex('batch-\\d+')");

// at the root level
protocol.matches("in-root");
```


### Logical Operators

Atoms and sub-expressions are combined with `and`, `or`, and `not`. Infix `and` binds more tightly than infix `or`.
Parentheses override precedence. `not(expr)` negates any sub-expression.

```java
// warn and tagged "ops"
protocol.matches("warn and tag(ops)");

// errors or anything with a throwable
protocol.matches("error or throwable");

// everything except debug
protocol.matches("not(debug)");

// parentheses for grouping
protocol.matches("(warn or error) and tag(audit)");
```

In addition to the infix form, `and` and `or` can be written in prefix form with a comma-separated argument list. This
is convenient when combining more than two sub-expressions, because it avoids repeated infix operators.

```java
// prefix form — equivalent to
// "error and throwable and in-root"
protocol.matches("and(error, throwable, in-root)");

// prefix or — equivalent to
// "tag(ui) or tag(ops) or tag(support)"
protocol.matches("or(tag(ui), tag(ops), tag(support))");
```

Parentheses without a preceding `not` keyword simply group the enclosed expression. When preceded by `not`, the
parenthesized expression is negated.

```java
// grouping only
protocol.matches("(warn) and tag(ops)");

// negation
protocol.matches("not(in-group) and error");
```


## Tag Selector Expressions

Tag selector expressions use a subset of the message matcher syntax. Only atoms that evaluate tag information are
allowed: `any`, `none`, bare tag names, `tag(name)`, `any-of(...)`, `all-of(...)`, and `none-of(...)`. Level,
parameter, throwable, message ID, and structure atoms are not permitted. The same logical operators `and`, `or`, and
`not` are available.

Tag selectors are primarily used as the source criterion in
[tag propagation](../core/tags.md#tag-propagation) rules.

```java
// simple tag selector for propagation
protocol.propagate("validation").to("ui");

// compound selector
protocol
    .propagate("any-of(ops, support)")
    .to("escalation");

// logical combination
protocol
    .propagate("audit and not(internal)")
    .to("compliance");
```


## String Literals

Quoted strings appear in function arguments such as `tag('name')`, `has-param('name')`, `message('id')`,
`in-group('name')`, and `in-group-regex('pattern')`. Both single-quoted and double-quoted strings are supported.

```java
// single-quoted
protocol.matches("tag('my-tag')");

// double-quoted
protocol.matches("tag(\"my-tag\")");
```

Four escape sequences are available within quoted strings:

| Sequence | Meaning                     |
|----------|-----------------------------|
| `\\`     | Literal backslash           |
| `\'`     | Literal single quote        |
| `\"`     | Literal double quote        |
| `\xHH`   | Character by hex byte value |
| `\uHHHH` | Character by Unicode point  |

Escaping is needed when the string itself contains the delimiter character or a backslash. It is also the only way to
embed characters that cannot be typed directly, such as non-ASCII symbols.

```java
// tag name containing a single quote
protocol.matches("tag('it\\'s')");

// tag name containing a backslash
protocol.matches("tag('path\\\\data')");

// Unicode escape for "système"
protocol.matches("tag('syst\\u00e8me')");

// hex escape for a dot character
protocol.matches("tag('a\\x2eb')");
// matches tag "a.b"
```


## Identifiers

Identifiers start with a letter and are followed by any combination of letters, digits, and hyphens. Letters include
ASCII letters (`a`–`z`, `A`–`Z`), the dollar sign (`$`), the underscore (`_`), and any Unicode character above
U+007F that is not a surrogate. Bare identifiers serve as tag names and as unquoted level names.

```java
// valid identifiers
protocol.matches("ops");
protocol.matches("my-tag");
protocol.matches("tag2");
protocol.matches("$special");
protocol.matches("_internal");

// NOT a valid identifier (starts with digit):
// use tag('2nd') instead
protocol.matches("tag('2nd')");
```

A hyphen can appear between letter/digit sequences but not at the start or end of an identifier. For example,
`debug-trace` is a valid identifier, while `-trace` is not.

Keywords such as `any`, `none`, `debug`, `info`, `warn`, `error`, `and`, `or`, `not`, `throwable`, `tag`, `any-of`,
`all-of`, `none-of`, `has-param`, `has-param-value`, `level`, `between`, `message`, `in-group`, `in-group-regex`, and
`in-root` are reserved. They cannot be used as bare tag names. To match a tag whose name collides with a keyword,
use the `tag('name')` function with a quoted string.

```java
// "error" is a keyword — this matches the
// ERROR level, not a tag named "error"
protocol.matches("error");

// to match a tag named "error", quote it
protocol.matches("tag('error')");
```


## Error Handling

Invalid expressions cause a `MessageMatcherException` (or its subclass `MessageMatcherParserException` for syntax
errors). The exception includes a formatted error message that indicates what went wrong and where in the expression
the problem was found.

```java
import de.sayayi.lib.protocol.exception.MessageMatcherException;

try {
  factory.parseMessageMatcher("all-of(");
} catch (MessageMatcherException ex) {
  // ex.getMessage() describes the syntax error
}
```

Common causes include unmatched parentheses, missing arguments in function calls, unrecognized characters, and using
message matcher atoms (like `warn` or `throwable`) inside a tag selector expression.

If the `protocol-message-matcher` module is not on the classpath, all parsing methods throw `MessageMatcherException`
with a message indicating that parsing is not supported. The programmatic API from `MessageMatchers` remains fully
available regardless of module presence.
