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
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor.MessageWithId;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.ProtocolGroup.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.ProtocolIterator;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.exception.ProtocolException;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;
import static de.sayayi.lib.protocol.Level.Shared.LOWEST;
import static de.sayayi.lib.protocol.Level.compare;
import static de.sayayi.lib.protocol.Level.max;
import static de.sayayi.lib.protocol.Level.min;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ALWAYS;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_IF_NOT_EMPTY;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ONLY;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;


/**
 * Default implementation of a {@link ProtocolGroup}. A protocol group is a nested protocol that
 * groups a set of messages under an optional header message and controls how that group is rendered
 * through its visibility and level limit settings.
 *
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
@SuppressWarnings("java:S2160")
final class ProtocolGroupImpl<M>
    extends AbstractProtocol<M,ProtocolMessageBuilder<M>>
    implements ProtocolGroup<M>, InternalProtocolEntry.Group<M>
{
  private final @NotNull AbstractProtocol<M,Protocol.ProtocolMessageBuilder<M>> parent;

  private @NotNull Level levelLimit;
  private @NotNull Visibility visibility;
  private GroupMessage groupMessage;
  private String name;


  /**
   * Creates a new protocol group as a child of the given parent protocol.
   *
   * @param parent  parent protocol, not {@code null}
   */
  ProtocolGroupImpl(@NotNull AbstractProtocol<M,Protocol.ProtocolMessageBuilder<M>> parent)
  {
    super(parent.getFactory(), parent.parameterMap);

    this.parent = parent;

    levelLimit = HIGHEST;
    visibility = SHOW_HEADER_IF_NOT_EMPTY;
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull Protocol<M> getParent() {
    return parent;
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull Level getLevelLimit() {
    return levelLimit;
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull Visibility getVisibility() {
    return visibility;
  }


  /** {@inheritDoc} */
  @Override
  public GenericMessage<M> getGroupMessage() {
    return groupMessage;
  }


  /** {@inheritDoc} */
  @Override
  public String getName() {
    return name;
  }


  /** {@inheritDoc} */
  @Override
  protected @NotNull Set<String> getPropagatedTags(@NotNull Set<String> tags) {
    return parent.getPropagatedTags(super.getPropagatedTags(tags));
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull Visibility getEffectiveVisibility() {
    return groupMessage == null ? visibility.forAbsentHeader() : visibility;
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull ProtocolGroup<M> setVisibility(@NotNull Visibility visibility)
  {
    this.visibility = requireNonNull(visibility, "visibility must not be null");
    return this;
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull ProtocolGroup<M> setLevelLimit(@NotNull Level level)
  {
    levelLimit = requireNonNull(level, "level must not be null");
    return this;
  }


  /** {@inheritDoc} */
  @Override
  public boolean isHeaderVisible0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher)
  {
    if (groupMessage != null)
      return switch(visibility) {
        case FLATTEN, HIDDEN -> false;
        case SHOW_HEADER_ONLY, SHOW_HEADER_ALWAYS -> true;
        case SHOW_HEADER_IF_NOT_EMPTY -> matches0(levelLimit, matcher, false);
        case FLATTEN_ON_SINGLE_ENTRY -> super.getVisibleEntryCount0(min(this.levelLimit, levelLimit), matcher) > 1;
      };

    return false;
  }


  /** {@inheritDoc} */
  @Override
  public boolean isHeaderVisible(@NotNull MessageMatcher matcher) {
    return isHeaderVisible0(levelLimit, matcher);
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull Level getHeaderLevel0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher)
  {
    Level headerLevel = LOWEST;

    levelLimit = min(this.levelLimit, levelLimit);

    for(var entry: getEntries(levelLimit, matcher))
    {
      Level protocolEntryLevel;

      if (entry instanceof ProtocolEntry.Message)
        protocolEntryLevel = ((ProtocolEntry.Message<M>)entry).getLevel();
      else if (entry instanceof ProtocolEntry.Group)
        protocolEntryLevel = ((ProtocolEntry.Group<M>)entry).getHeaderLevel(matcher);
      else
        continue;

      headerLevel = max(headerLevel, protocolEntryLevel);

      if (compare(headerLevel, levelLimit) > 0)
        return levelLimit;
    }

    return min(levelLimit, headerLevel);
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull Level getHeaderLevel(@NotNull MessageMatcher matcher) {
    return getHeaderLevel0(levelLimit, matcher);
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull List<ProtocolEntry<M>> getEntries0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher)
  {
    return getEffectiveVisibility().isShowEntries()
        ? super.getEntries(min(this.levelLimit, levelLimit), matcher)
        : emptyList();
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull List<ProtocolEntry<M>> getEntries(@NotNull MessageMatcher matcher) {
    return getEntries(levelLimit, matcher);
  }


  /** {@inheritDoc} */
  @Override
  public int getVisibleEntryCount0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher)
  {
    var effectiveVisibility = getEffectiveVisibility();
    if (effectiveVisibility == SHOW_HEADER_ONLY)
      return 1;

    final var entryCount = super.getVisibleEntryCount0(min(this.levelLimit, levelLimit), matcher);
    final var entryCountWithHeader = 1 + entryCount;

    return switch(effectiveVisibility) {
      case SHOW_HEADER_ALWAYS -> entryCountWithHeader;
      case SHOW_HEADER_IF_NOT_EMPTY -> entryCount == 0 ? 0 : entryCountWithHeader;
      case FLATTEN_ON_SINGLE_ENTRY -> entryCount > 1 ? entryCountWithHeader : entryCount;
      case FLATTEN -> entryCount;

      default -> 0;
    };
  }


  /** {@inheritDoc} */
  @Override
  public int getVisibleEntryCount(@NotNull MessageMatcher matcher) {
    return getVisibleEntryCount0(levelLimit, matcher);
  }


  /** {@inheritDoc} */
  @Override
  public int getVisibleGroupEntryMessageCount0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher)
  {
    return getEffectiveVisibility().isShowEntries()
        ? super.getVisibleEntryCount0(min(this.levelLimit, levelLimit), matcher) : 0;
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull ProtocolGroup.MessageParameterBuilder<M> setGroupMessage(@NotNull String message)
  {
    groupMessage = new GroupMessage(factory.getMessageProcessor()
        .processMessage(requireNonNull(message, "message must not be null")));

    return new ParameterBuilderImpl(groupMessage);
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull ProtocolGroup<M> removeGroupMessage()
  {
    groupMessage = null;

    return this;
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull ProtocolGroup<M> setName(String name)
  {
    if (name == null || name.isEmpty())
      this.name = null;
    else if (!name.equals(this.name))
    {
      getRootProtocol().getGroupByName(name).ifPresent(group -> {
        throw new ProtocolException("group name '" + name + "' must be unique");
      });

      this.name = name;
    }

    return this;
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull Optional<ProtocolGroup<M>> getGroupByName(@NotNull String name) {
    return name.equals(this.name) ? Optional.of(this) : super.getGroupByName(name);
  }


  /** {@inheritDoc} */
  @Override
  public void forEachGroupByRegex(@NotNull String regex, @NotNull Consumer<ProtocolGroup<M>> action)
  {
    if (name.isEmpty())
      throw new ProtocolException("regex must not be empty");

    super.forEachGroupByRegex(regex, action);

    if (name.matches(regex))
      action.accept(this);
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull ProtocolGroup.ProtocolMessageBuilder<M> add(@NotNull Level level) {
    return new MessageBuilder(requireNonNull(level, "level must not be null"));
  }


  /** {@inheritDoc} */
  @Override
  @SuppressWarnings("unchecked")
  public @NotNull Protocol<M> getRootProtocol() {
    return parent.isProtocolGroup() ? ((ProtocolGroup<M>)parent).getRootProtocol() : parent;
  }


  /** {@inheritDoc} */
  @Override
  public boolean matches0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher, boolean messageOnly)
  {
    final var ev = getEffectiveVisibility();

    if (!messageOnly && (ev == SHOW_HEADER_ONLY || ev == SHOW_HEADER_ALWAYS))
      return true;

    return ev.isShowEntries() && super.matches0(min(this.levelLimit, levelLimit), matcher, messageOnly);
  }


  /** {@inheritDoc} */
  @Override
  public boolean matches(@NotNull MessageMatcher matcher) {
    return matches0(levelLimit, matcher, true);
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull ProtocolIterator<M> iterator(@NotNull MessageMatcher matcher)
  {
    return new ProtocolStructureIterator.ForGroup<>(levelLimit, matcher, 0, this,
        false, false, true);
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull ProtocolGroup.TargetTagBuilder<M> propagate(@NotNull TagSelector tagSelector) {
    return new PropagationBuilder(tagSelector);
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull ProtocolGroup.TargetTagBuilder<M> propagate(@NotNull String tagSelectorExpression) {
    return propagate(factory.parseTagSelector(tagSelectorExpression));
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull ProtocolGroup<M> set(@NotNull String parameter, Object value)
  {
    parameterMap.put(parameter, value);
    return this;
  }


  @Override
  public String toString()
  {
    final var s = new StringBuilder("ProtocolGroup(id=").append(getId())
        .append(",visibility=").append(visibility);

    if (compare(levelLimit, HIGHEST) < 0)
      s.append(",levelLimit=").append(levelLimit);
    if (name != null)
      s.append(",name=").append(name);
    if (!parameterMap.isEmpty())
    {
      s.append(",params=").append(parameterMap.stream().map(Entry::toString)
          .collect(joining(",", "{", "}")));
    }

    return s.append(')').toString();
  }




  /**
   * Message builder for messages added to this protocol group.
   */
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




  /**
   * Internal representation of the group header message.
   */
  private final class GroupMessage extends AbstractGenericMessage<M>
  {
    private GroupMessage(@NotNull MessageWithId<M> messageWithId) {
      super(messageWithId, ProtocolGroupImpl.this.parameterMap);
    }


    @Override
    public String toString()
    {
      final var s = new StringBuilder("GroupMessage(id=").append(getMessageId())
          .append(",message=").append(getMessage());

      if (!parameterMap.isEmpty())
      {
        s.append(",params=").append(parameterMap.stream().map(Entry::toString)
            .collect(joining(",", "{", "}")));
      }

      return s.append(')').toString();
    }
  }




  /**
   * Parameter builder for the group header message and messages added to this group. Group
   * configuration calls are delegated to the enclosing protocol group.
   */
  private class ParameterBuilderImpl
      extends AbstractParameterBuilder<M,ProtocolGroup.MessageParameterBuilder<M>,ProtocolGroup.ProtocolMessageBuilder<M>>
      implements ProtocolGroup.MessageParameterBuilder<M>
  {
    private ParameterBuilderImpl(AbstractGenericMessage<M> message) {
      super(ProtocolGroupImpl.this, message);
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull Visibility getVisibility() {
      return ProtocolGroupImpl.this.getVisibility();
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull Visibility getEffectiveVisibility() {
      return ProtocolGroupImpl.this.getEffectiveVisibility();
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull ProtocolGroup<M> setVisibility(@NotNull Visibility visibility) {
      return ProtocolGroupImpl.this.setVisibility(visibility);
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull Level getLevelLimit() {
      return ProtocolGroupImpl.this.getLevelLimit();
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull ProtocolGroup<M> setLevelLimit(@NotNull Level level) {
      return ProtocolGroupImpl.this.setLevelLimit(level);
    }


    /** {@inheritDoc} */
    @Override
    public boolean isHeaderVisible(@NotNull MessageMatcher matcher) {
      return ProtocolGroupImpl.this.isHeaderVisible(matcher);
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull ProtocolGroup.MessageParameterBuilder<M> setGroupMessage(@NotNull String message) {
      return ProtocolGroupImpl.this.setGroupMessage(message);
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull ProtocolGroup<M> removeGroupMessage() {
      return ProtocolGroupImpl.this.removeGroupMessage();
    }


    /** {@inheritDoc} */
    @Override
    public String getName() {
      return ProtocolGroupImpl.this.getName();
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull ProtocolGroup<M> setName(String uniqueId) {
      return ProtocolGroupImpl.this.setName(uniqueId);
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull Protocol<M> getRootProtocol() {
      return ProtocolGroupImpl.this.getRootProtocol();
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull ProtocolIterator<M> iterator(@NotNull MessageMatcher matcher) {
      return ProtocolGroupImpl.this.iterator(matcher);
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull ProtocolGroup.TargetTagBuilder<M> propagate(@NotNull TagSelector tagSelector) {
      return (ProtocolGroup.TargetTagBuilder<M>)super.propagate(tagSelector);
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull ProtocolGroup.TargetTagBuilder<M> propagate(@NotNull String tagSelectorExpression) {
      return (ProtocolGroup.TargetTagBuilder<M>)super.propagate(tagSelectorExpression);
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull ProtocolGroup<M> set(@NotNull String parameter, Object value)
    {
      parameterMap.put(parameter, value);
      return this;
    }


    /** {@inheritDoc} */
    @Override
    public <T> T get(@NotNull String parameter, @NotNull Class<T> type) {
      return ProtocolGroupImpl.this.get(parameter, type);
    }
  }




  /**
   * Tag propagation builder for propagation rules defined on this protocol group.
   */
  private class PropagationBuilder
      extends AbstractPropagationBuilder<M,ProtocolGroup.ProtocolMessageBuilder<M>>
      implements ProtocolGroup.TargetTagBuilder<M>
  {
    PropagationBuilder(@NotNull TagSelector tagSelector) {
      super(ProtocolGroupImpl.this, tagSelector);
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull ProtocolGroup<M> to(@NotNull String targetTagName) {
      return (ProtocolGroup<M>)super.to(targetTagName);
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull ProtocolGroup<M> to(@NotNull String ... targetTagNames) {
      return (ProtocolGroup<M>)super.to(targetTagNames);
    }
  }
}
