package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.Tag;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


class ProtocolMessageEntry implements ProtocolEntry.Message
{
  private final Level level;
  private final Set<Tag> tags;

  final String message;
  final Map<String,Object> parameterValues;


  ProtocolMessageEntry(Level level, Set<Tag> tags, String message)
  {
    this.level = level;
    this.tags = tags;
    this.message = message;
    this.parameterValues = new HashMap<String,Object>();
  }


  @Override
  public boolean isMatch(Level level, Tag tag)
  {
    if (level == null)
      throw new NullPointerException("level must not be null");
    if (tag == null)
      throw new NullPointerException("tag must not be null");

    return this.level.severity() >= level.severity() && this.tags.contains(tag) && tag.isMatch(level);
  }


  @Override
  public List<ProtocolEntry> getEntries(Level level, Tag tag)
  {
    if (level == null)
      throw new NullPointerException("level must not be null");
    if (tag == null)
      throw new NullPointerException("tag must not be null");

    return isMatch(level, tag) ? Collections.<ProtocolEntry>singletonList(this) : Collections.<ProtocolEntry>emptyList();
  }


  @Override
  public String format(Locale locale) {
    return "level=" + level + ", message=" + message + ", params=" + parameterValues;
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
