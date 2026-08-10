# MessageAccessor Message Processor

`MessageAccessorMessageProcessor` integrates with the
[message-format](https://github.com/jgremmen/message-format) library. It resolves messages through a
`MessageAccessor`, which is the message-format library's central registry for looking up message templates by code.
The internal message type for this processor is `Message` (from the message-format library), providing full access to
the library's rich formatting capabilities.

The processor operates in two modes depending on the `parserFallback` constructor argument. When `parserFallback` is
`false` (the default), every string passed to `message()` must be a known message code registered in the
`MessageAccessor`. If the code is not found, a `ProtocolException` is thrown.

```java
import de.sayayi.lib.message.MessageSupport;
import de.sayayi.lib.message.MessageSupport.MessageAccessor;
import de.sayayi.lib.protocol.message.processor
    .MessageAccessorMessageProcessor;

MessageSupport messageSupport = ...;
MessageAccessor accessor = messageSupport.getMessageAccessor();

// strict mode: code must exist
var processor = new MessageAccessorMessageProcessor(accessor);
```

When `parserFallback` is `true`, the processor first attempts a code lookup. If no matching code is found, it parses
the input string as an inline message-format template instead. This mode is convenient during development or in mixed
scenarios where some messages are registered codes and others are ad-hoc templates.

```java
// fallback mode: parse if code not found
var processor = new MessageAccessorMessageProcessor(accessor, true);

// registered code → looked up
processor.processMessage("MSG-001");

// not a code → parsed as inline template
processor.processMessage("Processed %{count} records");
```

To help the processor distinguish codes from inline templates before attempting the lookup, the `isInvalidMessageCode`
method can be overridden. The default implementation returns `false` for all inputs, meaning everything is first tried
as a code. A subclass can provide a more efficient check based on a naming convention.

```java
var processor = new MessageAccessorMessageProcessor(accessor, true) {
  @Override
  protected boolean isInvalidMessageCode(String codeOrMessageFormat) 
  {
    // codes always start with "MSG-"
    return !codeOrMessageFormat.startsWith("MSG-");
  }
};

// skips code lookup, parsed directly
processor.processMessage("Processed %{count} records");
```
