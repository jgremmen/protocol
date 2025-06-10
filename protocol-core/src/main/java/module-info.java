module de.sayayi.lib.protocol {

  requires static org.jetbrains.annotations;
  requires static de.sayayi.lib.message;

  exports de.sayayi.lib.protocol;
  exports de.sayayi.lib.protocol.exception;
  exports de.sayayi.lib.protocol.factory;
  exports de.sayayi.lib.protocol.formatter;
  exports de.sayayi.lib.protocol.matcher;
  exports de.sayayi.lib.protocol.matcher.internal to de.sayayi.lib.protocol.message.matcher;
  exports de.sayayi.lib.protocol.message;
  exports de.sayayi.lib.protocol.message.formatter;
  exports de.sayayi.lib.protocol.message.processor;
  exports de.sayayi.lib.protocol.util;

  uses de.sayayi.lib.protocol.ProtocolMessageMatcher;

}