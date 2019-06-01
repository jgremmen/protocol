package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolEntry.Group;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.ProtocolGroup.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.ProtocolIterator;
import de.sayayi.lib.protocol.Tag;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_IF_NOT_EMPTY;


final class ProtocolGroupImpl<M>
    extends AbstractProtocol<M,ProtocolMessageBuilder<M>>
    implements ProtocolGroup<M>, Group<M>
{
  private final AbstractProtocol<M,Protocol.ProtocolMessageBuilder<M>> parent;

  private Visibility visibility;
  private GroupMessage groupMessage;


  ProtocolGroupImpl(AbstractProtocol<M,Protocol.ProtocolMessageBuilder<M>> parent)
  {
    super(parent.factory);

    this.parent = parent;
    visibility = SHOW_HEADER_IF_NOT_EMPTY;
  }


  @Override
  protected void updateTagAndLevel(Set<Tag> tags, Level level)
  {
    parent.updateTagAndLevel(tags, level);
    super.updateTagAndLevel(tags, level);
  }


  @Override
  public Visibility getVisibility() {
    return visibility;
  }


  @Override
  public Visibility getEffectiveVisibility() {
    return (groupMessage == null) ? visibility.forAbsentHeader() : visibility;
  }


  @Override
  public ProtocolGroup<M> setVisibility(Visibility visibility)
  {
    if (visibility == null)
      throw new NullPointerException("visibility must not be null");

    this.visibility = visibility;

    return this;
  }


  @Override
  public boolean isHeaderVisible(Level level, Tag tag)
  {
    if (groupMessage != null)
      switch(visibility)
      {
        case FLATTEN:
        case HIDDEN:
          return false;

        case SHOW_HEADER_ONLY:
        case SHOW_HEADER_ALWAYS:
          return true;

        case SHOW_HEADER_IF_NOT_EMPTY:
          return isMatch(level, tag);

        case FLATTEN_ON_SINGLE_ENTRY: {
          boolean found = false;

          for(ProtocolEntry entry: entries)
            if (entry.isMatch(level, tag))
            {
              if (!found)
                found = true;
              else
                return true;
            }

          return false;
        }
      }

    return false;
  }


  @Override
  public List<ProtocolEntry<M>> getEntries(Level level, Tag tag) {
    return visibility.isShowEntries() ? super.getEntries(level, tag) : Collections.<ProtocolEntry<M>>emptyList();
  }


  @Override
  public boolean hasVisibleElement(Level level, Tag tag)
  {
    if (tag.isMatch(level))
      switch(getEffectiveVisibility())
      {
        case HIDDEN:
          return false;

        case FLATTEN:
        case FLATTEN_ON_SINGLE_ENTRY:
        case SHOW_HEADER_IF_NOT_EMPTY:
          return super.hasVisibleElement(level, tag);

        case SHOW_HEADER_ALWAYS:
        case SHOW_HEADER_ONLY:
          return true;
      }

    return false;
  }


  @Override
  public ProtocolGroup.MessageParameterBuilder<M> setGroupMessage(String message)
  {
    if (message == null)
      throw new NullPointerException("message must not be null");

    M processedMessage = factory.processMessage(message);

    return new ParameterBuilderImpl(groupMessage = new GroupMessage(processedMessage));
  }


  @Override
  public ProtocolGroup<M> removeGroupMessage()
  {
    groupMessage = null;

    return this;
  }


  @Override
  public BasicMessage<M> getGroupMessage() {
    return groupMessage;
  }


  @Override
  public ProtocolGroup.ProtocolMessageBuilder<M> add(Level level)
  {
    if (level == null)
      throw new NullPointerException("level must not be null");

    return new MessageBuilder(level);
  }


  @SuppressWarnings("unchecked")
  @Override
  public Protocol<M> getRootProtocol() {
    return (parent instanceof ProtocolGroup) ? ((ProtocolGroup<M>)parent).getRootProtocol() : parent;
  }


  @Override
  public boolean isMatch(Level level, Tag tag) {
    return getEffectiveVisibility().isShowEntries() && super.isMatch(level, tag);
  }


  @Override
  public ProtocolIterator<M> iterator(Level level, Tag tag)
  {
    return new ProtocolStructureIterator.ForGroup<M>(level, tag, 0,this, false,
        false);
  }


  private class MessageBuilder
      extends AbstractMessageBuilder<M,ProtocolGroup.ProtocolMessageBuilder<M>,ProtocolGroup.MessageParameterBuilder<M>>
      implements ProtocolGroup.ProtocolMessageBuilder<M>
  {
    MessageBuilder(Level level) {
      super(ProtocolGroupImpl.this, level);
    }


    @Override
    protected ProtocolGroup.MessageParameterBuilder<M> createMessageParameterBuilder(ProtocolMessageEntry<M> message) {
      return new ParameterBuilderImpl(message);
    }
  }


  private class GroupMessage extends AbstractBasicMessage<M>
  {
    GroupMessage(M message) {
      super(message, factory.defaultParameterValues);
    }


    @Override
    public boolean isMatch(Level level, Tag tag) {
      return isHeaderVisible(level, tag);
    }


    @Override
    public boolean hasVisibleElement(Level level, Tag tag) {
      return isHeaderVisible(level, tag);
    }


    @Override
    public String toString()
    {
      StringBuilder s = new StringBuilder("GroupMessage[message=").append(message);

      if (!parameterValues.isEmpty())
        s.append(",params=").append(parameterValues);

      return s.append(']').toString();
    }
  }


  private class ParameterBuilderImpl
      extends AbstractParameterBuilder<M,ProtocolGroup.MessageParameterBuilder<M>,ProtocolGroup.ProtocolMessageBuilder<M>>
      implements ProtocolGroup.MessageParameterBuilder<M>
  {
    ParameterBuilderImpl(AbstractBasicMessage<M> message) {
      super(ProtocolGroupImpl.this, message);
    }


    @Override
    public Visibility getVisibility() {
      return ProtocolGroupImpl.this.getVisibility();
    }


    @Override
    public Visibility getEffectiveVisibility() {
      return ProtocolGroupImpl.this.getEffectiveVisibility();
    }


    @Override
    public ProtocolGroup<M> setVisibility(Visibility visibility) {
      return ProtocolGroupImpl.this.setVisibility(visibility);
    }


    @Override
    public boolean isHeaderVisible(Level level, Tag tag) {
      return ProtocolGroupImpl.this.isHeaderVisible(level, tag);
    }


    @Override
    public ProtocolGroup.MessageParameterBuilder<M> setGroupMessage(String message) {
      return ProtocolGroupImpl.this.setGroupMessage(message);
    }


    @Override
    public ProtocolGroup<M> removeGroupMessage() {
      return ProtocolGroupImpl.this.removeGroupMessage();
    }


    @Override
    public Protocol<M> getRootProtocol() {
      return ProtocolGroupImpl.this.getRootProtocol();
    }


    @Override
    public ProtocolIterator<M> iterator(Level level, Tag tag) {
      return ProtocolGroupImpl.this.iterator(level, tag);
    }
  }
}
