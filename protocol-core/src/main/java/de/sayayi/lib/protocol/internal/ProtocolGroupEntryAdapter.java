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
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;
import static de.sayayi.lib.protocol.Level.compare;


/**
 * Adapter that wraps an {@link InternalProtocolEntry.Group} and constrains all level-dependent operations to a given
 * level limit. This ensures that queries for entries, header visibility, and header level are evaluated against the
 * capped level rather than the unrestricted one.
 * <p>
 * Instances are created via the {@link #from(Level, InternalProtocolEntry.Group)} factory method.
 *
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.4.1
 */
final class ProtocolGroupEntryAdapter<M> implements InternalProtocolEntry.Group<M>
{
  private final @NotNull Level levelLimit;
  private final @NotNull InternalProtocolEntry.Group<M> group;


  /**
   * Creates a new adapter that wraps the given group with a capped level.
   *
   * @param levelLimit  maximum level to apply to group operations, not {@code null}
   * @param group       internal group entry to wrap, not {@code null}
   */
  private ProtocolGroupEntryAdapter(@NotNull Level levelLimit, @NotNull InternalProtocolEntry.Group<M> group)
  {
    this.levelLimit = levelLimit;
    this.group = group;
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull List<ProtocolEntry<M>> getEntries(@NotNull MessageMatcher matcher) {
    return group.getEntries0(levelLimit, matcher);
  }


  /** {@inheritDoc} */
  @Override
  public boolean isHeaderVisible(@NotNull MessageMatcher matcher) {
    return group.isHeaderVisible0(levelLimit, matcher);
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull Level getHeaderLevel(@NotNull MessageMatcher matcher) {
    return group.getHeaderLevel0(levelLimit, matcher);
  }


  /** {@inheritDoc} */
  @Override
  public String getName() {
    return group.getName();
  }


  /** {@inheritDoc} */
  @Override
  public Protocol.GenericMessage<M> getGroupMessage() {
    return group.getGroupMessage();
  }


  /** {@inheritDoc} */
  @Override
  public boolean matches(@NotNull MessageMatcher matcher) {
    return group.matches0(levelLimit, matcher, true);
  }


  /** {@inheritDoc} */
  @Override
  public int getVisibleEntryCount(@NotNull MessageMatcher matcher) {
    return group.getVisibleEntryCount0(levelLimit, matcher);
  }


  /** {@inheritDoc} */
  @Override
  public int getId() {
    return group.getId();
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull ProtocolGroup.Visibility getVisibility() {
    return group.getVisibility();
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull ProtocolGroup.Visibility getEffectiveVisibility() {
    return group.getEffectiveVisibility();
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull List<ProtocolEntry<M>> getEntries0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher) {
    return group.getEntries0(levelLimit, matcher);
  }


  /** {@inheritDoc} */
  @Override
  public boolean isHeaderVisible0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher) {
    return group.isHeaderVisible0(levelLimit, matcher);
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull Level getHeaderLevel0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher) {
    return group.getHeaderLevel0(levelLimit, matcher);
  }


  /** {@inheritDoc} */
  @Override
  public int getVisibleGroupEntryMessageCount0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher) {
    return group.getVisibleGroupEntryMessageCount0(levelLimit, matcher);
  }


  /** {@inheritDoc} */
  @Override
  public boolean matches0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher, boolean messageOnly) {
    return group.matches0(levelLimit, matcher, messageOnly);
  }


  /** {@inheritDoc} */
  @Override
  public int getVisibleEntryCount0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher) {
    return group.getVisibleEntryCount0(levelLimit, matcher);
  }


  @Override
  public String toString()
  {
    final var s = new StringBuilder("ProtocolGroup(id=").append(group.getId())
        .append(",visibility=").append(group.getVisibility());

    if (compare(levelLimit, HIGHEST) < 0)
      s.append(",levelLimit=").append(levelLimit);

    return s.append(')').toString();
  }


  /**
   * Creates a new {@link ProtocolEntry.Group} adapter that constrains the given group entry to the specified level
   * limit.
   *
   * @param levelLimit  maximum level to apply, not {@code null}
   * @param groupEntry  internal group entry to wrap, not {@code null}
   *
   * @param <M>  internal message object type
   *
   * @return  group entry adapter with the given level limit applied, never {@code null}
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull <M> ProtocolEntry.Group<M> from(@NotNull Level levelLimit,
                                                  @NotNull InternalProtocolEntry.Group<M> groupEntry) {
    return new ProtocolGroupEntryAdapter<>(levelLimit, groupEntry);
  }
}
