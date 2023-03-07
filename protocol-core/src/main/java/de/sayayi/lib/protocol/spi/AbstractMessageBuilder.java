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
import de.sayayi.lib.protocol.Protocol.MessageParameterBuilder;
import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor.MessageWithId;

import lombok.val;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static de.sayayi.lib.protocol.ProtocolFactory.DEFAULT_TAG_NAME;
import static java.util.Objects.requireNonNull;


/**
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
@SuppressWarnings({"unchecked", "java:S100"})
abstract class AbstractMessageBuilder
    <M,B extends ProtocolMessageBuilder<M>,P extends MessageParameterBuilder<M>>
    extends AbstractBuilder<M,B>
    implements ProtocolMessageBuilder<M>
{
  private final @NotNull Level level;
  private final @NotNull Set<String> tags;

  private Throwable throwable;


  protected AbstractMessageBuilder(@NotNull AbstractProtocol<M,B> protocol, @NotNull Level level)
  {
    super(protocol);

    this.level = level;

    tags = new HashSet<>(8);
    tags.add(DEFAULT_TAG_NAME);
  }


  @Contract("_ -> new")
  protected abstract @NotNull P createMessageParameterBuilder(@NotNull ProtocolMessageEntry<M> message);


  @Override
  public @NotNull B forTag(@NotNull String tagName)
  {
    if (requireNonNull(tagName, "tagName must not be null").isEmpty())
      throw new IllegalArgumentException("tagName must not be empty");

    tags.add(tagName);

    return (B)this;
  }


  @Override
  public @NotNull B forTags(@NotNull String ... tagNames)
  {
    for(val tagName: requireNonNull(tagNames, "tagNames must not be null"))
      forTag(tagName);

    return (B)this;
  }


  @Override
  public @NotNull B withThrowable(@NotNull Throwable throwable)
  {
    this.throwable = requireNonNull(throwable, "throwable must not be null");

    return (B)this;
  }


  @Override
  public @NotNull P message(@NotNull String message)
  {
    return message0(protocol.getFactory().getMessageProcessor()
        .processMessage(requireNonNull(message, "message must not be null")));
  }


  @Override
  public @NotNull P withMessage(@NotNull M message)
  {
    val msg = new ProtocolMessageEntry<>(protocol, level, protocol.getPropagatedTags(tags),
        throwable,
        new GenericMessageWithId<>(
            protocol.getFactory().getMessageProcessor().getIdFromMessage(message),
            requireNonNull(message, "message must not be null")),
        protocol.parameterMap);

    protocol.entries.add(msg);

    return createMessageParameterBuilder(msg);
  }


  @SuppressWarnings("squid:S2583")
  private @NotNull P message0(@NotNull MessageWithId<M> messageWithId)
  {
    val msg = new ProtocolMessageEntry<>(protocol, level, protocol.getPropagatedTags(tags),
        throwable, messageWithId, protocol.parameterMap);

    protocol.entries.add(msg);

    return createMessageParameterBuilder(msg);
  }
}