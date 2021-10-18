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
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.ProtocolIterator.GroupEndEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupStartEntry;
import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import de.sayayi.lib.protocol.ProtocolIterator.ProtocolEnd;
import de.sayayi.lib.protocol.ProtocolIterator.ProtocolStart;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;
import lombok.var;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SORTED;


/**
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true, doNotUseGetters = true, callSuper = false)
abstract class AbstractProtocol<M,B extends ProtocolMessageBuilder<M>> implements Protocol<M>, InternalProtocolQueryable
{
  private static final AtomicInteger PROTOCOL_ID = new AtomicInteger(0);

  @EqualsAndHashCode.Include
  @Getter private final int id;

  @Getter final ProtocolFactory<M> factory;

  final ParameterMap parameterMap;
  final List<InternalProtocolEntry<M>> entries;
  final Map<TagSelector,Set<String>> tagPropagationMap;


  protected AbstractProtocol(@NotNull ProtocolFactory<M> factory, ParameterMap parentParameterMap)
  {
    this.factory = factory;

    id = PROTOCOL_ID.incrementAndGet();
    parameterMap = new ParameterMap(parentParameterMap);
    entries = new ArrayList<>(8);
    tagPropagationMap = new HashMap<>(8);
  }


  protected @NotNull Set<String> getPropagatedTags(@NotNull Set<String> tags)
  {
    if (tagPropagationMap.isEmpty())
      return tags;

    val collectedPropagatedTagDefs = new TreeSet<>(tags);

    for(val tagPropagation: tagPropagationMap.entrySet())
      if (tagPropagation.getKey().match(collectedPropagatedTagDefs))
        collectedPropagatedTagDefs.addAll(tagPropagation.getValue());

    return collectedPropagatedTagDefs;
  }


  public abstract @NotNull B add(@NotNull Level level);


  @Override
  public boolean matches0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher)
  {
    for(val entry: entries)
      if (entry.matches0(levelLimit, matcher))
        return true;

    return false;
  }


  @NotNull List<ProtocolEntry<M>> getEntries(@NotNull Level levelLimit, @NotNull MessageMatcher matcher)
  {
    val filteredEntries = new ArrayList<ProtocolEntry<M>>();

    for(final InternalProtocolEntry<M> entry: entries)
      if (entry.matches0(levelLimit, matcher))
      {
        if (entry instanceof InternalProtocolEntry.Group)
          filteredEntries.add(ProtocolGroupEntryAdapter.from(levelLimit, (InternalProtocolEntry.Group<M>)entry));
        else
          filteredEntries.add(ProtocolMessageEntryAdapter.from(levelLimit, (InternalProtocolEntry.Message<M>)entry));
      }

    return filteredEntries;
  }


  @Override
  public int getVisibleEntryCount0(@NotNull Level levelLimit, boolean recursive, @NotNull MessageMatcher matcher)
  {
    var count = 0;

    for(val entry: entries)
      count += entry.getVisibleEntryCount0(levelLimit, recursive, matcher);

    return count;
  }


  @Override
  public @NotNull Optional<ProtocolGroup<M>> getGroupByName(@NotNull String name)
  {
    for(final Iterator<ProtocolGroup<M>> groupIterator = groupIterator(); groupIterator.hasNext();)
    {
      val result = groupIterator.next().getGroupByName(name);
      if (result.isPresent())
        return result;
    }

    return Optional.empty();
  }


  @Override
  public void forEachGroupByRegex(@NotNull String regex, @NotNull Consumer<ProtocolGroup<M>> action) {
    groupIterator().forEachRemaining(group -> group.forEachGroupByRegex(regex, action));
  }


  @Override
  public @NotNull ProtocolGroup<M> createGroup()
  {
    @SuppressWarnings("unchecked")
    val group = new ProtocolGroupImpl<>((AbstractProtocol<M,ProtocolMessageBuilder<M>>)this);

    entries.add(group);

    return group;
  }


  @Override
  public @NotNull Iterator<ProtocolGroup<M>> groupIterator() {
    return new GroupIterator();
  }


  @Override
  public @NotNull Spliterator<ProtocolGroup<M>> groupSpliterator() {
    return Spliterators.spliterator(groupIterator(), entries.size(), DISTINCT | ORDERED | SORTED | NONNULL);
  }


  @Override
  public <R> R format(@NotNull ProtocolFormatter<M,R> formatter, @NotNull MessageMatcher matcher)
  {
    // initialize formatter
    formatter.init(factory, matcher, countGroupDepth());

    iterator(matcher).forEachRemaining(entry -> {
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
    });

    return formatter.getResult();
  }


  int countGroupDepth()
  {
    var depth = 0;

    for(val entry: entries)
      if (entry instanceof ProtocolGroupImpl)
        depth = Math.max(depth, 1 + ((ProtocolGroupImpl<M>)entry).countGroupDepth());

    return depth;
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
  }
}
