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
import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolIterator;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.NotNull;

import java.util.Map.Entry;

import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;


/**
 * Default implementation of a root {@link Protocol}. A root protocol has no parent and acts as the
 * top-level container to which messages and protocol groups are added.
 *
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
public final class ProtocolImpl<M> extends AbstractProtocol<M,ProtocolMessageBuilder<M>>
{
  /**
   * Creates a new root protocol for the given factory.
   *
   * @param factory  protocol factory, not {@code null}
   */
  public ProtocolImpl(@NotNull ProtocolFactory<M> factory) {
    super(factory, null);
  }


  /** {@inheritDoc} */
  @Override
  public Protocol<M> getParent() {
    return null;
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull ProtocolMessageBuilder<M> add(@NotNull Level level) {
    return new MessageBuilder(requireNonNull(level, "level must not be null"));
  }


  /** {@inheritDoc} */
  @Override
  public boolean matches(@NotNull MessageMatcher matcher) {
    return matches0(HIGHEST, matcher, true);
  }


  /** {@inheritDoc} */
  @Override
  public int getVisibleEntryCount(@NotNull MessageMatcher matcher) {
    return getVisibleEntryCount0(HIGHEST, matcher);
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull ProtocolIterator<M> iterator(@NotNull MessageMatcher matcher) {
    return new ProtocolStructureIterator.ForProtocol<>(matcher, 0, this);
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull TargetTagBuilder<M> propagate(@NotNull TagSelector tagSelector) {
    return new PropagationBuilder(tagSelector);
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull TargetTagBuilder<M> propagate(@NotNull String tagSelectorExpression) {
    return propagate(factory.parseTagSelector(tagSelectorExpression));
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull Protocol<M> set(@NotNull String parameter, Object value)
  {
    parameterMap.put(parameter, value);
    return this;
  }


  @Override
  public String toString()
  {
    final var s = new StringBuilder("Protocol(id=").append(getId());

    if (!parameterMap.isEmpty())
    {
      s.append(",params=").append(parameterMap.stream().map(Entry::toString)
          .collect(joining(",", "{", "}")));
    }

    return s.append(')').toString();
  }




  /**
   * Message builder for messages added directly to this root protocol.
   */
  private class MessageBuilder extends AbstractMessageBuilder<M,ProtocolMessageBuilder<M>,MessageParameterBuilder<M>>
  {
    MessageBuilder(@NotNull Level level) {
      super(ProtocolImpl.this, level);
    }


    @Override
    protected @NotNull MessageParameterBuilder<M> createMessageParameterBuilder(
        @NotNull ProtocolMessageEntry<M> message) {
      return new ParameterBuilder(message);
    }
  }




  /**
   * Parameter builder for messages added directly to this root protocol.
   */
  private class ParameterBuilder
      extends AbstractParameterBuilder<M,MessageParameterBuilder<M>,ProtocolMessageBuilder<M>>
  {
    ParameterBuilder(ProtocolMessageEntry<M> message) {
      super(ProtocolImpl.this, message);
    }


    @Override
    public @NotNull Protocol<M> set(@NotNull String parameter, Object value) {
      return ProtocolImpl.this.set(parameter, value);
    }


    @Override
    public <T> T get(@NotNull String parameter, @NotNull Class<T> type) {
      return ProtocolImpl.this.get(parameter, type);
    }
  }




  /**
   * Tag propagation builder for propagation rules defined on this root protocol.
   */
  private class PropagationBuilder extends AbstractPropagationBuilder<M,ProtocolMessageBuilder<M>>
  {
    PropagationBuilder(TagSelector tagSelector) {
      super(ProtocolImpl.this, tagSelector);
    }
  }
}
