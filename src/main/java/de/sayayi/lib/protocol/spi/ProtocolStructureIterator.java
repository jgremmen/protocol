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
import de.sayayi.lib.protocol.Protocol.MessageWithLevel;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolGroup.Visibility;
import de.sayayi.lib.protocol.ProtocolIterator;
import de.sayayi.lib.protocol.Tag;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.FLATTEN;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ALWAYS;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_IF_NOT_EMPTY;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ONLY;
import static de.sayayi.lib.protocol.spi.LevelHelper.max;


/**
 * @author Jeroen Gremmen
 */
abstract class ProtocolStructureIterator<M> implements ProtocolIterator<M>
{
  @Getter private final Level level;
  @Getter private final Tag[] tags;

  private ForGroup<M> groupIterator;
  private final EnumSet<StructureMarker> structureMarker;
  private final boolean rootProtocol;


  int depth;
  Iterator<ProtocolEntry<M>> iterator;
  RankingDepthEntry<M> lastVisibleEntry;
  DepthEntry<M> nextEntry;


  ProtocolStructureIterator(@NotNull Level level, @NotNull Tag[] tags, int depth,
                            @NotNull AbstractProtocol<M,?> protocol, boolean rootProtocol)
  {
    this.level = level;
    this.tags = tags;
    this.depth = depth;
    this.rootProtocol = rootProtocol;

    groupIterator = null;
    iterator = new VisibleProtocolEntryAdapter(protocol.getEntries(level, tags).iterator());
    structureMarker = EnumSet.noneOf(StructureMarker.class);
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

    DepthEntry<M> returnValue = nextEntry;

    if (nextEntry instanceof ProtocolIterator.RankingDepthEntry)
      lastVisibleEntry = (RankingDepthEntry<M>)nextEntry;

    prepareNextEntry();

    return returnValue;
  }


  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }


  abstract void prepareNextEntry();


  @SuppressWarnings("squid:S3776")
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

      if (rootProtocol && !structureMarker.contains(StructureMarker.START))
      {
        structureMarker.add(StructureMarker.START);
        nextEntry = new ProtocolStartImpl<M>();
        return;
      }

      if (!iterator.hasNext())
      {
        if (!structureMarker.contains(StructureMarker.END))
        {
          structureMarker.add(StructureMarker.END);
          nextEntry = rootProtocol ? new ProtocolEndImpl<M>() : new GroupEndEntryImpl<M>(depth);
          return;
        }

        nextEntry = null;
        return;
      }

      ProtocolEntry<M> protocolEntry = iterator.next();

      if (protocolEntry instanceof ProtocolGroupImpl)
      {
        groupIterator = new ProtocolStructureIterator.ForGroup<M>(level, tags, depth,
            (ProtocolGroupImpl<M>)protocolEntry, hasEntryBefore, iterator.hasNext(), false);
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
        "[level=" + level + ",tags=" + Arrays.toString(tags) + ",depth=" + depth + ']';
  }


  static class ForProtocol<M> extends ProtocolStructureIterator<M>
  {
    ForProtocol(@NotNull Level level, @NotNull Tag[] tags, int depth, @NotNull ProtocolImpl<M> protocol)
    {
      super(level, tags, depth, protocol, true);

      prepareNextEntry(false);
    }


    @Override
    void prepareNextEntry() {
      prepareNextEntry(lastVisibleEntry != null);
    }
  }


  static class ForGroup<M> extends ProtocolStructureIterator<M>
  {
    boolean forceFirst;


    ForGroup(@NotNull Level level, @NotNull Tag[] tags, int depth, @NotNull ProtocolGroupImpl<M> protocol,
             boolean hasEntryBeforeGroup, boolean hasEntryAfterGroup, boolean rootProtocol)
    {
      super(level, tags, depth, protocol, rootProtocol);

      Visibility visibility = protocol.getEffectiveVisibility();

      if (visibility == SHOW_HEADER_ALWAYS && !iterator.hasNext())
        visibility = SHOW_HEADER_ONLY;
      else if (visibility == SHOW_HEADER_IF_NOT_EMPTY)
        visibility = iterator.hasNext() ? SHOW_HEADER_ALWAYS : FLATTEN;

      switch(visibility)
      {
        case SHOW_HEADER_ALWAYS:
          // header + messages, increase depth
          nextEntry = new GroupStartEntryImpl<M>(protocol.getGroupHeader(),
              max(level, protocol.getHeaderLevel(level, tags)),
              protocol.getVisibleGroupEntryCount(level, tags), ++this.depth, !hasEntryBeforeGroup, !hasEntryAfterGroup);
          forceFirst = true;
          break;

        case SHOW_HEADER_ONLY:
          // header only, no messages; remain at same depth
          iterator = null;
          nextEntry = new GroupMessageEntryImpl<M>(depth, !hasEntryBeforeGroup, !hasEntryAfterGroup,
              max(level, protocol.getHeaderLevel(level, tags)), protocol.getGroupHeader());
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
      prepareNextEntry(lastVisibleEntry != null && !forceFirst);
      forceFirst = false;
    }
  }


  abstract static class DepthEntryImpl<M> implements DepthEntry<M>
  {
    @Getter final int depth;


    DepthEntryImpl(int depth) {
      this.depth = depth;
    }
  }


  abstract static class RankingDepthEntryImpl<M> extends DepthEntryImpl<M> implements RankingDepthEntry<M>
  {
    @Getter final boolean first;
    @Getter final boolean last;


    RankingDepthEntryImpl(int depth, boolean first, boolean last)
    {
      super(depth);

      this.first = first;
      this.last = last;
    }
  }


  private static class MessageEntryImpl<M> extends RankingDepthEntryImpl<M> implements MessageEntry<M>
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
    public boolean isGroupMessage() {
      return false;
    }


    @Override
    public String toString() {
      return message.toString();
    }
  }


  private static class GroupMessageEntryImpl<M> extends RankingDepthEntryImpl<M> implements GroupMessageEntry<M>
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
    public boolean isGroupMessage() {
      return true;
    }


    @Override
    public String toString() {
      return groupHeader.toString();
    }
  }


  private static class GroupStartEntryImpl<M> extends RankingDepthEntryImpl<M> implements GroupStartEntry<M>
  {
    private final Level level;
    private final GenericMessage<M> groupMessage;
    @Getter private final int messageCount;


    GroupStartEntryImpl(GenericMessage<M> groupMessage, Level level, int messageCount, int depth, boolean first, boolean last)
    {
      super(depth, first, last);

      this.level = level;
      this.groupMessage = groupMessage;
      this.messageCount = messageCount;
    }


    @Override
    public @NotNull Protocol.MessageWithLevel<M> getGroupHeader()
    {
      return new MessageWithLevel<M>() {
        @Override public @NotNull Level getLevel() { return level; }
        @Override public @NotNull M getMessage() { return groupMessage.getMessage(); }
        @Override public @NotNull Map<String,Object> getParameterValues() { return groupMessage.getParameterValues(); }
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
        if (nextEntry.getVisibleEntryCount(level, tags) > 0)
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


  private static class ProtocolStartImpl<M> implements ProtocolStart<M>
  {
    @Override
    public int getDepth() {
      return 0;
    }


    @Override
    public String toString() {
      return "ProtocolStart";
    }
  }


  private static class ProtocolEndImpl<M> implements ProtocolEnd<M>
  {
    @Override
    public int getDepth() {
      return 0;
    }


    @Override
    public String toString() {
      return "ProtocolEnd";
    }
  }


  private static class GroupEndEntryImpl<M> extends DepthEntryImpl<M> implements GroupEndEntry<M>
  {
    GroupEndEntryImpl(int depth) {
      super(depth);
    }


    @Override
    public String toString() {
      return "GroupEnd";
    }
  }


  private enum StructureMarker {
    START, END
  }
}
