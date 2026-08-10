# ToStringMessageFormatter

`ToStringMessageFormatter` invokes `toString()` on the internal message object. It performs no parameter substitution
at all. The companion constant `ToStringMessageFormatter.IDENTITY` is a specialized variant for `M = String` that
returns the message string directly without calling `toString()`.

This formatter is useful when messages are pre-formatted strings that need no further processing, or when parameter
values are embedded in the message text at creation time rather than at formatting time.

```java
import de.sayayi.lib.protocol.message.formatter.ToStringMessageFormatter;

// IDENTITY returns the String as-is
var formatter = ToStringMessageFormatter.IDENTITY;
```
