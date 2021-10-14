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
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


/**
 * @param <M>  internal message object type
 * @param <R>  formatting result type
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
public interface ProtocolFormatter<M,R>
{
  /**
   * This method is invoked before any other formatting methods are invoked and must initialize the formatter in
   * such way that it can be reused.
   *
   * @param matcher              message matcher, never {@code null}
   * @param estimatedGroupDepth  the estimated depth of nested protocol groups ({@code 0} means the protocol contains
   *                             no groups). The real depth depends on {@code level}, {@code tags} and group
   *                             visibility settings but is never greater than the estimated depth.
   *
   * @see Protocol#format(ProtocolFormatter, MessageMatcher)
   */
  @Contract(mutates = "this")
  void init(@NotNull ProtocolFactory<M> factory, @NotNull MessageMatcher matcher, int estimatedGroupDepth);


  /**
   * <p>
   *   This method is invoked when the formatting process starts.
   * </p>
   *
   * @see #protocolEnd()
   */
  default void protocolStart() {
  }


  /**
   * <p>
   *   This method is invoked when the formatting process ends but before the result is obtained.
   * </p>
   *
   * @see #protocolStart()
   * @see #getResult()
   */
  default void protocolEnd() {
  }


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


  default void groupStart(@NotNull GroupStartEntry<M> group) {
  }


  default void groupEnd(@NotNull GroupEndEntry<M> groupEnd) {
  }


  /**
   * Returns the formatted result.
   *
   * @return  formatted result
   */
  @Contract(pure = true)
  R getResult();


  /**
   * Formats a {@code protocol} using this formatter iterating over all elements matching {@code level} and
   * {@code tagSelector}.
   *
   * @param protocol  protocol to be formatted, never {@code null}
   * @param matcher   message matcher, never {@code null}
   *
   * @return  formatted protocol, or {@code null}
   *
   * @see Protocol#format(ProtocolFormatter, MessageMatcher)
   */
  default R format(@NotNull Protocol<M> protocol, @NotNull MessageMatcher matcher) {
    return protocol.format(this, matcher);
  }




  /**
   * {@inheritDoc}
   *
   * @since 0.1.0
   */
  interface ConfiguredProtocolFormatter<M,R> extends ProtocolFormatter<M,R>
  {
    /**
     * Returns the message matcher to be used for formatting.
     *
     * @param protocolFactory  the factory from which the protocol was created
     *
     * @return  tag selector, never {@code null}
     */
    @Contract(pure = true)
    @NotNull MessageMatcher getMatcher(@NotNull ProtocolFactory<M> protocolFactory);


    /**
     * Formats a {@code protocol} using this formatter.
     *
     * @param protocol  protocol to be formatted, never {@code null}
     *
     * @return  formatted protocol, or {@code null}
     *
     * @see Protocol#format(ConfiguredProtocolFormatter)
     */
    default R format(@NotNull Protocol<M> protocol) {
      return protocol.format(this);
    }
  }
}
