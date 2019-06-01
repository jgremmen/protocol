package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol.MessageParameterBuilder;
import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.ProtocolIterator;
import de.sayayi.lib.protocol.Tag;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


@SuppressWarnings("unchecked")
abstract class AbstractParameterBuilder<M,P extends MessageParameterBuilder<M>,B extends ProtocolMessageBuilder<M>>
    implements MessageParameterBuilder<M>
{
  private final AbstractProtocol<M,B> protocol;
  private final AbstractBasicMessage<M> message;


  AbstractParameterBuilder(AbstractProtocol<M,B> protocol, AbstractBasicMessage<M> message)
  {
    this.protocol = protocol;
    this.message = message;
  }


  @Override
  public P with(Map<String, Object> parameterValues)
  {
    for(Entry<String,Object> entry: parameterValues.entrySet())
      with(entry.getKey(), entry.getValue());

    return (P)this;
  }


  @Override
  public P with(String parameter, boolean value) {
    return with(parameter, Boolean.valueOf(value));
  }


  @Override
  public P with(String parameter, int value) {
    return with(parameter, Integer.valueOf(value));
  }


  @Override
  public P with(String parameter, long value) {
    return with(parameter, Long.valueOf(value));
  }


  @Override
  public P with(String parameter, float value) {
    return with(parameter, Float.valueOf(value));
  }


  @Override
  public P with(String parameter, double value) {
    return with(parameter, Double.valueOf(value));
  }


  @Override
  public P with(String parameter, Object value)
  {
    if (message != null)
    {
      if (parameter == null || parameter.isEmpty())
        throw new IllegalArgumentException("parameter must not be empty");

      message.parameterValues.put(parameter, value);
    }

    return (P)this;
  }


  @Override
  public B debug() {
    return protocol.debug();
  }


  @Override
  public B info() {
    return protocol.info();
  }


  @Override
  public B warn() {
    return protocol.warn();
  }


  @Override
  public B error() {
    return protocol.error();
  }


  @Override
  public B add(Level level) {
    return protocol.add(level);
  }


  @Override
  public ProtocolGroup<M> createGroup() {
    return protocol.createGroup();
  }


  @Override
  public <R> R format(Level level, Tag tag, ProtocolFormatter<M,R> formatter) {
    return null;
  }


  @Override
  public boolean isMatch(Level level, Tag tag) {
    return protocol.isMatch(level, tag);
  }


  @Override
  public List<ProtocolEntry<M>> getEntries(Level level, Tag tag) {
    return protocol.getEntries(level, tag);
  }


  @Override
  public boolean hasVisibleElement(Level level, Tag tag) {
    return protocol.hasVisibleElement(level, tag);
  }


  @Override
  public ProtocolIterator<M> iterator(Level level, Tag tag) {
    return protocol.iterator(level, tag);
  }
}
