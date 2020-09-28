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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Set;


/**
 * <p>
 *   Tag defs are protocol factory specific labels to be used with protocol messages.
 * </p>
 * <p>
 *   Tags can have dependencies to other tags which means that if a message is protocolled
 *   with a particular tag, all tags implied by that tag are also assigned to that message.
 * </p>
 * <p>
 *   The validity of a tag is related to a protocol level. This allows for certain
 *   tags to match only when a particular protocol level is selected.
 * </p>
 *
 * @author Jeroen Gremmen
 *
 * @see ProtocolFactory#createTag(String)
 */
public interface TagDef
{
  /**
   * Tells the tag name.
   *
   * @return  tag name, never {@code null}
   */
  @Contract(pure = true)
  @NotNull String getName();


  /**
   * Tells the matching condition to be used in level matching.
   *
   * @return  matching condition, never {@code null}
   *
   * @see #getMatchLevel()
   */
  @Contract(pure = true)
  @NotNull TagDef.MatchCondition getMatchCondition();


  /**
   * Tells the tag level to be used in level matching.
   *
   * @return  tag level, never {@code null}
   *
   * @see #getMatchCondition()
   */
  @Contract(pure = true)
  @NotNull Level getMatchLevel();


  /**
   * Tells whether this tag matches the given {@code level}.
   *
   * @param level  level to match against
   *
   * @return  {@code true} if this tag matches the given {@code level}
   *
   * @see #getMatchCondition()
   * @see #getMatchLevel()
   */
  @Contract(pure = true)
  boolean matches(@NotNull Level level);


  /**
   * Returns a set of tags which are implied by this tag, including this tag as well.
   *
   * @return  set of implied tags
   */
  @Contract(pure = true, value = "-> new")
  @NotNull Set<TagDef> getImpliedTags();


  /**
   * Returns a tag selector for the current tag def.
   *
   * @return  tag selector
   */
  @Contract(pure = true, value = "-> new")
  @NotNull TagSelector asSelector();




  enum MatchCondition
  {
    /** The selected level must be equal to the tag level in order to match. */
    EQUAL,

    /** The selected level must not be equal to the tag level in order to match. */
    NOT_EQUAL,

    /** The selected level must be at least (including) the tag level in order to match. */
    AT_LEAST,

    /** The selected level must be at most (including) the tag level in order to match. */
    UNTIL
  }
}
