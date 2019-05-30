package de.sayayi.lib.protocol;

import java.util.Map;


public interface Protocol<M> extends ProtocolQuery<M>
{
  /**
   * Add a debug level message.
   *
   * @return  message builder instance for the debug message
   */
  ProtocolMessageBuilder<M> debug();


  /**
   * Add an info level message.
   *
   * @return  message builder instance for the info message
   */
  ProtocolMessageBuilder<M> info();


  /**
   * Add a warning level message.
   *
   * @return  message builder instance for the warning message
   */
  ProtocolMessageBuilder<M> warn();


  /**
   * Add an error level message.
   *
   * @return  message builder instance for the error message
   */
  ProtocolMessageBuilder<M> error();


  ProtocolMessageBuilder<M> add(Level level);


  /**
   * Create a new protocol group.
   *
   * @return  new protocol group
   */
  ProtocolGroup<M> createGroup();


  <R> R format(Level level, Tag tag, ProtocolFormatter<M,R> formatter);


  ProtocolIterator<M> iterator(Level level, Tag tag);


  interface ProtocolMessageBuilder<M>
  {
    ProtocolMessageBuilder<M> forTag(Tag tag);


    ProtocolMessageBuilder<M> forTags(Tag ... tags);


    ProtocolMessageBuilder<M> forTags(String ... tagNames);


    ProtocolMessageBuilder<M> withThrowable(Throwable throwable);


    MessageParameterBuilder<M> message(String message);
  }


  interface MessageParameterBuilder<M> extends Protocol<M>
  {
    /**
     * <p>
     *   Associate the provided {@code parameterValues} with this message. New parameters are added, existing
     *   parameters are overridden.
     * </p>
     *
     * @param parameterValues  map with parameter values. the parameter name must not be {@code null} or empty.
     *
     * @return  paramter builder instance for the current message
     */
    MessageParameterBuilder<M> with(Map<String,Object> parameterValues);


    MessageParameterBuilder<M> with(String parameter, boolean value);


    MessageParameterBuilder<M> with(String parameter, int value);


    MessageParameterBuilder<M> with(String parameter, long value);


    MessageParameterBuilder<M> with(String parameter, float value);


    MessageParameterBuilder<M> with(String parameter, double value);


    MessageParameterBuilder<M> with(String parameter, Object value);
  }
}
