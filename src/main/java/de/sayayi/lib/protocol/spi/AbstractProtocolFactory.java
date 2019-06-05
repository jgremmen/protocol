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
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.Tag;
import de.sayayi.lib.protocol.Tag.LevelMatch;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import static de.sayayi.lib.protocol.Level.Shared.ALL;


/**
 * @author Jeroen Gremmen
 */
public abstract class AbstractProtocolFactory<M> implements ProtocolFactory<M>
{
  private static final AtomicInteger FACTORY_ID = new AtomicInteger(0);
  private static final AtomicInteger TAG_ID = new AtomicInteger(0);

  private final Map<String,TagImpl> registeredTags = new TreeMap<String,TagImpl>();
  private final int id;
  private final Tag defaultTag;

  final Map<String,Object> defaultParameterValues;


  protected AbstractProtocolFactory()
  {
    id = FACTORY_ID.incrementAndGet();
    defaultTag = createTag(DEFAULT_TAG_NAME).getTag();

    defaultParameterValues = new HashMap<String,Object>();
    defaultParameterValues.put("factoryid", id);
  }


  public Tag getDefaultTag() {
    return defaultTag;
  }


  @Override
  public Protocol<M> createProtocol() {
    return new ProtocolImpl<M>(this);
  }


  @SuppressWarnings("SuspiciousMethodCalls")
  public boolean isRegisteredTag(Tag tag)
  {
    if (tag == null)
      throw new NullPointerException("tag must not be null");

    return registeredTags.values().contains(tag);
  }


  @Override
  public TagBuilder<M> createTag(String name)
  {
    if (name == null || name.isEmpty())
      throw new IllegalArgumentException("name must not be empty");

    if (hasTag(name))
      throw new IllegalArgumentException("tag with name " + name + " already exists");

    TagImpl tag = new TagImpl(name);
    registeredTags.put(name, tag);

    return new TagBuilderImpl(tag);
  }


  @Override
  public TagBuilder<M> modifyTag(String name)
  {
    if (name == null || name.isEmpty())
      throw new IllegalArgumentException("name must not be empty");

    TagImpl tag = registeredTags.get(name);
    if (tag == null)
      throw new IllegalArgumentException("tag with name " + name + " does not exist");

    return new TagBuilderImpl(tag);
  }


  @Override
  public TagImpl getTagByName(String name)
  {
    if (name == null || name.isEmpty())
      throw new IllegalArgumentException("name must not be empty");

    return registeredTags.get(name);
  }


  @Override
  public boolean hasTag(String name)
  {
    if (name == null || name.isEmpty())
      throw new IllegalArgumentException("name must not be empty");

    return registeredTags.containsKey(name);
  }


  @Override
  public Set<Tag> getTags() {
    return new TreeSet<Tag>(registeredTags.values());
  }


  @Override
  public Map<String,Object> getDefaultParameterValues() {
    return Collections.unmodifiableMap(defaultParameterValues);
  }


  @Override
  public String toString() {
    return "ProtocolFactory[id=" + id + ",tags=" + registeredTags.keySet() + ']';
  }


  private class TagBuilderImpl implements TagBuilder<M>
  {
    private final TagImpl tag;


    TagBuilderImpl(TagImpl tag) {
      this.tag = tag;
    }


    @Override
    public Tag getTag() {
      return tag;
    }


    @Override
    public TagBuilder<M> match(LevelMatch match, Level level)
    {
      if (match == null)
        throw new NullPointerException("match must not be null");
      if (level == null)
        throw new NullPointerException("level must not be null");

      tag.match = match;
      tag.level = level;

      return this;
    }


    @Override
    public TagBuilder<M> implies(String ... tags)
    {
      for(String tagName: tags)
      {
        TagImpl impliedTag = AbstractProtocolFactory.this.getTagByName(tagName);
        if (impliedTag != null)
          tag.implies.add(impliedTag);
      }

      return this;
    }


