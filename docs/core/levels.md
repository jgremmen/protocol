# Levels

Every message in a protocol carries a severity level. The level indicates how significant the message is, ranging from
low-priority diagnostics to critical errors. Levels are the primary axis for filtering protocol output: a consumer
interested only in warnings and errors can exclude debug and info messages by applying a level-based matcher.


## The Level Interface

`Level` is a functional interface with a single method, `severity()`, that returns an `int`. A higher severity number
indicates a more severe condition. Because `Level` is a functional interface, custom levels can be created as lambda
expressions or method references wherever the API expects a `Level` argument.

```java
import de.sayayi.lib.protocol.Level;

// custom level as a lambda
Level notice = () -> 250;

// severity sits between INFO (200) and WARN (300)
int s = notice.severity();
// s == 250
```


## Predefined Levels

The `Level.Shared` enum provides six predefined levels that cover the most common severity scenarios.

| Constant  | Severity            |
|-----------|---------------------|
| `LOWEST`  | `Integer.MIN_VALUE` |
| `DEBUG`   | 100                 |
| `INFO`    | 200                 |
| `WARN`    | 300                 |
| `ERROR`   | 400                 |
| `HIGHEST` | `Integer.MAX_VALUE` |

`DEBUG`, `INFO`, `WARN`, and `ERROR` mirror the traditional log-level hierarchy. `LOWEST` and `HIGHEST` are sentinel
values that represent the absolute boundaries of the severity range. They are not intended for tagging individual
messages but serve as limits in queries and filters. For example, passing `LOWEST` as a level boundary to `between()`
includes everything from the lowest possible severity upward.


## Convenience Methods on Protocol

The `Protocol` interface maps each of the four standard levels to a dedicated builder method, so the `Level.Shared`
constants rarely need to be referenced explicitly.

```java
// these two calls are identical
protocol
    .debug()
    .message("trace output");
protocol
    .add(Level.Shared.DEBUG)
    .message("trace output");

// same equivalence for info, warn, error
protocol.info().message("step completed");
protocol.warn().message("deprecated call");
protocol.error().message("connection lost");
```

For error messages that carry an exception, the `error(Throwable)` shorthand combines the level selection with
`withThrowable()` in a single call.

```java
try {
  connectToDatabase();
} catch(SQLException ex) {
  // shorthand
  protocol
      .error(ex)
      .message("Database unreachable");

  // equivalent longhand
  protocol
      .add(Level.Shared.ERROR)
      .withThrowable(ex)
      .message("Database unreachable");
}
```


## Custom Levels

When the four standard levels are not granular enough, custom levels fill the gaps. A custom level is any `Level`
implementation that returns a severity value from `severity()`. Because `Level` is a functional interface, a simple
lambda is sufficient.

```java
Level notice = () -> 250;
Level critical = () -> 500;
Level trace = () -> 50;

protocol
    .add(notice)
    .message("Cache refreshed");

protocol
    .add(critical)
    .forTag("ops")
    .message("Disk space below threshold");

protocol
    .add(trace)
    .message("Entering method validateOrder");
```

Custom levels integrate seamlessly with the rest of the API. They can be used in matchers, compared with the static
utility methods, and sorted alongside the predefined levels. The only requirement is that the severity value is
consistent: the same `Level` instance (or instances with the same severity) should always return the same value.

For a reusable set of custom levels, consider defining them as an enum that implements `Level`, similar to how
`Level.Shared` is defined. The [User-Defined Levels](../configuration/user-defined-levels.md) section covers this
pattern.


## Comparing Levels

The `Level` interface provides static utility methods for comparing two levels by their severity values.

`Level.compare(l1, l2)` returns `-1` if the first level is less severe, `1` if it is more severe, and `0` if both have
the same severity. `Level.equals(l1, l2)` returns `true` when both levels share the same severity value, regardless of
whether they are the same object or even the same class. `Level.max(l1, l2)` and `Level.min(l1, l2)` return the level
with the higher or lower severity respectively. When both severities are equal, the first argument is returned.

```java
import static de.sayayi.lib.protocol.Level.*;

Level notice = () -> 250;

// compare returns -1 (INFO < notice)
int cmp = compare(Shared.INFO, notice);

// equals compares by severity, not identity
boolean same = equals(Shared.INFO, () -> 200);
// same == true

// min / max
Level lower = min(Shared.WARN, Shared.ERROR);
// lower == Shared.WARN

Level higher = max(notice, Shared.DEBUG);
// higher == notice (250 > 100)
```


## Sorting Levels

Two predefined `Comparator<Level>` constants support sorting collections of levels.

`Level.SORT_ASCENDING` orders from lowest to highest severity. `Level.SORT_DESCENDING` orders from highest to lowest.
Both comparators use `Level::severity` as the sort key.

```java
import de.sayayi.lib.protocol.Level;
import java.util.List;
import java.util.ArrayList;

Level notice = () -> 250;

var levels = new ArrayList<>(List.of(
    Level.Shared.ERROR,
    notice,
    Level.Shared.DEBUG,
    Level.Shared.WARN
));

levels.sort(Level.SORT_ASCENDING);
// order: DEBUG (100), notice (250),
//        WARN (300), ERROR (400)

levels.sort(Level.SORT_DESCENDING);
// order: ERROR (400), WARN (300),
//        notice (250), DEBUG (100)
```


## Levels in Filtering and Formatting

Levels are a key criterion when filtering protocol entries through matchers. The `MessageMatchers` factory class
provides `isDebug()`, `isInfo()`, `isWarn()`, `isError()`, `is(Level)`, and `between(Level, Level)` for building
level-based filters. The single-level matchers use "at least" semantics: `isWarn()` matches any message whose effective
severity is greater than or equal to WARN (300), which includes both WARN and ERROR messages. To match a specific range
instead, use `between()`.

```java
import static de.sayayi.lib.protocol.matcher
    .MessageMatchers.*;

// count messages at WARN level or above
// (matches WARN, ERROR, and any custom level with severity >= 300)
int warnOrAbove = protocol.getVisibleEntryCount(isWarn());

// match only a specific severity range
// (between is inclusive on both ends)
// matches INFO (200) and WARN (300) but
// excludes DEBUG (100) and ERROR (400)
int midRange = protocol.getVisibleEntryCount(
    between(
        Level.Shared.INFO,
        Level.Shared.WARN));

// match messages at or above a custom level
Level notice = () -> 250;
// is(notice) matches severity >= 250,
// which includes notice, WARN and ERROR
boolean hasNotice = protocol.matches(is(notice));
```

When a `ProtocolGroup` has a level limit set via `setLevelLimit(Level)`, messages within that group are capped to the
limit during iteration and formatting. A message with severity higher than the limit appears as though it has the
limit's severity. The original severity is preserved on the message object itself and is only adjusted for display
purposes. Level limits are covered in the [Groups](groups.md) section.
