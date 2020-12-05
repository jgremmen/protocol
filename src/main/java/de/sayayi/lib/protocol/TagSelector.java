/*
 * Copyright 2020 Jeroen Gremmen
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

import java.util.Collection;


/**
 * @author Jeroen Gremmen
 * @since 0.6.0
 */
public interface TagSelector
{
  @Contract(pure = true)
  @NotNull MatchType getType();


  @Contract(pure = true)
  boolean match(@NotNull Collection<String> tagNames);




  /**
   * Builder for constructing complex tag selectors.
   */
  interface Builder extends TagSelector
  {
    @Contract(pure = true)
    @NotNull Builder and(@NotNull TagSelector tagSelector);


    @Contract(pure = true)
    @NotNull Builder and(@NotNull String tagName);


    @Contract(pure = true)
    @NotNull Builder or(@NotNull TagSelector tagSelector);


    @Contract(pure = true)
    @NotNull Builder or(@NotNull String tagName);
  }




  /**
   * @see MatchType#ANY
   * @see MatchType#ANY_OF
   * @see MatchType#ALL_OF
   */
  interface TagReference extends TagSelector
  {
    @Contract(pure = true)
    String[] getTagNames();
  }




  /**
   * @see MatchType#AND
   * @see MatchType#OR
   * @see MatchType#NOT
   */
  interface SelectorReference extends TagSelector
  {
    @Contract(pure = true)
    @NotNull TagSelector[] getSelectors();
  }




  enum MatchType
  {
    ANY, ALL_OF, ANY_OF, AND, OR, NOT, FIX;


    public boolean isOf() {
      return this == ALL_OF || this == ANY_OF;
    }
  }
}
