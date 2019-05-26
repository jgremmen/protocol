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

    MessageParameterBuilder message(String message);

    MessageParameterBuilder messageKey(String key);
  }


  interface MessageParameterBuilder extends Protocol
  {
    MessageParameterBuilder with(Map<String,Object> parameterValues);

    MessageParameterBuilder with(String parameter, boolean value);

    MessageParameterBuilder with(String parameter, int value);

    MessageParameterBuilder with(String parameter, long value);

    MessageParameterBuilder with(String parameter, float value);

    MessageParameterBuilder with(String parameter, double value);

    MessageParameterBuilder with(String parameter, Object value);
  }
}
