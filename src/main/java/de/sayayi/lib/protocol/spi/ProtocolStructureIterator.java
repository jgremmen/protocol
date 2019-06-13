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
import de.sayayi.lib.protocol.Protocol.GenericMessage;
import de.sayayi.lib.protocol.Protocol.Message;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolGroup.Visibility;
import de.sayayi.lib.protocol.ProtocolIterator;
import de.sayayi.lib.protocol.Tag;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.FLATTEN;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ALWAYS;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_IF_NOT_EMPTY;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ONLY;


/**
 * @author Jeroen Gremmen
 */
abstract class ProtocolStructureIterator<M> implements ProtocolIterator<M>
{
  @Getter private final Level level;
  @Getter private final Tag tag;

  private ForGroup<M> groupIterator;

  int depth;
  Iterator<ProtocolEntry<M>> iterator;
  DepthEntry<M> lastReturnedEntry;
  DepthEntry<M> nextEntry;


  ProtocolStructureIterator(@NotNull Level level, @NotNull Tag tag, int depth, @NotNull AbstractProtocol<M,?> protocol)
  {
    this.level = level;
    this.tag = tag;
    this.depth = depth;

    groupIterator = null;
    iterator = new VisibleProtocolEntryAdapter(protocol.getEntries(level, tag).iterator());
  }


  @Override
  public boolean hasNext() {
    return nextEntry != null;
  }