    @Override
    public TagBuilder<M> dependsOn(String ... tags)
    {
      for(String tagName: tags)
      {
        TagImpl dependsOnTag = AbstractProtocolFactory.this.getTagByName(tagName);
        if (dependsOnTag != null)
          dependsOnTag.implies.add(tag);
      }

      return this;
    }


    @Override
    public TagBuilder<M> createTag(String name) {
      return AbstractProtocolFactory.this.createTag(name);
    }


    @Override
    public TagBuilder<M> modifyTag(String name) {
      return AbstractProtocolFactory.this.modifyTag(name);
    }


    @Override
    public Protocol<M> createProtocol() {
      return AbstractProtocolFactory.this.createProtocol();
    }


    @Override
    public Tag getTagByName(String name) {
      return AbstractProtocolFactory.this.getTagByName(name);
    }


    @Override
    public boolean hasTag(String name) {
      return AbstractProtocolFactory.this.hasTag(name);
    }


    @Override
    public boolean isRegisteredTag(Tag tag) {
      return AbstractProtocolFactory.this.isRegisteredTag(tag);
    }


    @Override
    public Set<Tag> getTags() {
      return AbstractProtocolFactory.this.getTags();
    }


    @Override
    public Tag getDefaultTag() {
      return AbstractProtocolFactory.this.getDefaultTag();
    }


    @Override
    public Map<String,Object> getDefaultParameterValues() {
      return AbstractProtocolFactory.this.getDefaultParameterValues();
    }


    @Override
    public M processMessage(String message) {
      return AbstractProtocolFactory.this.processMessage(message);
    }
  }


  static class TagImpl implements Tag, Comparable<TagImpl>
  {
    private final int id;
    private final String name;

    private LevelMatch match = LevelMatch.AT_LEAST;
    private Level level = ALL;
    private Set<TagImpl> implies = new HashSet<TagImpl>(8);


    TagImpl(String name)
    {
      this.name = name;
      id = TAG_ID.incrementAndGet();
    }


    @Override
    public String getName() {
      return name;
    }


    @Override
    public LevelMatch getLevelMatch() {
      return match;
    }


    @Override
    public Level getLevel() {
      return level;
    }


    @Override
    public boolean isMatch(Level level)
    {
      if (level == null)
        throw new NullPointerException("level must not be null");

      switch(match)
      {
        case AT_LEAST:
          return level.severity() >= this.level.severity();

        case EQUAL:
          return level.severity() == this.level.severity();

        case NOT_EQUAL:
          return level.severity() != this.level.severity();

        case UNTIL:
          return level.severity() <= this.level.severity();
      }

      return false;
    }


    @Override
    public Set<Tag> getImpliedTags()
    {
      HashSet<Tag> tags = new HashSet<Tag>();

      collectImpliedTags(tags);

      return tags;
    }


    private void collectImpliedTags(Set<Tag> tags)
    {
      tags.add(this);

      for(TagImpl tag: implies)
        if (!tags.contains(tag))
          tag.collectImpliedTags(tags);
    }


    @Override
    public int hashCode() {
      return id;
    }


    @Override
    public int compareTo(TagImpl o) {
      return id - o.id;
    }


    @Override
    public String toString()
    {
      StringBuilder s = new StringBuilder("Tag[id=").append(id).append(",name=").append(name);

      if (!implies.isEmpty())
      {
        s.append(",implies={");
        boolean first = true;

        for(Tag tag: implies)
        {
          if (first)
            first = false;
          else
            s.append(',');

          s.append(tag.getName());
        }

        s.append('}');
      }

      s.append(',').append(level);

      switch(match)
      {
        case AT_LEAST:
          s.append("(>=)");
          break;

        case NOT_EQUAL:
          s.append("(!=)");
          break;

        case UNTIL:
          s.append("(<=)");
          break;
      }

      return s.append(']').toString();
    }
  }
}
