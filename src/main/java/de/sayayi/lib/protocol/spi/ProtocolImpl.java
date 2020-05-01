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
import de.sayayi.lib.protocol.ProtocolIterator;
import de.sayayi.lib.protocol.Tag;

import lombok.EqualsAndHashCode;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;


/**
 * @author Jeroen Gremmen
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true, doNotUseGetters = true, callSuper = false)
final class ProtocolImpl<M> extends AbstractProtocol<M,ProtocolMessageBuilder<M>>
{
  private static final AtomicInteger PROTOCOL_ID = new AtomicInteger(0);

  @EqualsAndHashCode.Include
  private final int id;


  ProtocolImpl(@NotNull AbstractProtocolFactory<M> factory)
  {
    super(factory);

    id = PROTOCOL_ID.incrementAndGet();
  }


  @Override
  public Protocol<M> getGroupParent() {
    return null;
  }


  @SuppressWarnings("squid:S2583")
  @Override
  public @NotNull ProtocolMessageBuilder<M> add(@NotNull Level level)
  {
    //noinspection ConstantConditions
    if (level == null)
      throw new NullPointerException("level must not be null");

    return new MessageBuilder(level);
  }


  @Override
  public boolean matches(@NotNull Level level, @NotNull Tag... tags) {
    return matches0(HIGHEST, level, tags);
  }


  @Override
  public boolean matches(@NotNull Level level) {
    return matches0(HIGHEST, level);
  }


  @Override
  public int getVisibleEntryCount(boolean recursive, @NotNull Level level, @NotNull Tag... tags) {
    return getVisibleEntryCount0(HIGHEST, recursive, level, tags);
  }


  @Override
  public @NotNull ProtocolIterator<M> iterator(@NotNull Level level, @NotNull Tag ... tags) {
    return new ProtocolStructureIterator.ForProtocol<M>(level, tags, 0, this);
  }


  private class MessageBuilder
      extends AbstractMessageBuilder<M,ProtocolMessageBuilder<M>,MessageParameterBuilder<M>>
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


  @Override
  public String toString() {
    return "Protocol[id=" + id + ']';
  }
}
