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
package de.sayayi.lib.protocol.internal;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.Protocol.GenericMessage;
import de.sayayi.lib.protocol.Protocol.GenericMessageWithLevel;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolIterator;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

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
 * Core implementation of {@link ProtocolIterator} that traverses the protocol entry hierarchy in depth-first order,
 * applying level limits and message matcher filtering. It produces a flat sequence of {@link DepthEntry} instances
 * representing the visible protocol structure.
 * <p>
 * This class handles the complexities of group visibility modes, depth tracking, and entry boundary (first/last)
 * detection. Concrete iteration is performed by the nested {@link ForProtocol} and {@link ForGroup} subclasses.
 *
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
public abstract class ProtocolStructureIterator<M> implements ProtocolIterator<M>
{
  private static final ProtocolStart<?> PROTOCOL_START = new ProtocolStartImpl<>();
  private static final ProtocolEnd<?> PROTOCOL_END = new ProtocolEndImpl<>();

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


  /**
   * Creates a new structure iterator for the given protocol entries.
   *
   * @param levelLimit       maximum level to consider when matching, not {@code null}
   * @param matcher          message matcher used to filter visible entries, not {@code null}
   * @param depth            initial depth for entries produced by this iterator
   * @param protocolEntries  list of protocol entries to iterate over, not {@code null}
   * @param rootProtocol     {@code true} if this iterator is for the root protocol (emits
   *                         {@link ProtocolStart}/{@link ProtocolEnd} markers)
   */
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


  /**
   * Returns the current depth of this iterator.
   *
   * @return  current depth &gt;= 0
   */
  @Contract(pure = true)
  public int getDepth() {
    return depth;
  }


  /**
   * Sets the current depth of this iterator.
   *
   * @param depth  new depth value
   */
  protected void setDepth(int depth) {
    this.depth = depth;
  }


  /**
   * Adds an entry to the internal FIFO queue for subsequent retrieval via {@link #next()}.
   *
   * @param entry  depth entry to enqueue, not {@code null}
   */
  protected void addNextEntry(@NotNull DepthEntry<M> entry)
  {
    nextEntries[lastEntryIdx] = entry;
    lastEntryIdx = (lastEntryIdx + 1) & 3;

    // this should not happen, checking anyway
    if (lastEntryIdx == firstEntryIdx)
      throw new IllegalStateException();
  }


  /** {@inheritDoc} */
  @Override
  public boolean hasNext() {
    return firstEntryIdx != lastEntryIdx;
  }


  /**
   * Tells whether there are more visible entries remaining at the current depth level.
   *
   * @return  {@code true} if more visible entries are available at the same depth
   */
  @Contract(pure = true)
  protected boolean hasNextVisibleEntryAtSameDepth() {
    return iterator != null && iterator.hasNext();
  }


  /**
   * Disables further iteration of visible entries at the current depth. After calling this method,
   * {@link #hasNextVisibleEntryAtSameDepth()} will return {@code false}.
   */
  protected void disableVisibleEntriesAtSameDepth() {
    iterator = null;
  }


  /** {@inheritDoc} */
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


  /**
   * Tells whether a bounded visible entry has already been emitted by this iterator.
   *
   * @return  {@code true} if at least one bounded entry was previously returned
   */
  @Contract(pure = true)
  protected boolean hasPreviousVisibleEntry() {
    return previousVisibleEntry != null;
  }


  /**
   * Tells whether there is at least one more visible entry following the current one at the same or a higher depth.
   *
   * @return  {@code true} if a visible entry exists after the current position
   */
  @Contract(pure = true)
  protected abstract boolean hasVisibleEntryAfter();


  /**
   * Prepares the next entry to be returned by {@link #next()}. Called when the internal queue is empty and more
   * entries may be available.
   */
  protected abstract void prepareNextEntry();


