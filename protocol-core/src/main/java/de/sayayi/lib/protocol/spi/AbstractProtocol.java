/*
 * Copyright 2019 Jeroen Gremmen
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
package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.ProtocolIterator.DepthEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupEndEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupStartEntry;
import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import de.sayayi.lib.protocol.ProtocolIterator.ProtocolEnd;
import de.sayayi.lib.protocol.ProtocolIterator.ProtocolStart;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;
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
abstract class AbstractProtocol<M,B extends ProtocolMessageBuilder<M>>
    implements Protocol<M>, InternalProtocolQueryable
{
  private static final AtomicInteger PROTOCOL_ID = new AtomicInteger(0);

  private final int id;

  final @NotNull ProtocolFactory<M> factory;

  final @NotNull ParameterMap parameterMap;
  final @NotNull List<InternalProtocolEntry<M>> entries;
  final @NotNull Map<TagSelector,Set<String>> tagPropagationMap;


  protected AbstractProtocol(@NotNull ProtocolFactory<M> factory, ParameterMap parentParameterMap)
  {
    this.factory = factory;

    id = PROTOCOL_ID.incrementAndGet();
    parameterMap = new ParameterMap(parentParameterMap);
    entries = new ArrayList<>(8);
    tagPropagationMap = new HashMap<>(8);
  }


  @Contract(pure = true)
  public int getId() {
    return id;
  }


  @Contract(pure = true)
  public @NotNull ProtocolFactory<M> getFactory() {
    return factory;
  }


  protected @NotNull Set<String> getPropagatedTags(@NotNull Set<String> tags)
  {
    if (tagPropagationMap.isEmpty())
      return tags;

    final Set<String> collectedPropagatedTagDefs = new TreeSet<>(tags);

    for(final Entry<TagSelector,Set<String>> tagPropagation: tagPropagationMap.entrySet())
      if (tagPropagation.getKey().match(collectedPropagatedTagDefs))
        collectedPropagatedTagDefs.addAll(tagPropagation.getValue());

    return collectedPropagatedTagDefs;
  }


  public abstract @NotNull B add(@NotNull Level level);


  @Override
  public boolean matches(@NotNull String matcher) {
    return matches(factory.parseMessageMatcher(matcher));
  }


  @Override
  public boolean matches0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher,
                          boolean messageOnly)
  {
    for(final InternalProtocolEntry<M> entry: entries)
      if (entry.matches0(levelLimit, matcher, messageOnly))
        return true;

    return false;
  }


  @NotNull List<ProtocolEntry<M>> getEntries(@NotNull Level levelLimit,
                                             @NotNull MessageMatcher matcher)
  {
    final List<ProtocolEntry<M>> filteredEntries = new ArrayList<>();

    for(final InternalProtocolEntry<M> entry: entries)
      if (entry.matches0(levelLimit, matcher, false))
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
  public int getVisibleEntryCount0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher)
  {
    int count = 0;

    for(final InternalProtocolEntry<M> entry: entries)
      count += entry.getVisibleEntryCount0(levelLimit, matcher);

    return count;
  }


  @Override
  public @NotNull Optional<ProtocolGroup<M>> getGroupByName(@NotNull String name)
  {
    for(final Iterator<ProtocolGroup<M>> groupIterator = groupIterator(); groupIterator.hasNext();)
    {
      final Optional<ProtocolGroup<M>> result = groupIterator.next().getGroupByName(name);
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
    final ProtocolGroupImpl<M> group =
        new ProtocolGroupImpl<>((AbstractProtocol<M,ProtocolMessageBuilder<M>>)this);

    entries.add(group);

    return group;
  }


  @Override
  public @NotNull Spliterator<DepthEntry<M>> spliterator(@NotNull MessageMatcher matcher) {
    return new ProtocolSpliterator<>(iterator(matcher));
  }


  @Override
  public @NotNull Iterator<ProtocolGroup<M>> groupIterator() {
    return new GroupIterator();
  }


  @Override
  public @NotNull Spliterator<ProtocolGroup<M>> groupSpliterator()
  {
    return Spliterators.spliterator(groupIterator(), entries.size(),
        DISTINCT | ORDERED | SORTED | NONNULL);
  }


  @Override
  public <R> R format(@NotNull ProtocolFormatter<M,R> formatter, @NotNull MessageMatcher matcher)
  {
    // initialize formatter
    formatter.init(factory, matcher,
        countGroupDepth() + (isProtocolGroup() ? 1 : 0));

    iterator(matcher).forEachRemaining(entry -> {
      if (entry instanceof MessageEntry)
        formatter.message((MessageEntry<M>)entry);
      else if (entry instanceof GroupStartEntry)
        formatter.groupStart((GroupStartEntry<M>)entry);
      else if (entry instanceof GroupEndEntry)
        formatter.groupEnd((GroupEndEntry<M>)entry);
      else if (entry instanceof ProtocolStart)
        formatter.protocolStart();
      else if (entry instanceof ProtocolEnd)
        formatter.protocolEnd();
    });

    return formatter.getResult();
  }


  int countGroupDepth()
  {
    int depth = 0;

    for(final InternalProtocolEntry<M> entry: entries)
      if (entry instanceof ProtocolGroupImpl)
        depth = Math.max(depth, 1 + ((ProtocolGroupImpl<M>)entry).countGroupDepth());

    return depth;
  }


  @Override
  public boolean equals(Object o) {
    return this == o || o instanceof AbstractProtocol && id == ((AbstractProtocol<?,?>)o).id;
  }


  @Override
  public int hashCode() {
    return Integer.hashCode(id);
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
        final InternalProtocolEntry<M> entry = iterator.next();
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

      final ProtocolGroup<M> nextGroup = next;
      findNext();

      return nextGroup;
    }
  }
}
