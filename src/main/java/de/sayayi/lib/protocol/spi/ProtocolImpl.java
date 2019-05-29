package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;

import java.util.concurrent.atomic.AtomicInteger;


public class ProtocolImpl<M> extends AbstractProtocol<M,ProtocolMessageBuilder>
{
  private static final AtomicInteger PROTOCOL_ID = new AtomicInteger(0);

  private final int id;


  ProtocolImpl(AbstractProtocolFactory<M> factory)
  {
    super(factory);

    id = PROTOCOL_ID.incrementAndGet();
  }


  @Override
  public ProtocolMessageBuilder add(Level level)
  {
    if (level == null)
      throw new NullPointerException("level must not be null");

    return new MessageBuilder(level);
  }


  private class MessageBuilder extends AbstractMessageBuilder<M,ProtocolMessageBuilder,MessageParameterBuilder>
  {
    MessageBuilder(Level level) {
      super(ProtocolImpl.this, level);
    }


    @Override
    protected MessageParameterBuilder createMessageParameterBuilder(ProtocolMessageEntry<M> message) {
      return new ParameterBuilder(message);
    }
  }


  private class ParameterBuilder extends AbstractParameterBuilder<M,MessageParameterBuilder,ProtocolMessageBuilder>
  {
    ParameterBuilder(ProtocolMessageEntry<M> message) {
      super(ProtocolImpl.this, message);
    }
  }


  @Override
  public int hashCode() {
    return id;
  }


  @Override
  public String toString() {
    return "Protocol[id=" + id + ']';
  }
}
