package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol.MessageParameterBuilder;
import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.Tag;

import java.util.HashSet;
import java.util.Set;


@SuppressWarnings("unchecked")
abstract class AbstractMessageBuilder<M,B extends ProtocolMessageBuilder,P extends MessageParameterBuilder>
    implements ProtocolMessageBuilder
{
  private final AbstractProtocol<M,B> protocol;
  private final Level level;
  private final Set<Tag> tags;


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
  public P message(String message)
  {
    if (message == null)
      throw new NullPointerException("message must not be null");

    Set<Tag> allTags = new HashSet<Tag>();
    for(Tag tag: tags)
      allTags.addAll(tag.getImpliedTags());

    M processedMessage = protocol.factory.processMessage(message);
    ProtocolMessageEntry<M> msg = new ProtocolMessageEntry<M>(level, allTags, processedMessage);

    protocol.entries.add(msg);
    protocol.updateTagAndLevel(allTags, level);

    return createMessageParameterBuilder(msg);
  }
}
