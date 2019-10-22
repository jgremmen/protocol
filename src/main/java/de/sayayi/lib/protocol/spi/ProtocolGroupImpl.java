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
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolEntry.Group;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.ProtocolGroup.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.ProtocolIterator;
import de.sayayi.lib.protocol.Tag;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_IF_NOT_EMPTY;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ONLY;


/**
 * @author Jeroen Gremmen
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true, doNotUseGetters = true, callSuper = false)
final class ProtocolGroupImpl<M> extends AbstractProtocol<M,ProtocolMessageBuilder<M>>
    implements ProtocolGroup<M>, Group<M>
{
  private static final Level NO_VISIBLE_ENTRIES = new Level() {
    @Override public int severity() { return Integer.MIN_VALUE; }
    @Override public String toString() { return "NO_VISIBLE_ENTRIES"; }
  };

  private static final AtomicInteger PROTOCOL_GROUP_ID = new AtomicInteger(0);


  private final AbstractProtocol<M,Protocol.ProtocolMessageBuilder<M>> parent;

  @EqualsAndHashCode.Include
  private final int id;

  @Getter private Visibility visibility;
  @Getter private GroupMessage groupHeader;


  ProtocolGroupImpl(@NotNull AbstractProtocol<M,Protocol.ProtocolMessageBuilder<M>> parent)
  {
    super(parent.factory);

    id = PROTOCOL_GROUP_ID.incrementAndGet();

    this.parent = parent;
    visibility = SHOW_HEADER_IF_NOT_EMPTY;
  }


  @Override
  public Protocol<M> getGroupParent() {
    return parent;
  }


  @Override
  public @NotNull Visibility getEffectiveVisibility() {
    return (groupHeader == null) ? visibility.forAbsentHeader() : visibility;
  }


  @SuppressWarnings({ "squid:S2583", "ConstantConditions" })
  @Override
  public @NotNull ProtocolGroup<M> setVisibility(@NotNull Visibility visibility)
  {
    if (visibility == null)
      throw new NullPointerException("visibility must not be null");

    this.visibility = visibility;

    return this;
  }


  @Override
  public boolean isHeaderVisible(@NotNull Level level, @NotNull Tag ... tags)
  {
    if (groupHeader != null)
      switch(visibility)
      {
        case FLATTEN:
        case HIDDEN:
          return false;

        case SHOW_HEADER_ONLY:
        case SHOW_HEADER_ALWAYS:
          return true;

        case SHOW_HEADER_IF_NOT_EMPTY:
          return matches(level, tags);

        case FLATTEN_ON_SINGLE_ENTRY:
          return super.getVisibleEntryCount(true, level, tags) > 1;
      }

    return false;
  }


  @Override
  public @NotNull Level getHeaderLevel(@NotNull Level level, @NotNull Tag ... tags)
  {
    Level headerLevel = NO_VISIBLE_ENTRIES;

    for(ProtocolEntry<M> entry: getEntries(level, tags))
    {
      Level protocolEntryLevel;

      if (entry instanceof ProtocolEntry.Message)
        protocolEntryLevel = ((ProtocolEntry.Message<M>)entry).getLevel();
      else if (entry instanceof ProtocolEntry.Group)
        protocolEntryLevel = ((ProtocolEntry.Group<M>)entry).getHeaderLevel(level, tags);
      else
        continue;

      headerLevel = LevelHelper.max(headerLevel, protocolEntryLevel);
    }

    return headerLevel;
  }


  @Override
  public @NotNull List<ProtocolEntry<M>> getEntries(@NotNull Level level, @NotNull Tag ... tags) {
    return getEffectiveVisibility().isShowEntries() ? super.getEntries(level, tags) : Collections.<ProtocolEntry<M>>emptyList();
  }


  @Override
  public int getVisibleEntryCount(boolean recursive, @NotNull Level level, @NotNull Tag ... tags)
  {
    if (LevelHelper.matchLevelAndTags(level, tags))
    {
      Visibility effectiveVisibility = getEffectiveVisibility();

      if (effectiveVisibility == SHOW_HEADER_ONLY)
        return 1;

      int entryCount = super.getVisibleEntryCount(true, level, tags);
      int recursiveEntryCountWithHeader = recursive ? entryCount + 1 : 1;

      switch(effectiveVisibility)
      {
        case SHOW_HEADER_ALWAYS:
          return recursiveEntryCountWithHeader;

        case SHOW_HEADER_IF_NOT_EMPTY:
          return (entryCount == 0) ? 0 : recursiveEntryCountWithHeader;

        case FLATTEN_ON_SINGLE_ENTRY:
          return (entryCount > 1) ? recursiveEntryCountWithHeader : entryCount;

        case FLATTEN:
          return entryCount;

        default:
          break;
      }
    }

    return 0;
  }


  int getVisibleGroupEntryMessageCount(@NotNull Level level, @NotNull Tag ... tags)
  {
    switch(getEffectiveVisibility())
    {
      case HIDDEN:
      case SHOW_HEADER_ONLY:
        break;

      default:
        return super.getVisibleEntryCount(false, level, tags);
    }

    return 0;
  }


  @SuppressWarnings("squid:S2583")
  @Override
  public @NotNull ProtocolGroup.MessageParameterBuilder<M> setGroupMessage(@NotNull String message)
  {
    //noinspection ConstantConditions
    if (message == null)
      throw new NullPointerException("message must not be null");

    M processedMessage = factory.processMessage(message);
    groupHeader = new GroupMessage(processedMessage);

    return new ParameterBuilderImpl(groupHeader);
  }


  @Override
  public @NotNull ProtocolGroup<M> removeGroupMessage()
  {
    groupHeader = null;

    return this;
  }


  @SuppressWarnings("squid:S2583")
  @Override
  public @NotNull ProtocolGroup.ProtocolMessageBuilder<M> add(@NotNull Level level)
  {
    //noinspection ConstantConditions
    if (level == null)
      throw new NullPointerException("level must not be null");

    return new MessageBuilder(level);
  }


  @SuppressWarnings("unchecked")
  @Override
  public @NotNull Protocol<M> getRootProtocol() {
    return (parent instanceof ProtocolGroup) ? ((ProtocolGroup<M>)parent).getRootProtocol() : parent;
  }


  @Override
  public boolean matches(@NotNull Level level, @NotNull Tag ... tags) {
    return getEffectiveVisibility().isShowEntries() && super.matches(level, tags);
  }


  @Override
  public boolean matches(@NotNull Level level) {
    return getEffectiveVisibility().isShowEntries() && super.matches(level);
  }


  @Override
  public @NotNull ProtocolIterator<M> iterator(@NotNull Level level, @NotNull Tag ... tags)
  {
    return new ProtocolStructureIterator.ForGroup<M>(level, tags, 0,this, false,
        false, true);
  }


  @Override
  public String toString() {
    return "ProtocolGroup[id=" + id + ']';
  }


  private class MessageBuilder
      extends AbstractMessageBuilder<M,ProtocolGroup.ProtocolMessageBuilder<M>,ProtocolGroup.MessageParameterBuilder<M>>
      implements ProtocolGroup.ProtocolMessageBuilder<M>
  {
    MessageBuilder(@NotNull Level level) {
      super(ProtocolGroupImpl.this, level);
    }


    @Override
    protected @NotNull ProtocolGroup.MessageParameterBuilder<M> createMessageParameterBuilder(
        @NotNull ProtocolMessageEntry<M> message) {
      return new ParameterBuilderImpl(message);
    }
  }


  private class GroupMessage extends AbstractGenericMessage<M>
  {
    GroupMessage(@NotNull M message) {
      super(message, factory.defaultParameterValues);
    }


    @Override
    public String toString()
    {
      StringBuilder s = new StringBuilder("GroupMessage[message=").append(message);

      if (!parameterValues.isEmpty())
        s.append(",params=").append(parameterValues);

      return s.append(']').toString();
    }
  }


  private class ParameterBuilderImpl
      extends AbstractParameterBuilder<M,ProtocolGroup.MessageParameterBuilder<M>,ProtocolGroup.ProtocolMessageBuilder<M>>
      implements ProtocolGroup.MessageParameterBuilder<M>
  {
    ParameterBuilderImpl(AbstractGenericMessage<M> message) {
      super(ProtocolGroupImpl.this, message);
    }


    @Override
    public @NotNull Visibility getVisibility() {
      return ProtocolGroupImpl.this.getVisibility();
    }


    @Override
    public @NotNull Visibility getEffectiveVisibility() {
      return ProtocolGroupImpl.this.getEffectiveVisibility();
    }


    @Override
    public @NotNull ProtocolGroup<M> setVisibility(@NotNull Visibility visibility) {
      return ProtocolGroupImpl.this.setVisibility(visibility);
    }


    @Override
    public boolean isHeaderVisible(@NotNull Level level, @NotNull Tag ... tags) {
      return ProtocolGroupImpl.this.isHeaderVisible(level, tags);
    }


    @Override
    public @NotNull ProtocolGroup.MessageParameterBuilder<M> setGroupMessage(@NotNull String message) {
      return ProtocolGroupImpl.this.setGroupMessage(message);
    }


    @Override
    public @NotNull ProtocolGroup<M> removeGroupMessage() {
      return ProtocolGroupImpl.this.removeGroupMessage();
    }


    @Override
    public @NotNull Protocol<M> getRootProtocol() {
      return ProtocolGroupImpl.this.getRootProtocol();
    }


    @Override
    public @NotNull ProtocolIterator<M> iterator(@NotNull Level level, @NotNull Tag ... tags) {
      return ProtocolGroupImpl.this.iterator(level, tags);
    }
  }
}
