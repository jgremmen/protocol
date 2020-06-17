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
import de.sayayi.lib.protocol.Protocol.MessageParameterBuilder;
import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.Tag;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;


/**
 * @author Jeroen Gremmen
 */
@SuppressWarnings("unchecked")
abstract class AbstractMessageBuilder<M,B extends ProtocolMessageBuilder<M>,P extends MessageParameterBuilder<M>>
    extends AbstractBuilder<M,B>
    implements ProtocolMessageBuilder<M>
{
  private final Level level;
  private final Set<Tag> tags;

  private Throwable throwable;


  protected AbstractMessageBuilder(@NotNull AbstractProtocol<M,B> protocol, @NotNull Level level)
  {
    super(protocol);

    this.level = level;

    tags = new HashSet<Tag>();
    tags.add(protocol.factory.getDefaultTag());
  }


  protected abstract @NotNull P createMessageParameterBuilder(@NotNull ProtocolMessageEntry<M> message);


  @Override
  public @NotNull B forTag(@NotNull Tag tag)
  {
    tag = protocol.validateTag(tag);

    if (tag.matches(level))
      tags.add(tag);

    return (B)this;
  }


  @Override
  public @NotNull B forTag(@NotNull String tagName) {
    return forTag(protocol.resolveTagByName(tagName));
  }


  @Override
  public @NotNull B forTags(@NotNull Tag ... tags)
  {
    for(Tag tag: tags)
      forTag(tag);

    return (B)this;
  }


  @Override
  public @NotNull B forTags(@NotNull String ... tagNames)
  {
    for(String tagName: tagNames)
      forTag(tagName);

    return (B)this;
  }


  @Override
  public @NotNull B withThrowable(Throwable throwable)
  {
    this.throwable = throwable;

    return (B)this;
  }


  @SuppressWarnings({ "squid:S2583", "ConstantConditions" })
  @Override
  public @NotNull P message(@NotNull String message)
  {
    if (message == null)
      throw new NullPointerException("message must not be null");

    return message0(protocol.factory.processMessage(message));
  }


  @SuppressWarnings({ "squid:S2583", "ConstantConditions" })
  @Override
  public @NotNull P withMessage(@NotNull M message)
  {
    if (message == null)
      throw new NullPointerException("message must not be null");

    return message0(message);
  }


  @SuppressWarnings("squid:S2583")
  private @NotNull P message0(@NotNull M message)
  {
    Set<Tag> resolvedTags = new HashSet<Tag>();

    // add implied dependencies
    for(Tag tag: protocol.getPropagatedTags(tags))
      for(Tag impliedTag: tag.getImpliedTags())
        if (impliedTag.matches(level))
          resolvedTags.add(impliedTag);

    ProtocolMessageEntry<M> msg = new ProtocolMessageEntry<M>(level, resolvedTags, throwable,
        message, protocol.factory.getDefaultParameterValues());

    protocol.entries.add(msg);

    return createMessageParameterBuilder(msg);
  }
}
