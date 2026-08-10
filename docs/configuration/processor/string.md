# String Message Processor

`StringMessageProcessor` is the simplest processor. It stores the message string as-is, using the string itself as
both the message content and its identifier. Because the message string serves as the ID, two calls to `message()`
with the same text produce entries with the same message ID.

```java
import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor;
import de.sayayi.lib.protocol.message.processor.StringMessageProcessor;

MessageProcessor<String> processor = StringMessageProcessor.INSTANCE;

// message and id are both the input string
var result = processor.processMessage("Upload complete");
// result.id() == "Upload complete"
// result.message() == "Upload complete"
```

This processor is used internally by `StringProtocolFactory` and suits any scenario where messages are inline text
literals rather than lookup keys.
