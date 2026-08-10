# Tags

Tags classify protocol messages by audience or purpose. While levels indicate severity, tags indicate who or what a
message is intended for. A message tagged `ui` might be shown to end users, while a message tagged `ops` is
directed at operations teams. A message tagged `audit` could feed into compliance logs, and `validation` might
drive a summary of business rule violations. Tags are free-form strings, so no upfront registration or configuration
is required.


## Assigning Tags to Messages

Tags are attached to a message through the `forTag(String)` and `forTags(String...)` methods on the message builder.
Both methods can be called multiple times, and each call adds to the set of tags already associated with the message.

```java
// single tag
protocol
    .warn()
    .forTag("validation")
    .message("Field 'email' is missing");

// multiple tags in one call
protocol
    .error()
    .forTags("ui", "ops")
    .message("Payment gateway timeout");

// combining forTag and forTags
protocol
    .info()
    .forTag("audit")
    .forTags("ui", "compliance")
    .message("User {0} logged in")
    .with("0", "jdoe");
```


## The Default Tag

Every message implicitly carries the tag `default`. This tag is added automatically when the message builder is
created and cannot be removed. The constant `ProtocolFactory.DEFAULT_TAG_NAME` holds its value.

The default tag ensures that every message is reachable by at least one tag, even when no explicit tag is assigned.
When matchers such as `hasTag("default")` are used, they match all messages regardless of any additional tags. The
`MessageMatchers.any()` matcher exploits this property internally: since every message has the default tag,
`hasTag("default")` and `any()` are equivalent.

```java
// no explicit tag — still has "default"
protocol
    .info()
    .message("Starting import");

// explicit tag — has both "ui" and "default"
protocol
    .info()
    .forTag("ui")
    .message("Upload complete");
```

When filtering, be aware that messages without explicit tags still match `hasTag("default")`. To select only messages
that were explicitly tagged for a specific audience, filter on that audience's tag name rather than on `default`.


## Tag Propagation

Tag propagation automates the association of additional tags based on tags that are already present on a message. A
propagation rule is defined on a protocol (or group) and states: "whenever a message carries tags matching a given
selector, also add a target tag." This is useful for ensuring that certain audiences always see messages originally
intended for a more specific audience, without requiring every call site to list all relevant tags.

Consider a scenario where every message tagged `validation` should also be visible to the UI layer. Without
propagation, every call site would need to specify both `validation` and `ui`. With a propagation rule, specifying
`validation` is enough.

```java
// define the rule: validation → ui
protocol
    .propagate(MessageMatchers.hasTag("validation").asTagSelector())
    .to("ui");

// this message will carry "validation",
// "ui", and "default"
protocol
    .warn()
    .forTag("validation")
    .message("Delivery date is in the past");
```

The rule applies to all messages added **after** it is defined. Messages that were added before the rule was registered
are not retroactively modified.


### Defining Propagation Rules

A propagation rule consists of a source criterion (a `TagSelector`) and one or more target tag names. The
`Protocol.propagate(TagSelector)` method accepts the source criterion and returns a `TargetTagBuilder` whose `to()`
method specifies the targets.

```java
import de.sayayi.lib.protocol.TagSelector;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.hasTag;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.hasAnyOf;

// single source tag, single target
protocol
    .propagate(hasTag("ops").asTagSelector())
    .to("monitoring");

// single source tag, multiple targets
protocol
    .propagate(hasTag("audit").asTagSelector())
    .to("compliance", "archive");

// compound source selector: any message tagged
// "ops" or "support" also gets "escalation"
protocol
    .propagate(hasAnyOf("ops", "support").asTagSelector())
    .to("escalation");
```

The `TagSelector` passed to `propagate()` is evaluated against the tag set of each message at the time the message is
added. If the selector matches, the target tags are merged into the message's tag set. Multiple propagation rules can
coexist on the same protocol, and they are all evaluated independently.


### Expression-Based Propagation

When the `protocol-message-matcher` module is on the classpath, propagation rules can be defined using tag selector
expressions instead of programmatic `TagSelector` instances. The `Protocol.propagate(String)` overload parses the
expression and uses the resulting selector as the source criterion.

```java
// equivalent to the programmatic form above
protocol
    .propagate("validation")
    .to("ui");

// compound expression
protocol
    .propagate("any-of(ops, support)")
    .to("escalation");
```

The expression syntax for tag selectors supports `any`, `none`, bare tag names, `tag(name)`, `any-of(...)`,
`all-of(...)`, `none-of(...)`, and the logical operators `and`, `or`, and `not`. The full grammar is documented in the
[Expression Language](../matcher/expression-language.md) section.


### Propagation Inheritance in Groups

Propagation rules are scoped to the protocol or group on which they are defined. Each protocol and group maintains its
own propagation map. When a message is added to a group, only that group's propagation rules are evaluated. Rules
defined on the parent protocol do **not** automatically apply to messages added to a child group.

