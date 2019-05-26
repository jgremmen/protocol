package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolEntry.Group;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.ProtocolGroup.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.Tag;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static de.sayayi.lib.protocol.Level.Shared.ALL;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.HIDDEN;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_IF_NOT_EMPTY;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ONLY;


public final class ProtocolGroupImpl extends AbstractProtocol<ProtocolMessageBuilder> implements ProtocolGroup, Group
{
  private final AbstractProtocol parent;

  private Visibility visibility;
  private ProtocolMessageEntry groupMessage;


  ProtocolGroupImpl(AbstractProtocol parent)
  {
    super(parent.factory);

    this.parent = parent;
    visibility = SHOW_HEADER_IF_NOT_EMPTY;
  }


  @Override
  public Visibility getVisibility() {
    return visibility;
  }


  @Override
  public Visibility getEffectiveVisibility() {
    return (groupMessage == null) ? visibility.modifyNoHeader() : visibility;
  }


  @Override
  public ProtocolGroup setVisibility(Visibility visibility)
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
  public List<ProtocolEntry> getEntries(Level level, Tag tag)
  {
    if (visibility == HIDDEN || visibility == SHOW_HEADER_ONLY)
      return Collections.emptyList();
    else
      return super.getEntries(level, tag);
  }


  @Override
  public ProtocolGroup.MessageParameterBuilder setGroupMessage(String message)
  {
    if (message == null)
      throw new NullPointerException("message must not be null");

    return new ParameterBuilderImpl(groupMessage = new GroupMessage(message));
  }


  @Override
  public ProtocolGroup.MessageParameterBuilder setGroupMessageKey(String key)
  {
    if (key == null)
      throw new NullPointerException("key must not be null");

    throw new UnsupportedOperationException();
  }


  @Override
  public String formatGroupMessage(Locale locale) {
    return groupMessage.format(locale);
  }


  @Override
  public ProtocolGroup.ProtocolMessageBuilder add(Level level)
  {
    if (level == null)
      throw new NullPointerException("level must not be null");

    return new MessageBuilder(level);
  }


  @Override
  public Protocol getRootProtocol() {
    return (parent instanceof ProtocolGroup) ? ((ProtocolGroup)parent).getRootProtocol() : parent;
  }


  @Override
  public boolean isMatch(Level level, Tag tag) {
    return getEffectiveVisibility().isShowEntries() && super.isMatch(level, tag);
  }


  private class MessageBuilder
      extends AbstractMessageBuilder<ProtocolGroup.ProtocolMessageBuilder,ProtocolGroup.MessageParameterBuilder>
      implements ProtocolGroup.ProtocolMessageBuilder
  {
    MessageBuilder(Level level) {
      super(ProtocolGroupImpl.this, level);
    }


    @Override
    protected ProtocolGroup.MessageParameterBuilder createMessageParameterBuilder(ProtocolMessageEntry message) {
      return new ParameterBuilderImpl(message);
    }
  }


  private static class GroupMessage extends ProtocolMessageEntry
  {
    GroupMessage(String message) {
      super(ALL, null, message);
    }


    @Override
    public boolean isMatch(Level level, Tag tag) {
      return true;
    }


    @Override
    public List<ProtocolEntry> getEntries(Level level, Tag tag) {
      return Collections.<ProtocolEntry>singletonList(this);
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
      extends AbstractParameterBuilder<ProtocolGroup.MessageParameterBuilder,ProtocolGroup.ProtocolMessageBuilder>
      implements ProtocolGroup.MessageParameterBuilder
  {
    ParameterBuilderImpl(ProtocolMessageEntry message) {
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
    public ProtocolGroup setVisibility(Visibility visibility) {
      return ProtocolGroupImpl.this.setVisibility(visibility);
    }


    @Override
    public boolean isHeaderVisible(Level level, Tag tag) {
      return ProtocolGroupImpl.this.isHeaderVisible(level, tag);
    }


    @Override
    public ProtocolGroup.MessageParameterBuilder setGroupMessage(String message) {
      return ProtocolGroupImpl.this.setGroupMessage(message);
    }


    @Override
    public ProtocolGroup.MessageParameterBuilder setGroupMessageKey(String key) {
      return ProtocolGroupImpl.this.setGroupMessageKey(key);
    }


    @Override
    public Protocol getRootProtocol() {
      return ProtocolGroupImpl.this.getRootProtocol();
    }
  }
}
