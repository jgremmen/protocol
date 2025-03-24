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
import de.sayayi.lib.protocol.Protocol.GenericMessage;
import de.sayayi.lib.protocol.Protocol.GenericMessageWithLevel;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolIterator;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.FLATTEN;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.FLATTEN_ON_SINGLE_ENTRY;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ALWAYS;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_IF_NOT_EMPTY;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ONLY;
import static java.util.Collections.emptySet;


/**
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
abstract class ProtocolStructureIterator<M> implements ProtocolIterator<M>
{
  private static final ProtocolStart<?> PROTOCOL_START = new ProtocolStart<>() {
    @Override public int getDepth() { return 0; }
    @Override public String toString() { return "ProtocolStart"; }
  };

  private static final ProtocolEnd<?> PROTOCOL_END = new ProtocolEnd<>() {
    @Override public int getDepth() { return 0; }
    @Override public String toString() { return "ProtocolEnd"; }
  };


  private final @NotNull Level levelLimit;
  private final @NotNull MessageMatcher matcher;
  private final int entryCount;
  private int depth;

  private ForGroup<M> groupIterator;
  private Iterator<ProtocolEntry<M>> iterator;
  private BoundedDepthEntry<M> previousVisibleEntry;

  private final boolean rootProtocol;
  private boolean finished;

  // FIFO queue with maximum size 3
  @SuppressWarnings("unchecked")
  private final DepthEntry<M>[] nextEntries = new DepthEntry[4];
  private int firstEntryIdx;
  private int lastEntryIdx;


  @SuppressWarnings("unchecked")
  protected ProtocolStructureIterator(@NotNull Level levelLimit, @NotNull MessageMatcher matcher, int depth,
                                      @NotNull List<ProtocolEntry<M>> protocolEntries, boolean rootProtocol)
  {
    this.levelLimit = levelLimit;
    this.matcher = matcher;
    this.depth = depth;
    this.rootProtocol = rootProtocol;

    entryCount = protocolEntries.size();
    iterator = new VisibleEntryIterator(protocolEntries.iterator());

    if (rootProtocol)
      addNextEntry((ProtocolStart<M>)PROTOCOL_START);
  }


  @Contract(pure = true)
  public int getDepth() {
    return depth;
  }


  protected void setDepth(int depth) {
    this.depth = depth;
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
  protected boolean hasNextVisibleEntryAtSameDepth() {
    return iterator != null && iterator.hasNext();
  }


  protected void disableVisibleEntriesAtSameDepth() {
    iterator = null;
  }


  @Override
  public @NotNull DepthEntry<M> next()
  {
    if (!hasNext())
      throw new NoSuchElementException();

    var entry = nextEntries[firstEntryIdx];

    nextEntries[firstEntryIdx] = null;
    firstEntryIdx = (firstEntryIdx + 1) & 3;

    if (entry instanceof ProtocolIterator.BoundedDepthEntry)
      previousVisibleEntry = (BoundedDepthEntry<M>)entry;

    if (!hasNext())
      prepareNextEntry();

    return entry;
  }


  @Contract(pure = true)
  protected boolean hasPreviousVisibleEntry() {
    return previousVisibleEntry != null;
  }


  @Contract(pure = true)
  protected abstract boolean hasVisibleEntryAfter();


  protected abstract void prepareNextEntry();


  @SuppressWarnings("unchecked")
  protected void handleAdditionalEntriesAtCurrentDepth()
  {
    if (rootProtocol)
      addNextEntry((ProtocolEnd<M>)PROTOCOL_END);
  }


  @SuppressWarnings("unchecked")
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

      if (!hasNextVisibleEntryAtSameDepth())
      {
        if (!hasNext() && !finished)
        {
          handleAdditionalEntriesAtCurrentDepth();
          finished = true;
        }

        return;
      }

      var protocolEntry = iterator.next();
      if (protocolEntry instanceof InternalProtocolEntry.Group)
      {
        groupIterator = new ProtocolStructureIterator.ForGroup<>(levelLimit, matcher, depth,
            (InternalProtocolEntry.Group<M>)protocolEntry, hasEntryBefore, iterator.hasNext(),
            false);
        continue;
      }

      addNextEntry(new MessageEntryImpl<>(depth, !hasEntryBefore, !hasVisibleEntryAfter(),
          (Protocol.Message<M>)protocolEntry));
      return;
    }
  }


  @Override
  public String toString() {
    return "Iterator(matcher=" + matcher + ",depth=" + depth + ')';
  }




  static final class ForProtocol<M> extends ProtocolStructureIterator<M>
  {
    ForProtocol(@NotNull MessageMatcher matcher, int depth, @NotNull ProtocolImpl<M> protocol)
    {
      super(HIGHEST, matcher, depth, protocol.getEntries(HIGHEST, matcher), true);

      prepareNextEntry(false);
    }


    @Override
    protected boolean hasVisibleEntryAfter() {
      return hasNextVisibleEntryAtSameDepth();
    }


    @Override
    protected void prepareNextEntry() {
      prepareNextEntry(hasPreviousVisibleEntry());
    }


    @Override
    public String toString() {
      return "Protocol" + super.toString();
    }
  }




  static final class ForGroup<M> extends ProtocolStructureIterator<M>
  {
    private final boolean hasEntryAfterGroup;
    private boolean groupHeader;
    private boolean forceFirst;


    ForGroup(@NotNull Level levelLimit, @NotNull MessageMatcher matcher, int depth,
             @NotNull InternalProtocolEntry.Group<M> protocol, boolean hasEntryBeforeGroup,
             boolean hasEntryAfterGroup, boolean rootProtocol)
    {
      super(protocol.getHeaderLevel0(levelLimit, matcher), matcher, depth,
          protocol.getEntries0(levelLimit, matcher), rootProtocol);

      this.hasEntryAfterGroup = hasEntryAfterGroup;

      // normalize visibility
      var visibility = protocol.getEffectiveVisibility();
      if (visibility == SHOW_HEADER_ALWAYS && !hasNextVisibleEntryAtSameDepth())
        visibility = SHOW_HEADER_ONLY;
      else if (visibility == SHOW_HEADER_IF_NOT_EMPTY)
        visibility = hasNextVisibleEntryAtSameDepth() ? SHOW_HEADER_ALWAYS : FLATTEN;
      else if (visibility == FLATTEN_ON_SINGLE_ENTRY)
        visibility = super.entryCount > 1 ? SHOW_HEADER_ALWAYS : FLATTEN;

      switch(visibility)
      {
        case SHOW_HEADER_ALWAYS:
          // header + messages, increase depth
          setDepth(depth + 1);
          addNextEntry(new GroupStartEntryImpl<>(protocol.getName(), protocol.getGroupMessage(),
              super.levelLimit, protocol.getVisibleGroupEntryMessageCount0(super.levelLimit, matcher),
              depth + 1, !hasEntryBeforeGroup, !hasEntryAfterGroup));
          groupHeader = true;
          forceFirst = true;
          break;

        case SHOW_HEADER_ONLY:
          // header only, no messages; remain at same depth
          disableVisibleEntriesAtSameDepth();
          addNextEntry(new GroupMessageEntryImpl<>(depth, !hasEntryBeforeGroup, !hasEntryAfterGroup,
              protocol.getName(), super.levelLimit, protocol.getGroupMessage()));
          break;

        case HIDDEN:
          // no header, no messages
          break;

        default:
          prepareNextEntry(hasEntryBeforeGroup);
          break;
      }
    }


    @Override
    protected boolean hasVisibleEntryAfter() {
      return hasNextVisibleEntryAtSameDepth() || (!groupHeader && hasEntryAfterGroup);
    }


    @Override
    protected void handleAdditionalEntriesAtCurrentDepth()
    {
      if (groupHeader)
        addNextEntry(new GroupEndEntryImpl<>(getDepth()));

      super.handleAdditionalEntriesAtCurrentDepth();
    }


    @Override
    protected void prepareNextEntry()
    {
      prepareNextEntry(hasPreviousVisibleEntry() && !forceFirst);
      forceFirst = false;
    }


    @Override
    public String toString() {
      return "Group" + super.toString();
    }
  }




  abstract static class DepthEntryImpl<M> implements DepthEntry<M>
  {
    final int depth;


    protected DepthEntryImpl(int depth) {
      this.depth = depth;
    }


    @Override
    public int getDepth() {
      return depth;
    }
  }




  abstract static class BoundedDepthEntryImpl<M> extends DepthEntryImpl<M> implements BoundedDepthEntry<M>
  {
    final boolean first;
    final boolean last;


    protected BoundedDepthEntryImpl(int depth, boolean first, boolean last)
    {
      super(depth);

      this.first = first;
      this.last = last;
    }


    @Contract(pure = true)
    public boolean isFirst() {
      return first;
    }


    @Contract(pure = true)
    public boolean isLast() {
      return last;
    }
  }




  private static class MessageEntryImpl<M> extends BoundedDepthEntryImpl<M> implements MessageEntry<M>
  {
    final Protocol.Message<M> message;


    private MessageEntryImpl(int depth, boolean first, boolean last, @NotNull Protocol.Message<M> message)
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
    public @NotNull Set<String> getTagNames() {
      return message.getTagNames();
    }


    @Override
    public @NotNull String getMessageId() {
      return message.getMessageId();
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
      return "MessageEntry(depth=" + depth + ",first=" + first + ",last=" + last + ',' + message + ')';
    }
  }




  private static class GroupMessageEntryImpl<M> extends BoundedDepthEntryImpl<M> implements GroupMessageEntry<M>
  {
    final String name;
    final Level level;
    final GenericMessage<M> groupMessage;


    private GroupMessageEntryImpl(int depth, boolean first, boolean last, String name, @NotNull Level level,
                                  @NotNull GenericMessage<M> groupMessage)
    {
      super(depth, first, last);

      this.name = name;
      this.level = level;
      this.groupMessage = groupMessage;
    }


    @Override
    public String getName() {
      return name;
    }


    @Override
    public @NotNull Level getLevel() {
      return level;
    }


    @Override
    public @NotNull String getMessageId() {
      return groupMessage.getMessageId();
    }


    @Override
    public @NotNull M getMessage() {
      return groupMessage.getMessage();
    }


    @Override
    public @NotNull Map<String,Object> getParameterValues() {
      return groupMessage.getParameterValues();
    }


    @Override
    public @NotNull Set<String> getTagNames() {
      return emptySet();
    }


    @Override
    public long getTimeMillis() {
      return groupMessage.getTimeMillis();
    }


    @Override
    public String toString()
    {
      return "GroupMessageEntry(depth=" + depth + ",first=" + first + ",last=" + last +
             ",level=" + level + ',' + groupMessage + ')';
    }
  }




  private final class VisibleEntryIterator implements Iterator<ProtocolEntry<M>>
  {
    private final @NotNull Iterator<ProtocolEntry<M>> iterator;
    private ProtocolEntry<M> nextEntry;


    private VisibleEntryIterator(@NotNull Iterator<ProtocolEntry<M>> iterator)
    {
      this.iterator = iterator;

      prepareNextEntry();
    }


    void prepareNextEntry()
    {
      while(iterator.hasNext())
        if ((nextEntry = iterator.next()).getVisibleEntryCount(matcher) > 0)
          return;

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

      final ProtocolEntry<M> entry = nextEntry;

      prepareNextEntry();

      return entry;
    }
  }




  private static class GroupStartEntryImpl<M> extends BoundedDepthEntryImpl<M> implements GroupStartEntry<M>
  {
    private final String name;
    private final GenericMessageWithLevel<M> groupMessage;
    private final int messageCount;


    private GroupStartEntryImpl(String name, final GenericMessage<M> groupMessage, final Level level,
                                int messageCount, int depth, boolean first, boolean last)
    {
      super(depth, first, last);

      this.groupMessage = new GenericMessageWithLevel<>() {
        @Override public @NotNull Level getLevel() { return level; }
        @Override public @NotNull String getMessageId() { return groupMessage.getMessageId(); }
        @Override public @NotNull M getMessage() { return groupMessage.getMessage(); }
        @Override public @NotNull Map<String,Object> getParameterValues() { return groupMessage.getParameterValues(); }
        @Override public long getTimeMillis() { return groupMessage.getTimeMillis(); }
      };

      this.name = name;
      this.messageCount = messageCount;
    }


    @Override
    public String getName() {
      return name;
    }


    @Override
    public @NotNull GenericMessageWithLevel<M> getGroupMessage() {
      return groupMessage;
    }


    @Override
    public int getMessageCount() {
      return messageCount;
    }


    @Override
    public String toString()
    {
      return "GroupStartEntry(depth=" + depth + ",first=" + first + ",last=" + last +
             ",level=" + groupMessage.getLevel() + ",messages=" + messageCount + ')';
    }
  }




  private static class GroupEndEntryImpl<M> extends DepthEntryImpl<M> implements GroupEndEntry<M>
  {
    private GroupEndEntryImpl(int depth) {
      super(depth);
    }


    @Override
    public String toString() {
      return "GroupEndEntry(depth=" + depth + ')';
    }
  }
}
