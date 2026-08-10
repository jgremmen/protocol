# Groups

A `ProtocolGroup` is a nested scope within a protocol that organizes related messages under an optional header.
Groups give structure to protocol output. Instead of producing a flat list of dozens of messages, an import job can
create one group per order, a validation pass can create one group per checked entity, and each group carries its own
header message, visibility rules, and level limit. The result is a hierarchy that formatters render as indented trees,
nested JSON arrays, or any other structured representation.

Because `ProtocolGroup` extends `Protocol`, every operation available on a protocol is also available on a group:
adding messages, setting parameters, defining tag propagation rules, creating sub-groups, querying, and formatting.
A group is simply a protocol that lives inside another protocol.


## Creating Groups

Groups are created through the `createGroup()` method on any `Protocol` or `ProtocolGroup` instance. The method
returns a new, empty `ProtocolGroup` that is a direct child of the protocol it was called on.

```java
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.factory.StringProtocolFactory;

StringProtocolFactory factory =
    StringProtocolFactory.createJavaMessageFormatFactory();

Protocol<String> protocol = factory.createProtocol();

// unnamed group
ProtocolGroup<String> group = protocol.createGroup();

// named group (convenience overload)
ProtocolGroup<String> orderGroup = protocol.createGroup("order-4711");
```

The no-argument `createGroup()` produces a group without a name. The overload `createGroup(String)` is a convenience
shortcut that calls `createGroup().setName(name)`. Both forms return the new group immediately, ready for messages.

Once created, a group receives messages through the same builder chain used on a protocol. The group header message,
visibility, and level limit can be configured on the returned `ProtocolGroup` reference before or after messages are
added.

```java
ProtocolGroup<String> group = protocol.createGroup("validation");

group
    .info()
    .message("Checking address fields");
group
    .warn()
    .forTag("ui")
    .message("ZIP code {0} is invalid")
    .with("0", "ABCDE");
```


## Group Naming

Every group can carry an optional name that uniquely identifies it across the entire protocol hierarchy. The name is
set via `setName(String)` and retrieved via `getName()`. When no name has been assigned, `getName()` returns `null`.

```java
ProtocolGroup<String> group = protocol.createGroup();
// group.getName() == null

group.setName("shipping");
// group.getName() == "shipping"
```

The uniqueness constraint spans the full tree rooted at the top-level protocol. Attempting to assign a name that is
already in use by any other group anywhere in the hierarchy throws a `ProtocolException`. This guarantee makes names
safe to use as lookup keys.

```java
protocol.createGroup("order-100");

// throws ProtocolException: name already in use
protocol.createGroup("order-100");
```

Passing `null` or an empty string to `setName()` removes the name from the group. A group whose name has been removed
can receive a new name later, and its previous name becomes available for other groups.

```java
ProtocolGroup<String> group = protocol.createGroup("temp");

group.setName(null);
// group.getName() == null

// "temp" is now available again
ProtocolGroup<String> other = protocol.createGroup("temp");
```


## Looking Up Groups

Named groups can be retrieved from any point in the protocol hierarchy. The lookup methods search recursively through
all descendant groups starting from the protocol or group they are called on.

`getGroupByName(String)` returns an `Optional<ProtocolGroup<M>>` that is present if a group with the given name exists
among the descendants, and empty otherwise.

```java
protocol.createGroup("order-100");
protocol.createGroup("order-200");

Optional<ProtocolGroup<String>> found =
    protocol.getGroupByName("order-100");
// found.isPresent() == true

Optional<ProtocolGroup<String>> missing =
    protocol.getGroupByName("order-999");
// missing.isPresent() == false
```

`forEachGroupByRegex(String, Consumer)` applies an action to every named descendant group whose name matches the given
regular expression. This is useful for batch operations across groups that follow a naming convention.

```java
protocol.createGroup("order-100");
protocol.createGroup("order-200");
protocol.createGroup("shipment-100");

// matches "order-100" and "order-200"
protocol.forEachGroupByRegex(
    "order-.*",
    g -> g.setVisibility(
        ProtocolGroup.Visibility.FLATTEN));
```

For iterating over the direct child groups of a protocol without filtering by name, `groupIterator()` returns a
standard `Iterator<ProtocolGroup<M>>` and `groupSpliterator()` returns a `Spliterator<ProtocolGroup<M>>`. Both
traverse only the immediate children, not the full descendant tree.

