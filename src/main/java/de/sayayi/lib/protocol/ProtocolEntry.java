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

import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 *
 * @see Message
 * @see Group
 */
@SuppressWarnings({"unused", "squid:S2326"})
public interface ProtocolEntry<M> extends ProtocolQueryable
{
  /**
   *
   * @param <M>  internal message object type
   */
  interface Message<M> extends ProtocolEntry<M>, Protocol.Message<M> {
  }




  /**
   *
   * @param <M>  internal message object type
   */
  @SuppressWarnings("squid:S2176")
  interface Group<M> extends ProtocolEntry<M>, Protocol.Group<M>
  {
    /**
     * Returns a list of protocol entries provided by this protocol object for the given {@code level} and
     * {@code tagSelector}.
     *
     * @param matcher  message matcher, not {@code null}
     *
     * @return  a list of protocol entries, never {@code null}
     *
     * @since 1.0.0
     */
    @Contract(pure = true, value = "_ -> new")
    @NotNull List<ProtocolEntry<M>> getEntries(@NotNull MessageMatcher matcher);


    /**
     * Tells if, for the given {@code level} and {@code tagSelector}, the group header message is visible.
     *
     * @param matcher  message matcher, not {@code null}
     *
     * @return  {@code true} if the group header message is visible, {@code false} otherwise
     *
     * @since 1.0.0
     */
    @Contract(pure = true)
    boolean isHeaderVisible(@NotNull MessageMatcher matcher);


    /**
     * <p>
     *   Returns the level of the group header message for the given {@code level} and {@code tagSelector}.
     * </p>
     * <p>
     *   The group header message level is defined as the highest (= most severe) level of all containing messages
     *   and sub-groups which are visible for the given {@code level} and {@code tagSelector}.
     * </p>
     * <p>
     *   If the group does not contain any messages, the returned value will be a level with the lowest possible
     *   severity.
     * </p>
     *
     * @param matcher  message matcher, not {@code null}
     *
     * @return  header message level, never {@code null}
     *
     * @since 1.0.0
     */
    @Contract(pure = true)
    @NotNull Level getHeaderLevel(@NotNull MessageMatcher matcher);
  }
}