  /**
   * Appends protocol-level boundary entries (e.g. {@link ProtocolEnd}) when all entries at the current depth have
   * been exhausted.
   */
  @SuppressWarnings("unchecked")
  protected void handleAdditionalEntriesAtCurrentDepth()
  {
    if (rootProtocol)
      addNextEntry((ProtocolEnd<M>)PROTOCOL_END);
  }


  /**
   * Prepares the next entry taking into account whether preceding entries have already been emitted. Handles
   * delegation to group sub-iterators and determines boundary flags.
   *
   * @param hasEntryBefore  {@code true} if at least one entry has been emitted before this call
   */
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




  /**
   * Iterator over a root protocol. Starts at depth 0 and iterates all visible entries matched by the given matcher.
   *
   * @param <M>  internal message object type
   */
  static final class ForProtocol<M> extends ProtocolStructureIterator<M>
  {
    /**
     * Creates a protocol iterator for the given root protocol.
     *
     * @param matcher   message matcher used to filter entries, not {@code null}
     * @param depth     initial depth (typically 0)
     * @param protocol  protocol instance to iterate, not {@code null}
     */
    ForProtocol(@NotNull MessageMatcher matcher, int depth, @NotNull ProtocolImpl<M> protocol)
    {
      super(HIGHEST, matcher, depth, protocol.getEntries(HIGHEST, matcher), true);

      prepareNextEntry(false);
    }


    /** {@inheritDoc} */
    @Override
    protected boolean hasVisibleEntryAfter() {
      return hasNextVisibleEntryAtSameDepth();
    }


    /** {@inheritDoc} */
    @Override
    protected void prepareNextEntry() {
      prepareNextEntry(hasPreviousVisibleEntry());
    }


    @Override
    public String toString() {
      return "Protocol" + super.toString();
    }
  }




  /**
   * Iterator over a protocol group. Handles group visibility modes and adjusts depth accordingly when the group
   * header is shown.
   *
   * @param <M>  internal message object type
   */
  static final class ForGroup<M> extends ProtocolStructureIterator<M>
  {
    private final boolean hasEntryAfterGroup;
    private boolean groupHeader;
    private boolean forceFirst;


    /**
     * Creates a group iterator for the given protocol group entry.
     *
     * @param levelLimit           maximum level to consider, not {@code null}
     * @param matcher              message matcher for filtering, not {@code null}
     * @param depth                current depth before entering the group
     * @param protocol             protocol group entry to iterate, not {@code null}
     * @param hasEntryBeforeGroup  {@code true} if entries exist before this group at the same level
     * @param hasEntryAfterGroup   {@code true} if entries exist after this group at the same level
     * @param rootProtocol         {@code true} if this group acts as the root protocol
     */
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


    /** {@inheritDoc} */
    @Override
    protected boolean hasVisibleEntryAfter() {
      return hasNextVisibleEntryAtSameDepth() || (!groupHeader && hasEntryAfterGroup);
    }


    /** {@inheritDoc} */
    @Override
    protected void handleAdditionalEntriesAtCurrentDepth()
    {
      if (groupHeader)
        addNextEntry(new GroupEndEntryImpl<>(getDepth()));

      super.handleAdditionalEntriesAtCurrentDepth();
    }


    /** {@inheritDoc} */
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




  /**
   * Base implementation for {@link DepthEntry} that stores the entry depth.
   *
   * @param <M>  internal message object type
   */
  public non-sealed abstract static class DepthEntryImpl<M> implements DepthEntry<M>
  {
    final int depth;


    /**
     * Creates a depth entry with the given depth.
     *
     * @param depth  entry depth (&gt;= 0)
     */
    protected DepthEntryImpl(int depth) {
      this.depth = depth;
    }


    /** {@inheritDoc} */
    @Override
    public int getDepth() {
      return depth;
    }
  }




