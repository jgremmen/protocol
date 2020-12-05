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

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;


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
     * @param level        requested protocol level, not {@code null}
     * @param tagSelector  tag selector, not {@code null}
     *
     * @return  a list of protocol entries, never {@code null}
     */
    @Contract(pure = true, value = "_, _ -> new")
    @NotNull List<ProtocolEntry<M>> getEntries(@NotNull Level level, @NotNull TagSelector tagSelector);


    /**
     * Tells if, for the given {@code level} and {@code tagSelector}, the group header message is visible.
     *
     * @param level        protocol level, not {@code null}
     * @param tagSelector  tag selector, not {@code null}
     *
     * @return  {@code true} if the group header message is visible, {@code false} otherwise
     */
    @Contract(pure = true)
    boolean isHeaderVisible(@NotNull Level level, @NotNull TagSelector tagSelector);


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
     * @param level        protocol level, not {@code null}
     * @param tagSelector  tag selector, not {@code null}
     *
     * @return  header message level, never {@code null}
     */
    @Contract(pure = true)
    @NotNull Level getHeaderLevel(@NotNull Level level, @NotNull TagSelector tagSelector);


    /**
     * <p>
     *   Find a group with the given unique {@code name}.
     * </p>
     * <p>
     *   The search probes every descendant group starting from this protocol until a matching group is found.
     * </p>
     *
     * @param name  group name, not {@code null} or empty
     *
     * @return  protocol group with the name set or {@code null} if no group was found.
     */
    @Contract(pure = true)
    ProtocolGroup<M> findGroupWithName(@NotNull String name);


    /**
     * <p>
     *   Find all groups with names that match the given regular expression {@code regex}.
     * </p>
     * <p>
     *   The search probes every descendant group starting from this protocol for matching groups.
     * </p>
     *
     * @param regex  regular expression for matching group names, not {@code null} or empty
     *
     * @return  set of protocol groups with matching names, never {@code null}.
     */
    @Contract(pure = true)
    @NotNull Set<ProtocolGroup<M>> findGroupsByRegex(@NotNull String regex);


    /**
     * @since 1.0.0
     */
    void forEachGroupByRegex(@NotNull String regex, @NotNull Consumer<ProtocolGroup<M>> action);
  }
}
