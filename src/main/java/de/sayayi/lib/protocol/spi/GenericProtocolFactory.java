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
import de.sayayi.lib.protocol.TagDef;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.exception.ProtocolException;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.var;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static de.sayayi.lib.protocol.Level.Shared.LOWEST;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.UNICODE_CASE;


/**
 * {@inheritDoc}
 *
 * @author Jeroen Gremmen
 */
public class GenericProtocolFactory<M> implements ProtocolFactory<M>
{
  private static final Pattern TAG_NAME_PATTERN =
      Pattern.compile("\\p{Alpha}[-_\\p{Alnum}]*", CASE_INSENSITIVE | UNICODE_CASE);

  private static final AtomicInteger FACTORY_ID = new AtomicInteger(0);
  private static final AtomicInteger TAG_ID = new AtomicInteger(0);

  private final Map<String,TagDefImpl> registeredTags = new TreeMap<String,TagDefImpl>();
  private final int id;
  @Getter private final TagDef defaultTag;

  @Getter @Setter private MessageProcessor<M> messageProcessor;

  protected final Map<String,Object> defaultParameterValues;


  @SuppressWarnings({ "java:S2583", "ConstantConditions" })
  public GenericProtocolFactory(@NotNull MessageProcessor<M> messageProcessor)
  {
    if (messageProcessor == null)
      throw new NullPointerException("messageProcessur must not be null");

    setMessageProcessor(messageProcessor);

    id = FACTORY_ID.incrementAndGet();

    defaultTag = createTag(DEFAULT_TAG_NAME).getTagDef();
    defaultParameterValues = new HashMap<String,Object>();
  }


  @Override
  public @NotNull Protocol<M> createProtocol() {
    return new ProtocolImpl<M>(this);
  }


  @Override
  public boolean isValidTagName(String tagName) {
    return tagName != null && !tagName.isEmpty() && TAG_NAME_PATTERN.matcher(tagName).matches();
  }


  @Override
  @SuppressWarnings({ "squid:S2589", "squid:S1192" })
  public @NotNull TagBuilder<M> createTag(@NotNull String name)
  {
    if (!isValidTagName(name))
      throw new ProtocolException("invalid tag name '" + name + "'");

    if (hasTag(name))
      throw new ProtocolException("tag with name " + name + " already exists");

    val tag = new TagDefImpl(name);
    registeredTags.put(name, tag);

    return new TagBuilderImpl(tag);
  }


  @Override
  @SuppressWarnings({ "squid:S2589", "squid:S1192" })
  public @NotNull TagBuilder<M> modifyTag(@NotNull String name)
  {
    if (!isValidTagName(name))
      throw new ProtocolException("invalid tag name '" + name + "'");

    val tag = registeredTags.get(name);
    if (tag == null)
      throw new ProtocolException("tag with name " + name + " does not exist");

    return new TagBuilderImpl(tag);
  }


  @Override
  public @NotNull TagDef getTagByName(@NotNull String name) {
    return getTagByName0(name);
  }


  @SuppressWarnings({ "squid:S2589", "java:S1121" })
  private @NotNull TagDefImpl getTagByName0(@NotNull String name)
  {
    if (!isValidTagName(name))
      throw new ProtocolException("invalid tag name '" + name + "'");

    var tagDef = registeredTags.get(name);
    if (tagDef == null)
      registeredTags.put(name, tagDef = new TagDefImpl(name));

    return tagDef;
  }


  @Override
  @SuppressWarnings({ "squid:S2589" })
  public boolean hasTag(String name) {
    return name != null && registeredTags.containsKey(name);
  }


  @Override
  public @NotNull Set<String> getTagNames() {
    return Collections.unmodifiableSet(registeredTags.keySet());
  }


  @Override
  public @NotNull Set<TagDef> getTagDefs() {
    return new TreeSet<TagDef>(registeredTags.values());
  }


  @Override
  public @NotNull Map<String,Object> getDefaultParameterValues() {
    return Collections.unmodifiableMap(defaultParameterValues);
  }


  @Override
  public @NotNull String toString() {
    return "ProtocolFactory[id=" + id + ",tagDefs=" + registeredTags.keySet() + ']';
  }




  private class TagBuilderImpl implements TagBuilder<M>
  {
    @Getter private final TagDefImpl tagDef;


    TagBuilderImpl(@NotNull TagDefImpl tagDef) {
      this.tagDef = tagDef;
    }


