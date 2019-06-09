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

import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
  @Contract(pure = true)
  @NotNull Visibility getVisibility();


  /**
   * Returns the effective visibility for this protocol group. The effective visibility depends on the visibility
   * set for this group as well as the existence of a group message.
   *
   * @return  effective visibility, never {@code null}
   */
  @Contract(pure = true)
  @NotNull Visibility getEffectiveVisibility();


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
  @NotNull ProtocolGroup<M> setVisibility(@NotNull Visibility visibility);


  /**
   * Sets a group message and initiates a parameter builder which allows configuring paramater values for the message.
   *
   * @param message  message text
   *
   * @return  parameter builder instance
   */
  @NotNull MessageParameterBuilder<M> setGroupMessage(@NotNull String message);


  @NotNull ProtocolGroup<M> removeGroupMessage();


  @Contract(pure = true)
  boolean isHeaderVisible(@NotNull Level level, @NotNull Tag tag);


  @Contract(pure = true)
  @NotNull ProtocolMessageBuilder<M> debug();


  @Contract(pure = true)
  @NotNull ProtocolMessageBuilder<M> info();


  @Contract(pure = true)
  @NotNull ProtocolMessageBuilder<M> warn();


  @Contract(pure = true)
  @NotNull ProtocolMessageBuilder<M> error();


  @Contract(pure = true)
  @NotNull ProtocolMessageBuilder<M> add(@NotNull Level level);


  /**
   * Returns the protocol instance this protocol group belongs to.
   *
   * @return  protocol root instance
   */
  @Contract(pure = true)
  @NotNull Protocol<M> getRootProtocol();


  interface ProtocolMessageBuilder<M> extends Protocol.ProtocolMessageBuilder<M>
  {
    @NotNull ProtocolMessageBuilder<M> forTag(@NotNull Tag tag);


    @NotNull ProtocolMessageBuilder<M> forTags(@NotNull Tag ... tags);


    @NotNull ProtocolMessageBuilder<M> forTags(@NotNull String ... tagNames);


    @NotNull ProtocolMessageBuilder<M> withThrowable(Throwable throwable);


    @NotNull MessageParameterBuilder<M> message(@NotNull String message);
  }


  interface MessageParameterBuilder<M> extends Protocol.MessageParameterBuilder<M>, ProtocolGroup<M>
  {
    @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull Map<String,Object> parameterValues);


    @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull @Pattern("\\p{Alpha}\\p{Graph}*") String parameter, boolean value);


    @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull @Pattern("\\p{Alpha}\\p{Graph}*") String parameter, int value);


    @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull @Pattern("\\p{Alpha}\\p{Graph}*") String parameter, long value);


    @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull @Pattern("\\p{Alpha}\\p{Graph}*") String parameter, float value);


    @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull @Pattern("\\p{Alpha}\\p{Graph}*") String parameter, double value);


    @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull @Pattern("\\p{Alpha}\\p{Graph}*") String parameter, Object value);
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


    public @NotNull Visibility forAbsentHeader()
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
