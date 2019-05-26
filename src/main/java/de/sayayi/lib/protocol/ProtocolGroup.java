package de.sayayi.lib.protocol;

import java.util.Map;


public interface ProtocolGroup extends Protocol
{
  /**
   * Returns the visibility set for this protocol group.
   *
   * @return  visibility, never {@code null}
   *
   * @see #getEffectiveVisibility()
   */
  Visibility getVisibility();


  /**
   * Returns the effective visibility for this protocol group. The effective visibility depends on the visibility
   * set for this group as well as the existence of a group message.
   *
   * @return  effective visibility, never {@code null}
   */
  Visibility getEffectiveVisibility();


  /**
   * Sets the visibility for this protocol group.
   *
   * @param visibility  visibility, never |@code null}
   *
   * @return  protocol group instance
   *
   * @see #getVisibility()
   * @see #getEffectiveVisibility()
   */
  ProtocolGroup setVisibility(Visibility visibility);


  /**
   * Sets a group message and initiates a parameter builder which allows configuring paramater values for the message.
   *
   * @param message  message text
   *
   * @return  parameter builder instance
   */
  MessageParameterBuilder setGroupMessage(String message);


  MessageParameterBuilder setGroupMessageKey(String key);


  boolean isHeaderVisible(Level level, Tag tag);

  ProtocolMessageBuilder debug();
  ProtocolMessageBuilder info();
  ProtocolMessageBuilder warn();
  ProtocolMessageBuilder error();

  ProtocolMessageBuilder add(Level level);


  /**
   * Returns the protocol instance this protocol group belongs to.
   *
   * @return  protocol root instance
   */
  Protocol getRootProtocol();


  interface ProtocolMessageBuilder extends Protocol.ProtocolMessageBuilder
  {
    ProtocolMessageBuilder forTag(Tag tag);

    ProtocolMessageBuilder forTags(Tag ... tags);

    ProtocolMessageBuilder forTags(String ... tagNames);

    MessageParameterBuilder message(String message);

    MessageParameterBuilder messageKey(String key);
  }


  interface MessageParameterBuilder extends Protocol.MessageParameterBuilder, ProtocolGroup
  {
    ProtocolGroup.MessageParameterBuilder with(Map<String,Object> parameterValues);

    ProtocolGroup.MessageParameterBuilder with(String parameter, boolean value);

    ProtocolGroup.MessageParameterBuilder with(String parameter, int value);

    ProtocolGroup.MessageParameterBuilder with(String parameter, long value);

    ProtocolGroup.MessageParameterBuilder with(String parameter, float value);

    ProtocolGroup.MessageParameterBuilder with(String parameter, double value);

    ProtocolGroup.MessageParameterBuilder with(String parameter, Object value);
  }


  enum Visibility
  {
    /**
     * <p>
     *   Show the group message header, regardless whether the group contains visible entries or not.
     * </p>
     * <p>
     *   If no group message is set, the effective visibility is {@link #FLATTEN}.
     * </p>
     *
     * @see ProtocolGroup#getEffectiveVisibility()
     */
    SHOW_HEADER_ALWAYS,


    /**
     * <p>
     *   Show the group message header only if the group contains one or more visible entries.
     * </p>
     * <p>
     *   If no group message is set, the effective visibility is {@link #FLATTEN}.
     * </p>
     *
     * @see ProtocolGroup#getEffectiveVisibility()
     */
    SHOW_HEADER_IF_NOT_EMPTY,


    /**
     * <p>
     *   Show the group message header only. If the group contains visible entries, they are ignored.
     * </p>
     * <p>
     *   If no group message is set, the effective visibility is {@link #HIDDEN}.
     * </p>
     *
     * @see ProtocolGroup#getEffectiveVisibility()
     */
    SHOW_HEADER_ONLY,


    /**
     * Do not show the group message header, regardless of the number of visible entries contained in the group.
     */
    FLATTEN,


    /**
     * Do not show the group message header if the group only contains 1 visible entry.
     */
    FLATTEN_ON_SINGLE_ENTRY,


    /**
     * Suppress any output for the protocol group.
     */
    HIDDEN
    ;


    public Visibility modifyNoHeader()
    {
      if (this == SHOW_HEADER_ALWAYS || this == SHOW_HEADER_IF_NOT_EMPTY)
        return FLATTEN;
      else if (this == SHOW_HEADER_ONLY)
        return HIDDEN;
      else
        return this;
    }


    public boolean isShowEntries() {
      return this != SHOW_HEADER_ONLY && this != HIDDEN;
    }
  }
}
