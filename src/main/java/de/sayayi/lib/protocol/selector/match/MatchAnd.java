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
package de.sayayi.lib.protocol.selector.match;

import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.TagSelector.SelectorReference;

import lombok.Getter;
import lombok.val;
import lombok.var;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static de.sayayi.lib.protocol.TagSelector.MatchType.AND;


/**
 * @author Jeroen Gremmen
 * @since 0.6.0
 */
public final class MatchAnd extends AbstractTagSelectorBuilder implements SelectorReference
{
  @Getter private final TagSelector[] selectors;


  public MatchAnd(@NotNull TagSelector[] selectors)
  {
    super(AND);
    this.selectors = selectors;
  }


  @Override
  public boolean match(@NotNull Collection<String> tagNames)
  {
    for(val tagSelector: selectors)
      if (!tagSelector.match(tagNames))
        return false;

    return true;
  }


  @Override
  public String toString()
  {
    if (selectors.length == 1)
      return selectors[0].toString();

    val s = new StringBuilder("and(");
    var first = true;

    for(val selector: selectors)
    {
      if (first)
        first = false;
      else
        s.append(',');

      s.append(selector);
    }

    return s.append(')').toString();
  }
}
