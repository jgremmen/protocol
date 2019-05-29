package de.sayayi.lib.protocol;

import java.util.Set;


public interface Tag
{
  /**
   * Tells the tag name.
   *
   * @return  tag name
   */
  String getName();


  LevelMatch getLevelMatch();


  Level getLevel();


  boolean isMatch(Level level);


  Set<Tag> getImpliedTags();


  enum LevelMatch {
    EQUAL, NOT_EQUAL, AT_LEAST, UNTIL
  }
}
