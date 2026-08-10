# Custom Message Formatters

- The MessageFormatter<M> functional interface: formatMessage(GenericMessage<M>) → String
- Distinction from ProtocolFormatter (message-level vs protocol-level)
- GenericMessage<M>: getMessage(), getParameterValues(), getMessageId(), getTimeMillis()
- When to implement a custom message formatter:
  - Custom parameter substitution logic
  - Integration with template engines
  - Locale-aware formatting beyond what Java MessageFormat offers
- AbstractIndexedMessageFormatter: base class for index-based parameter formatters
- Integrating with GenericProtocolFactory or StringProtocolFactory
