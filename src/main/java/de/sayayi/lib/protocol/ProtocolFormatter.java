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

import de.sayayi.lib.protocol.ProtocolGroup.Visibility;
import de.sayayi.lib.protocol.ProtocolIterator.GroupEndEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupStartEntry;
import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


/**
 * @param <M>  internal message object type
 * @param <R>  formatting result type
 *
 * @author Jeroen Gremmen
 */
public interface ProtocolFormatter<M,R>
{
  /**
   * <p>
   *   This method is invoked when the formatting process starts.
   * </p>
   *
   * @see #protocolEnd()
   */
  void protocolStart();


  /**
   * <p>
   *   This method is invoked when the formatting process ends but before the result is obtained.
   * </p>
   *
   * @see #protocolStart()
   * @see #getResult()
   */
  void protocolEnd();


  /**
   * <p>
   *   Format the given message.
   * </p>
   * <p>
   *   This method is used for both regular messages as well as group header messages for groups with no containing
   *   messages. A distinction can be made by checking {@link MessageEntry#isGroupMessage()}.
   * </p>
   *
   * @param message, never {@code null}
   *
   * @see ProtocolGroup#setVisibility(Visibility)
   */
  void message(@NotNull MessageEntry<M> message);


  void groupStart(@NotNull GroupStartEntry<M> group);


  void groupEnd(GroupEndEntry<M> groupEnd);


  /**
   * Returns the formatted result.
   *
   * @return  formatted result
   */
  @Contract(pure = true)
  R getResult();


  /**
   * {@inheritDoc}
   *
   * <p>
   *   A formatter of this type is automatically initialized by {@link Protocol#format(ProtocolFormatter, Level, Tag[])}.
   * </p>
   * <p>
   *   Implementing classes must make sure, that the formatter is reusable after invoking
   *   {@link #init(Level, Tag[], int)}. Thread safety however is not a requirement.
   * </p>
   */
  interface InitializableProtocolFormatter<M,R> extends ProtocolFormatter<M,R>
  {
    /**
     * This method is invoked before any other formatting methods are invoked and must initialize the formatter in
     * such way that it can be reused.
     *
     * @param level  matching protocol level
     * @param tags   matching protocol tag
     * @param estimatedGroupDepth  the estimated depth of nested protocol groups ({@code 0} means the protocol contains
     *                             no groups). The real depth depends on {@code level}, {@code tags} and group
     *                             visibility settings but is never greater than the estimated depth.
     *
     * @see Protocol#format(ProtocolFormatter, Level, Tag[])
     */
    void init(@NotNull Level level, @NotNull Tag[] tags, int estimatedGroupDepth);
  }


  /**
   * {@inheritDoc}
   */
  interface ConfiguredProtocolFormatter<M,R> extends ProtocolFormatter<M,R>
  {
    /**
     * Returns the protocol level to be used for formatting.
     *
     * @return  protocol level, never {@code null}
     */
    @Contract(pure = true)
    @NotNull Level getLevel();


    /**
     * Returns the tag to be used for formatting.
     *
     * @return  tags, never {@code null}
     */
    @Contract(pure = true)
    @NotNull Tag[] getTags();
  }
}
