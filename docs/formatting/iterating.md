# Iterating and Streaming

- ProtocolIterator: traverses protocol hierarchy in depth-first order
  - protocol.iterator(matcher) returns ProtocolIterator<M>
  - Standard Iterator<DepthEntry<M>> contract
- Sealed entry type hierarchy:
  - DepthEntry (base, provides getDepth())
  - BoundedDepthEntry (extends DepthEntry, adds isFirst/isLast)
  - ProtocolStart, ProtocolEnd (structural markers)
  - MessageEntry (message content, level, tags, throwable, isGroupMessage)
  - GroupMessageEntry (group header without visible children)
  - GroupStartEntry (group header with children, includes message count)
  - GroupEndEntry (closes a group)
- Iteration sequence: ProtocolStart → entries → ProtocolEnd
- Depth semantics: root messages at 0, group children at parent+1
- Spliterator support: protocol.spliterator(matcher)
  - Characteristics: ORDERED, DISTINCT, NONNULL, IMMUTABLE
- Stream support: protocol.stream(matcher)
- Pattern matching with sealed types (Java 21 switch expressions)