  @Override
  public @NotNull DepthEntry<M> next()
  {
    if (!hasNext())
      throw new NoSuchElementException();

    lastReturnedEntry = nextEntry;

    prepareNextEntry();

    return lastReturnedEntry;
  }


  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }


  abstract void prepareNextEntry();


  void prepareNextEntry(boolean hasEntryBefore)
  {
    for(;;)
    {
      if (groupIterator != null)
      {
        if (groupIterator.hasNext())
        {
          nextEntry = groupIterator.next();
          return;
        }

        groupIterator = null;
      }

      if (iterator == null)
      {
        nextEntry = null;
        return;
      }

      if (!iterator.hasNext())
      {
        nextEntry = null;
        return;
      }

      ProtocolEntry<M> protocolEntry = iterator.next();

      if (protocolEntry instanceof ProtocolGroupImpl)
      {
        groupIterator = new ProtocolStructureIterator.ForGroup<M>(level, tag, depth,
            (ProtocolGroupImpl<M>)protocolEntry, hasEntryBefore, iterator.hasNext());
        continue;
      }

      nextEntry = new MessageEntryImpl<M>(depth, !hasEntryBefore, !iterator.hasNext(),
          (ProtocolMessageEntry<M>)protocolEntry);
      return;
    }
  }


  @Override
  public String toString()
  {
    return (this instanceof ForGroup ? "GroupIterator" : "ProtocolIterator") +
        "[level=" + level + ",tag=" + tag + ",depth=" + depth + ']';
  }


  static class ForProtocol<M> extends ProtocolStructureIterator<M>
  {
    ForProtocol(@NotNull Level level, @NotNull Tag tag, int depth, @NotNull ProtocolImpl<M> protocol)
    {
      super(level, tag, depth, protocol);

      prepareNextEntry(false);
    }


    @Override
    void prepareNextEntry() {
      prepareNextEntry(lastReturnedEntry != null);
    }
  }


  static class ForGroup<M> extends ProtocolStructureIterator<M>
  {
    boolean forceFirst;


    ForGroup(@NotNull Level level, @NotNull Tag tag, int depth, @NotNull ProtocolGroupImpl<M> protocol,
             boolean hasEntryBeforeGroup, boolean hasEntryAfterGroup)
    {
      super(level, tag, depth, protocol);

      Visibility visibility = protocol.getEffectiveVisibility();

      if (visibility == SHOW_HEADER_ALWAYS && !iterator.hasNext())
        visibility = SHOW_HEADER_ONLY;
      else if (visibility == SHOW_HEADER_IF_NOT_EMPTY)
        visibility = iterator.hasNext() ? SHOW_HEADER_ALWAYS : FLATTEN;

      switch(visibility)
      {
        case SHOW_HEADER_ALWAYS:
          // header + messages, increase depth
          nextEntry = new GroupEntryImpl<M>(protocol.getGroupHeader(), protocol.getHeaderLevel(level, tag),
              protocol.getVisibleEntryCount(level, tag), this.depth++, !hasEntryBeforeGroup, !hasEntryAfterGroup);
          forceFirst = true;
          break;

        case SHOW_HEADER_ONLY:
          // header only, no messages; remain at same depth
          iterator = null;
          nextEntry = new GroupMessageEntryImpl<M>(depth, !hasEntryBeforeGroup, !hasEntryAfterGroup,
              protocol.getHeaderLevel(level, tag), protocol.getGroupHeader());
          break;

        case HIDDEN:
          break;

        default:
          prepareNextEntry(hasEntryBeforeGroup);
          break;
      }
    }


    @Override
    void prepareNextEntry()
    {
      prepareNextEntry(lastReturnedEntry != null && !forceFirst);
      forceFirst = false;
    }
  }


  static abstract class DepthEntryImpl<M> implements DepthEntry<M>
  {
    @Getter final int depth;


    DepthEntryImpl(int depth) {
      this.depth = depth;
    }
  }


  static abstract class VisibleDepthEntryImpl<M> extends DepthEntryImpl<M> implements VisibleDepthEntry<M>
  {
    @Getter final boolean first;
    @Getter final boolean last;


    VisibleDepthEntryImpl(int depth, boolean first, boolean last)
    {
      super(depth);

      this.first = first;
      this.last = last;
    }
  }


  private static class MessageEntryImpl<M> extends VisibleDepthEntryImpl<M> implements MessageEntry<M>
  {
    final Protocol.Message<M> message;


    MessageEntryImpl(int depth, boolean first, boolean last, @NotNull Protocol.Message<M> message)
    {
      super(depth, first, last);

      this.message = message;
    }


    @Override
    public @NotNull Level getLevel() {
      return message.getLevel();
    }


    @Override
    public Throwable getThrowable() {
      return message.getThrowable();
    }


    @Override
    public @NotNull M getMessage() {
      return message.getMessage();
    }


    @Override
    public @NotNull Map<String,Object> getParameterValues() {
      return message.getParameterValues();
    }


    @Override
    public String toString() {
      return message.toString();
    }
  }


  private static class GroupMessageEntryImpl<M> extends VisibleDepthEntryImpl<M> implements GroupMessageEntry<M>
  {
    final Level level;
    final GenericMessage<M> groupHeader;


    GroupMessageEntryImpl(int depth, boolean first, boolean last, Level level, GenericMessage<M> groupHeader)
    {
      super(depth, first, last);

      this.level = level;
      this.groupHeader = groupHeader;
    }


    @Override
    public @NotNull M getMessage() {
      return groupHeader.getMessage();
    }


    @Override
    public @NotNull Map<String, Object> getParameterValues() {
      return groupHeader.getParameterValues();
    }


    @Override
    public @NotNull Level getLevel() {
      return level;
    }


    @Override
    public Throwable getThrowable() {
      return null;
    }


    @Override
    public String toString() {
      return groupHeader.toString();
    }
  }


  private static class GroupEntryImpl<M> extends VisibleDepthEntryImpl<M> implements GroupEntry<M>
  {
    private final Level level;
    private final GenericMessage<M> groupMessage;
    @Getter private final int messageCount;


    GroupEntryImpl(GenericMessage<M> groupMessage, Level level, int messageCount, int depth, boolean first, boolean last)
    {
      super(depth, first, last);

      this.level = level;
      this.groupMessage = groupMessage;
      this.messageCount = messageCount;
    }


    @Override
    public @NotNull Protocol.Message<M> getGroupHeader()
    {
      return new Message<M>() {
        @Override public @NotNull Level getLevel() { return level; }
        @Override public @NotNull M getMessage() { return groupMessage.getMessage(); }
        @Override public @NotNull Map<String,Object> getParameterValues() { return groupMessage.getParameterValues(); }
        @Override public Throwable getThrowable() { return null; }
      };
    }
  }


  private final class VisibleProtocolEntryAdapter implements Iterator<ProtocolEntry<M>>
  {
    private final Iterator<ProtocolEntry<M>> iterator;
    private ProtocolEntry<M> nextEntry;


    VisibleProtocolEntryAdapter(Iterator<ProtocolEntry<M>> iterator)
    {
      this.iterator = iterator;

      prepareNextEntry();
    }


    void prepareNextEntry()
    {
      while(iterator.hasNext())
      {
        nextEntry = iterator.next();
        if (nextEntry.getVisibleEntryCount(level, tag) > 0)
          return;
      }

      nextEntry = null;
    }


    @Override
    public boolean hasNext() {
      return nextEntry != null;
    }


    @Override
    public ProtocolEntry<M> next()
    {
      if (!hasNext())
        throw new NoSuchElementException();

      ProtocolEntry<M> entry = nextEntry;

      prepareNextEntry();

      return entry;
    }


    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