  /**
   * Base implementation for {@link BoundedDepthEntry} that stores the entry depth and first/last boundary flags.
   *
   * @param <M>  internal message object type
   */
  public non-sealed abstract static class BoundedDepthEntryImpl<M> extends DepthEntryImpl<M>
      implements BoundedDepthEntry<M>
  {
    final boolean first;
    final boolean last;


    /**
     * Creates a bounded depth entry with the given depth and boundary flags.
     *
     * @param depth  entry depth (&gt;= 0)
     * @param first  {@code true} if this is the first entry at its depth
     * @param last   {@code true} if this is the last entry at its depth
     */
    protected BoundedDepthEntryImpl(int depth, boolean first, boolean last)
    {
      super(depth);

      this.first = first;
      this.last = last;
    }


    /** {@inheritDoc} */
    @Contract(pure = true)
    public boolean isFirst() {
      return first;
    }


    /** {@inheritDoc} */
    @Contract(pure = true)
    public boolean isLast() {
      return last;
    }
  }




  /**
   * Implementation of {@link MessageEntry} that wraps a {@link Protocol.Message} and provides depth and boundary
   * information.
   *
   * @param <M>  internal message object type
   */
  public non-sealed static class MessageEntryImpl<M> extends BoundedDepthEntryImpl<M> implements MessageEntry<M>
  {
    final Protocol.Message<M> message;


    /**
     * Creates a message entry wrapping the given protocol message.
     *
     * @param depth    entry depth (&gt;= 0)
     * @param first    {@code true} if this is the first entry at its depth
     * @param last     {@code true} if this is the last entry at its depth
     * @param message  protocol message to wrap, not {@code null}
     */
    private MessageEntryImpl(int depth, boolean first, boolean last, @NotNull Protocol.Message<M> message)
    {
      super(depth, first, last);

      this.message = message;
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull Level getLevel() {
      return message.getLevel();
    }


    /** {@inheritDoc} */
    @Override
    public Throwable getThrowable() {
      return message.getThrowable();
    }


    /** {@inheritDoc} */
    @Override
    @UnmodifiableView
    public @NotNull Set<String> getTagNames() {
      return message.getTagNames();
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull String getMessageId() {
      return message.getMessageId();
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull M getMessage() {
      return message.getMessage();
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull Map<String,Object> getParameterValues() {
      return message.getParameterValues();
    }


    /** {@inheritDoc} */
    @Override
    @Contract(value = "-> false", pure = true)
    public boolean isGroupMessage() {
      return false;
    }


    /** {@inheritDoc} */
    @Override
    public long getTimeMillis() {
      return message.getTimeMillis();
    }


    @Override
    public String toString() {
      return "MessageEntry(depth=" + depth + ",first=" + first + ",last=" + last + ',' + message + ')';
    }
  }




  /**
   * Implementation of {@link GroupMessageEntry} that represents a group whose header is visible but has no visible
   * child entries.
   *
   * @param <M>  internal message object type
   */
  public non-sealed static class GroupMessageEntryImpl<M> extends BoundedDepthEntryImpl<M> implements GroupMessageEntry<M>
  {
    final String name;
    final Level level;
    final GenericMessage<M> groupMessage;


    /**
     * Creates a group message entry for a group with a visible header but no visible children.
     *
     * @param depth         entry depth (&gt;= 0)
     * @param first         {@code true} if this is the first entry at its depth
     * @param last          {@code true} if this is the last entry at its depth
     * @param name          group name, or {@code null} if unnamed
     * @param level         effective level of the group header, not {@code null}
     * @param groupMessage  group header message, not {@code null}
     */
    private GroupMessageEntryImpl(int depth, boolean first, boolean last, String name, @NotNull Level level,
                                  @NotNull GenericMessage<M> groupMessage)
    {
      super(depth, first, last);

      this.name = name;
      this.level = level;
      this.groupMessage = groupMessage;
    }


    /** {@inheritDoc} */
    @Override
    public String getName() {
      return name;
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull Level getLevel() {
      return level;
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull String getMessageId() {
      return groupMessage.getMessageId();
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull M getMessage() {
      return groupMessage.getMessage();
    }


    /** {@inheritDoc} */
    @Override
    @UnmodifiableView
    public @NotNull Map<String,Object> getParameterValues() {
      return groupMessage.getParameterValues();
    }


    /** {@inheritDoc} */
    @Override
    @Unmodifiable
    public @NotNull Set<String> getTagNames() {
      return emptySet();
    }


    /** {@inheritDoc} */
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




  /**
   * Internal iterator that filters protocol entries, yielding only those with at least one visible entry according
   * to the current matcher.
   */
  private final class VisibleEntryIterator implements Iterator<ProtocolEntry<M>>
  {
    private final @NotNull Iterator<ProtocolEntry<M>> iterator;
    private ProtocolEntry<M> nextEntry;


    /**
     * Creates a visible entry iterator wrapping the given source iterator.
     *
     * @param iterator  source iterator over all protocol entries, not {@code null}
     */
    private VisibleEntryIterator(@NotNull Iterator<ProtocolEntry<M>> iterator)
    {
      this.iterator = iterator;

      prepareNextEntry();
    }


    /**
     * Advances to the next entry that has at least one visible sub-entry according to the enclosing iterator's matcher.
     */
    void prepareNextEntry()
    {
      while(iterator.hasNext())
        if ((nextEntry = iterator.next()).getVisibleEntryCount(matcher) > 0)
          return;

      nextEntry = null;
    }


    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
      return nextEntry != null;
    }


    /** {@inheritDoc} */
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




  /**
   * Implementation of {@link GroupStartEntry} that marks the beginning of a protocol group with a visible header and
   * at least one visible child entry.
   *
   * @param <M>  internal message object type
   */
  public non-sealed static class GroupStartEntryImpl<M> extends BoundedDepthEntryImpl<M> implements GroupStartEntry<M>
  {
    private final String name;
    private final GenericMessageWithLevel<M> groupMessage;
    private final int messageCount;


    /**
     * Creates a group start entry.
     *
     * @param name          group name, or {@code null} if unnamed
     * @param groupMessage  group header message, not {@code null}
     * @param level         effective level of the group, not {@code null}
     * @param messageCount  number of visible messages in this group (&gt;= 1)
     * @param depth         entry depth (&gt;= 0)
     * @param first         {@code true} if this is the first entry at its depth
     * @param last          {@code true} if this is the last entry at its depth
     */
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


    /** {@inheritDoc} */
    @Override
    public String getName() {
      return name;
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull GenericMessageWithLevel<M> getGroupMessage() {
      return groupMessage;
    }


    /** {@inheritDoc} */
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




  /**
   * Implementation of {@link GroupEndEntry} that marks the end of a protocol group.
   *
   * @param <M>  internal message object type
   */
  public non-sealed static class GroupEndEntryImpl<M> extends DepthEntryImpl<M> implements GroupEndEntry<M>
  {
    /**
     * Creates a group end entry at the given depth.
     *
     * @param depth  entry depth (&gt;= 0)
     */
    private GroupEndEntryImpl(int depth) {
      super(depth);
    }


    @Override
    public String toString() {
      return "GroupEndEntry(depth=" + depth + ')';
    }
  }




  /**
   * Implementation of {@link ProtocolStart} that marks the beginning of a protocol iteration.
   *
   * @param <M>  internal message object type
   */
  public static final class ProtocolStartImpl<M> implements ProtocolStart<M>
  {
    /** {@inheritDoc} */
    @Override
    public int getDepth() {
      return 0;
    }


    @Override
    public String toString() {
      return "ProtocolStart";
    }
  }




  /**
   * Implementation of {@link ProtocolEnd} that marks the end of a protocol iteration.
   *
   * @param <M>  internal message object type
   */
  public static final class ProtocolEndImpl<M> implements ProtocolEnd<M>
  {
    /** {@inheritDoc} */
    @Override
    public int getDepth() {
      return 0;
    }


    @Override
    public String toString() {
      return "ProtocolEnd";
    }
  }
}
