package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.Tag.LevelMatch;

import java.util.Set;


public interface ProtocolFactory
{
  Protocol createProtocol();

  TagBuilder createTag(String name);

  TagBuilder modifyTag(String name);

  Tag getTagByName(String name);

  boolean hasTag(String name);

  Set<Tag> getTags();


  interface TagBuilder extends ProtocolFactory
  {
    TagBuilder dependsOn(String ... tags);

    TagBuilder implies(String ... tags);

    TagBuilder match(LevelMatch match, Level level);

    Tag getTag();
  }
}
