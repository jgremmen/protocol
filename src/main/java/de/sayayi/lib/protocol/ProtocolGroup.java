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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


/**
 * <p>
 *   A protocol group provides the same functionality as a {@link Protocol}. In addition, a protocol group
 *   can be assigned a group header message and the visibility of the protocol entries in the group can be
 *   controlled by setting the {@link Visibility}.
 * </p>
 * <p>
 *   ProtocolGroup instances are not thread safe. It is however possible to use separate protocol groups for each
 *   thread, created by the same parent protocol as long as the parent is not used for formatting/querying operations
 *   during the time other threads are protocolling on their group.
 * </p>
 *
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 */
public interface ProtocolGroup<M> extends Protocol<M>
{
  /**
   * Returns the visibility for this protocol group.
   *
   * @return  visibility, never {@code null}
   *
   * @see #getEffectiveVisibility()
   */
  @Contract(pure = true)
  @NotNull Visibility getVisibility();


  /**
   * Returns the effective visibility for this protocol group. The effective visibility depends on the visibility
   * set for this group as well as the existence of a group message.
   *
   * @return  effective visibility, never {@code null}
   *
   * @see #getVisibility()
   */
  @Contract(pure = true)
  @NotNull Visibility getEffectiveVisibility();


  /**
   * Sets the visibility for this protocol group.
   *
   * @param visibility  visibility, never {@code null}
   *
   * @return  this protocol group instance
   *
   * @see #getVisibility()
   * @see #getEffectiveVisibility()
   */
  @Contract("_ -> this")
  @NotNull ProtocolGroup<M> setVisibility(@NotNull Visibility visibility);


  /**
   * Returns the level limit valid for this group.
   *
   * @return  level limit, never {@code null}
   *
   * @see #setLevelLimit(Level)
   */
  @Contract(pure = true)
  @NotNull Level getLevelLimit();


  /**
   * <p>
   *   Sets the highest level for this protocol group. The default setting is {@link Level.Shared#HIGHEST}.
   * </p>
   * <p>
   *   The severity for messages in this protocol group are limited to {@code level}. If a message has a higher
   *   severity its level will equal to the limit set by this method. If a message has a lower severity level,
   *   the level is not modified.
   * </p>
   * <p>
   *   Messages returned by the protocol iterator or passed to formatting methods during formatting, will respect
   *   the limit set by this method. References to the message itself however will show the real severity level.
   * </p>
   *
   * @param level  the highest level propagated for messages in this group, never {@code null}
   *
   * @return  this protocol group instance
   *
   * @see #getLevelLimit()
   */
  @Contract("_ -> this")
  @NotNull ProtocolGroup<M> setLevelLimit(@NotNull Level level);


  /**
   * Sets a group header message and initiates a parameter builder which allows configuring parameter values for the
   * message.
   *
   * @param message  message text
   *
   * @return  parameter builder instance
   *
   * @see #removeGroupMessage()
   */
  @Contract("_ -> new")
  @NotNull MessageParameterBuilder<M> setGroupMessage(@NotNull String message);


  /**
   * Removes the group header message.
   *
   * @return  this protocol group instance
   *
   * @see #setGroupMessage(String)
   */
  @Contract("-> this")
  @NotNull ProtocolGroup<M> removeGroupMessage();


  @Contract(pure = true)
  boolean isHeaderVisible(@NotNull Level level, @NotNull Tag ... tags);


  @Override
  @Contract(pure = true, value = "-> new")
  @NotNull ProtocolMessageBuilder<M> debug();


  @Override
  @Contract(pure = true, value = "-> new")
  @NotNull ProtocolMessageBuilder<M> info();


  @Override
  @Contract(pure = true, value = "-> new")
  @NotNull ProtocolMessageBuilder<M> warn();


  @Override
  @Contract(pure = true, value = "-> new")
  @NotNull ProtocolMessageBuilder<M> error();


  @Override
  @Contract(pure = true, value = "_ -> new")
  @NotNull ProtocolMessageBuilder<M> add(@NotNull Level level);


