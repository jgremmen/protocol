package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol.MessageParameterBuilder;
import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.Tag;

import java.util.List;
import java.util.Map;


@SuppressWarnings("unchecked")
abstract class AbstractParameterBuilder<P extends MessageParameterBuilder, B extends ProtocolMessageBuilder>
    implements MessageParameterBuilder
{
  private final AbstractProtocol protocol;
  private final ProtocolMessageEntry message;


  AbstractParameterBuilder(AbstractProtocol protocol, ProtocolMessageEntry message)
  {
    this.protocol = protocol;
    this.message = message;
  }


  @Override
  public P with(Map<String, Object> parameterValues)
  {
    message.parameterValues.putAll(parameterValues);
    return (P)this;
  }


  @Override
  public P with(String parameter, boolean value)
  {
    //noinspection UnnecessaryBoxing
    message.parameterValues.put(parameter, Boolean.valueOf(value));
    return (P)this;
  }


  @Override
  public P with(String parameter, int value)
  {
    //noinspection UnnecessaryBoxing
    message.parameterValues.put(parameter, Integer.valueOf(value));
    return (P)this;
  }


  @Override
  public P with(String parameter, long value)
  {
    //noinspection UnnecessaryBoxing
    message.parameterValues.put(parameter, Long.valueOf(value));
    return (P)this;
  }


  @Override
  public P with(String parameter, float value)
  {
    //noinspection UnnecessaryBoxing
    message.parameterValues.put(parameter, Float.valueOf(value));
    return (P)this;
  }


  @Override
  public P with(String parameter, double value)
  {
    //noinspection UnnecessaryBoxing
    message.parameterValues.put(parameter, Double.valueOf(value));
    return (P)this;
  }


  @Override
  public P with(String parameter, Object value)
  {
    message.parameterValues.put(parameter, value);
    return (P)this;
  }


  @Override
  public B debug() {
    return (B)protocol.debug();
  }


  @Override
  public B info() {
    return (B)protocol.info();
  }


  @Override
  public B warn() {
    return (B)protocol.warn();
  }


  @Override
  public B error() {
    return (B)protocol.error();
  }


  @Override
  public B add(Level level) {
    return (B)protocol.add(level);
  }


  @Override
  public ProtocolGroup createGroup() {
    return protocol.createGroup();
  }


  @Override
  public boolean isMatch(Level level, Tag tag) {
    return protocol.isMatch(level, tag);
  }


  @Override
  public List<ProtocolEntry> getEntries(Level level, Tag tag) {
    return protocol.getEntries(level, tag);
  }
}
