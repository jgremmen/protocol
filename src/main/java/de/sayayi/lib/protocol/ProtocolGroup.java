/*
 * Copyright 2019 Jeroen Gremmen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.sayayi.lib.protocol;

import java.util.Map;


/**
 * @author Jeroen Gremmen
 */
public interface ProtocolGroup<M> extends Protocol<M>
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
  ProtocolGroup<M> setVisibility(Visibility visibility);


  /**
   * Sets a group message and initiates a parameter builder which allows configuring paramater values for the message.
   *
   * @param message  message text
   *
   * @return  parameter builder instance
   */
  MessageParameterBuilder<M> setGroupMessage(String message);


  ProtocolGroup<M> removeGroupMessage();


  boolean isHeaderVisible(Level level, Tag tag);


  ProtocolMessageBuilder<M> debug();


  ProtocolMessageBuilder<M> info();


  ProtocolMessageBuilder<M> warn();


  ProtocolMessageBuilder<M> error();


  ProtocolMessageBuilder<M> add(Level level);


  /**
   * Returns the protocol instance this protocol group belongs to.
   *
   * @return  protocol root instance
   */
  Protocol<M> getRootProtocol();


  interface ProtocolMessageBuilder<M> extends Protocol.ProtocolMessageBuilder<M>
  {
    ProtocolMessageBuilder<M> forTag(Tag tag);


    ProtocolMessageBuilder<M> forTags(Tag ... tags);


    ProtocolMessageBuilder<M> forTags(String ... tagNames);


    ProtocolMessageBuilder<M> withThrowable(Throwable throwable);


    MessageParameterBuilder<M> message(String message);
  }


  interface MessageParameterBuilder<M> extends Protocol.MessageParameterBuilder<M>, ProtocolGroup<M>
  {
    ProtocolGroup.MessageParameterBuilder<M> with(Map<String,Object> parameterValues);


    ProtocolGroup.MessageParameterBuilder<M> with(String parameter, boolean value);


    ProtocolGroup.MessageParameterBuilder<M> with(String parameter, int value);


    ProtocolGroup.MessageParameterBuilder<M> with(String parameter, long value);


    ProtocolGroup.MessageParameterBuilder<M> with(String parameter, float value);


    ProtocolGroup.MessageParameterBuilder<M> with(String parameter, double value);


    ProtocolGroup.MessageParameterBuilder<M> with(String parameter, Object value);
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


    public Visibility forAbsentHeader()
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
