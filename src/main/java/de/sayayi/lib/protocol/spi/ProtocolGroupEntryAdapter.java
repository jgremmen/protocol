/*
 * Copyright 2020 Jeroen Gremmen
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
package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.Tag;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * @author Jeroen Gremmen
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class ProtocolGroupEntryAdapter<M> implements InternalProtocolEntry.Group<M>
{
  private final Level levelLimit;
  private final InternalProtocolEntry.Group<M> group;


  @NotNull
  @Override
  public List<ProtocolEntry<M>> getEntries(@NotNull Level level, @NotNull Tag... tags) {
    return group.getEntries0(levelLimit, level, tags);
  }


  @Override
  public boolean isHeaderVisible(@NotNull Level level, @NotNull Tag... tags) {
    return group.isHeaderVisible0(levelLimit, level, tags);
  }


  @NotNull
  @Override
  public Level getHeaderLevel(@NotNull Level level, @NotNull Tag... tags) {
    return group.getHeaderLevel0(levelLimit, level, tags);
  }


  @Override
  public Protocol.GenericMessage<M> getGroupMessage() {
    return group.getGroupMessage();
  }


  @Override
  public boolean matches(@NotNull Level level, @NotNull Tag... tags) {
    return group.matches0(levelLimit, level, tags);
  }


  @Override
  public boolean matches(@NotNull Level level) {
    return group.matches0(levelLimit, level);
  }


  @Override
  public int getVisibleEntryCount(boolean recursive, @NotNull Level level, @NotNull Tag... tags) {
    return group.getVisibleEntryCount0(levelLimit, recursive, level, tags);
  }


  @Override
  public int getId() {
    return group.getId();
  }


  @Override
  public ProtocolGroup.Visibility getVisibility() {
    return group.getVisibility();
  }


  @Override
  public ProtocolGroup.Visibility getEffectiveVisibility() {
    return group.getEffectiveVisibility();
  }


  @NotNull
  @Override
  public List<ProtocolEntry<M>> getEntries0(@NotNull Level levelLimit, @NotNull Level level, @NotNull Tag... tags) {
    return group.getEntries0(levelLimit, level, tags);
  }


  @Override
  public boolean isHeaderVisible0(@NotNull Level levelLimit, @NotNull Level level, @NotNull Tag... tags) {
    return group.isHeaderVisible0(levelLimit, level, tags);
  }


  @NotNull
  @Override
  public Level getHeaderLevel0(@NotNull Level levelLimit, @NotNull Level level, @NotNull Tag... tags) {
    return group.getHeaderLevel0(levelLimit, level, tags);
  }


  @Override
  public int getVisibleGroupEntryMessageCount0(@NotNull Level levelLimit, @NotNull Level level, @NotNull Tag... tags) {
    return group.getVisibleGroupEntryMessageCount0(levelLimit, level, tags);
  }


  @Override
  public boolean matches0(@NotNull Level levelLimit, @NotNull Level level, @NotNull Tag... tags) {
    return group.matches0(levelLimit, level, tags);
  }


  @Override
  public boolean matches0(@NotNull Level levelLimit, @NotNull Level level) {
    return group.matches0(levelLimit, level);
  }


  @Override
  public int getVisibleEntryCount0(@NotNull Level levelLimit, boolean recursive, @NotNull Level level, @NotNull Tag... tags) {
    return group.getVisibleEntryCount0(levelLimit, recursive, level, tags);
  }


  @Override
  public String toString()
  {
    final StringBuilder s = new StringBuilder("ProtocolGroup[id=").append(group.getId())
        .append(",visibility=").append(group.getVisibility());

    if (levelLimit.severity() < Level.Shared.HIGHEST.severity())
      s.append(",levelLimit=").append(levelLimit);

    return s.append(']').toString();
  }


  static <M> ProtocolEntry.Group<M> from(@NotNull Level levelLimit,
                                         @NotNull InternalProtocolEntry.Group<M> groupEntry) {
    return new ProtocolGroupEntryAdapter<M>(levelLimit, groupEntry);
  }
}
