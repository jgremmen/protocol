package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Level.Shared;
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.Tag;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractProtocol<B extends ProtocolMessageBuilder> implements Protocol
{
  final ProtocolFactoryImpl factory;
  final List<ProtocolEntry> entries;


  AbstractProtocol(ProtocolFactoryImpl factory)
  {
    this.factory = factory;
    entries = new ArrayList<ProtocolEntry>(8);
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
  public boolean isMatch(Level level, Tag tag)
  {
    for(ProtocolEntry entry: entries)
      if (entry.isMatch(level, tag))
        return true;

    return false;
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
    ProtocolGroupImpl group = new ProtocolGroupImpl(this);

    entries.add(group);

    return group;
  }
}
