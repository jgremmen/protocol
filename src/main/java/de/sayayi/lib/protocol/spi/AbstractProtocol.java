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
import de.sayayi.lib.protocol.ProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolFormatter.ConfiguredProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolFormatter.InitializableProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.ProtocolIterator;
import de.sayayi.lib.protocol.ProtocolIterator.DepthEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupEndEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupStartEntry;
import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import de.sayayi.lib.protocol.ProtocolIterator.ProtocolEnd;
import de.sayayi.lib.protocol.ProtocolIterator.ProtocolStart;
import de.sayayi.lib.protocol.Tag;

import lombok.Getter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author Jeroen Gremmen
 */
abstract class AbstractProtocol<M,B extends ProtocolMessageBuilder<M>> implements Protocol<M>
{
  @Getter final AbstractProtocolFactory<M> factory;
  final List<ProtocolEntry<M>> entries;


  AbstractProtocol(@NotNull AbstractProtocolFactory<M> factory)
  {
    this.factory = factory;
    entries = new ArrayList<ProtocolEntry<M>>(8);
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
  public boolean matches(@NotNull Level level, @NotNull Tag ... tags)
  {
    if (LevelHelper.matchLevelAndTags(level, tags))
      for(ProtocolEntry<M> entry: entries)
        if (entry.matches(level, tags))
          return true;

    return false;
  }


  @Override
  public boolean matches(@NotNull Level level, @NotNull String ... tagNames)
  {
    Tag[] tags = new Tag[tagNames.length];

    for(int n = 0; n < tagNames.length; n++)
      tags[n] = factory.getTagByName(tagNames[n]);

    return matches(level, tags);
  }


  @Override
  public boolean matches(@NotNull Level level)
  {
    for(ProtocolEntry<M> entry: entries)
      if (entry.matches(level))
        return true;

    return false;
  }


  @NotNull List<ProtocolEntry<M>> getEntries(@NotNull Level level, @NotNull Tag ... tags)
  {
    List<ProtocolEntry<M>> filteredEntries = new ArrayList<ProtocolEntry<M>>();

    if (LevelHelper.matchLevelAndTags(level, tags))
      for(ProtocolEntry<M> entry: entries)
        if (entry.matches(level, tags))
          filteredEntries.add(entry);

    return filteredEntries;
  }


  @Override
  public int getVisibleEntryCount(boolean recursive, @NotNull Level level, @NotNull Tag ... tags)
  {
    int count = 0;

    if (LevelHelper.matchLevelAndTags(level, tags))
      for(ProtocolEntry<M> entry: entries)
        count += entry.getVisibleEntryCount(recursive, level, tags);

    return count;
  }


  @Override
  public @NotNull ProtocolGroup<M> createGroup()
  {
    @SuppressWarnings("unchecked")
    ProtocolGroupImpl<M> group = new ProtocolGroupImpl<M>((AbstractProtocol<M,ProtocolMessageBuilder<M>>)this);

    entries.add(group);

    return group;
  }


  @Override
  public <R> R format(@NotNull ProtocolFormatter<M, R> formatter, @NotNull Level level) {
    return format(formatter, level, factory.getDefaultTag());
  }


  @Override
  public <R> R format(@NotNull ProtocolFormatter<M, R> formatter, @NotNull Level level, @NotNull String... tagNames)
  {
    int tagCount = tagNames.length;
    Tag[] tags = new Tag[tagCount];

    for(int n = 0; n < tagCount; n++)
    {
      Tag tag = factory.getTagByName(tagNames[n]);
      if (tag == null)
        throw new IllegalArgumentException("tag with name " + tagNames[n] + " does not exist");

      tags[n] = tag;
    }

    return format(formatter, level, tags);
  }


  @Override
  public <R> R format(@NotNull ProtocolFormatter<M,R> formatter, @NotNull Level level, @NotNull Tag ... tags)
  {
    // initialize formatter
    if (formatter instanceof InitializableProtocolFormatter)
      ((InitializableProtocolFormatter<M,R>)formatter).init(level, tags, countGroupDepth());

    for(Iterator<DepthEntry<M>> iterator = iterator(level, tags); iterator.hasNext();)
    {
      DepthEntry<M> entry = iterator.next();

      if (entry instanceof ProtocolStart)
        formatter.protocolStart();
      else if (entry instanceof ProtocolEnd)
        formatter.protocolEnd();
      else if (entry instanceof MessageEntry)
        formatter.message((MessageEntry<M>)entry);
      else if (entry instanceof ProtocolIterator.GroupStartEntry)
        formatter.groupStart((GroupStartEntry<M>)entry);
      else if (entry instanceof GroupEndEntry)
        formatter.groupEnd((GroupEndEntry<M>)entry);
    }

    return formatter.getResult();
  }


  @Override
  public <R> R format(@NotNull ConfiguredProtocolFormatter<M,R> formatter) {
    return format(formatter, formatter.getLevel(), formatter.getTags(factory));
  }


  int countGroupDepth()
  {
    int depth = 0;

    for(ProtocolEntry<M> entry: entries)
      if (entry instanceof ProtocolGroupImpl)
        depth = Math.max(depth, 1 + ((ProtocolGroupImpl<M>)entry).countGroupDepth());

    return depth;
  }
}
