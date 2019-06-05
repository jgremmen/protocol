/**
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
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolGroup.Visibility;
import de.sayayi.lib.protocol.ProtocolIterator;
import de.sayayi.lib.protocol.Tag;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.FLATTEN;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ALWAYS;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_IF_NOT_EMPTY;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ONLY;


/**
 * @author Jeroen Gremmen
 */
abstract class ProtocolStructureIterator<M> implements ProtocolIterator<M>
{
  private final Level level;
  private final Tag tag;

  private ForGroup<M> groupIterator;

  int depth;
  Iterator<ProtocolEntry<M>> iterator;
  DepthEntry lastReturnedEntry;
  DepthEntry nextEntry;


  ProtocolStructureIterator(Level level, Tag tag, int depth, AbstractProtocol<M,?> protocol)
  {
    this.level = level;
    this.tag = tag;
    this.depth = depth;

    groupIterator = null;
    iterator = new VisibleProtocolEntryAdapter(protocol.getEntries(level, tag).iterator());
  }


  @Override
  public Level getLevel() {
    return level;
  }


  @Override
  public Tag getTag() {
    return tag;
  }


  @Override
  public boolean hasNext() {
    return nextEntry != null;
  }


  @Override
  public DepthEntry next()
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


  static class ForProtocol<M> extends ProtocolStructureIterator<M>
  {
    ForProtocol(Level level, Tag tag, int depth, ProtocolImpl<M> protocol)
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
    final boolean hasEntryBeforeGroup;


    ForGroup(Level level, Tag tag, int depth, ProtocolGroupImpl<M> protocol, boolean hasEntryBeforeGroup,
             boolean hasEntryAfterGroup)
    {
      super(level, tag, depth, protocol);

      this.hasEntryBeforeGroup = hasEntryBeforeGroup;

      Visibility visibility = protocol.getEffectiveVisibility();

      if (visibility == SHOW_HEADER_ALWAYS && !iterator.hasNext())
        visibility = SHOW_HEADER_ONLY;
      else if (visibility == SHOW_HEADER_IF_NOT_EMPTY)
        visibility = iterator.hasNext() ? SHOW_HEADER_ALWAYS : FLATTEN;

      switch(visibility)
      {
        case SHOW_HEADER_ALWAYS:
          // header + messages, increase depth
          nextEntry = new GroupEntryImpl<M>(hasEntryAfterGroup, protocol.getGroupMessage(), this.depth++,
              true, false);
          break;

        case SHOW_HEADER_ONLY:
          // header only, no messages; remain at same depth
          nextEntry = new GroupEntryImpl<M>(hasEntryAfterGroup, protocol.getGroupMessage(), depth,
              !hasEntryBeforeGroup, !hasEntryAfterGroup);
          break;

        case HIDDEN:
          break;

        default:
          prepareNextEntry();
          break;
      }
    }


    @Override
    void prepareNextEntry() {
      prepareNextEntry(hasEntryBeforeGroup);
    }
  }


  static abstract class DepthEntryImpl implements DepthEntry
  {
    final int depth;
    final boolean first;
    final boolean last;


    DepthEntryImpl(int depth, boolean first, boolean last)
    {
      this.depth = depth;
      this.first = first;
      this.last = last;
    }


    @Override
    public int getDepth() {
      return depth;
    }


    @Override
    public boolean isFirst() {
      return first;
    }


    @Override
    public boolean isLast() {
      return last;
    }
  }


  private static class MessageEntryImpl<M> extends DepthEntryImpl implements MessageEntry<M>
  {
    final ProtocolMessageEntry<M> message;


    MessageEntryImpl(int depth, boolean first, boolean last, ProtocolMessageEntry<M> message)
    {
      super(depth, first, last);

      this.message = message;
    }


    @Override
    public Level getLevel() {
      return message.getLevel();
    }


    @Override
    public Set<Tag> getTags() {
      return message.getTags();
    }


    @Override
    public Throwable getThrowable() {
      return message.getThrowable();
    }


    @Override
    public M getMessage() {
      return message.getMessage();
    }


    @Override
    public Map<String, Object> getParameterValues() {
      return message.getParameterValues();
    }


    @Override
    public boolean isMatch(Level level, Tag tag) {
      return message.isMatch(level, tag);
    }


    @Override
    public List<ProtocolEntry<M>> getEntries(Level level, Tag tag) {
      return message.getEntries(level, tag);
    }


    @Override
    public boolean hasVisibleElement(Level level, Tag tag) {
      return message.hasVisibleElement(level, tag);
    }
  }


  private static class GroupEntryImpl<M> extends DepthEntryImpl implements GroupEntry<M>
  {
    private final boolean entryAfterGroup;
    private final BasicMessage<M> groupMessage;


    GroupEntryImpl(boolean entryAfterGroup, BasicMessage<M> groupMessage, int depth, boolean first, boolean last)
    {
      super(depth, first, last);

      this.entryAfterGroup = entryAfterGroup;
      this.groupMessage = groupMessage;
    }


    @Override
    public boolean hasEntryAfterGroup() {
      return entryAfterGroup;
    }


    @Override
    public BasicMessage<M> getGroupMessage() {
      return groupMessage;
    }


    @Override
    public boolean isMatch(Level level, Tag tag) {
      return true;
    }


    @Override
    public List<ProtocolEntry<M>> getEntries(Level level, Tag tag) {
      return Collections.<ProtocolEntry<M>>singletonList(groupMessage);
    }


    @Override
    public boolean hasVisibleElement(Level level, Tag tag) {
      return true;
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
        if (nextEntry.hasVisibleElement(level, tag))
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
