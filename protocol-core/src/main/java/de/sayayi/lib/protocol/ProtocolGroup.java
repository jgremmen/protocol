/*
 * Copyright 2019 Jeroen Gremmen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.Level.Shared;
import de.sayayi.lib.protocol.exception.ProtocolException;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;


/**
 * A protocol group provides the same functionality as a {@link Protocol}. In addition, a
 * protocol group can be assigned a group header message and the visibility of the protocol
 * entries in the group can be controlled by setting the {@link Visibility}.
 * <p>
 * ProtocolGroup instances are not thread safe. It is however possible to use separate protocol
 * groups for each thread, created by the same parent protocol as long as the parent is not used
 * for formatting/querying operations during the time other threads are protocolling on their
 * group.
 *
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
public interface ProtocolGroup<M> extends Protocol<M>
{
  /**
   * Returns the visibility setting for this protocol group.
   *
   * @return  visibility, never {@code null}
   *
   * @see #getEffectiveVisibility()
   */
  @Contract(pure = true)
  @NotNull Visibility getVisibility();


  /**
   * Returns the effective visibility for this protocol group. The effective visibility depends
   * on the visibility set for this group as well as the existence of a group message.
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
  @Contract(value = "_ -> this", mutates = "this")
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
   * Sets the highest level for this protocol group. The default setting is
   * {@link Shared#HIGHEST}.
   * <p>
   * The severity for messages in this protocol group are limited to {@code level}. If a message
   * has a higher severity its level will equal to the limit set by this method. If a message has
   * a lower severity level, the level is not modified.
   * <p>
   * Messages returned by the protocol iterator or passed to formatting methods during formatting,
   * will respect the limit set by this method. References to the message itself however will
   * show the real severity level.
   *
   * @param level  the highest level propagated for messages in this group, never {@code null}
   *
   * @return  this protocol group instance
   *
   * @see #getLevelLimit()
   */
  @Contract(value = "_ -> this", mutates = "this")
  @NotNull ProtocolGroup<M> setLevelLimit(@NotNull Level level);


  /**
   * Sets a group header message and initiates a parameter builder which allows configuring
   * parameter values for the message.
   *
   * @param message  message text
   *
   * @return  parameter builder instance
   *
   * @see #removeGroupMessage()
   */
  @Contract(value = "_ -> new", mutates = "this")
  @NotNull MessageParameterBuilder<M> setGroupMessage(@NotNull String message);


  /**
   * Removes the group header message.
   *
   * @return  this protocol group instance
   *
   * @see #setGroupMessage(String)
   */
  @Contract(value = "-> this", mutates = "this")
  @NotNull ProtocolGroup<M> removeGroupMessage();


  /**
   * Returns the unique name for this group. The name can be used to find a group from a parent
   * protocol instance.
   *
   * @return  unique name for this group or {@code null} if no name is set.
   *
   * @see #setName(String)
   * @see Protocol#getGroupByName(String)
   * @see Protocol#forEachGroupByRegex(String, Consumer)
   */
  @Contract(pure = true)
  String getName();


  /**
   * Sets a unique name for this group. If {@code name} is {@code null} or an empty string the
   * group name will be removed.
   *
   * @param name  unique name or {@code null}
   *
   * @return  this protocol group instance
   *
   * @throws ProtocolException  if the group name is not unique across the protocol structure
   *
   * @see #getName()
   * @see Protocol#getGroupByName(String)
   * @see Protocol#forEachGroupByRegex(String, Consumer)
   */
  @Contract(value = "_ -> this", mutates = "this")
  @NotNull ProtocolGroup<M> setName(String name);


  /**
   * @param matcher  Message matcher, never {@code null}
   *
   * @return  {@code true} if a group header is set and is visible for the given {@code matcher},
   *          {@code false} otherwise
   *
   * @since 1.0.0
   */
  @Contract(pure = true)
  boolean isHeaderVisible(@NotNull MessageMatcher matcher);


  @Override
  @Contract(pure = true, value = "-> new")
  default @NotNull ProtocolMessageBuilder<M> debug() {
    return add(Shared.DEBUG);
  }


  @Override
  @Contract(pure = true, value = "-> new")
  default @NotNull ProtocolMessageBuilder<M> info() {
    return add(Shared.INFO);
  }


  @Override
  @Contract(pure = true, value = "-> new")
  default @NotNull ProtocolMessageBuilder<M> warn() {
    return add(Shared.WARN);
  }


  @Override
  @Contract(pure = true, value = "-> new")
  default @NotNull ProtocolMessageBuilder<M> error() {
    return add(Shared.ERROR);
  }


  @Override
  @Contract(pure = true, value = "_ -> new")
  default @NotNull ProtocolMessageBuilder<M> error(@NotNull Throwable throwable) {
    return add(Shared.ERROR).withThrowable(throwable);
  }


  @Override
  @Contract(pure = true, value = "_ -> new")
  @NotNull ProtocolMessageBuilder<M> add(@NotNull Level level);


  @Override
  @Contract(value = "_ -> new", pure = true)
  @NotNull ProtocolGroup.TargetTagBuilder<M> propagate(@NotNull TagSelector tagSelector);


  @Override
  @Contract(value = "_ -> new", pure = true)
  @NotNull ProtocolGroup.TargetTagBuilder<M> propagate(@NotNull String tagSelectorExpression);


  /**
   * Set a parameter value.
   * <p>
   * Parameter values set for this protocol are available for both messages and groups added to
   * this protocol.
   *
   * @param parameter  name of the parameter to set, never {@code null}
   * @param b          parameter value
   *
   * @return  current protocol instance
   *
   * @since 1.0.0
   */
  @Override
  @Contract(value = "_, _ -> this", mutates = "this")
  default @NotNull ProtocolGroup<M> set(@NotNull String parameter, boolean b) {
    return set(parameter, Boolean.valueOf(b));
  }


  /**
   * Set a parameter value.
   * <p>
   * Parameter values set for this protocol are available for both messages and groups added to
   * this protocol.
   *
   * @param parameter  name of the parameter to set, never {@code null}
   * @param i          parameter value
   *
   * @return  current protocol instance
   *
   * @since 1.0.0
   */
  @Override
  @Contract(value = "_, _ -> this", mutates = "this")
  default @NotNull ProtocolGroup<M> set(@NotNull String parameter, int i) {
    return set(parameter, Integer.valueOf(i));
  }


  /**
   * Set a parameter value.
   * <p>
   * Parameter values set for this protocol are available for both messages and groups added to
   * this protocol.
   *
   * @param parameter  name of the parameter to set, never {@code null}
   * @param l          parameter value
   *
   * @return  current protocol instance
   *
   * @since 1.0.0
   */
  @Override
  @Contract(value = "_, _ -> this", mutates = "this")
  default @NotNull ProtocolGroup<M> set(@NotNull String parameter, long l) {
    return set(parameter, Long.valueOf(l));
  }


  /**
   * Set a parameter value.
   * <p>
   * Parameter values set for this protocol are available for both messages and groups added to
   * this protocol.
   *
   * @param parameter  name of the parameter to set, never {@code null}
   * @param f          parameter value
   *
   * @return  current protocol instance
   *
   * @since 1.0.0
   */
  @Override
  @Contract(value = "_, _ -> this", mutates = "this")
  default @NotNull ProtocolGroup<M> set(@NotNull String parameter, float f) {
    return set(parameter, Float.valueOf(f));
  }


  /**
   * Set a parameter value.
   * <p>
   * Parameter values set for this protocol are available for both messages and groups added to
   * this protocol.
   *
   * @param parameter  name of the parameter to set, never {@code null}
   * @param d          parameter value
   *
   * @return  current protocol instance
   *
   * @since 1.0.0
   */
  @Override
  @Contract(value = "_, _ -> this", mutates = "this")
  default @NotNull ProtocolGroup<M> set(@NotNull String parameter, double d) {
    return set(parameter, Double.valueOf(d));
  }


  /**
   * Set a parameter value.
   * <p>
   * Parameter values set for this protocol are available for both messages and groups added to
   * this protocol.
   *
   * @param parameter  name of the parameter to set, never {@code null}
   * @param value      parameter value
   *
   * @return  current protocol instance
   *
   * @since 1.0.0
   */
  @Override
  @Contract(value = "_, _ -> this", mutates = "this")
  @NotNull ProtocolGroup<M> set(@NotNull String parameter, Object value);


  /**
   * Returns the protocol instance this protocol group belongs to.
   *
   * @return  protocol root instance, never {@code null}
   */
  @Contract(pure = true)
  @NotNull Protocol<M> getRootProtocol();




  interface ProtocolMessageBuilder<M> extends Protocol.ProtocolMessageBuilder<M>
  {
    @Override
    @Contract("_ -> this")
    @NotNull ProtocolMessageBuilder<M> forTag(@NotNull String tagName);


    @Override
    @Contract("_ -> this")
    @NotNull ProtocolMessageBuilder<M> forTags(@NotNull String ... tagNames);


    @Override
    @Contract("_ -> this")
    @NotNull ProtocolMessageBuilder<M> withThrowable(@NotNull Throwable throwable);


    @Override
    @Contract("_ -> new")
    @NotNull MessageParameterBuilder<M> message(@NotNull String message);
  }




  interface MessageParameterBuilder<M> extends Protocol.MessageParameterBuilder<M>, ProtocolGroup<M>
  {
    @Override
    @Contract("_ -> this")
    @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull Map<String,Object> parameterValues);


    @Override
    @Contract("_, _ -> this")
    default @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull String parameter, boolean value) {
      return with(parameter, Boolean.valueOf(value));
    }


    @Override
    @Contract("_, _ -> this")
    default @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull String parameter, int value) {
      return with(parameter, Integer.valueOf(value));
    }


    @Override
    @Contract("_, _ -> this")
    default @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull String parameter, long value) {
      return with(parameter, Long.valueOf(value));
    }


    @Override
    @Contract("_, _ -> this")
    default @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull String parameter, float value) {
      return with(parameter, Float.valueOf(value));
    }


    @Override
    @Contract("_, _ -> this")
    default @NotNull ProtocolGroup.MessageParameterBuilder<M> with(@NotNull String parameter, double value) {
      return with(parameter, Double.valueOf(value));
    }


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
     * Show the group header message, regardless whether the group contains visible entries or not.
     * <p>
     * If no group header message is set, the effective visibility is {@link #FLATTEN}.
     *
     * @see ProtocolGroup#getEffectiveVisibility()
     */
    SHOW_HEADER_ALWAYS,


    /**
     * Show the group header message only if the group contains one or more visible entries.
     * <p>
     * If no group header message is set, the effective visibility is {@link #FLATTEN}.
     *
     * @see ProtocolGroup#getEffectiveVisibility()
     */
    SHOW_HEADER_IF_NOT_EMPTY,


    /**
     * Show the group header message only. If the group contains visible entries, they are ignored.
     * <p>
     * If no group header message is set, the effective visibility is {@link #HIDDEN}.
     *
     * @see ProtocolGroup#getEffectiveVisibility()
     */
    SHOW_HEADER_ONLY,


    /**
     * Do not show the group header message, regardless of the number of visible entries
     * contained in the group.
     * <p>
     * All group entries are merged with the parent as if the messages had been added to the
     * parent protocol directly.
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
     * @return  effective visibility for a group without a header message, never {@code null}
     */
    @Contract(pure = true)
    public @NotNull Visibility forAbsentHeader()
    {
      if (this == SHOW_HEADER_ALWAYS ||
          this == SHOW_HEADER_IF_NOT_EMPTY ||
          this == FLATTEN_ON_SINGLE_ENTRY)
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




  interface TargetTagBuilder<M> extends Protocol.TargetTagBuilder<M>
  {
    @Override
    @NotNull ProtocolGroup<M> to(@NotNull String targetTagName);


    @Override
    @NotNull ProtocolGroup<M> to(@NotNull String... targetTagNames);
  }
}
