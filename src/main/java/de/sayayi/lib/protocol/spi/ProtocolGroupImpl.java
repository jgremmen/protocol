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
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.exception.ProtocolException;

import lombok.Getter;
import lombok.val;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;
import static de.sayayi.lib.protocol.Level.Shared.LOWEST;
import static de.sayayi.lib.protocol.Level.compare;
import static de.sayayi.lib.protocol.Level.max;
import static de.sayayi.lib.protocol.Level.min;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_IF_NOT_EMPTY;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ONLY;
import static java.util.Objects.requireNonNull;


/**
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 */
@SuppressWarnings("java:S2160")
final class ProtocolGroupImpl<M>
    extends AbstractProtocol<M,ProtocolMessageBuilder<M>>
    implements ProtocolGroup<M>, InternalProtocolEntry.Group<M>
{
  @Getter private final AbstractProtocol<M,Protocol.ProtocolMessageBuilder<M>> parent;

  @Getter private Level levelLimit;
  @Getter private Visibility visibility;
  @Getter private GroupMessage groupMessage;
  @Getter private String name;


  ProtocolGroupImpl(@NotNull AbstractProtocol<M,Protocol.ProtocolMessageBuilder<M>> parent)
  {
    super(parent.getFactory());

    this.parent = parent;

    levelLimit = HIGHEST;
    visibility = SHOW_HEADER_IF_NOT_EMPTY;
  }


  @Override
  protected @NotNull Set<String> getPropagatedTags(@NotNull Set<String> tags) {
    return parent.getPropagatedTags(super.getPropagatedTags(tags));
  }


  @Override
  public @NotNull Visibility getEffectiveVisibility() {
    return groupMessage == null ? visibility.forAbsentHeader() : visibility;
  }


  @Override
  public @NotNull ProtocolGroup<M> setVisibility(@NotNull Visibility visibility)
  {
    this.visibility = requireNonNull(visibility, "visibility must not be null");
    return this;
  }


  @Override
  public @NotNull ProtocolGroup<M> setLevelLimit(@NotNull Level level)
  {
    levelLimit = requireNonNull(level, "level must not be null");
    return this;
  }


  @Override
  public boolean isHeaderVisible0(@NotNull Level levelLimit, @NotNull Level level, @NotNull TagSelector tagSelector)
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
          return matches0(levelLimit, level, tagSelector);

        case FLATTEN_ON_SINGLE_ENTRY:
          return super.getVisibleEntryCount0(min(this.levelLimit, levelLimit),true, level, tagSelector) > 1;
      }

    return false;
  }


  @Override
  public boolean isHeaderVisible(@NotNull Level level, @NotNull TagSelector tagSelector) {
    return isHeaderVisible0(levelLimit, level, tagSelector);
  }


  @Override
  public @NotNull Level getHeaderLevel0(@NotNull Level levelLimit, @NotNull Level level,
                                        @NotNull TagSelector tagSelector)
  {
    Level headerLevel = LOWEST;

    levelLimit = min(this.levelLimit, levelLimit);

    for(ProtocolEntry<M> entry: getEntries(levelLimit, level, tagSelector))
    {
      Level protocolEntryLevel;

      if (entry instanceof ProtocolEntry.Message)
        protocolEntryLevel = ((ProtocolEntry.Message<M>)entry).getLevel();
      else if (entry instanceof ProtocolEntry.Group)
        protocolEntryLevel = ((ProtocolEntry.Group<M>)entry).getHeaderLevel(level, tagSelector);
      else
        continue;

      headerLevel = max(headerLevel, protocolEntryLevel);

      if (compare(headerLevel, levelLimit) > 0)
        return levelLimit;
    }

    return min(levelLimit, headerLevel);
  }


  @Override
  public @NotNull Level getHeaderLevel(@NotNull Level level, @NotNull TagSelector tagSelector) {
    return getHeaderLevel0(levelLimit, level, tagSelector);
  }


  @Override
  public @NotNull List<ProtocolEntry<M>> getEntries0(@NotNull Level levelLimit, @NotNull Level level,
                                                     @NotNull TagSelector tagSelector)
  {
    levelLimit = min(this.levelLimit, levelLimit);

    return compare(levelLimit, level) >= 0 && getEffectiveVisibility().isShowEntries()
        ? super.getEntries(levelLimit, level, tagSelector) : Collections.emptyList();
  }


  @Override
  public @NotNull List<ProtocolEntry<M>> getEntries(@NotNull Level level, @NotNull TagSelector tagSelector) {
    return getEntries(levelLimit, level, tagSelector);
  }


  @Override
  public int getVisibleEntryCount0(@NotNull Level levelLimit, boolean recursive,
                                   @NotNull Level level, @NotNull TagSelector tagSelector)
  {
    levelLimit = min(this.levelLimit, levelLimit);

    if (compare(levelLimit, level) >= 0)
    {
      val effectiveVisibility = getEffectiveVisibility();

      if (effectiveVisibility == SHOW_HEADER_ONLY)
        return 1;

      val recursiveEntryCount = super.getVisibleEntryCount0(levelLimit, true, level, tagSelector);
      val entryCountWithHeader = recursive ? recursiveEntryCount + 1 : 1;

      switch(effectiveVisibility)
      {
        case SHOW_HEADER_ALWAYS:
          return entryCountWithHeader;

        case SHOW_HEADER_IF_NOT_EMPTY:
          return recursiveEntryCount == 0 ? 0 : entryCountWithHeader;

        case FLATTEN_ON_SINGLE_ENTRY:
          return recursiveEntryCount > 1 ? entryCountWithHeader : recursiveEntryCount;

        case FLATTEN:
          return recursiveEntryCount;

        default:
          break;
      }
    }

    return 0;
  }


  @Override
  public int getVisibleEntryCount(boolean recursive, @NotNull Level level, @NotNull TagSelector tagSelector) {
    return getVisibleEntryCount0(levelLimit, recursive, level, tagSelector);
  }


  @Override
  public int getVisibleGroupEntryMessageCount0(@NotNull Level levelLimit, @NotNull Level level,
                                               @NotNull TagSelector tagSelector)
  {
    return getEffectiveVisibility().isShowEntries()
        ? super.getVisibleEntryCount0(min(this.levelLimit, levelLimit), false, level, tagSelector) : 0;
  }


  @Override
  public @NotNull ProtocolGroup.MessageParameterBuilder<M> setGroupMessage(@NotNull String message)
  {
    groupMessage = new GroupMessage(factory.getMessageProcessor()
        .processMessage(requireNonNull(message, "message must not be null")));

    return new ParameterBuilderImpl(groupMessage);
  }


  @Override
  public @NotNull ProtocolGroup<M> removeGroupMessage()
  {
    groupMessage = null;

    return this;
  }


  @Override
  public @NotNull ProtocolGroup<M> setName(String name)
  {
    if (name == null || name.isEmpty())
      this.name = null;
    else if (!name.equals(this.name))
    {
      getRootProtocol().forGroupWithName(name,
          group -> { throw new ProtocolException("group name '" + name + "' must be unique"); });

      this.name = name;
    }

    return this;
  }


  @Override
  public ProtocolGroup<M> findGroupWithName(@NotNull String name)
  {
    if (name.isEmpty())
      throw new ProtocolException("name must not be empty");

    return name.equals(this.name) ? this : super.findGroupWithName(name);
  }


  @Override
  public boolean forGroupWithName(@NotNull String name, @NotNull Consumer<ProtocolGroup<M>> action)
  {
    if (name.equals(this.name))
    {
      action.accept(this);
      return true;
    }

    return super.forGroupWithName(name, action);
  }


  @Override
  public @NotNull Set<ProtocolGroup<M>> findGroupsByRegex(@NotNull String regex)
  {
    if (name.isEmpty())
      throw new ProtocolException("regex must not be empty");

    val groups = super.findGroupsByRegex(regex);

    if (name.matches(regex))
      groups.add(this);

    return groups;
  }


  @Override
  public void forEachGroupByRegex(@NotNull String regex, @NotNull Consumer<ProtocolGroup<M>> action)
  {
    if (name.isEmpty())
      throw new ProtocolException("regex must not be empty");

    super.forEachGroupByRegex(regex, action);

    if (name.matches(regex))
      action.accept(this);
  }


  @Override
  public @NotNull ProtocolGroup.ProtocolMessageBuilder<M> add(@NotNull Level level) {
    return new MessageBuilder(requireNonNull(level, "level must not be null"));
  }


  @Override
  @SuppressWarnings("unchecked")
  public @NotNull Protocol<M> getRootProtocol() {
    return parent instanceof ProtocolGroup ? ((ProtocolGroup<M>)parent).getRootProtocol() : parent;
  }


  @Override
  public boolean matches0(@NotNull Level levelLimit, @NotNull Level level, @NotNull TagSelector tagSelector)
  {
    levelLimit = min(this.levelLimit, levelLimit);

    return compare(levelLimit, level) >= 0 &&
           getEffectiveVisibility().isShowEntries() &&
           super.matches0(levelLimit, level, tagSelector);
  }


  @Override
  public boolean matches(@NotNull Level level, @NotNull TagSelector tagSelector) {
    return matches0(levelLimit, level, tagSelector);
  }


  @Override
  public boolean matches0(@NotNull Level levelLimit, @NotNull Level level)
  {
    levelLimit = min(this.levelLimit, levelLimit);

    return compare(levelLimit, level) >= 0 &&
           getEffectiveVisibility().isShowEntries() &&
           super.matches0(levelLimit, level);
  }


  @Override
  public boolean matches(@NotNull Level level) {
    return matches0(levelLimit, level);
  }


  @Override
  public @NotNull ProtocolIterator<M> iterator(@NotNull Level level, @NotNull TagSelector tagSelector)
  {
    return new ProtocolStructureIterator.ForGroup<>(levelLimit, level, tagSelector, 0, this,
        false, false, true);
  }


  @Override
  public @NotNull ProtocolGroup.TargetTagBuilder<M> propagate(@NotNull TagSelector tagSelector) {
    return new PropagationBuilder(tagSelector);
  }


  @Override
  public String toString()
  {
    val s = new StringBuilder("ProtocolGroup[id=").append(getId())
        .append(",visibility=").append(visibility);

    if (compare(levelLimit, HIGHEST) < 0)
      s.append(",levelLimit=").append(levelLimit);
    if (name != null)
      s.append(",name=").append(name);

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
      val s = new StringBuilder("GroupMessage[message=").append(message);

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
    public boolean isHeaderVisible(@NotNull Level level, @NotNull TagSelector tagSelector) {
      return ProtocolGroupImpl.this.isHeaderVisible(level, tagSelector);
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
    public String getName() {
      return ProtocolGroupImpl.this.getName();
    }


    @Override
    public @NotNull ProtocolGroup<M> setName(String uniqueId) {
      return ProtocolGroupImpl.this.setName(uniqueId);
    }


    @Override
    public @NotNull Protocol<M> getRootProtocol() {
      return ProtocolGroupImpl.this.getRootProtocol();
    }


    @Override
    public @NotNull ProtocolIterator<M> iterator(@NotNull Level level, @NotNull TagSelector tagSelector) {
      return ProtocolGroupImpl.this.iterator(level, tagSelector);
    }


    @Override
    public @NotNull ProtocolGroup.TargetTagBuilder<M> propagate(@NotNull TagSelector tagSelector) {
      return (ProtocolGroup.TargetTagBuilder<M>)super.propagate(tagSelector);
    }
  }




  private class PropagationBuilder
      extends AbstractPropagationBuilder<M,ProtocolGroup.ProtocolMessageBuilder<M>>
      implements ProtocolGroup.TargetTagBuilder<M>
  {
    PropagationBuilder(TagSelector tagSelector) {
      super(ProtocolGroupImpl.this, tagSelector);
    }


    @Override
    public @NotNull ProtocolGroup<M> to(@NotNull String targetTagName) {
      return (ProtocolGroup<M>)super.to(targetTagName);
    }


    @Override
    public @NotNull ProtocolGroup<M> to(@NotNull String ... targetTagNames) {
      return (ProtocolGroup<M>)super.to(targetTagNames);
    }
  }
}
