/**
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


  AbstractMessageBuilder(AbstractProtocol<M,B> protocol, Level level)
  {
    this.protocol = protocol;
    this.level = level;

    tags = new HashSet<Tag>();
    tags.add(protocol.factory.getDefaultTag());
  }


  protected abstract P createMessageParameterBuilder(ProtocolMessageEntry<M> message);


  @Override
  public B forTag(Tag tag)
  {
    if (tag == null)
      throw new NullPointerException("tag must not be null");

    if (!protocol.factory.isRegisteredTag(tag))
      throw new IllegalArgumentException("tag with name " + tag.getName() + " is not registered for this protocol");

    tags.add(tag);

    return (B)this;
  }


  @Override
  public B forTags(Tag ... tags)
  {
    for(Tag tag: tags)
      forTag(tag);

    return (B)this;
  }


  @Override
  public B forTags(String ... tagNames)
  {
    for(String tagName: tagNames)
    {
      Tag tag = protocol.factory.getTagByName(tagName);
      if (tag == null)
        throw new IllegalArgumentException("tag with name " + tagName + " is not registered for this protocol");

      tags.add(tag);
    }

    return (B)this;
  }


  @Override
  public B withThrowable(Throwable throwable)
  {
    this.throwable = throwable;

    return (B)this;
  }


  @Override
  public P message(String message)
  {
    if (message == null)
      throw new NullPointerException("message must not be null");

    Set<Tag> resolvedTags = new HashSet<Tag>();

    // add implied dependencies
    for(Tag tag: tags)
      for(Tag impliedTag: tag.getImpliedTags())
        if (impliedTag.isMatch(level))
          resolvedTags.add(impliedTag);

    // if this message has no tags, don't add it to the protocol.
    if (!resolvedTags.isEmpty())
    {
      AbstractProtocolFactory<M> factory = protocol.factory;
      ProtocolMessageEntry<M> msg = new ProtocolMessageEntry<M>(level, resolvedTags, throwable,
          factory.processMessage(message), factory.defaultParameterValues);

      protocol.entries.add(msg);
      protocol.updateTagAndLevel(resolvedTags, level);

      return createMessageParameterBuilder(msg);
    }

    return createMessageParameterBuilder(null);
  }
}
