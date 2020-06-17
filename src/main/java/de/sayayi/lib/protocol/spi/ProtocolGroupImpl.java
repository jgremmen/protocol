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
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.ProtocolGroup.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.ProtocolIterator;
import de.sayayi.lib.protocol.Tag;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;
import static de.sayayi.lib.protocol.Level.Shared.LOWEST;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_IF_NOT_EMPTY;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ONLY;


/**
 * @author Jeroen Gremmen
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true, doNotUseGetters = true, callSuper = false)
final class ProtocolGroupImpl<M>
    extends AbstractProtocol<M,ProtocolMessageBuilder<M>>
    implements ProtocolGroup<M>, InternalProtocolEntry.Group<M>
{
  private static final AtomicInteger PROTOCOL_GROUP_ID = new AtomicInteger(0);


  @Getter private final AbstractProtocol<M,Protocol.ProtocolMessageBuilder<M>> parent;

  @EqualsAndHashCode.Include
  @Getter private final int id;

  @Getter private Level levelLimit;
  @Getter private Visibility visibility;
  @Getter private GroupMessage groupMessage;


  ProtocolGroupImpl(@NotNull AbstractProtocol<M,Protocol.ProtocolMessageBuilder<M>> parent)
  {
    super(parent.getFactory());

    this.parent = parent;

    id = PROTOCOL_GROUP_ID.incrementAndGet();
    levelLimit = HIGHEST;
    visibility = SHOW_HEADER_IF_NOT_EMPTY;
  }


  @Override
  protected @NotNull Set<Tag> getPropagatedTags(@NotNull Set<Tag> tags) {
    return parent.getPropagatedTags(super.getPropagatedTags(tags));
  }


  @Override
  public @NotNull Visibility getEffectiveVisibility() {
    return (groupMessage == null) ? visibility.forAbsentHeader() : visibility;
  }


  @Override
  @SuppressWarnings({ "squid:S2583", "ConstantConditions" })
  public @NotNull ProtocolGroup<M> setVisibility(@NotNull Visibility visibility)
  {
    if (visibility == null)
      throw new NullPointerException("visibility must not be null");

    this.visibility = visibility;

    return this;
  }


  @Override
  @SuppressWarnings({ "squid:S2583", "ConstantConditions" })
  public @NotNull ProtocolGroup<M> setLevelLimit(@NotNull Level level)
  {
    if (level == null)
      throw new NullPointerException("level must not be null");

    levelLimit = level;

    return this;
  }


  @Override
  public boolean isHeaderVisible0(@NotNull Level levelLimit, @NotNull Level level, @NotNull Tag ... tags)
  {
    if (groupMessage != null)
      switch(visibility)
      {
        case FLATTEN:
        case HIDDEN:
          return false;

        case SHOW_HEADER_ONLY:
        case SHOW_HEADER_ALWAYS:
          return true;

        case SHOW_HEADER_IF_NOT_EMPTY:
          return matches0(levelLimit, level, tags);

        case FLATTEN_ON_SINGLE_ENTRY:
          return super.getVisibleEntryCount0(
              LevelHelper.min(this.levelLimit, levelLimit),true, level, tags) > 1;
      }

    return false;
  }


  @Override
  public boolean isHeaderVisible(@NotNull Level level, @NotNull Tag ... tags) {
    return isHeaderVisible0(levelLimit, level, tags);
  }


  @Override
  public @NotNull Level getHeaderLevel0(@NotNull Level levelLimit, @NotNull Level level,
                                        @NotNull Tag ... tags)
  {
    Level headerLevel = LOWEST;

    levelLimit = LevelHelper.min(this.levelLimit, levelLimit);

    for(ProtocolEntry<M> entry: getEntries(levelLimit, level, tags))
    {
      Level protocolEntryLevel;

      if (entry instanceof ProtocolEntry.Message)
        protocolEntryLevel = ((ProtocolEntry.Message<M>)entry).getLevel();
      else if (entry instanceof ProtocolEntry.Group)
        protocolEntryLevel = ((ProtocolEntry.Group<M>)entry).getHeaderLevel(level, tags);
      else
        continue;

      headerLevel = LevelHelper.max(headerLevel, protocolEntryLevel);

      if (headerLevel.severity() > levelLimit.severity())
        return levelLimit;
    }

    return LevelHelper.min(levelLimit, headerLevel);
  }


  @Override
  public @NotNull Level getHeaderLevel(@NotNull Level level, @NotNull Tag ... tags) {
    return getHeaderLevel0(levelLimit, level, tags);
  }


  @Override
  public @NotNull List<ProtocolEntry<M>> getEntries0(@NotNull Level levelLimit, @NotNull Level level,
                                                     @NotNull Tag ... tags)
  {
    levelLimit = LevelHelper.min(this.levelLimit, levelLimit);

    return levelLimit.severity() >= level.severity() && getEffectiveVisibility().isShowEntries()
        ? super.getEntries(levelLimit, level, tags) : Collections.<ProtocolEntry<M>>emptyList();
  }


  @Override
  public @NotNull List<ProtocolEntry<M>> getEntries(@NotNull Level level, @NotNull Tag ... tags) {
    return getEntries(levelLimit, level, tags);
  }


  @Override
  @SuppressWarnings("squid:SwitchLastCaseIsDefaultCheck")
  public int getVisibleEntryCount0(@NotNull Level levelLimit, boolean recursive,
                                   @NotNull Level level, @NotNull Tag ... tags)
  {
    levelLimit = LevelHelper.min(this.levelLimit, levelLimit);

    if (levelLimit.severity() >= level.severity())
    {
      final Visibility effectiveVisibility = getEffectiveVisibility();

      if (effectiveVisibility == SHOW_HEADER_ONLY)
        return 1;

      final int recursiveEntryCount = super.getVisibleEntryCount0(levelLimit, true, level, tags);
      final int entryCountWithHeader = recursive ? recursiveEntryCount + 1 : 1;

      switch(effectiveVisibility)
      {
        case SHOW_HEADER_ALWAYS:
          return entryCountWithHeader;

        case SHOW_HEADER_IF_NOT_EMPTY:
          return (recursiveEntryCount == 0) ? 0 : entryCountWithHeader;

        case FLATTEN_ON_SINGLE_ENTRY:
          return (recursiveEntryCount > 1) ? entryCountWithHeader : recursiveEntryCount;

        case FLATTEN:
          return recursiveEntryCount;
      }
    }

    return 0;
  }


  @Override
  public int getVisibleEntryCount(boolean recursive, @NotNull Level level, @NotNull Tag ... tags) {
    return getVisibleEntryCount0(levelLimit, recursive, level, tags);
  }


  @Override
  public int getVisibleGroupEntryMessageCount0(@NotNull Level levelLimit, @NotNull Level level,
                                               @NotNull Tag ... tags)
  {
    return getEffectiveVisibility().isShowEntries()
        ? super.getVisibleEntryCount0(LevelHelper.min(this.levelLimit, levelLimit), false, level, tags) : 0;
  }


  @Override
  @SuppressWarnings({ "squid:S2583", "ConstantConditions" })
  public @NotNull ProtocolGroup.MessageParameterBuilder<M> setGroupMessage(@NotNull String message)
  {
    if (message == null)
      throw new NullPointerException("message must not be null");

    groupMessage = new GroupMessage(factory.processMessage(message));

    return new ParameterBuilderImpl(groupMessage);
  }


  @Override
  public @NotNull ProtocolGroup<M> removeGroupMessage()
  {
    groupMessage = null;

    return this;
  }


  @Override
  @SuppressWarnings({ "squid:S2583", "ConstantConditions" })
  public @NotNull ProtocolGroup.ProtocolMessageBuilder<M> add(@NotNull Level level)
  {
    if (level == null)
      throw new NullPointerException("level must not be null");

    return new MessageBuilder(level);
  }


  @Override
  @SuppressWarnings("unchecked")
  public @NotNull Protocol<M> getRootProtocol() {
    return (parent instanceof ProtocolGroup) ? ((ProtocolGroup<M>)parent).getRootProtocol() : parent;
  }


  @Override
  public boolean matches0(@NotNull Level levelLimit, @NotNull Level level, @NotNull Tag ... tags)
  {
    levelLimit = LevelHelper.min(this.levelLimit, levelLimit);

    return levelLimit.severity() >= level.severity() &&
           getEffectiveVisibility().isShowEntries() &&
           super.matches0(levelLimit, level, tags);
  }


  @Override
  public boolean matches(@NotNull Level level, @NotNull Tag ... tags) {
    return matches0(levelLimit, level, tags);
  }


  @Override
  public boolean matches0(@NotNull Level levelLimit, @NotNull Level level)
  {
    levelLimit = LevelHelper.min(this.levelLimit, levelLimit);

    return levelLimit.severity() >= level.severity() &&
           getEffectiveVisibility().isShowEntries() &&
           super.matches0(levelLimit, level);
  }


  @Override
  public boolean matches(@NotNull Level level) {
    return matches0(levelLimit, level);
  }


  @Override
  public @NotNull ProtocolIterator<M> iterator(@NotNull Level level, @NotNull Tag ... tags)
  {
    return new ProtocolStructureIterator.ForGroup<M>(levelLimit, level, tags, 0,this,
        false, false, true);
  }


  @Override
  public @NotNull ProtocolGroup.TargetTagBuilder<M> propagate(@NotNull String tagName) {
    return new PropagationTargetTagBuilder(resolveTagByName(tagName));
  }


  @Override
  public @NotNull ProtocolGroup.TargetTagBuilder<M> propagate(@NotNull Tag tag) {
    return new PropagationTargetTagBuilder(validateTag(tag));
  }


  @Override
  public String toString()
  {
    final StringBuilder s = new StringBuilder("ProtocolGroup[id=").append(id)
        .append(",visibility=").append(visibility);

    if (levelLimit.severity() < HIGHEST.severity())
      s.append(",levelLimit=").append(levelLimit);

    return s.append(']').toString();
  }




  private class MessageBuilder
      extends AbstractMessageBuilder<M,ProtocolGroup.ProtocolMessageBuilder<M>,ProtocolGroup.MessageParameterBuilder<M>>
      implements ProtocolGroup.ProtocolMessageBuilder<M>
  {
    private MessageBuilder(@NotNull Level level) {
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
    private GroupMessage(@NotNull M message) {
      super(message, factory.getDefaultParameterValues());
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
    private ParameterBuilderImpl(AbstractGenericMessage<M> message) {
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
    public @NotNull Level getLevelLimit() {
      return ProtocolGroupImpl.this.getLevelLimit();
    }


    @Override
    public @NotNull ProtocolGroup<M> setLevelLimit(@NotNull Level level) {
      return ProtocolGroupImpl.this.setLevelLimit(level);
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


    @Override
    public @NotNull ProtocolGroup.TargetTagBuilder<M> propagate(@NotNull String tagName) {
      return (ProtocolGroup.TargetTagBuilder<M>)super.propagate(tagName);
    }


    @Override
    public @NotNull ProtocolGroup.TargetTagBuilder<M> propagate(@NotNull Tag tag) {
      return (ProtocolGroup.TargetTagBuilder<M>)super.propagate(tag);
    }
  }


  private class PropagationTargetTagBuilder
      extends AbstractPropagationTargetTagBuilder<M,ProtocolGroup.ProtocolMessageBuilder<M>>
      implements ProtocolGroup.TargetTagBuilder<M>
  {
    PropagationTargetTagBuilder(Tag sourceTag) {
      super(ProtocolGroupImpl.this, sourceTag);
    }


    @Override
    public @NotNull ProtocolGroup<M> to(@NotNull String targetTagName) {
      return (ProtocolGroup<M>)super.to(targetTagName);
    }


    @Override
    public @NotNull ProtocolGroup<M> to(@NotNull Tag targetTag) {
      return (ProtocolGroup<M>)super.to(targetTag);
    }


    @Override
    public @NotNull ProtocolGroup<M> to(@NotNull String ... targetTagNames) {
      return (ProtocolGroup<M>)super.to(targetTagNames);
    }


    @Override
    public @NotNull ProtocolGroup<M> to(@NotNull Tag ... targetTags) {
      return (ProtocolGroup<M>)super.to(targetTags);
    }
  }
}
