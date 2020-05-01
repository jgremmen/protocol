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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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
  private final Level levelLimit;
  @Getter private final Level level;
  @Getter private final Tag[] tags;
  @Getter @Setter(AccessLevel.PROTECTED) private int depth;

  private ForGroup<M> groupIterator;
  private Iterator<ProtocolEntry<M>> iterator;
  private RankingDepthEntry<M> previousVisibleEntry;

  private final boolean rootProtocol;
  private boolean finished;

  // FIFO queue with maximum size 3
  @SuppressWarnings("unchecked")
  private final DepthEntry<M>[] nextEntries = new DepthEntry[4];
  private int firstEntryIdx, lastEntryIdx;


  protected ProtocolStructureIterator(@NotNull Level levelLimit, @NotNull Level level, @NotNull Tag[] tags, int depth,
                                      @NotNull List<ProtocolEntry<M>> protocolEntries, boolean rootProtocol)
  {
    this.levelLimit = levelLimit;
    this.level = level;
    this.tags = tags;
    this.depth = depth;

    iterator = new VisibleEntryIterator(protocolEntries.iterator());

    if (this.rootProtocol = rootProtocol)
      addNextEntry(new ProtocolStartImpl<M>());
  }


  protected void addNextEntry(@NotNull DepthEntry<M> entry)
  {
    nextEntries[lastEntryIdx] = entry;
    lastEntryIdx = (lastEntryIdx + 1) & 3;

    // this should not happen, checking anyway
    if (lastEntryIdx == firstEntryIdx)
      throw new IllegalStateException();
  }


  @Override
  public boolean hasNext() {
    return firstEntryIdx != lastEntryIdx;
  }


  @Contract(pure = true)
  protected boolean hasNextMessageOrGroup() {
    return iterator != null && iterator.hasNext();
  }


  protected void setLastMessageOrGroupEncountered() {
    iterator = null;
  }


  @Override
  public @NotNull DepthEntry<M> next()
  {
    if (!hasNext())
      throw new NoSuchElementException();

    final DepthEntry<M> entry = nextEntries[firstEntryIdx];

    nextEntries[firstEntryIdx] = null;
    firstEntryIdx = (firstEntryIdx + 1) & 3;

    if (entry instanceof RankingDepthEntry)
      previousVisibleEntry = (RankingDepthEntry<M>)entry;

    prepareNextEntry();

    return entry;
  }


  @Override
  @Contract("-> fail")
  public void remove() {
    throw new UnsupportedOperationException();
  }


  @Contract(pure = true)
  protected boolean hasPreviousVisibleEntry() {
    return previousVisibleEntry != null;
  }


  protected abstract void prepareNextEntry();


  protected void lastEntryEncountered()
  {
    if (rootProtocol)
      addNextEntry(new ProtocolEndImpl<M>());
  }


  @SuppressWarnings("squid:S3776")
  void prepareNextEntry(boolean hasEntryBefore)
  {
    while(!hasNext())
    {
      if (groupIterator != null)
      {
        if (groupIterator.hasNext())
        {
          addNextEntry(groupIterator.next());
          return;
        }
        else
          groupIterator = null;
      }

      if (!hasNextMessageOrGroup())
      {
        if (!hasNext() && !finished)
        {
          lastEntryEncountered();
          finished = true;
        }

        return;
      }

      final ProtocolEntry<M> protocolEntry = iterator.next();

      if (protocolEntry instanceof InternalProtocolEntry.Group)
      {
        groupIterator = new ProtocolStructureIterator.ForGroup<M>(levelLimit, level, tags, depth,
            (InternalProtocolEntry.Group<M>)protocolEntry, hasEntryBefore, iterator.hasNext(), false);
        continue;
      }

      addNextEntry(new MessageEntryImpl<M>(depth, !hasEntryBefore, !iterator.hasNext(),
          (ProtocolMessageEntry<M>)protocolEntry));
      return;
    }
  }


  @Override
  public String toString()
  {
    return (this instanceof ForGroup ? "GroupIterator" : "ProtocolIterator") +
        "[level=" + level + ",tags=" + Arrays.toString(tags) + ",depth=" + depth + ']';
  }




  static final class ForProtocol<M> extends ProtocolStructureIterator<M>
  {
    ForProtocol(@NotNull Level level, @NotNull Tag[] tags, int depth, @NotNull ProtocolImpl<M> protocol)
    {
      super(Level.Shared.HIGHEST, level, tags, depth,
          protocol.getEntries(Level.Shared.HIGHEST, level, tags), true);

      prepareNextEntry(false);
    }


    @Override
    protected void prepareNextEntry() {
      prepareNextEntry(hasPreviousVisibleEntry());
    }
  }




  static final class ForGroup<M> extends ProtocolStructureIterator<M>
  {
    private boolean groupHeader;
    private boolean forceFirst;


    ForGroup(@NotNull Level levelLimit, @NotNull Level level, @NotNull Tag[] tags, int depth,
             @NotNull InternalProtocolEntry.Group<M> protocol, boolean hasEntryBeforeGroup, boolean hasEntryAfterGroup,
             boolean rootProtocol)
    {
      super(protocol.getHeaderLevel0(levelLimit, level, tags), level, tags, depth,
          protocol.getEntries0(levelLimit, level, tags), rootProtocol);

      Visibility visibility = protocol.getEffectiveVisibility();

      // normalize visibility
      if (visibility == SHOW_HEADER_ALWAYS && !hasNextMessageOrGroup())
        visibility = SHOW_HEADER_ONLY;
      else if (visibility == SHOW_HEADER_IF_NOT_EMPTY)
        visibility = hasNextMessageOrGroup() ? SHOW_HEADER_ALWAYS : FLATTEN;

      switch(visibility)
      {
        case SHOW_HEADER_ALWAYS:
          // header + messages, increase depth
          setDepth(depth + 1);
          addNextEntry(new GroupStartEntryImpl<M>(protocol.getGroupMessage(), super.levelLimit,
              protocol.getVisibleGroupEntryMessageCount0(super.levelLimit, level, tags), depth + 1,
              !hasEntryBeforeGroup, !hasEntryAfterGroup));
          groupHeader = true;
          forceFirst = true;
          break;

        case SHOW_HEADER_ONLY:
          // header only, no messages; remain at same depth
          setLastMessageOrGroupEncountered();
          addNextEntry(new GroupMessageEntryImpl<M>(depth, !hasEntryBeforeGroup, !hasEntryAfterGroup,
              super.levelLimit, protocol.getGroupMessage()));
          break;

        case HIDDEN:
          // no header, no messages
          assert !hasNext();
          break;

        default:
          prepareNextEntry(hasEntryBeforeGroup);
          break;
      }
    }


    @Override
    protected void lastEntryEncountered()
    {
      if (groupHeader)
        addNextEntry(new GroupEndEntryImpl<M>(getDepth()));

      super.lastEntryEncountered();
    }


    @Override
    protected void prepareNextEntry()
    {
      prepareNextEntry(hasPreviousVisibleEntry() && !forceFirst);
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
    @Contract(value = "-> false", pure = true)
    public boolean isGroupMessage() {
      return false;
    }


    @Override
    public long getTimeMillis() {
      return message.getTimeMillis();
    }


    @Override
    public String toString() {
      return "Message[depth=" + depth + ",first=" + first + ",last=" + last + ',' + message + ']';
    }
  }




  private static class GroupMessageEntryImpl<M> extends RankingDepthEntryImpl<M> implements GroupMessageEntry<M>
  {
    final Level level;
    final GenericMessage<M> groupMessage;


    GroupMessageEntryImpl(int depth, boolean first, boolean last, Level level, GenericMessage<M> groupMessage)
    {
      super(depth, first, last);

      this.level = level;
      this.groupMessage = groupMessage;
    }


    @Override
    public @NotNull M getMessage() {
      return groupMessage.getMessage();
    }


    @Override
    public @NotNull Map<String, Object> getParameterValues() {
      return groupMessage.getParameterValues();
    }


    @Override
    public @NotNull Level getLevel() {
      return level;
    }


    @Override
    @Contract(value = "-> null", pure = true)
    public Throwable getThrowable() {
      return null;
    }


    @Override
    @Contract(value = "-> true", pure = true)
    public boolean isGroupMessage() {
      return true;
    }


    @Override
    public long getTimeMillis() {
      return groupMessage.getTimeMillis();
    }


    @Override
    public String toString()
    {
      return "GroupMessage[depth=" + depth + ",first=" + first + ",last=" + last + ",level=" + level +
             ',' + groupMessage + ']';
    }
  }




  private final class VisibleEntryIterator implements Iterator<ProtocolEntry<M>>
  {
    private final Iterator<ProtocolEntry<M>> iterator;
    private ProtocolEntry<M> nextEntry;


    VisibleEntryIterator(Iterator<ProtocolEntry<M>> iterator)
    {
      this.iterator = iterator;

      prepareNextEntry();
    }


    void prepareNextEntry()
    {
      while(iterator.hasNext())
      {
        nextEntry = iterator.next();
        if (nextEntry.getVisibleEntryCount(true, level, tags) > 0)
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




  private static abstract class RootProtocolEntry<M> implements DepthEntry<M> {
    @Override public int getDepth() { return 0; }
  }


  private static class ProtocolStartImpl<M> extends RootProtocolEntry<M> implements ProtocolStart<M> {
    @Override public String toString() { return "ProtocolStart"; }
  }


  private static class ProtocolEndImpl<M> extends RootProtocolEntry<M> implements ProtocolEnd<M> {
    @Override public String toString() { return "ProtocolEnd"; }
  }


  private static class GroupStartEntryImpl<M> extends RankingDepthEntryImpl<M> implements GroupStartEntry<M>
  {
    @Getter private final MessageWithLevel<M> groupMessage;
    @Getter private final int messageCount;


    GroupStartEntryImpl(final GenericMessage<M> groupMessage, final Level level, int messageCount, int depth,
                        boolean first, boolean last)
    {
      super(depth, first, last);

      this.groupMessage = new MessageWithLevel<M>() {
        @Override public @NotNull Level getLevel() { return level; }
        @Override public @NotNull M getMessage() { return groupMessage.getMessage(); }
        @Override public @NotNull Map<String,Object> getParameterValues() { return groupMessage.getParameterValues(); }
        @Override public long getTimeMillis() { return groupMessage.getTimeMillis(); }
      };

      this.messageCount = messageCount;
    }


    @Override
    public String toString() {
      return "GroupStart[depth=" + depth + ",level=" + groupMessage.getLevel() + ",messages=" + messageCount + ']';
    }
  }




  private static class GroupEndEntryImpl<M> extends DepthEntryImpl<M> implements GroupEndEntry<M>
  {
    GroupEndEntryImpl(int depth) {
      super(depth);
    }


    @Override
    public String toString() {
      return "GroupEnd[depth=" + depth + ']';
    }
  }
}