  /**
   * Returns the protocol instance this protocol group belongs to.
   *
   * @return  protocol root instance, never {@code null}
   */
  @Contract(pure = true)
  @NotNull Protocol<M> getRootProtocol();




  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("squid:S2176")
  interface ProtocolMessageBuilder<M> extends Protocol.ProtocolMessageBuilder<M>
  {
    @Override
    @Contract("_ -> this")
    @NotNull ProtocolMessageBuilder<M> forTag(@NotNull String tagName);


    @Override
    @Contract("_ -> this")
    @NotNull ProtocolMessageBuilder<M> forTag(@NotNull Tag tag);


    @Override
    @Contract("_ -> this")
    @NotNull ProtocolMessageBuilder<M> forTags(@NotNull Tag ... tags);


    @Override
    @Contract("_ -> this")
    @NotNull ProtocolMessageBuilder<M> forTags(@NotNull String ... tagNames);


    @Override
    @Contract("_ -> this")
    @NotNull ProtocolMessageBuilder<M> withThrowable(Throwable throwable);


    @Override
    @Contract("_ -> new")
    @NotNull MessageParameterBuilder<M> message(@NotNull String message);
  }




  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("squid:S2176")
  interface MessageParameterBuilder<M> extends Protocol.MessageParameterBuilder<M>, ProtocolGroup<M>
  {
    @Override
    @Contract("_ -> this")
    @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull Map<String,Object> parameterValues);


    @Override
    @Contract("_, _ -> this")
    @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull String parameter, boolean value);


    @Override
    @Contract("_, _ -> this")
    @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull String parameter, int value);


    @Override
    @Contract("_, _ -> this")
    @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull String parameter, long value);


    @Override
    @Contract("_, _ -> this")
    @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull String parameter, float value);


    @Override
    @Contract("_, _ -> this")
    @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull String parameter, double value);


    @Override
    @Contract("_, _ -> this")
    @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull String parameter, Object value);
  }




  /**
   * The various visibility settings allow for more control on how to format a protocol group.
   */
  enum Visibility
  {
    /**
     * <p>
     *   Show the group header message, regardless whether the group contains visible entries or not.
     * </p>
     * <p>
     *   If no group header message is set, the effective visibility is {@link #FLATTEN}.
     * </p>
     *
     * @see ProtocolGroup#getEffectiveVisibility()
     */
    SHOW_HEADER_ALWAYS,


    /**
     * <p>
     *   Show the group header message only if the group contains one or more visible entries.
     * </p>
     * <p>
     *   If no group header message is set, the effective visibility is {@link #FLATTEN}.
     * </p>
     *
     * @see ProtocolGroup#getEffectiveVisibility()
     */
    SHOW_HEADER_IF_NOT_EMPTY,


    /**
     * <p>
     *   Show the group header message only. If the group contains visible entries, they are ignored.
     * </p>
     * <p>
     *   If no group header message is set, the effective visibility is {@link #HIDDEN}.
     * </p>
     *
     * @see ProtocolGroup#getEffectiveVisibility()
     */
    SHOW_HEADER_ONLY,


    /**
     * <p>
     *   Do not show the group header message, regardless of the number of visible entries contained in the group.
     * </p>
     * <p>
     *   All group entries are merged with the parent as if the messages had been added to the parent protocol directly.
     * </p>
     */
    FLATTEN,


    /**
     * Do not show the group header message if the group only contains 1 visible entry.
     */
    FLATTEN_ON_SINGLE_ENTRY,


    /**
     * Suppress any output for the protocol group.
     */
    HIDDEN
    ;


    /**
     * Returns the effective visibility in case a group has no header message.
     *
     * @return  effective visibility for a group without a header message
     */
    public @NotNull Visibility forAbsentHeader()
    {
      if (this == SHOW_HEADER_ALWAYS || this == SHOW_HEADER_IF_NOT_EMPTY)
        return FLATTEN;
      else if (this == SHOW_HEADER_ONLY)
        return HIDDEN;
      else
        return this;
    }


    /**
     * Tells if group entries are to be shown for this visibility instance.
     *
     * @return  {@code true} if group entries are shown, {@code false} otherwise
     */
    public boolean isShowEntries() {
      return this != SHOW_HEADER_ONLY && this != HIDDEN;
    }
  }
}
