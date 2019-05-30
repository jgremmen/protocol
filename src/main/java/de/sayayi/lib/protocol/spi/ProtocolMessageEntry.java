package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.Tag;

import java.util.Collections;
import java.util.Set;


class ProtocolMessageEntry<M> extends AbstractBasicMessage<M> implements ProtocolEntry.Message<M>
{
  private final Level level;
  private final Set<Tag> tags;
  private final Throwable throwable;


  ProtocolMessageEntry(Level level, Set<Tag> tags, Throwable throwable, M message)
  {
    super(message);

    this.level = level;
    this.tags = tags;
    this.throwable = throwable;
  }


  @Override
  public Level getLevel() {
    return level;
  }


  @Override
  public Set<Tag> getTags() {
    return Collections.unmodifiableSet(tags);
  }


  @Override
  public Throwable getThrowable() {
    return throwable;
  }


  @Override
  public boolean isMatch(Level level, Tag tag) {
    return this.level.severity() >= level.severity() && this.tags.contains(tag) && tag.isMatch(level);
  }


  @Override
  public boolean hasVisibleEntry(Level level, Tag tag) {
    return isMatch(level, tag);
  }


  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder("Message[level=").append(level).append(",tags={");
    boolean first = true;

    for(Tag tag: tags)
    {
      if (first)
        first = false;
      else
        s.append(',');

      s.append(tag.getName());
    }

    s.append("},message=").append(message);

    if (!parameterValues.isEmpty())
      s.append(",params=").append(parameterValues);

    return s.append(']').toString();
  }
}