```java
import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

Iterator<ProtocolGroup<String>> it = protocol.groupIterator();
while(it.hasNext())
{
  ProtocolGroup<String> g = it.next();
  // process each direct child group
}

// stream-based approach using the spliterator
Spliterator<ProtocolGroup<String>> split = protocol.groupSpliterator();
StreamSupport
    .stream(split, false)
    .filter(g -> g.getName() != null)
    .forEach(g -> System.out.println(g.getName()));
```

The spliterator reports `ORDERED`, `DISTINCT`, and `NONNULL` characteristics, making it suitable for sequential
stream processing.


## Group Header Messages

A group can carry a header message that represents the group as a whole during formatting. The header appears as the
parent node in a tree, the title in a JSON group object, or whatever representation the formatter defines for group
entries. Groups without a header message behave differently depending on their visibility setting, as described in the
next section.

The header is set with `setGroupMessage(String)`, which accepts a message string processed by the factory's
`MessageProcessor` in the same way as regular `message()` calls. The method returns a `MessageParameterBuilder` that
allows parameters to be attached to the header, and also exposes the full `ProtocolGroup` interface for continued
fluent configuration.

```java
ProtocolGroup<String> group = protocol
    .createGroup("order-4711")
    .setGroupMessage("Order {0}")
    .with("0", 4711);

// messages within the group
group.info().message("Validated");
group.info().message("Dispatched");

// tree output:
// ├──Order 4711  {level=INFO,...}
// │  │
// │  ├──Validated  {level=INFO,...}
// │  │
// │  └──Dispatched  {level=INFO,...}
```

The fluent chain returned by `setGroupMessage()` supports the same `with()` overloads as the regular message builder,
including typed variants for `boolean`, `int`, `long`, `float`, and `double`. Because the builder also implements
`ProtocolGroup`, visibility and level limit can be set directly in the same chain.

```java
ProtocolGroup<String> group = protocol
    .createGroup("batch-7")
    .setGroupMessage("Batch {0}: {1} items")
    .with("0", 7)
    .with("1", 250)
    .setVisibility(ProtocolGroup.Visibility.SHOW_HEADER_ALWAYS)
    .setLevelLimit(Level.Shared.WARN);
```

To remove a previously set header, call `removeGroupMessage()`. After removal the group has no header, and its
effective visibility may change as a result.

```java
group.removeGroupMessage();
// the group now has no header message
```

When a group header is rendered, the formatter receives a computed severity level for the header. This level equals the
highest severity found among the group's visible child messages, capped by the group's level limit. If the group
contains only `INFO` messages, the header appears at `INFO` level. If the group contains an `ERROR` message, the
header appears at `ERROR` level (or at the level limit if one is set and the limit is lower). The header itself does
not carry tags or a throwable.


## Group Visibility

The `Visibility` enum controls how a group and its entries are presented during formatting and iteration. Each group
carries a visibility setting that determines whether the group header is shown, whether the child entries are shown,
or whether the group is suppressed entirely. The default visibility for a newly created group is
`SHOW_HEADER_IF_NOT_EMPTY`.

```java
import de.sayayi.lib.protocol.ProtocolGroup.Visibility;

group.setVisibility(Visibility.FLATTEN);

Visibility v = group.getVisibility();
// v == Visibility.FLATTEN
```

The six visibility modes cover the full range of common rendering scenarios.


### SHOW_HEADER_IF_NOT_EMPTY

This is the default. The group header is shown only when the group contains at least one visible child entry for the
current matcher. If the group is empty or all its entries are filtered out by the matcher, the group disappears from
the output entirely. This mode is the safest choice for groups that may or may not end up containing messages.

```java
ProtocolGroup<String> group = protocol
    .createGroup()
    .setGroupMessage("Validation")
    .setVisibility(
        Visibility.SHOW_HEADER_IF_NOT_EMPTY);

// no messages added yet
// tree output: (nothing — the group is empty)

group
    .warn()
    .message("Field X is missing");

// tree output now includes the group:
// ├──Validation  {level=WARN,...}
// │  │
// │  └──Field X is missing  {level=WARN,...}
```


