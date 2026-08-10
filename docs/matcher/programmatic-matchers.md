# Programmatic Matchers

A `MessageMatcher` is the central predicate type that the Protocol library uses to decide which messages
participate in querying, formatting, and iteration. Every method that filters protocol content, from
`Protocol.matches(MessageMatcher)` to `Protocol.format(ProtocolFormatter, MessageMatcher)`, ultimately delegates to a
matcher. The `MessageMatchers` utility class provides a rich set of factory methods that cover the most common criteria,
and the `Junction` sub-interface adds fluent `and()`/`or()` composition so that complex predicates can be assembled
without writing anonymous classes.


## The MessageMatcher Interface

`MessageMatcher` declares a single matching method:

```java
<M> boolean matches(
    Level levelLimit,
    Message<M> message
);
```

The `message` parameter carries all metadata of the protocol message being evaluated: its level, tags, parameters,
throwable, message ID, and the protocol or group it belongs to. The `levelLimit` parameter represents the maximum
effective level for the current context. When a group defines a level limit, the library passes that limit to the
matcher so that a message whose stored level exceeds the limit is evaluated as though its level were capped at the
limit. This mechanism is described in more detail in the [level limit interaction](#how-level-limits-affect-matching)
section.


## The Junction Interface

`Junction` is a nested interface inside `MessageMatcher` that adds two default methods for logical composition:

```java
Junction and(MessageMatcher other);
Junction or(MessageMatcher other);
```

Every factory method in `MessageMatchers` returns a `Junction`, so composition is always available without extra
conversion steps.

```java
import static de.sayayi.lib.protocol.matcher.MessageMatchers.*;

// warnings or errors that carry a throwable
Junction matcher = isWarn().and(hasThrowable());

// messages tagged "ui" or tagged "ops"
Junction audienceMatcher = hasTag("ui").or(hasTag("ops"));
```

If application code holds a `MessageMatcher` that is not a `Junction` (for example, a custom implementation), the
`asJunction()` method wraps it in an adapter that provides `and()` and `or()`.

```java
MessageMatcher custom = (levelLimit, message) ->
    message.getMessageId().startsWith("ORD-");

// wrap to enable fluent composition
Junction ordersOnly = custom.asJunction();
Junction criticalOrders = ordersOnly.and(isError());
```


## The MessageMatchers Factory

The `MessageMatchers` class is the primary entry point for creating matchers. All methods are static and return
`Junction` instances. The following sections group them by the criterion they evaluate.


### Boolean Matchers

`any()` matches every message. `none()` matches no message. These two constants are useful as neutral elements in
dynamically composed matcher chains and serve as the identity values for disjunction and conjunction respectively.
`not(MessageMatcher)` inverts any matcher.

```java
import static de.sayayi.lib.protocol.matcher.MessageMatchers.*;

// matches everything
Junction all = any();

// matches nothing
Junction nothing = none();

// invert: all messages except warnings
// and above
Junction belowWarn = not(isWarn());
```


### Level Matchers

Level matchers test the effective severity of a message. The four convenience methods `isDebug()`, `isInfo()`,
`isWarn()`, and `isError()` each create a matcher that accepts messages whose effective level is at least as severe as
the named level. Because severity increases from `DEBUG` to `ERROR`, `isWarn()` matches both `WARN` and `ERROR`
messages, while `isDebug()` matches all four standard levels.

```java
import static de.sayayi.lib.protocol.matcher.MessageMatchers.*;

// matches WARN and ERROR
Junction warningsAndAbove = isWarn();

// matches only ERROR
Junction errorsOnly = isError();

// matches all standard levels
// (DEBUG, INFO, WARN, ERROR)
Junction everything = isDebug();
```

For custom levels or for levels only known at runtime, `is(Level)` accepts any `Level` instance. It follows the same
"at least" semantics: a message matches if its effective severity is greater than or equal to the given level.

```java
Level notice = () -> 250;

// matches messages with severity >= 250
// (notice, WARN, ERROR)
Junction noticeAndAbove = is(notice);
```

`between(Level, Level)` creates a matcher that restricts matching to a closed range. Both boundaries are inclusive.
This is the only way to select an exact level or a narrow band without including everything above it.

```java
import static de.sayayi.lib.protocol.Level.Shared.*;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.*;

// only INFO messages, excluding WARN and ERROR
Junction infoOnly = between(INFO, INFO);

// INFO and WARN, but not DEBUG or ERROR
Junction infoToWarn = between(INFO, WARN);
```


### Tag Matchers

Tag matchers inspect the set of tag names associated with a message. `hasTag(String)` matches messages that carry the
specified tag. Because every message implicitly carries the `default` tag, `hasTag("default")` is equivalent to
`any()`.

```java
import static de.sayayi.lib.protocol.matcher.MessageMatchers.*;

// messages tagged "audit"
Junction auditOnly = hasTag("audit");

// equivalent to any()
Junction allMessages = hasTag("default");
```

For multi-tag conditions, three set-oriented methods are available. `hasAnyOf(String...)` matches when the message
carries at least one of the given tags (disjunction). `hasAllOf(String...)` matches when the message carries every one
of the given tags (conjunction). `hasNoneOf(String...)` matches when the message carries none of the given tags.

```java
import static de.sayayi.lib.protocol.matcher.MessageMatchers.*;

// at least one of "ops" or "support"
Junction opsOrSupport = hasAnyOf("ops", "support");

// both "audit" and "compliance" required
Junction auditAndCompliance = hasAllOf("audit", "compliance");

// exclude internal messages
Junction noInternal = hasNoneOf("internal", "debug-trace");
```

All tag matchers are pure tag selectors, meaning `isTagSelector()` returns `true` and they can be converted to a
`TagSelector` via `asTagSelector()`. More on this conversion is covered in
[Converting Between MessageMatcher and TagSelector](#converting-between-messagematcher-and-tagselector).


### Parameter Matchers

Parameter matchers test whether a message carries a parameter with a given name. `hasParam(String)` matches if the
parameter exists in the message's parameter map, regardless of its value (including `null`). `hasParamValue(String)`
is stricter: it matches only when the parameter exists and its value is not `null`.

```java
import static de.sayayi.lib.protocol.matcher.MessageMatchers.*;

// parameter "orderId" is present (value may
// be null)
Junction hasOrderId = hasParam("orderId");

// parameter "orderId" is present and non-null
Junction hasOrderIdValue = hasParamValue("orderId");
```

A third overload, `hasParamValue(String, Object)`, matches when the parameter exists and its value equals the given
object. When `null` is passed as the expected value, the matcher matches parameters that are explicitly set to `null`
(not the same as absent).

```java
import static de.sayayi.lib.protocol.matcher.MessageMatchers.*;

// parameter "status" equals "FAILED"
Junction failedStatus = hasParamValue("status", "FAILED");

// parameter "retryCount" equals 3
Junction thirdRetry =
    hasParamValue("retryCount", 3);
```

The `hasParamValue(String, Object)` overload is only available through the programmatic API. The expression language
does not support value-based parameter matching.


### Throwable Matchers

`hasThrowable()` matches any message that has a throwable associated with it. `hasThrowable(Class)` narrows the check
to throwables that are instances of the specified class (using `instanceof` semantics, so subclasses match as well).

```java
import static de.sayayi.lib.protocol.matcher
    .MessageMatchers.*;

// any message with a throwable
Junction withException = hasThrowable();

// only messages with an IOException
// or subclass
Junction ioProblems =
    hasThrowable(java.io.IOException.class);
```


### Message ID Matcher

`hasMessage(String)` matches messages whose ID equals the given string. Message IDs are assigned by the
`MessageProcessor` during message creation and are typically derived from the message text or an external key. This
matcher is useful for pinpointing a specific message in the protocol, for example to check whether a known error
condition was recorded.

```java
import static de.sayayi.lib.protocol.matcher
    .MessageMatchers.*;

Junction specificError =
    hasMessage("ERR-CONN-TIMEOUT");
```


### Structure Matchers

Structure matchers test the position of a message within the protocol hierarchy. `inGroup()` matches any message that
resides inside a protocol group, regardless of the group's name. `inGroup(String)` narrows the match to groups whose
name equals the given string. `inGroupRegex(String)` matches groups whose name satisfies the given regular expression.
`inRoot()` matches messages that belong directly to the root protocol (i.e., not inside any group).

```java
import static de.sayayi.lib.protocol.matcher
    .MessageMatchers.*;

// messages inside any group
Junction grouped = inGroup();

// messages in the group named "validation"
Junction inValidation = inGroup("validation");

// messages in groups whose name starts
// with "batch-"
Junction batchGroups =
    inGroupRegex("batch-.*");

// messages at the root level
Junction rootOnly = inRoot();
```

`inProtocol(Protocol)` matches messages that belong to a specific `Protocol` instance. This is useful when multiple
protocol instances exist simultaneously and a matcher needs to target one of them.

```java
import static de.sayayi.lib.protocol.matcher
    .MessageMatchers.*;

Protocol<String> importProtocol =
    factory.createProtocol();
Protocol<String> exportProtocol =
    factory.createProtocol();

// only messages from the import protocol
Junction fromImport =
    inProtocol(importProtocol);
```


### TagSelector Adapter

`is(TagSelector)` converts a `TagSelector` into a `Junction`. This bridges the tag propagation API, which works with
`TagSelector` instances, and the filtering API, which works with `MessageMatcher` instances.

```java
import de.sayayi.lib.protocol.TagSelector;
import static de.sayayi.lib.protocol.matcher
    .MessageMatchers.*;

TagSelector selector =
    hasTag("ops").asTagSelector();

// use the same selector as a matcher
Junction matcher = is(selector);
boolean hasOps = protocol.matches(matcher);
```


## Composing Matchers

The `and()` and `or()` methods on `Junction` return new `Junction` instances, so arbitrary chains can be built.
Operator precedence follows the order of method calls: there is no implicit precedence between `and()` and `or()`.
To express complex boolean logic, break the expression into intermediate variables.

```java
import static de.sayayi.lib.protocol.matcher
    .MessageMatchers.*;

// critical: errors with a throwable
Junction critical =
    isError().and(hasThrowable());

// audit warnings or audit errors
Junction auditProblems =
    hasTag("audit").and(isWarn());

// errors in root, or anything in the
// "validation" group
Junction errorOrValidation =
    isError().and(inRoot())
        .or(inGroup("validation"));
```

Because `not()` is a static factory method rather than an instance method, negation is expressed by wrapping the
matcher.

```java
import static de.sayayi.lib.protocol.matcher
    .MessageMatchers.*;

// everything except debug messages
Junction noDebug = not(isDebug()).or(isInfo());

// messages without the "internal" tag
Junction publicOnly = not(hasTag("internal"));

// non-grouped error messages
Junction rootErrors =
    isError().and(not(inGroup()));
```


## Converting Between MessageMatcher and TagSelector

The `MessageMatcher` and `TagSelector` interfaces serve different purposes but overlap when the criterion is purely
tag-based. Three methods enable conversion between them.

`isTagSelector()` returns `true` when the matcher evaluates only tag information. This is the case for matchers
created by `hasTag()`, `hasAnyOf()`, `hasAllOf()`, `hasNoneOf()`, `any()`, `none()`, and logical combinations thereof.
Matchers that inspect levels, parameters, throwables, message IDs, or structure return `false`.

`asTagSelector()` converts a tag-only matcher into a `TagSelector`. It throws `MessageMatcherException` if
`isTagSelector()` returns `false`.

`TagSelector.asMessageMatcher()` performs the reverse conversion: it wraps the selector in a `MessageMatcher` that
matches any message whose tag set satisfies the selector.

```java
import de.sayayi.lib.protocol.TagSelector;
import static de.sayayi.lib.protocol.matcher
    .MessageMatchers.*;

// build a tag matcher
Junction tagMatcher =
    hasAnyOf("ops", "support");

// it is a pure tag selector
boolean pure = tagMatcher.isTagSelector();
// pure == true

// convert to TagSelector for propagation
TagSelector selector =
    tagMatcher.asTagSelector();
protocol.propagate(selector).to("escalation");

// convert back to MessageMatcher for querying
var matcher = selector.asMessageMatcher();
boolean found = protocol.matches(matcher);
```

Attempting to convert a non-tag matcher fails at runtime:

```java
import static de.sayayi.lib.protocol.matcher
    .MessageMatchers.*;

Junction levelMatcher = isWarn();

// false — involves level, not just tags
boolean pure = levelMatcher.isTagSelector();

// throws MessageMatcherException
levelMatcher.asTagSelector();
```


## How Level Limits Affect Matching

When a [protocol group](../core/groups.md) defines a level limit, the library evaluates matchers with the effective
level rather than the stored level. The effective level is the minimum of the message's own level and the group's level
limit.

Consider a group with a level limit of `WARN`. An `ERROR` message stored in that group is evaluated as though its
level were `WARN`. A matcher like `isError()` would not match that message, because the effective level is `WARN`, not
`ERROR`. Conversely, `isWarn()` would match.

```java
import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolGroup;
import static de.sayayi.lib.protocol.matcher
    .MessageMatchers.*;

var group = protocol.createGroup("capped");
group.setLevelLimit(Level.Shared.WARN);

group
    .error()
    .message("Critical failure");

// effective level is WARN, not ERROR
boolean isError = group.matches(isError());
// isError == false

boolean isWarn = group.matches(isWarn());
// isWarn == true
```

This behavior ensures that level limits are respected uniformly across querying, formatting, and iteration. When
composing matchers for use inside groups with level limits, keep in mind that `between(Level, Level)` also operates on
the effective level, not the stored level. A `between(ERROR, ERROR)` matcher would never match a message whose group
caps its level below `ERROR`.
