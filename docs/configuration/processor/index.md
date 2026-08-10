---
icon: material/message-cog-outline
---

# Message Processor

The `MessageProcessor<M>` interface defines a single method, `processMessage(String)`, that transforms the string
provided by application code into the internal representation `M` paired with a unique message identifier. The
identifier is later available via `getMessageId()` on the stored message. Several built-in processors cover the most
common scenarios.
