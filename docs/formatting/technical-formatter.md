# Technical Formatter

The `TechnicalProtocolFormatter` renders a protocol as an ASCII tree structure using box-drawing characters. It is
designed for debugging and diagnostics, producing a compact textual overview of the entire protocol hierarchy including
metadata such as the level and tags of each message. Because it implements `ConfiguredProtocolFormatter`, no matcher
needs to be supplied at format time; the formatter unconditionally matches all messages regardless of level or tags.


## Obtaining the Formatter

`TechnicalProtocolFormatter` is accessed through a shared singleton. The generic message type parameter is inferred
from the calling context, so the same instance can be used with any protocol factory.

```java
// obtain the shared singleton
ConfiguredProtocolFormatter<String,String> fmt =
    TechnicalProtocolFormatter.getInstance();
```


## Formatting a Protocol

Because `TechnicalProtocolFormatter` is a `ConfiguredProtocolFormatter`, it can be passed directly to
`Protocol.format()` without a matcher argument. The protocol also exposes a shorthand method `toStringTree()` that
delegates to the same formatter internally.

```java
// explicit invocation
String tree = protocol.format(TechnicalProtocolFormatter.getInstance());

// shorthand (equivalent to above)
String tree = protocol.toStringTree();
```

Both approaches produce identical output.


## Output Format

After each formatted message text, the formatter appends technical metadata in braces: the level, and for regular
messages also the assigned tag names. Group headers only show the level because they are not associated with tags
directly.

```java
Protocol<String> protocol = factory.createProtocol();

protocol
    .info()
    .forTag("audit")
    .message("Starting process");

ProtocolGroup<String> validation = protocol
    .createGroup()
    .setGroupMessage("Validation");
validation
    .debug()
    .forTag("field-check")
    .message("Checking email format");
validation
    .warn()
    .forTag("field-check")
    .message("Phone number is empty");

ProtocolGroup<String> persistence = protocol
    .createGroup()
    .setGroupMessage("Persistence");

ProtocolGroup<String> dbOps = persistence
    .createGroup()
    .setGroupMessage("Database operations");
dbOps
    .info()
    .forTag("db")
    .message("Inserted customer record");
dbOps
    .info()
    .forTag("db")
    .message("Updated account balance");

persistence
    .info()
    .forTag("cache")
    .message("Cache invalidated");

protocol
    .error()
    .message("Process failed unexpectedly");
```

Calling `protocol.toStringTree()` produces:

```text
■──Starting process  {level=INFO,tags=[default,audit]}
│
├──Validation  {level=WARN}
│  │
│  ├──Checking email format  {level=DEBUG,tags=[default,field-check]}
│  │
│  └──Phone number is empty  {level=WARN,tags=[default,field-check]}
│
├──Persistence  {level=INFO}
│  │
│  ├──Database operations  {level=INFO}
│  │  │
│  │  ├──Inserted customer record  {level=INFO,tags=[default,db]}
│  │  │
│  │  └──Updated account balance  {level=INFO,tags=[default,db]}
│  │
│  └──Cache invalidated  {level=INFO,tags=[default,cache]}
│
└──Process failed unexpectedly  {level=ERROR,tags=[default]}
```

The group header level reflects the highest severity among its visible child messages.


## Thread Safety

The `TechnicalProtocolFormatter` singleton is not thread-safe. It maintains internal state (a `StringBuilder` and
prefix array) that is reset on each `init()` call. Concurrent formatting from multiple threads requires either
separate instances created via subclassing, or external synchronization around the `format()` call.
