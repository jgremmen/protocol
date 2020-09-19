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
import de.sayayi.lib.protocol.TagDef;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;


/**
 * @author Jeroen Gremmen
 */
@SuppressWarnings("unchecked")
abstract class AbstractMessageBuilder<M,B extends ProtocolMessageBuilder<M>,P extends MessageParameterBuilder<M>>
    extends AbstractBuilder<M,B>
    implements ProtocolMessageBuilder<M>
{
  private final Level level;
  private final Set<TagDef> tagDefs;

  private Throwable throwable;


  protected AbstractMessageBuilder(@NotNull AbstractProtocol<M,B> protocol, @NotNull Level level)
  {
    super(protocol);

    this.level = level;

    tagDefs = new HashSet<TagDef>();
    tagDefs.add(protocol.factory.getDefaultTag());
  }


  @Contract("_ -> new")
  protected abstract @NotNull P createMessageParameterBuilder(@NotNull ProtocolMessageEntry<M> message);


  @Override
  public @NotNull B forTag(@NotNull String tagName)
  {
    final TagDef tagDef = protocol.factory.getTagByName(tagName);

    if (tagDef.matches(level))
      tagDefs.add(tagDef);

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


  @Override
  @SuppressWarnings({ "squid:S2583", "ConstantConditions" })
  public @NotNull P message(@NotNull String message)
  {
    if (message == null)
      throw new NullPointerException("message must not be null");

    return message0(protocol.factory.processMessage(message));
  }


  @Override
  @SuppressWarnings({ "squid:S2583", "ConstantConditions" })
  public @NotNull P withMessage(@NotNull M message)
  {
    if (message == null)
      throw new NullPointerException("message must not be null");

    return message0(message);
  }


  @SuppressWarnings("squid:S2583")
  private @NotNull P message0(@NotNull M message)
  {
    Set<String> resolvedTags = new TreeSet<String>();

    // add implied dependencies
    for(TagDef tagDef: protocol.getPropagatedTags(tagDefs))
      for(TagDef impliedTagDef: tagDef.getImpliedTags())
        if (impliedTagDef.matches(level))
          resolvedTags.add(impliedTagDef.getName());

    ProtocolMessageEntry<M> msg = new ProtocolMessageEntry<M>(level, resolvedTags, throwable,
        message, protocol.factory.getDefaultParameterValues());

    protocol.entries.add(msg);

    return createMessageParameterBuilder(msg);
  }
}
