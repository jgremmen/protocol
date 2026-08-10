# Resource Bundle Message Processor

`ResourceBundleMessageProcessor` treats the string passed to `message()` as a key into a `java.util.ResourceBundle`.
The processor looks up the key and stores the resolved value as the internal message. The key itself becomes the
message ID, enabling stable identification of messages across locales.

```java
import java.util.ResourceBundle;
import de.sayayi.lib.protocol.message.processor
    .ResourceBundleMessageProcessor;

ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.US);

var processor = new ResourceBundleMessageProcessor(bundle);

// key "err.disk.full" must exist in the bundle
var result = processor.processMessage("err.disk.full");
// result.id() == "err.disk.full"
// result.message() == bundle value for the key
```

If the bundle does not contain the given key, a `ProtocolException` is thrown at the point where `message()` is
called on the builder. This fail-fast behavior prevents silent missing translations from going unnoticed during
development.
