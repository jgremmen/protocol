package de.sayayi.lib.protocol;

import java.util.Map;
import java.util.Set;


public interface ProtocolEntry<M> extends ProtocolQuery<M>
{
  interface BasicMessage<M> extends ProtocolEntry<M>
  {
    M getMessage();


    Map<String,Object> getParameterValues();
  }


  interface Message<M> extends BasicMessage<M>
  {
    Level getLevel();


    Set<Tag> getTags();


    /**
     * Returns the throwable associated with the message.
     *
     * @return  throwable/exception or {@code null}
     */
    Throwable getThrowable();
  }


  interface Group<M> extends ProtocolEntry<M>
  {
    BasicMessage<M> getGroupMessage();
  }
}
