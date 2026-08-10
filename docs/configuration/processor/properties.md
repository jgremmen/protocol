# Properties Message Processor

`PropertiesMessageProcessor` works like `ResourceBundleMessageProcessor` but reads from a `java.util.Properties`
instance instead. The property key is the message ID, and the property value is the stored message string.

```java
import java.util.Properties;
import de.sayayi.lib.protocol.message.processor
    .PropertiesMessageProcessor;

var props = new Properties();
props.setProperty("val.email", "Invalid email: {0}");
props.setProperty("val.phone", "Invalid phone number");

var processor = new PropertiesMessageProcessor(props);

var result = processor.processMessage("val.email");
// result.id() == "val.email"
// result.message() == "Invalid email: {0}"
```

A `ProtocolException` is thrown when no property exists for the given key.
