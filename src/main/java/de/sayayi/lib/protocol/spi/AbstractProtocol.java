package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Level.Shared;
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.Tag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public abstract class AbstractProtocol<M,B extends ProtocolMessageBuilder> implements Protocol
{
  final AbstractProtocolFactory<M> factory;
  final List<ProtocolEntry> entries;

  private Set<Tag> tags;
  private Level maxLevel;


  AbstractProtocol(AbstractProtocolFactory<M> factory)
  {
    this.factory = factory;
    entries = new ArrayList<ProtocolEntry>(8);

    tags = new HashSet<Tag>();
    maxLevel = Shared.ALL;
  }


  protected void updateTagAndLevel(Set<Tag> tags, Level level)
  {
    if (tags != null)
      this.tags.addAll(tags);

    if (level != null && level.severity() > maxLevel.severity())
      maxLevel = level;
  }


  @Override
  public B debug() {
    return add(Shared.DEBUG);
  }


  @Override
  public B info() {
    return add(Shared.INFO);
  }


  @Override
  public B warn() {
    return add(Shared.WARN);
  }


  @Override
  public B error() {
    return add(Shared.ERROR);
  }


  public abstract B add(Level level);


  @Override
  public boolean isMatch(Level level, Tag tag) {
    return maxLevel.severity() >= level.severity() && tag.isMatch(level) && tags.contains(tag);
  }


  @Override
  public List<ProtocolEntry> getEntries(Level level, Tag tag)
  {
    List<ProtocolEntry> filteredEntries = new ArrayList<ProtocolEntry>();

    for(ProtocolEntry entry: entries)
      if (entry.isMatch(level, tag))
        filteredEntries.add(entry);

    return filteredEntries;
  }


  @Override
  public ProtocolGroup createGroup()
  {
    @SuppressWarnings("unchecked")
    ProtocolGroupImpl group = new ProtocolGroupImpl<M>((AbstractProtocol<M,ProtocolMessageBuilder>)this);

    entries.add(group);

    return group;
  }
}
