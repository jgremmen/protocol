# JavaStringFormatFormatter

`JavaStringFormatFormatter` formats messages using `String.format(Locale, String, Object...)`. Like
`JavaMessageFormatFormatter`, parameters are indexed numerically from `0` to `31`. The message string uses
`String.format` placeholders such as `%s`, `%d`, and `%f`. The positional parameter `%1$s` refers to index 0.

```java
import de.sayayi.lib.protocol.message.formatter.JavaStringFormatFormatter;
import java.util.Locale;

// default instance uses system locale
var formatter = JavaStringFormatFormatter.INSTANCE;

// custom locale
var germanFormatter = new JavaStringFormatFormatter(Locale.GERMANY);
```

```java
protocol
    .info()
    .message("Imported %d records in %.2f sec")
    .with("0", 1500)
    .with("1", 3.14);
// formatted: "Imported 1500 records in 3.14 sec"
```
