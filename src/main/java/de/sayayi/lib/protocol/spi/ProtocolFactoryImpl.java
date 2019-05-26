package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.Tag;
import de.sayayi.lib.protocol.Tag.LevelMatch;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import static de.sayayi.lib.protocol.Level.Shared.ALL;


public class ProtocolFactoryImpl implements ProtocolFactory
{
  private static final AtomicInteger FACTORY_ID = new AtomicInteger(0);
  private static final AtomicInteger TAG_ID = new AtomicInteger(0);

  private final Map<String,TagImpl> registeredTags = new TreeMap<String,TagImpl>();
  private final int id;

  final Tag systemTag;


  public ProtocolFactoryImpl()
  {
    id = FACTORY_ID.incrementAndGet();
    systemTag = createTag("system").getTag();
  }


  @Override
  public Protocol createProtocol() {
    return new ProtocolImpl(this);
  }


  @SuppressWarnings("SuspiciousMethodCalls")
  boolean isRegisteredTag(Tag tag) {
    return registeredTags.values().contains(tag);
  }


  @Override
  public TagBuilder createTag(String name)
  {
    if (hasTag(name))
      throw new IllegalArgumentException("tag with name " + name + " already exists");

    TagImpl tag = new TagImpl(name);
    registeredTags.put(name, tag);

    return new TagBuilderImpl(tag);
  }


  @Override
  public TagBuilder modifyTag(String name)
  {
    TagImpl tag = registeredTags.get(name);
    if (tag == null)
      throw new IllegalArgumentException("tag with name " + name + " does not exist");

    return new TagBuilderImpl(tag);
  }


  @Override
  public TagImpl getTagByName(String name) {
    return registeredTags.get(name);
  }


  @Override
  public boolean hasTag(String name) {
    return registeredTags.containsKey(name);
  }


  @Override
  public Set<Tag> getTags() {
    return new TreeSet<Tag>(registeredTags.values());
  }


  @Override
  public String toString() {
    return "ProtocolFactory[id=" + id + ",tags=" + registeredTags.keySet() + ']';
  }


  private class TagBuilderImpl implements TagBuilder
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
    public TagBuilder match(LevelMatch match, Level level)
    {
      tag.match = match;
      tag.level = level;

      return this;
    }


    @Override
    public TagBuilder implies(String... tags)
    {
      for(String tagName: tags)
      {
        TagImpl impliedTag = ProtocolFactoryImpl.this.getTagByName(tagName);
        if (impliedTag != null)
          tag.implies.add(impliedTag);
      }

      return this;
    }


    @Override
    public TagBuilder dependsOn(String... tags)
    {
      for(String tagName: tags)
      {
        TagImpl dependsOnTag = ProtocolFactoryImpl.this.getTagByName(tagName);
        if (dependsOnTag != null)
          dependsOnTag.implies.add(tag);
      }

      return this;
    }


    @Override
    public TagBuilder createTag(String name) {
      return ProtocolFactoryImpl.this.createTag(name);
    }


    @Override
    public TagBuilder modifyTag(String name) {
      return ProtocolFactoryImpl.this.modifyTag(name);
    }


    @Override
    public Protocol createProtocol() {
      return ProtocolFactoryImpl.this.createProtocol();
    }


    @Override
    public Tag getTagByName(String name) {
      return ProtocolFactoryImpl.this.getTagByName(name);
    }


    @Override
    public boolean hasTag(String name) {
      return ProtocolFactoryImpl.this.hasTag(name);
    }


    @Override
    public Set<Tag> getTags() {
      return ProtocolFactoryImpl.this.getTags();
    }
  }


  class TagImpl implements Tag, Comparable<TagImpl>
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


    void collectImpliedTags(Set<Tag> tags)
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
