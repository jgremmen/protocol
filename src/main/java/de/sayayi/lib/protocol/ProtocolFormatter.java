package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.ProtocolEntry.BasicMessage;
import de.sayayi.lib.protocol.ProtocolEntry.Message;


public interface ProtocolFormatter<M,R>
{
  void message(Message<M> message, boolean lastEntry);

  void startGroup(int group, BasicMessage<M> groupMessage, boolean lastEntry);

  void endGroup(int group, boolean lastEntry);

  R getResult();


  interface Initializable<M,R> extends ProtocolFormatter<M,R>
  {
    void init(Level level, Tag tag);
  }
}
