# Custom Message Processors

- The MessageProcessor<M> interface: processMessage(String) → MessageWithId<M>
- When to implement a custom processor:
  - Custom message storage or lookup mechanism
  - Integration with application-specific message catalogs
  - Custom message ID generation
- MessageWithId record: id() and message()
- getIdFromMessage: override to derive IDs from already-processed messages
- Integrating with GenericProtocolFactory
- Example: database-backed message processor
- Example: annotation-based message discovery
