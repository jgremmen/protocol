/*
 * Copyright 2020 Jeroen Gremmen
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
package de.sayayi.lib.protocol.internal;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolGroup.Visibility;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * Internal extension of {@link ProtocolEntry} that combines the public entry contract with
 * {@link InternalProtocolQueryable} for level-limited querying. This is the base type for all internal protocol entry
 * representations.
 *
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.4.1
 *
 * @see Message
 * @see Group
 */
interface InternalProtocolEntry<M> extends ProtocolEntry<M>, InternalProtocolQueryable
{
  /**
   * Internal representation of a protocol message entry, combining the public {@link ProtocolEntry.Message} contract
   * with internal level-limited querying.
   *
   * @param <M>  internal message object type
   */
  interface Message<M> extends ProtocolEntry.Message<M>, InternalProtocolEntry<M> {
  }




  /**
   * Internal representation of a protocol group entry, combining the public {@link ProtocolEntry.Group} contract with
   * internal level-limited querying and additional group metadata such as visibility and entry access.
   *
   * @param <M>  internal message object type
   */
  interface Group<M> extends ProtocolEntry.Group<M>, InternalProtocolEntry<M>
  {
    /**
     * Returns the unique internal identifier for this group.
     *
     * @return  group identifier
     */
    @Contract(pure = true)
    int getId();


    /**
     * Returns the configured visibility for this group.
     *
     * @return  configured visibility, never {@code null}
     *
     * @see #getEffectiveVisibility()
     */
    @Contract(pure = true)
    @NotNull Visibility getVisibility();


    /**
     * Returns the effective visibility for this group, which may differ from the configured
     * visibility based on the group's content and state.
     *
     * @return  effective visibility, never {@code null}
     *
     * @see #getVisibility()
     */
    @Contract(pure = true)
    @NotNull Visibility getEffectiveVisibility();


    /**
     * Returns the visible entries of this group constrained by the given level limit and matcher.
     *
     * @param levelLimit  maximum level to consider, not {@code null}
     * @param matcher     message matcher for filtering, not {@code null}
     *
     * @return  list of visible entries, never {@code null}
     */
    @Contract(pure = true, value = "_, _ -> new")
    @NotNull List<ProtocolEntry<M>> getEntries0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher);


    /**
     * Tells whether the group header is visible for the given level limit and matcher.
     *
     * @param levelLimit  maximum level to consider, not {@code null}
     * @param matcher     message matcher, not {@code null}
     *
     * @return  {@code true} if the group header is visible, {@code false} otherwise
     */
    @Contract(pure = true)
    boolean isHeaderVisible0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher);


    /**
     * Returns the aggregate level of the group header for the given level limit and matcher.
     *
     * @param levelLimit  maximum level to consider, not {@code null}
     * @param matcher     message matcher, not {@code null}
     *
     * @return  header level, never {@code null}
     */
    @Contract(pure = true)
    @NotNull Level getHeaderLevel0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher);


    /**
     * Returns the number of visible message entries in this group for the given level limit and matcher.
     *
     * @param levelLimit  maximum level to consider, not {@code null}
     * @param matcher     message matcher, not {@code null}
     *
     * @return  number of visible group entry messages &gt;= 0
     */
    @Contract(pure = true)
    int getVisibleGroupEntryMessageCount0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher);
  }
}
