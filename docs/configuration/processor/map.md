# Map Message Processor

`MapMessageProcessor<M>` resolves messages from a pre-built `Map<String, M>`. The string passed to `message()` acts
as the lookup key, and the corresponding map value becomes the internal message. Like `ResourceBundleMessageProcessor`,
the key is used as the message ID. The generic type parameter allows the map to hold any message type, not just
strings.

```java
import de.sayayi.lib.protocol.message.processor.MapMessageProcessor;
import java.util.Map;

var messages = Map.of(
    "import.start", "Starting import of {0}",
    "import.done", "Import completed: {0} items"
);

var processor = new MapMessageProcessor<>(messages);

var result = processor.processMessage("import.start");
// result.id() == "import.start"
// result.message() == "Starting import of {0}"
```

A `ProtocolException` is thrown when the map does not contain the requested key.
