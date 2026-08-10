# Querying

Querying allows application code to inspect the contents of a protocol without formatting it. A typical use case is
conditional logic that depends on whether certain messages have been recorded: checking if validation produced any
warnings before proceeding, counting errors to decide whether a batch should be rolled back, or verifying that a group
contains at least one matching entry before rendering it in a report. The querying API is built around the
`ProtocolQueryable` interface and the `MessageMatcher` predicate that determines which messages qualify.


## The ProtocolQueryable Interface

`ProtocolQueryable` is the common query contract implemented by `Protocol`, `ProtocolGroup`, and `ProtocolEntry`. It
provides two methods: `matches(MessageMatcher)` for existence checks, and `getVisibleEntryCount(MessageMatcher)` for
counting. Because the interface sits at multiple levels of the protocol hierarchy, the same query semantics apply
whether the target is a root protocol, a nested group, or an individual entry.


## Checking for Matching Entries

`matches(MessageMatcher)` returns `true` if at least one entry within the protocol (or group) satisfies the given
matcher. The search traverses the entire hierarchy rooted at the target: if a group is nested inside the protocol and
contains a matching message, the root protocol's `matches()` call will return `true`.

```java
import static de.sayayi.lib.protocol.matcher.MessageMatchers.*;

protocol
    .info()
    .forTag("ui")
    .message("Upload complete");
protocol
    .error()
    .forTag("ops")
    .message("Disk threshold exceeded");

// at least one error?
boolean hasErrors = protocol.matches(isError());
// hasErrors == true

// any messages tagged "audit"?
boolean hasAudit = protocol.matches(hasTag("audit"));
// hasAudit == false

// combine matchers
boolean hasOpsWarn = protocol.matches(hasTag("ops").and(isWarn()));
// hasOpsWarn == true (ERROR >= WARN)

// no ui-level warnings exist
boolean hasUiWarn = protocol.matches(hasTag("ui").and(isWarn()));
// hasUiWarn == false (ui message is INFO)
```


## Counting Visible Entries

`getVisibleEntryCount(MessageMatcher)` returns the number of entries that match the given matcher and are visible
according to the group visibility rules. This count reflects what a formatter would actually render: hidden groups and
messages suppressed by visibility settings are excluded.

```java
protocol.debug().message("trace A");
protocol.info().message("step 1 done");
protocol.info().message("step 2 done");
protocol.warn().message("deprecated call");
protocol.error().message("connection lost");

// count all messages
int total = protocol.getVisibleEntryCount(any());
// total == 5

// count only info and above
int infoAndAbove = protocol.getVisibleEntryCount(isInfo());
// infoAndAbove == 4

// count only warnings
int warnings = protocol.getVisibleEntryCount(isWarn());
// warnings == 2 (warn + error)

// exact level range
int infoOnly = protocol.getVisibleEntryCount(
    between(Level.Shared.INFO, Level.Shared.INFO)
);
// infoOnly == 2
```

The count includes entries from nested groups whose visibility permits them to be seen. A group with visibility
`HIDDEN` contributes zero entries to the count. A group with visibility `FLATTEN` contributes its child entries
directly as if they had been added to the parent.

```java
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.ProtocolGroup.Visibility;

var group = protocol.createGroup("batch");
group.setGroupMessage("Batch processing");
group.setVisibility(Visibility.SHOW_HEADER_IF_NOT_EMPTY);

group.info().message("Item 1 processed");
group.warn().message("Item 2 skipped");

// the group header + 2 entries = 3 visible
int count = protocol.getVisibleEntryCount(any());
// includes the group header and its children

// hide the group
group.setVisibility(Visibility.HIDDEN);
int hidden = protocol.getVisibleEntryCount(any());
// group entries no longer counted
```


## Expression-Based Matching on Protocol

The `Protocol` interface adds a convenience overload `matches(String)` that accepts a matcher expression string
instead of a programmatic `MessageMatcher` instance. The expression is parsed by the factory's
`ProtocolMessageMatcher` (discovered via ServiceLoader when the `protocol-message-matcher` module is on the classpath).
This enables concise, readable queries without importing matcher factory methods.

```java
// expression-based: equivalent to
// matches(isWarn().or(isError()))
boolean hasProblems = protocol.matches("warn");

// tag-based expression
boolean hasUi = protocol.matches("tag(ui)");

// combined expression
boolean criticalOps = protocol.matches("error and tag(ops)");
```

If the `protocol-message-matcher` module is not on the classpath, calling `matches(String)` throws a
`MessageMatcherException`. The programmatic `matches(MessageMatcher)` overload from `ProtocolQueryable` is always
available regardless of module presence.


## How Querying Respects Group Visibility and Level Limits

Querying operations honor the group's effective visibility and level limit. When `getVisibleEntryCount` traverses into
a group, it uses the minimum of the group's level limit and the level limit inherited from the parent. This means a
message with severity `ERROR` inside a group with a level limit of `WARN` is evaluated as though its severity were
`WARN` for matching purposes.

```java
import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.ProtocolGroup.Visibility;

var group = protocol.createGroup("capped");
group.setGroupMessage("Capped group");
group.setLevelLimit(Level.Shared.WARN);
group.setVisibility(Visibility.SHOW_HEADER_IF_NOT_EMPTY);

group.error().message("Critical failure");
// stored with ERROR severity internally

// but for querying, the level limit caps it
boolean hasError = group.matches(isError());
// hasError == false (capped to WARN)

boolean hasWarn = group.matches(isWarn());
// hasWarn == true (ERROR capped to WARN)
```

Visibility settings further control what counts as "visible." A group with visibility `SHOW_HEADER_ONLY` contributes
exactly one entry (its header) to the count, regardless of how many child messages it contains. A group with visibility
`FLATTEN_ON_SINGLE_ENTRY` shows its header only when it contains more than one visible child; with a single child, the
header is suppressed and only the child entry counts.


## Combining Queries with Matchers for Conditional Logic

A common pattern in application code is to use query results to drive behavior. The boolean return of `matches()` and
the numeric return of `getVisibleEntryCount()` integrate naturally with conditional branching.

```java
// abort on errors
if (protocol.matches(isError())) 
{
  rollback();
  return;
}

// summarize warnings in a report
int warningCount = protocol.getVisibleEntryCount(isWarn());
if (warningCount > 0) 
  report.addSummary(warningCount + " warnings detected");

// conditional formatting: only produce
// output when relevant entries exist
if (protocol.matches(hasTag("audit"))) 
{
  String auditLog = protocol.format(auditFormatter, hasTag("audit"));
  writeToAuditSystem(auditLog);
}
```

Matchers can be composed using `and()` and `or()` on `Junction` instances to express complex criteria. The `not()`
factory method from `MessageMatchers` inverts any matcher.

```java
import static de.sayayi.lib.protocol.matcher.MessageMatchers.*;

// errors that carry a throwable
var criticalMatcher = isError().and(hasThrowable());
boolean hasCritical = protocol.matches(criticalMatcher);

// info messages without a specific tag
var filteredMatcher = isInfo().and(not(hasTag("internal")));
int filtered = protocol.getVisibleEntryCount(filteredMatcher);

// messages in a named group with parameters
var groupedMatcher = inGroup("validation").and(hasParam("field"));
boolean hasValidation = protocol.matches(groupedMatcher);
```