### SHOW_HEADER_ALWAYS

The group header is shown regardless of whether the group contains visible entries. An empty group still produces a
header entry in the output. This is useful when the header itself carries meaningful information and must always be
visible, such as a status line or a section title.

```java
ProtocolGroup<String> group = protocol
    .createGroup()
    .setGroupMessage("Compliance checks")
    .setVisibility(Visibility.SHOW_HEADER_ALWAYS);

// no messages added
// tree output still shows the header:
// └──Compliance checks  {level=LOWEST,...}
```


### SHOW_HEADER_ONLY

Only the group header is shown. Child entries are suppressed, even if the group contains messages that match the
current filter. This mode is useful when the group is used as a summary line and the individual entries are not
relevant for the current output.

```java
ProtocolGroup<String> group = protocol
    .createGroup()
    .setGroupMessage("3 records processed")
    .setVisibility(Visibility.SHOW_HEADER_ONLY);

group.debug().message("Record 1 OK");
group.debug().message("Record 2 OK");
group.debug().message("Record 3 OK");

// tree output:
// └──3 records processed  {level=DEBUG,...}
// (child messages are suppressed)
```

The `isShowEntries()` method on the `Visibility` enum returns `false` for this mode, indicating that formatters should
not expect child entries.


### FLATTEN

The group header is not shown. All child entries are merged into the parent protocol as if they had been added directly
to the parent. This mode is useful when the group serves only as an organizational container in code but should not
create a visible hierarchy in the output.

```java
protocol
    .info()
    .message("Step 1 complete");

ProtocolGroup<String> group = protocol
    .createGroup()
    .setGroupMessage("Internal grouping")
    .setVisibility(Visibility.FLATTEN);

group.info().message("Step 2 complete");
group.warn().message("Step 3 had warnings");

protocol
    .info()
    .message("Step 4 complete");

// tree output (flat, no group nesting):
// ■──Step 1 complete  {level=INFO,...}
// │
// ├──Step 2 complete  {level=INFO,...}
// │
// ├──Step 3 had warnings  {level=WARN,...}
// │
// └──Step 4 complete  {level=INFO,...}
```


### FLATTEN_ON_SINGLE_ENTRY

This mode is a conditional hybrid. If the group contains more than one visible entry, it behaves like
`SHOW_HEADER_IF_NOT_EMPTY` and shows the header with all entries nested below it. If the group contains exactly one
visible entry, it flattens that entry into the parent without showing the header. If the group contains no visible
entries, the group does not appear in the output.

This mode is practical for groups where the header adds value only when there are multiple entries. A group that ended
up collecting just a single message can present that message directly without the overhead of a nested structure.

```java
ProtocolGroup<String> singleGroup = protocol
    .createGroup()
    .setGroupMessage("Order {0}")
    .with("0", 100)
    .setVisibility(Visibility.FLATTEN_ON_SINGLE_ENTRY);

singleGroup
    .info()
    .message("Order accepted");

// only 1 entry → flattened:
// └──Order accepted  {level=INFO,...}

ProtocolGroup<String> multiGroup = protocol
    .createGroup()
    .setGroupMessage("Order {0}")
    .with("0", 200)
    .setVisibility(
        Visibility.FLATTEN_ON_SINGLE_ENTRY);

multiGroup
    .info()
    .message("Validated");
multiGroup
    .warn()
    .message("Backordered");

// 2 entries → header shown:
// └──Order 200  {level=WARN,...}
//    │
//    ├──Validated  {level=INFO,...}
//    │
//    └──Backordered  {level=WARN,...}
```


### HIDDEN

The group and all its entries are completely suppressed. Nothing is shown in the output, regardless of the group's
content. This is useful for temporarily disabling a group without removing it from the protocol.

```java
ProtocolGroup<String> group = protocol
    .createGroup()
    .setGroupMessage("Debug internals")
    .setVisibility(Visibility.HIDDEN);

group.debug().message("Cache hit ratio: 0.92");
group.debug().message("Thread pool size: 8");

// tree output: (nothing)
```

Like `SHOW_HEADER_ONLY`, the `isShowEntries()` method returns `false` for `HIDDEN`.


## Effective Visibility

