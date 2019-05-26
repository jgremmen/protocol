package de.sayayi.lib.protocol;

import java.util.Set;


public interface Tag
{
  String getName();

  LevelMatch getLevelMatch();

  Level getLevel();

  boolean isMatch(Level level);

  Set<Tag> getImpliedTags();


  enum LevelMatch {
    EQUAL, NOT_EQUAL, AT_LEAST, UNTIL;
  }
}
