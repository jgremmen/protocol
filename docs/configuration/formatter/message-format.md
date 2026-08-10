# MessageFormatFormatter

`MessageFormatFormatter` formats messages using the message-format library. It delegates to
`Message.format(MessageAccessor, Message.Parameters)`, which supports named parameters, conditional logic, and the
full range of message-format features. This formatter is designed to pair with `MessageAccessorMessageProcessor`.

```java
import de.sayayi.lib.message.MessageSupport;
import de.sayayi.lib.protocol.message.formatter.MessageFormatFormatter;

MessageSupport messageSupport = ...;

var formatter = new MessageFormatFormatter(messageSupport);
```

Parameters are passed by name to the message-format engine, which resolves them according to the template's own
syntax. There is no numeric index constraint; any parameter name is valid.

```java
protocol
    .info()
    .message("MSG-IMPORT-DONE")
    .with("count", 200)
    .with("duration", "5s");
// formatted by message-format library using
// the template registered under "MSG-IMPORT-DONE"
```
