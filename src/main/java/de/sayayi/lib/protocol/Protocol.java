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

import de.sayayi.lib.protocol.ProtocolFormatter.ConfiguredProtocolFormatter;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


/**
 * @author Jeroen Gremmen
 */
public interface Protocol<M> extends ProtocolQuery<M>
{
  /**
   * Returns the protocol factory which was used to create this protocol instance.
   *
   * @return  protocol factory
   */
  @Contract(pure = true)
  @NotNull ProtocolFactory<M> getFactory();


  @Contract(pure = true)
  Protocol<M> getGroupParent();


  /**
   * <p>
   *   Add a debug level message.
   * </p>
   * <p>
   *   This method is functionally identical to {@code add(Level.Shared.DEBUG)}
   * </p>
   *
   * @return  message builder instance for the debug message, never {@code null}
   *
   * @see Level.Shared#DEBUG DEBUG
   */
  @Contract(pure = true)
  @NotNull ProtocolMessageBuilder<M> debug();


  /**
   * <p>
   *   Add an info level message.
   * </p>
   * <p>
   *   This method is functionally identical to {@code add(Level.Shared.INFO)}
   * </p>
   *
   * @return  message builder instance for the info message, never {@code null}
   *
   * @see Level.Shared#INFO INFO
   */
  @Contract(pure = true)
  @NotNull ProtocolMessageBuilder<M> info();


  /**
   * <p>
   *   Add a warning level message.
   * </p>
   * <p>
   *   This method is functionally identical to {@code add(Level.Shared.WARN)}
   * </p>
   *
   * @return  message builder instance for the warning message, never {@code null}
   *
   * @see Level.Shared#WARN WARN
   */
  @Contract(pure = true)
  @NotNull ProtocolMessageBuilder<M> warn();


  /**
   * <p>
   *   Add an error level message.
   * </p>
   * <p>
   *   This method is functionally identical to {@code add(Level.Shared.ERROR)}
   * </p>
   *
   * @return  message builder instance for the error message, never {@code null}
   *
   * @see Level.Shared#ERROR ERROR
   */
  @Contract(pure = true)
  @NotNull ProtocolMessageBuilder<M> error();


  /**
   * <p>
   *   Add an error level message with throwable.
   * </p>
   * <p>
   *   This method is functionally identical to {@code add(Level.Shared.ERROR).withThrowable(throwable)}
   * </p>
   *
   * @param throwable  throwable associated with message
   *
   * @return  message builder instance for the error message, never {@code null}
   *
   * @see Level.Shared#ERROR ERROR
   */
  @Contract(pure = true)
  @NotNull ProtocolMessageBuilder<M> error(Throwable throwable);


  /**
   * Add a new message with the given protocol {@code level}.
   *
   * @param level  protocol level, never {@code null}
   *
   * @return  new message builder instance, never {@code null}
   */
  @Contract(pure = true)
  @NotNull ProtocolMessageBuilder<M> add(@NotNull Level level);


  /**
   * Create a new protocol group.
   *
   * @return  new protocol group
   */
  @NotNull ProtocolGroup<M> createGroup();


  @SuppressWarnings("unused")
  @Contract(pure = true)
  <R> R format(@NotNull Level level, @NotNull Tag tag, @NotNull ProtocolFormatter<M,R> formatter);


  @Contract(pure = true)
  <R> R format(@NotNull ConfiguredProtocolFormatter<M,R> formatter);


  @Contract(pure = true)
  @NotNull ProtocolIterator<M> iterator(@NotNull Level level, @NotNull Tag tag);


  @SuppressWarnings({ "UnusedReturnValue", "unused" })
  interface ProtocolMessageBuilder<M>
  {
    @NotNull ProtocolMessageBuilder<M> forTag(@NotNull Tag tag);


    @NotNull ProtocolMessageBuilder<M> forTags(@NotNull Tag ... tags);


    @NotNull ProtocolMessageBuilder<M> forTags(@NotNull String ... tagNames);


    @NotNull ProtocolMessageBuilder<M> withThrowable(Throwable throwable);


    @NotNull MessageParameterBuilder<M> message(@NotNull String message);
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
    @NotNull MessageParameterBuilder<M> with(@NotNull Map<String,Object> parameterValues);


    @NotNull MessageParameterBuilder<M> with(@NotNull @Pattern("\\p{Alpha}\\p{Graph}*") String parameter, boolean value);


    @NotNull MessageParameterBuilder<M> with(@NotNull @Pattern("\\p{Alpha}\\p{Graph}*") String parameter, int value);


    @NotNull MessageParameterBuilder<M> with(@NotNull @Pattern("\\p{Alpha}\\p{Graph}*") String parameter, long value);


    @NotNull MessageParameterBuilder<M> with(@NotNull @Pattern("\\p{Alpha}\\p{Graph}*") String parameter, float value);


    @NotNull MessageParameterBuilder<M> with(@NotNull @Pattern("\\p{Alpha}\\p{Graph}*") String parameter, double value);


    @NotNull MessageParameterBuilder<M> with(@NotNull @Pattern("\\p{Alpha}\\p{Graph}*") String parameter, Object value);
  }
}
