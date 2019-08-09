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
    implements ProtocolMessageBuilder<M>
{
  private final AbstractProtocol<M,B> protocol;
  private final Level level;
  private final Set<Tag> tags;

  private Throwable throwable;


  AbstractMessageBuilder(@NotNull AbstractProtocol<M,B> protocol, @NotNull Level level)
  {
    this.protocol = protocol;
    this.level = level;

    tags = new HashSet<Tag>();
    tags.add(protocol.factory.getDefaultTag());
  }


  protected abstract @NotNull P createMessageParameterBuilder(@NotNull ProtocolMessageEntry<M> message);


  @SuppressWarnings("squid:S2583")
  @Override
  public @NotNull B forTag(@NotNull Tag tag)
  {
    //noinspection ConstantConditions
    if (tag == null)
      throw new NullPointerException("tag must not be null");

    if (!protocol.factory.isRegisteredTag(tag))
      throw new IllegalArgumentException("tag with name " + tag.getName() + " is not registered for this protocol");

    if (tag.isMatch(level))
      tags.add(tag);

    return (B)this;
  }


  @Override
  public @NotNull ProtocolMessageBuilder<M> forTag(@NotNull String tag)
  {
    //noinspection PatternValidation
    return forTag(protocol.factory.getTagByName(tag));
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
    {
      //noinspection PatternValidation
      forTag(protocol.factory.getTagByName(tagName));
    }

    return (B)this;
  }


  @Override
  public @NotNull B withThrowable(Throwable throwable)
  {
    this.throwable = throwable;

    return (B)this;
  }


  @SuppressWarnings("squid:S2583")
  @Override
  public @NotNull P message(@NotNull String message)
  {
    //noinspection ConstantConditions
    if (message == null)
      throw new NullPointerException("message must not be null");

    Set<Tag> resolvedTags = new HashSet<Tag>();

    // add implied dependencies
    for(Tag tag: tags)
      for(Tag impliedTag: tag.getImpliedTags())
        if (impliedTag.isMatch(level))
          resolvedTags.add(impliedTag);

    AbstractProtocolFactory<M> factory = protocol.factory;
    ProtocolMessageEntry<M> msg = new ProtocolMessageEntry<M>(level, resolvedTags, throwable,
        factory.processMessage(message), factory.defaultParameterValues);

    protocol.entries.add(msg);

    return createMessageParameterBuilder(msg);
  }
}
