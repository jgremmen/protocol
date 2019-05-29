package de.sayayi.lib.protocol;

import java.util.Map;


public interface ProtocolEntry extends ProtocolQuery
{
  interface BasicMessage<M> extends ProtocolEntry
  {
    M getMessage();

    Map<String,Object> getParameterValues();
  }


  interface Message<M> extends BasicMessage<M>
  {
    Level getLevel();

    Throwable getThrowable();
  }


  interface Group<M> extends ProtocolEntry
  {
    BasicMessage<M> getGroupMessage();
  }
}
