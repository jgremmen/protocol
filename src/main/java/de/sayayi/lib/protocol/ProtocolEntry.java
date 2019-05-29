package de.sayayi.lib.protocol;

import java.util.Map;
import java.util.Set;


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


    Set<Tag> getTags();


    Throwable getThrowable();
  }


  interface Group<M> extends ProtocolEntry
  {
    BasicMessage<M> getGroupMessage();
  }
}
