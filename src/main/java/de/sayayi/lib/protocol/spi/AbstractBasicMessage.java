package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolEntry.BasicMessage;
import de.sayayi.lib.protocol.Tag;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


abstract class AbstractBasicMessage<M> implements BasicMessage<M>
{
  final M message;
  final Map<String,Object> parameterValues;


  AbstractBasicMessage(M message)
  {
    this.message = message;
    this.parameterValues = new HashMap<String,Object>(8);
  }


  @Override
  public M getMessage() {
    return message;
  }


  @Override
  public Map<String,Object> getParameterValues() {
    return Collections.unmodifiableMap(parameterValues);
  }


  @Override
  public List<ProtocolEntry> getEntries(Level level, Tag tag) {
    return isMatch(level, tag) ? Collections.<ProtocolEntry>singletonList(this) : Collections.<ProtocolEntry>emptyList();
  }
}