The visibility setting alone does not fully determine how a group renders. When a group has no header message, the
modes that depend on a header to be present are automatically transformed. This transformed result is the
**effective visibility**, retrieved via `getEffectiveVisibility()`.

The transformation rules are:

| Configured Visibility      | Effective Visibility (No Header) |
|----------------------------|----------------------------------|
| `SHOW_HEADER_ALWAYS`       | `FLATTEN`                        |
| `SHOW_HEADER_IF_NOT_EMPTY` | `FLATTEN`                        |
| `SHOW_HEADER_ONLY`         | `HIDDEN`                         |
| `FLATTEN_ON_SINGLE_ENTRY`  | `FLATTEN`                        |
| `FLATTEN`                  | `FLATTEN`                        |
| `HIDDEN`                   | `HIDDEN`                         |

The logic follows a simple principle. If the visibility mode requires a header to be shown but no header exists, the
group falls back to `FLATTEN`, merging its entries into the parent. If the mode shows only the header and nothing else,
a missing header means there is nothing to show, so the group becomes `HIDDEN`.

When a group does have a header message, `getEffectiveVisibility()` returns the configured visibility unchanged.

```java
ProtocolGroup<String> group = protocol.createGroup();

// no header message set
group.setVisibility(Visibility.SHOW_HEADER_ALWAYS);

Visibility configured = group.getVisibility();
// configured == SHOW_HEADER_ALWAYS

Visibility effective = group.getEffectiveVisibility();
// effective == FLATTEN (no header present)

// now set a header
group.setGroupMessage("Batch results");

effective = group.getEffectiveVisibility();
// effective == SHOW_HEADER_ALWAYS
```

This mechanism ensures that groups without a header never produce empty header entries in the output. A group created
as an organizational container without calling `setGroupMessage()` automatically flattens its entries into the parent,
regardless of the configured visibility.


## Level Limits

A level limit caps the severity of all messages within a group during iteration and formatting. The default limit is
`Level.Shared.HIGHEST`, which effectively means no capping. Setting a lower limit causes any message whose severity
exceeds the limit to appear at the limit's severity in the output.

```java
import de.sayayi.lib.protocol.Level;

ProtocolGroup<String> group = protocol
    .createGroup()
    .setGroupMessage("Batch results")
    .setLevelLimit(Level.Shared.WARN);

group
    .info()
    .message("Record 1 processed");
group
    .error()
    .message("Record 2 failed");
group
    .warn()
    .message("Record 3 suspicious");

// during formatting, "Record 2 failed"
// appears at WARN severity instead of ERROR
// because the level limit caps it
```

The capping is transparent: the original severity stored on the message object is not modified. Only the iterator and
formatter infrastructure applies the limit. Direct access to the message through the `Protocol.Message` interface
still returns the original `ERROR` level.

```java
Level limit = group.getLevelLimit();
// limit == Level.Shared.WARN
```

Level limits are particularly useful when a group represents a subsystem whose findings should not elevate the overall
protocol severity beyond a certain threshold. For example, an optional validation step might use a level limit of
`INFO` so that even if it internally records warnings, those warnings do not influence the parent protocol's perceived
severity.

Because the group header level is computed as the maximum severity of its children (capped by the limit), a level
limit also affects the severity displayed on the group header itself.


## Nested Groups

Groups can be nested to arbitrary depth by calling `createGroup()` on an existing group. Each nested group is a fully
independent `ProtocolGroup` with its own header message, visibility, level limit, parameters, and tag propagation
rules.

```java
Protocol<String> protocol = factory.createProtocol();

ProtocolGroup<String> importGroup = protocol
    .createGroup("import")
    .setGroupMessage("Import run");

ProtocolGroup<String> orderGroup = importGroup
    .createGroup("order-500")
    .setGroupMessage("Order {0}")
    .with("0", 500);

ProtocolGroup<String> lineGroup = orderGroup
    .createGroup("line-1")
    .setGroupMessage("Line item {0}")
    .with("0", 1);

lineGroup
    .warn()
    .message("Quantity exceeds stock");

// tree output:
// ■──Import run  {level=WARN,...}
// │  │
// │  └──Order 500  {level=WARN,...}
// │     │
// │     └──Line item 1  {level=WARN,...}
// │        │
// │        └──Quantity exceeds stock  ...
```

