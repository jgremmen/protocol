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
package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Level.Shared;
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolFormatter.ConfiguredProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolFormatter.InitializableProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.ProtocolIterator.DepthEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupEndEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupStartEntry;
import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import de.sayayi.lib.protocol.ProtocolIterator.ProtocolEnd;
import de.sayayi.lib.protocol.ProtocolIterator.ProtocolStart;
import de.sayayi.lib.protocol.Tag;
import de.sayayi.lib.protocol.TagDef;
import de.sayayi.lib.protocol.TagSelector;

import lombok.Getter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Jeroen Gremmen
 */
abstract class AbstractProtocol<M,B extends ProtocolMessageBuilder<M>> implements Protocol<M>, InternalProtocolQuery
{
  @Getter final ProtocolFactory<M> factory;

  final List<InternalProtocolEntry<M>> entries;
  final Map<TagDef,Set<TagDef>> tagPropagationMap;


  protected AbstractProtocol(@NotNull ProtocolFactory<M> factory)
  {
    this.factory = factory;

    entries = new ArrayList<InternalProtocolEntry<M>>(8);
    tagPropagationMap = new HashMap<TagDef,Set<TagDef>>(8);
  }


  protected @NotNull Set<TagDef> getPropagatedTags(@NotNull Set<TagDef> tagDefs)
  {
    if (tagPropagationMap.isEmpty())
      return tagDefs;

    final Set<TagDef> collectedPropagatedTagDefs = new HashSet<TagDef>();

    for(TagDef tagDef: tagDefs)
    {
      collectedPropagatedTagDefs.add(tagDef);

      Set<TagDef> propagatedTagDefs = tagPropagationMap.get(tagDef);
      if (propagatedTagDefs != null)
        collectedPropagatedTagDefs.addAll(propagatedTagDefs);
    }

    return collectedPropagatedTagDefs;
  }


  @Override
  public @NotNull B debug() {
    return add(Shared.DEBUG);
  }


  @Override
  public @NotNull B info() {
    return add(Shared.INFO);
  }


  @Override
  public @NotNull B warn() {
    return add(Shared.WARN);
  }


  @Override
  public @NotNull B error() {
    return add(Shared.ERROR);
  }


  @SuppressWarnings("unchecked")
  @Override
  public @NotNull B error(Throwable throwable) {
    return (B)add(Shared.ERROR).withThrowable(throwable);
  }


  @SuppressWarnings("squid:S3038")
  public abstract @NotNull B add(@NotNull Level level);


  @Override
  public boolean matches0(@NotNull Level levelLimit, @NotNull Level level, @NotNull TagSelector tagSelector)
  {
    if (levelLimit.severity() >= level.severity())
      for(InternalProtocolEntry<M> entry: entries)
        if (entry.matches0(levelLimit, level, tagSelector))
          return true;

    return false;
  }


  @Override
  public boolean matches0(@NotNull Level levelLimit, @NotNull Level level)
  {
    if (levelLimit.severity() >= level.severity())
      for(InternalProtocolEntry<M> entry: entries)
        if (entry.matches0(levelLimit, level))
          return true;

    return false;
  }


  @NotNull List<ProtocolEntry<M>> getEntries(@NotNull Level levelLimit, @NotNull Level level,
                                             @NotNull TagSelector tagSelector)
  {
    final List<ProtocolEntry<M>> filteredEntries = new ArrayList<ProtocolEntry<M>>();

    if (levelLimit.severity() >= level.severity())
      for(InternalProtocolEntry<M> entry: entries)
        if (entry.matches0(levelLimit, level, tagSelector))
        {
          if (entry instanceof InternalProtocolEntry.Group)
          {
            filteredEntries.add(ProtocolGroupEntryAdapter.from(levelLimit,
                (InternalProtocolEntry.Group<M>)entry));
          }
          else
          {
            filteredEntries.add(ProtocolMessageEntryAdapter.from(levelLimit,
                (InternalProtocolEntry.Message<M>)entry));
          }
        }

    return filteredEntries;
  }


  @Override
  public int getVisibleEntryCount0(@NotNull Level levelLimit, boolean recursive, @NotNull Level level,
                                   @NotNull TagSelector tagSelector)
  {
    int count = 0;

    if (levelLimit.severity() >= level.severity())
      for(InternalProtocolEntry<M> entry: entries)
        count += entry.getVisibleEntryCount0(levelLimit, recursive, level, tagSelector);

    return count;
  }


  @Override
  public @NotNull ProtocolGroup<M> createGroup()
  {
    @SuppressWarnings("unchecked")
    final ProtocolGroupImpl<M> group =
        new ProtocolGroupImpl<M>((AbstractProtocol<M,ProtocolMessageBuilder<M>>)this);

    entries.add(group);

    return group;
  }


  @Override
  public <R> R format(@NotNull ProtocolFormatter<M,R> formatter, @NotNull Level level) {
    return format(formatter, level, Tag.any());
  }


  @Override
  public <R> R format(@NotNull ProtocolFormatter<M,R> formatter, @NotNull Level level, @NotNull TagSelector tagSelector)
  {
    // initialize formatter
    if (formatter instanceof InitializableProtocolFormatter)
      ((InitializableProtocolFormatter<M,R>)formatter).init(level, tagSelector, countGroupDepth());

    for(Iterator<DepthEntry<M>> iterator = iterator(level, tagSelector); iterator.hasNext();)
    {
      DepthEntry<M> entry = iterator.next();

      if (entry instanceof ProtocolStart)
        formatter.protocolStart();
      else if (entry instanceof ProtocolEnd)
        formatter.protocolEnd();
      else if (entry instanceof MessageEntry)
        formatter.message((MessageEntry<M>)entry);
      else if (entry instanceof GroupStartEntry)
        formatter.groupStart((GroupStartEntry<M>)entry);
      else if (entry instanceof GroupEndEntry)
        formatter.groupEnd((GroupEndEntry<M>)entry);
    }

    return formatter.getResult();
  }


  @Override
  public <R> R format(@NotNull ConfiguredProtocolFormatter<M,R> formatter) {
    return format(formatter, formatter.getLevel(), formatter.getTagSelector(factory));
  }


  int countGroupDepth()
  {
    int depth = 0;

    for(InternalProtocolEntry<M> entry: entries)
      if (entry instanceof ProtocolGroupImpl)
        depth = Math.max(depth, 1 + ((ProtocolGroupImpl<M>)entry).countGroupDepth());

    return depth;
  }
}