If the same propagation behavior is needed across multiple groups, the rule must be defined on each group individually.

```java
// rule on the root protocol — applies to
// messages added directly to the root
protocol
    .propagate("audit")
    .to("compliance");

protocol
    .info()
    .forTag("audit")
    .message("Import started");
// carries: audit, compliance, default

// the group has its own propagation scope
ProtocolGroup<String> group = protocol.createGroup("order");

// this rule only applies within the group
group
    .propagate("validation")
    .to("ui");

group
    .warn()
    .forTag("validation")
    .message("Missing delivery date");
// carries: validation, ui, default

// the root's audit→compliance rule does NOT
// apply here because the message is added
// to the group, not to the root
group
    .info()
    .forTag("audit")
    .message("Order received");
// carries: audit, default (no compliance)
```

To make a propagation rule effective inside a group, define it on that group directly.

```java
ProtocolGroup<String> group = protocol.createGroup("order");

// replicate the root rule on the group
group
    .propagate("audit")
    .to("compliance");

group
    .info()
    .forTag("audit")
    .message("Order received");
// now carries: audit, compliance, default
```


## The TagSelector Interface

A `TagSelector` decides whether a given set of tag names satisfies a condition. Its single abstract method,
`match(Iterable<String>)`, receives the tag names of a message and returns `true` or `false`.

Tag selectors appear in two contexts. First, they serve as the source criterion in propagation rules, as described
above. Second, they can be converted into a `MessageMatcher` via `asMessageMatcher()`, making them usable in any
filtering or querying method that accepts a `MessageMatcher`.

```java
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.matcher.MessageMatcher;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.hasTag;

// obtain a TagSelector from a matcher
TagSelector selector = hasTag("ops").asTagSelector();

// use it in propagation
protocol.propagate(selector).to("monitoring");

// convert back to a MessageMatcher for queries
MessageMatcher matcher = selector.asMessageMatcher();
boolean hasOps = protocol.matches(matcher);
```

The conversion between `TagSelector` and `MessageMatcher` is bidirectional. Any `MessageMatcher` that is a pure
tag-based selector (i.e., `isTagSelector()` returns `true`) can be converted to a `TagSelector` with `asTagSelector()`.
This includes matchers created by `hasTag()`, `hasAnyOf()`, `hasAllOf()`, `hasNoneOf()`, and their logical
combinations. Matchers that involve non-tag criteria (such as levels, parameters, or throwables) are not tag selectors,
and calling `asTagSelector()` on them throws a `MessageMatcherException`.


## Tags in Filtering and Formatting

Tags become most powerful when used as filter criteria during formatting or querying. The `MessageMatchers` factory
provides `hasTag(String)`, `hasAnyOf(String...)`, `hasAllOf(String...)`, and `hasNoneOf(String...)` for programmatic
matchers. Alternatively, the expression language offers the equivalent `tag(name)`, `any-of(...)`, `all-of(...)`, and
`none-of(...)` atoms.

```java
import de.sayayi.lib.protocol.formatter.JsonProtocolFormatter;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.*;

// format only messages for the UI audience
String uiJson = protocol.format(
    new JsonProtocolFormatter<>(),
    hasTag("ui"));

// count validation messages at WARN or above
int count = protocol.getVisibleEntryCount(
    hasTag("validation").and(isWarn()));

// expression-based filtering (requires
// protocol-message-matcher module)
String opsJson = protocol.format(
    new JsonProtocolFormatter<>(),
    "any-of(ops, support) and warn");
```


## Design Patterns for Tags

Tags work best when a small, consistent set of tag names is established across the application. Defining tag names as
constants prevents typos and makes refactoring easier.

```java
public final class Tags 
{
  public static final String UI = "ui";
  public static final String OPS = "ops";
  public static final String AUDIT = "audit";
  public static final String VALIDATION = "validation";
  public static final String SUPPORT = "support";

  private Tags() {}
}
```

A common pattern in layered architectures is to assign tags that mirror the consumer hierarchy. The service layer tags
messages for `validation` and `audit`, the controller layer filters on `ui` to build user-facing responses, and
the operations dashboard filters on `ops`. Propagation rules can bridge these layers so that, for example, critical
findings automatically reach additional audiences.

```java
// any validation finding should be visible to
// end users as well
protocol
    .propagate(hasTag("validation").asTagSelector())
    .to("ui");

// support team sees everything ops sees
protocol
    .propagate(hasTag("ops").asTagSelector())
    .to("support");

protocol
    .error()
    .forTag("ops")
    .message("Replication lag exceeded 5s");
// carries: ops, support, default
```

The tag set for each message is determined at the moment the message is added to the protocol. Once committed, a
message's tags are immutable. Changes to propagation rules after a message has been added do not affect that message.
