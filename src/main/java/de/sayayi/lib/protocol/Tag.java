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
package de.sayayi.lib.protocol;

import java.util.Set;


/**
 * @author Jeroen Gremmen
 */
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


  /**
   * Tells whether this tag matches the given {@code level}.
   *
   * @param level  level to match against
   *
   * @return  {@code true} if this tag matches the given {@code level}
   *
   * @see #getLevelMatch()
   * @see #getLevel()
   */
  boolean isMatch(Level level);


  /**
   * Returns a set of tags which are implied by this tag, including this tag as well.
   *
   * @return  set of implied tags
   */
  Set<Tag> getImpliedTags();


  enum LevelMatch {
    EQUAL, NOT_EQUAL, AT_LEAST, UNTIL
  }
}
