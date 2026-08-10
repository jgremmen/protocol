---
icon: material/message-cog-outline
---

# Message Formatter

The `MessageFormatter<M>` interface defines a single method, `formatMessage(GenericMessage<M>)`, that turns the
internal message representation plus its parameter values into a final `String`. The `GenericMessage<M>` argument
provides access to both the stored message (`getMessage()`) and the parameter map (`getParameterValues()`). Several
built-in formatters handle the most common formatting strategies.