    @SuppressWarnings({"ConstantConditions", "squid:S2583"})
    @Override
    public @NotNull TagBuilder<M> match(@NotNull TagDef.MatchCondition matchCondition, @NotNull Level matchLevel)
    {
      if (matchCondition == null)
        throw new NullPointerException("matchCondition must not be null");
      if (matchLevel == null)
        throw new NullPointerException("matchLevel must not be null");

      tagDef.matchCondition = matchCondition;
      tagDef.matchLevel = matchLevel;

      return this;
    }


    @Override
    public @NotNull TagBuilder<M> implies(@NotNull String ... tagDefs)
    {
      for(val tagName: tagDefs)
        tagDef.implies.add(GenericProtocolFactory.this.getTagByName0(tagName));

      return this;
    }


    @Override
    public @NotNull TagBuilder<M> dependsOn(@NotNull String ... tagDefs)
    {
      for(val tagName: tagDefs)
        GenericProtocolFactory.this.getTagByName0(tagName).implies.add(tagDef);

      return this;
    }


    @Override
    public boolean isValidTagName(String tagName) {
      return GenericProtocolFactory.this.isValidTagName(tagName);
    }


    @Override
    public @NotNull TagBuilder<M> createTag(@NotNull String name) {
      return GenericProtocolFactory.this.createTag(name);
    }


    @Override
    public @NotNull TagBuilder<M> modifyTag(@NotNull String name) {
      return GenericProtocolFactory.this.modifyTag(name);
    }


    @Override
    public @NotNull Protocol<M> createProtocol() {
      return GenericProtocolFactory.this.createProtocol();
    }


    @Override
    public @NotNull TagDef getTagByName(@NotNull String name) {
      return GenericProtocolFactory.this.getTagByName(name);
    }


    @Override
    public boolean hasTag(String name) {
      return GenericProtocolFactory.this.hasTag(name);
    }


    @Override
    public @NotNull Set<String> getTagNames() {
      return GenericProtocolFactory.this.getTagNames();
    }


    @Override
    public @NotNull Set<TagDef> getTagDefs() {
      return GenericProtocolFactory.this.getTagDefs();
    }


    @Override
    public @NotNull TagDef getDefaultTag() {
      return GenericProtocolFactory.this.getDefaultTag();
    }


    @Override
    public @NotNull Map<String,Object> getDefaultParameterValues() {
      return GenericProtocolFactory.this.getDefaultParameterValues();
    }


    @Override
    public @NotNull MessageProcessor<M> getMessageProcessor() {
      return GenericProtocolFactory.this.getMessageProcessor();
    }


    @Override
    public void setMessageProcessor(@NotNull MessageProcessor<M> messageProcessor) {
      GenericProtocolFactory.this.setMessageProcessor(messageProcessor);
    }
  }




  @EqualsAndHashCode(doNotUseGetters = true, onlyExplicitlyIncluded = true)
  static final class TagDefImpl implements TagDef, Comparable<TagDefImpl>
  {
    @EqualsAndHashCode.Include
    private final int id;

    @Getter private final String name;

    @Getter private MatchCondition matchCondition = MatchCondition.AT_LEAST;
    @Getter private Level matchLevel = LOWEST;

    private final Set<TagDefImpl> implies = new TreeSet<TagDefImpl>();


    TagDefImpl(@NotNull String name)
    {
      this.name = name;
      id = TAG_ID.incrementAndGet();
    }


    @Override
    @SuppressWarnings({ "squid:S2583", "ConstantConditions" })
    public boolean matches(@NotNull Level level)
    {
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

        default:
          return false;
      }
    }


    @Override
    public @NotNull Set<TagDef> getImpliedTags()
    {
      val tagDefs = new HashSet<TagDef>();

      collectImpliedTags(tagDefs);

      return tagDefs;
    }


    private void collectImpliedTags(Set<TagDef> tagDefs)
    {
      tagDefs.add(this);

      for(val tag: implies)
        if (!tagDefs.contains(tag))
          tag.collectImpliedTags(tagDefs);
    }


    @Override
    public @NotNull TagSelector asSelector() {
      return Tag.of(name);
    }


    @Override
    public int compareTo(TagDefImpl o) {
      return id - o.id;
    }


    @Override
    public String toString()
    {
      val s = new StringBuilder("Tag[id=").append(id).append(",name=").append(name);

      if (!implies.isEmpty())
      {
        s.append(",implies={");
        var first = true;

        for(TagDef tagDef: implies)
        {
          if (first)
            first = false;
          else
            s.append(',');

          s.append(tagDef.getName());
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

        default:
          break;
      }

      return s.append(']').toString();
    }
  }
}
