/*
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
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import static de.sayayi.lib.protocol.Level.Shared.LOWEST;


/**
 * @author Jeroen Gremmen
 */
public abstract class AbstractProtocolFactory<M> implements ProtocolFactory<M>
{
  private static final AtomicInteger FACTORY_ID = new AtomicInteger(0);
  private static final AtomicInteger TAG_ID = new AtomicInteger(0);

  private final Map<String,TagImpl> registeredTags = new TreeMap<String,TagImpl>();
  private final int id;
  @Getter private final Tag defaultTag;

  final Map<String,Object> defaultParameterValues;


  protected AbstractProtocolFactory()
  {
    id = FACTORY_ID.incrementAndGet();
    defaultTag = createTag(DEFAULT_TAG_NAME).getTag();

    defaultParameterValues = new HashMap<String,Object>();
    defaultParameterValues.put("factoryid", id);
  }


  @Override
  public @NotNull Protocol<M> createProtocol() {
    return new ProtocolImpl<M>(this);
  }


  @SuppressWarnings("SuspiciousMethodCalls")
  public boolean isRegisteredTag(@NotNull Tag tag)
  {
    //noinspection ConstantConditions
    if (tag == null)
      throw new NullPointerException("tag must not be null");

    return registeredTags.values().contains(tag);
  }


  @Override
  public @NotNull TagBuilder<M> createTag(@NotNull String name)
  {
    //noinspection ConstantConditions
    if (name == null || name.isEmpty())
      throw new IllegalArgumentException("name must not be empty");

    //noinspection PatternValidation
    if (hasTag(name))
      throw new IllegalArgumentException("tag with name " + name + " already exists");

    TagImpl tag = new TagImpl(name);
    registeredTags.put(name, tag);

    return new TagBuilderImpl(tag);
  }


  @Override
  public @NotNull TagBuilder<M> modifyTag(@NotNull String name)
  {
    //noinspection ConstantConditions
    if (name == null || name.isEmpty())
      throw new IllegalArgumentException("name must not be empty");

    TagImpl tag = registeredTags.get(name);
    if (tag == null)
      throw new IllegalArgumentException("tag with name " + name + " does not exist");

    return new TagBuilderImpl(tag);
  }


  @Override
  public Tag getTagByName(@NotNull String name) {
    return getTagByName0(name);
  }


  private TagImpl getTagByName0(@NotNull String name)
  {
    //noinspection ConstantConditions
    if (name == null || name.isEmpty())
      throw new IllegalArgumentException("name must not be empty");

    return registeredTags.get(name);
  }


  @Override
  public boolean hasTag(@NotNull String name)
  {
    //noinspection ConstantConditions
    if (name == null || name.isEmpty())
      throw new IllegalArgumentException("name must not be empty");

    return registeredTags.containsKey(name);
  }


  @Override
  public @NotNull Set<Tag> getTags() {
    return new TreeSet<Tag>(registeredTags.values());
  }


  @Override
  public @NotNull Map<String,Object> getDefaultParameterValues() {
    return Collections.unmodifiableMap(defaultParameterValues);
  }


  @Override
  public @NotNull String toString() {
    return "ProtocolFactory[id=" + id + ",tags=" + registeredTags.keySet() + ']';
  }


  private class TagBuilderImpl implements TagBuilder<M>
  {
    @Getter private final TagImpl tag;


    TagBuilderImpl(@NotNull TagImpl tag) {
      this.tag = tag;
    }


    @SuppressWarnings("ConstantConditions")
    @Override
    public @NotNull TagBuilder<M> match(@NotNull Tag.MatchCondition matchCondition, @NotNull Level matchLevel)
    {
      if (matchCondition == null)
        throw new NullPointerException("matchCondition must not be null");
      if (matchLevel == null)
        throw new NullPointerException("matchLevel must not be null");

      tag.matchCondition = matchCondition;
      tag.matchLevel = matchLevel;

      return this;
    }


    @Override
    public @NotNull TagBuilder<M> implies(@NotNull String ... tags)
    {
      for(String tagName: tags)
      {
        TagImpl impliedTag = AbstractProtocolFactory.this.getTagByName0(tagName);
        if (impliedTag != null)
          tag.implies.add(impliedTag);
      }

      return this;
    }


    @Override
    public @NotNull TagBuilder<M> dependsOn(@NotNull String ... tags)
    {
      for(String tagName: tags)
      {
        TagImpl dependsOnTag = AbstractProtocolFactory.this.getTagByName0(tagName);
        if (dependsOnTag != null)
          dependsOnTag.implies.add(tag);
      }

      return this;
    }


    @SuppressWarnings("PatternValidation")
    @Override
    public @NotNull TagBuilder<M> createTag(@NotNull String name) {
      return AbstractProtocolFactory.this.createTag(name);
    }


    @SuppressWarnings("PatternValidation")
    @Override
    public @NotNull TagBuilder<M> modifyTag(@NotNull String name) {
      return AbstractProtocolFactory.this.modifyTag(name);
    }


    @Override
    public @NotNull Protocol<M> createProtocol() {
      return AbstractProtocolFactory.this.createProtocol();
    }


    @SuppressWarnings("PatternValidation")
    @Override
    public Tag getTagByName(@NotNull String name) {
      return AbstractProtocolFactory.this.getTagByName(name);
    }


    @SuppressWarnings("PatternValidation")
    @Override
    public boolean hasTag(@NotNull String name) {
      return AbstractProtocolFactory.this.hasTag(name);
    }


    @Override
    public boolean isRegisteredTag(@NotNull Tag tag) {
      return AbstractProtocolFactory.this.isRegisteredTag(tag);
    }


    @Override
    public @NotNull Set<Tag> getTags() {
      return AbstractProtocolFactory.this.getTags();
    }


    @Override
    public @NotNull Tag getDefaultTag() {
      return AbstractProtocolFactory.this.getDefaultTag();
    }


    @Override
    public @NotNull Map<String,Object> getDefaultParameterValues() {
      return AbstractProtocolFactory.this.getDefaultParameterValues();
    }


    @Override
    public M processMessage(@NotNull String message) {
      return AbstractProtocolFactory.this.processMessage(message);
    }
  }


  static class TagImpl implements Tag, Comparable<TagImpl>
  {
    @Getter private final int id;
    @Getter private final String name;

    @Getter private MatchCondition matchCondition = MatchCondition.AT_LEAST;
    @Getter private Level matchLevel = LOWEST;

    private Set<TagImpl> implies = new HashSet<TagImpl>(8);


    TagImpl(@NotNull String name)
    {
      this.name = name;
      id = TAG_ID.incrementAndGet();
    }


    @Override
    public boolean isMatch(@NotNull Level level)
    {
      //noinspection ConstantConditions
      if (level == null)
        throw new NullPointerException("level must not be null");

      switch(matchCondition)
      {
        case AT_LEAST:
          return level.severity() >= matchLevel.severity();

        case EQUAL:
          return level.severity() == matchLevel.severity();

        case NOT_EQUAL:
          return level.severity() != matchLevel.severity();

        case UNTIL:
          return level.severity() <= matchLevel.severity();
      }

      return false;
    }


    @Override
    public @NotNull Set<Tag> getImpliedTags()
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

      s.append(',').append(matchLevel);

      switch(matchCondition)
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
