package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol.MessageParameterBuilder;
import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.Tag;

import java.util.HashSet;
import java.util.Set;


@SuppressWarnings("unchecked")
abstract class AbstractMessageBuilder<B extends ProtocolMessageBuilder,M extends MessageParameterBuilder>
    implements ProtocolMessageBuilder
{
  private final AbstractProtocol protocol;
  private final Level level;
  private final Set<Tag> tags;


  AbstractMessageBuilder(AbstractProtocol protocol, Level level)
  {
    this.protocol = protocol;
    this.level = level;

    tags = new HashSet<Tag>();
    tags.add(protocol.factory.systemTag);
  }


  protected abstract M createMessageParameterBuilder(ProtocolMessageEntry message);


  @Override
  public B forTag(Tag tag)
  {
    if (!protocol.factory.isRegisteredTag(tag))
      throw new IllegalArgumentException("tag with name " + tag.getName() + " is not registered for this protocol");

    tags.add(tag);

    return (B)this;
  }


  @Override
  public B forTags(Tag... tags)
  {
    for(Tag tag: tags)
      forTag(tag);

    return (B)this;
  }


  @Override
  public B forTags(String... tagNames)
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
  public M message(String message)
  {
    Set<Tag> allTags = new HashSet<Tag>();
    for(Tag tag: tags)
      allTags.addAll(tag.getImpliedTags());

    ProtocolMessageEntry msg = new ProtocolMessageEntry(level, allTags, message);
    protocol.entries.add(msg);

    return createMessageParameterBuilder(msg);
  }


  @Override
  public M messageKey(String key) {
    throw new UnsupportedOperationException();
  }
}
