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
import de.sayayi.lib.protocol.Level.Shared;
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolFormatter.Initializable;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.Tag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author Jeroen Gremmen
 */
abstract class AbstractProtocol<M,B extends ProtocolMessageBuilder<M>> implements Protocol<M>
{
  final AbstractProtocolFactory<M> factory;
  final List<ProtocolEntry<M>> entries;

  private Set<Tag> tags;
  private Level maxLevel;


  AbstractProtocol(AbstractProtocolFactory<M> factory)
  {
    this.factory = factory;
    entries = new ArrayList<ProtocolEntry<M>>(8);

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
  public List<ProtocolEntry<M>> getEntries(Level level, Tag tag)
  {
    List<ProtocolEntry<M>> filteredEntries = new ArrayList<ProtocolEntry<M>>();

    if (tag.isMatch(level))
      for(ProtocolEntry<M> entry: entries)
        if (entry.isMatch(level, tag))
          filteredEntries.add(entry);

    return filteredEntries;
  }


  @Override
  public boolean hasVisibleElement(Level level, Tag tag)
  {
    if (tag.isMatch(level))
      for(ProtocolEntry<M> entry: entries)
        if (entry.hasVisibleElement(level, tag))
          return true;

    return false;
  }


  @Override
  public ProtocolGroup<M> createGroup()
  {
    @SuppressWarnings("unchecked")
    ProtocolGroupImpl<M> group = new ProtocolGroupImpl<M>((AbstractProtocol<M,ProtocolMessageBuilder<M>>)this);

    entries.add(group);

    return group;
  }


  @Override
  public <R> R format(Level level, Tag tag, ProtocolFormatter<M,R> formatter)
  {
    if (formatter instanceof Initializable)
      ((Initializable)formatter).init(level, tag);

    return formatter.getResult();
  }
}
