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
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.ProtocolIterator.DepthEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupEndEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupStartEntry;
import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import de.sayayi.lib.protocol.ProtocolIterator.ProtocolEnd;
import de.sayayi.lib.protocol.ProtocolIterator.ProtocolStart;
import de.sayayi.lib.protocol.Tag;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.formatter.TechnicalProtocolFormatter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;
import lombok.var;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true, doNotUseGetters = true, callSuper = false)
abstract class AbstractProtocol<M,B extends ProtocolMessageBuilder<M>>
    implements Protocol<M>, InternalProtocolQueryable
{
  private static final AtomicInteger PROTOCOL_ID = new AtomicInteger(0);

  @EqualsAndHashCode.Include
  @Getter private final int id;

  @Getter final ProtocolFactory<M> factory;

  final List<InternalProtocolEntry<M>> entries;
  final Map<TagSelector,Set<String>> tagPropagationMap;


  protected AbstractProtocol(@NotNull ProtocolFactory<M> factory)
  {
    this.factory = factory;

    id = PROTOCOL_ID.incrementAndGet();
    entries = new ArrayList<InternalProtocolEntry<M>>(8);
    tagPropagationMap = new HashMap<TagSelector,Set<String>>(8);
  }


  protected @NotNull Set<String> getPropagatedTags(@NotNull Set<String> tags)
  {
    if (tagPropagationMap.isEmpty())
      return tags;

    val collectedPropagatedTagDefs = new TreeSet<String>(tags);

    for(val tagPropagation: tagPropagationMap.entrySet())
      if (tagPropagation.getKey().match(collectedPropagatedTagDefs))
        collectedPropagatedTagDefs.addAll(tagPropagation.getValue());

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
  public @NotNull B error(@NotNull Throwable throwable) {
    return (B)add(Shared.ERROR).withThrowable(throwable);
  }


  @SuppressWarnings("squid:S3038")
  public abstract @NotNull B add(@NotNull Level level);


  @Override
  public boolean matches0(@NotNull Level levelLimit, @NotNull Level level, @NotNull TagSelector tagSelector)
  {
    if (levelLimit.severity() >= level.severity())
      for(val entry: entries)
        if (entry.matches0(levelLimit, level, tagSelector))
          return true;

    return false;
  }


  @Override
  public boolean matches0(@NotNull Level levelLimit, @NotNull Level level)
  {
    if (levelLimit.severity() >= level.severity())
      for(val entry: entries)
        if (entry.matches0(levelLimit, level))
          return true;

    return false;
  }


  @NotNull List<ProtocolEntry<M>> getEntries(@NotNull Level levelLimit, @NotNull Level level,
                                             @NotNull TagSelector tagSelector)
  {
    val filteredEntries = new ArrayList<ProtocolEntry<M>>();

    if (levelLimit.severity() >= level.severity())
      for(final InternalProtocolEntry<M> entry: entries)
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
    var count = 0;

    if (levelLimit.severity() >= level.severity())
      for(val entry: entries)
        count += entry.getVisibleEntryCount0(levelLimit, recursive, level, tagSelector);

    return count;
  }


  @Override
  public ProtocolGroup<M> findGroupWithName(@NotNull String name)
  {
    ProtocolGroup<M> group = null;

    for(final Iterator<ProtocolGroup<M>> groupIterator = groupIterator(); groupIterator.hasNext();)
      if ((group = groupIterator.next().findGroupWithName(name)) != null)
        break;

    return group;
  }


  @Override
  public @NotNull Set<ProtocolGroup<M>> findGroupsByRegex(@NotNull String regex)
  {
    val groups = new LinkedHashSet<ProtocolGroup<M>>();

    for(final Iterator<ProtocolGroup<M>> groupIterator = groupIterator(); groupIterator.hasNext();)
      groups.addAll(groupIterator.next().findGroupsByRegex(regex));

    return groups;
  }


  @Override
  public @NotNull ProtocolGroup<M> createGroup()
  {
    @SuppressWarnings("unchecked")
    val group = new ProtocolGroupImpl<M>((AbstractProtocol<M,ProtocolMessageBuilder<M>>)this);

    entries.add(group);

    return group;
  }


  @Override
  public @NotNull Iterator<ProtocolGroup<M>> groupIterator() {
    return new GroupIterator();
  }


  @Override
  public <R> R format(@NotNull ProtocolFormatter<M,R> formatter, @NotNull Level level) {
    return format(formatter, level, Tag.any());
  }


  @Override
  public <R> R format(@NotNull ProtocolFormatter<M,R> formatter, @NotNull Level level, @NotNull TagSelector tagSelector)
  {
    // initialize formatter
    formatter.init(factory, level, tagSelector, countGroupDepth());

    for(Iterator<DepthEntry<M>> iterator = iterator(level, tagSelector); iterator.hasNext();)
    {
      val entry = iterator.next();

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
    var depth = 0;

    for(InternalProtocolEntry<M> entry: entries)
      if (entry instanceof ProtocolGroupImpl)
        depth = Math.max(depth, 1 + ((ProtocolGroupImpl<M>)entry).countGroupDepth());

    return depth;
  }


  @Override
  public @NotNull String toStringTree() {
    return format(TechnicalProtocolFormatter.<M>getInstance());
  }




  /**
   * @since 0.7.0
   */
  protected final class GroupIterator implements Iterator<ProtocolGroup<M>>
  {
    private final Iterator<InternalProtocolEntry<M>> iterator;
    private ProtocolGroup<M> next;


    private GroupIterator()
    {
      iterator = entries.iterator();
      findNext();
    }


    private void findNext()
    {
      while(iterator.hasNext())
      {
        val entry = iterator.next();
        if (entry instanceof ProtocolGroup)
        {
          //noinspection unchecked
          next = (ProtocolGroup<M>)entry;
          return;
        }
      }

      next = null;
    }


    @Override
    public boolean hasNext() {
      return next != null;
    }


    @Override
    public ProtocolGroup<M> next()
    {
      if (!hasNext())
        throw new NoSuchElementException();

      val nextGroup = next;
      findNext();

      return nextGroup;
    }


    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