`getParent()` returns the immediate parent protocol of a group. For a top-level group created directly on the root
protocol, the parent is the root protocol itself. For a nested group, the parent is the group it was created on.
`getRootProtocol()` walks up the chain and always returns the top-level protocol, regardless of nesting depth.

```java
Protocol<String> root = lineGroup.getRootProtocol();
// root == protocol

Protocol<String> parent = lineGroup.getParent();
// parent == orderGroup
```

Naming and lookup operate across the full hierarchy. A name assigned to a deeply nested group must still be unique
across all groups in the tree, and `getGroupByName()` called on the root protocol finds groups at any depth.

```java
Optional<ProtocolGroup<String>> found =
    protocol.getGroupByName("line-1");
// found.get() == lineGroup
```

Level limits compound across nested groups. If a parent group has a limit of `WARN` and a child group has a limit of
`INFO`, messages in the child group are capped at `INFO` because the child's limit is more restrictive. The effective
cap for any message is the minimum of all level limits along its path from the root.

```java
ProtocolGroup<String> outer = protocol
    .createGroup()
    .setGroupMessage("Outer")
    .setLevelLimit(Level.Shared.WARN);

ProtocolGroup<String> inner = outer
    .createGroup()
    .setGroupMessage("Inner")
    .setLevelLimit(Level.Shared.INFO);

inner
    .error()
    .message("Critical failure");

// the message appears at INFO severity
// (the more restrictive of WARN and INFO)
```


## Groups in Iteration and Formatting

When a protocol is iterated or formatted, groups produce structural entries that formatters use to build hierarchical
output. The `ProtocolIterator` yields a depth-first sequence of typed `DepthEntry` instances, and groups contribute
three entry types to this sequence.

A `GroupStartEntry` is emitted when a group has a visible header and at least one visible child entry. It carries the
group header message, the computed severity level, the group name, and the count of visible child messages at the
group's depth. The entries that follow belong to the group until a matching `GroupEndEntry` is emitted.

A `GroupMessageEntry` is emitted when a group has a visible header but no visible child entries. This happens with
`SHOW_HEADER_ALWAYS` when the group is empty, or with `SHOW_HEADER_ONLY` regardless of content. The entry is treated
like a regular message entry from the formatter's perspective, but it carries the group name and reports
`isGroupMessage()` as `true`.

A `GroupEndEntry` closes a group that was opened by a `GroupStartEntry`. It carries only the depth, and formatters
typically use it to close structural elements like closing tags, brackets, or indentation levels.

Each group increases the depth counter by one. Messages directly inside a group have a depth one level higher than the
group header. This depth information allows formatters to render indentation, tree connectors, or nested HTML elements.

The visibility and level limit settings directly influence which of these entries are emitted. A `FLATTEN` group
produces no structural entries at all; its children appear at the same depth as if they were part of the parent. A
`HIDDEN` group produces no entries whatsoever. The `isHeaderVisible(MessageMatcher)` method on `ProtocolGroup` can be
used to check programmatically whether a group header would be visible for a given matcher.

```java
import de.sayayi.lib.protocol.ProtocolIterator;
import de.sayayi.lib.protocol.ProtocolIterator.DepthEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupStartEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupEndEntry;
import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.any;

ProtocolIterator<String> it = protocol.iterator(any());

while(it.hasNext()) 
{
  DepthEntry<String> entry = it.next();

  switch(entry) 
  {
    case GroupStartEntry<String> gs ->
      System.out.println(
          "  ".repeat(gs.getDepth()) +
          "GROUP: " + gs.getGroupMessage().getMessage() +
          " (" + gs.getMessageCount() + " entries)");

    case GroupEndEntry<String> ge ->
      System.out.println(
          "  ".repeat(ge.getDepth()) +
          "END GROUP");

    case MessageEntry<String> me ->
      System.out.println(
          "  ".repeat(me.getDepth()) +
          me.getLevel() + ": " + me.getMessage());

    default -> {}
  }
}
```

The `format()` method on `Protocol` handles this iteration internally and passes each entry to the supplied
`ProtocolFormatter`. Details on the formatter contract and built-in formatters are covered in the
[Formatting](../formatting/protocol-formatter.md) and [Iterating](../formatting/iterating.md) sections.
