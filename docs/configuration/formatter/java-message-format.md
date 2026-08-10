# JavaMessageFormatFormatter

`JavaMessageFormatFormatter` formats messages using `java.text.MessageFormat`. Parameter values are resolved by
numeric index: the parameter named `0` maps to argument `{0}`, `1` maps to `{1}`, and so on up to index 31.
Parameter names that are not valid integers or exceed 31 are ignored during formatting.

```java
import de.sayayi.lib.protocol.message.formatter
    .JavaMessageFormatFormatter;
import java.util.Locale;

// default instance uses system locale
var formatter =
    JavaMessageFormatFormatter.INSTANCE;

// custom locale
var frenchFormatter =
    new JavaMessageFormatFormatter(Locale.FRANCE);
```

When used with `StringProtocolFactory.createJavaMessageFormatFactory()`, messages follow `java.text.MessageFormat`
syntax. Placeholders like `{0}`, `{1,number,#.##}`, and `{2,date,short}` are supported.

```java
protocol
    .info()
    .message("Processed {0} of {1} items")
    .with("0", 42)
    .with("1", 100);
// formatted: "Processed 42 of 100 items"

protocol
    .warn()
    .message("Price: {0,number,currency}")
    .with("0", 19.99);
// formatted: "Price: $19.99" (US locale)
```
