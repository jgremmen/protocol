package de.sayayi.lib.protocol;

import java.util.Map;


public interface Protocol extends ProtocolQuery
{
  /**
   * Add a debug level message.
   *
   * @return  message builder instance for the debug message
   */
  ProtocolMessageBuilder debug();


  /**
   * Add an info level message.
   *
   * @return  message builder instance for the info message
   */
  ProtocolMessageBuilder info();


  /**
   * Add a warning level message.
   *
   * @return  message builder instance for the warning message
   */
  ProtocolMessageBuilder warn();


  /**
   * Add an error level message.
   *
   * @return  message builder instance for the error message
   */
  ProtocolMessageBuilder error();


  ProtocolMessageBuilder add(Level level);


  ProtocolGroup createGroup();


  interface ProtocolMessageBuilder
  {
    ProtocolMessageBuilder forTag(Tag tag);


    ProtocolMessageBuilder forTags(Tag ... tags);


    ProtocolMessageBuilder forTags(String ... tagNames);


    ProtocolMessageBuilder withThrowable(Throwable throwable);


    MessageParameterBuilder message(String message);
  }


  interface MessageParameterBuilder extends Protocol
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
    MessageParameterBuilder with(Map<String,Object> parameterValues);


    MessageParameterBuilder with(String parameter, boolean value);


    MessageParameterBuilder with(String parameter, int value);


    MessageParameterBuilder with(String parameter, long value);


    MessageParameterBuilder with(String parameter, float value);


    MessageParameterBuilder with(String parameter, double value);


    MessageParameterBuilder with(String parameter, Object value);
  }
}
