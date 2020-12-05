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
import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolIterator;
import de.sayayi.lib.protocol.TagSelector;

import org.jetbrains.annotations.NotNull;

import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;
import static java.util.Objects.requireNonNull;


/**
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
final class ProtocolImpl<M> extends AbstractProtocol<M,ProtocolMessageBuilder<M>>
{
  ProtocolImpl(@NotNull ProtocolFactory<M> factory) {
    super(factory);
  }


  @Override
  public Protocol<M> getParent() {
    return null;
  }


  @Override
  public @NotNull ProtocolMessageBuilder<M> add(@NotNull Level level) {
    return new MessageBuilder(requireNonNull(level, "level must not be null"));
  }


  @Override
  public boolean matches(@NotNull Level level, @NotNull TagSelector tagSelector) {
    return matches0(HIGHEST, level, tagSelector);
  }


  @Override
  public boolean matches(@NotNull Level level) {
    return matches0(HIGHEST, level);
  }


  @Override
  public int getVisibleEntryCount(boolean recursive, @NotNull Level level, @NotNull TagSelector tagSelector) {
    return getVisibleEntryCount0(HIGHEST, recursive, level, tagSelector);
  }


  @Override
  public @NotNull ProtocolIterator<M> iterator(@NotNull Level level, @NotNull TagSelector tagSelector) {
    return new ProtocolStructureIterator.ForProtocol<>(level, tagSelector, 0, this);
  }


  @Override
  public @NotNull TargetTagBuilder<M> propagate(@NotNull TagSelector tagSelector) {
    return new PropagationBuilder(tagSelector);
  }


  @Override
  public String toString() {
    return "Protocol[id=" + getId() + ']';
  }




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




  private class ParameterBuilder
      extends AbstractParameterBuilder<M,MessageParameterBuilder<M>,ProtocolMessageBuilder<M>>
  {
    ParameterBuilder(ProtocolMessageEntry<M> message) {
      super(ProtocolImpl.this, message);
    }
  }




  private class PropagationBuilder extends AbstractPropagationBuilder<M,ProtocolMessageBuilder<M>>
  {
    PropagationBuilder(TagSelector tagSelector) {
      super(ProtocolImpl.this, tagSelector);
    }
  }
}
