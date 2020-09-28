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

import lombok.EqualsAndHashCode;
import lombok.val;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static de.sayayi.lib.protocol.TagSelector.MatchType.ALL_OF;


/**
 * @author Jeroen Gremmen
 */
@EqualsAndHashCode(callSuper = true)
public final class MatchAllOf extends AbstractTagNameMatch
{
  public MatchAllOf(@NotNull String tagName) {
    super(ALL_OF, new String[] { tagName });
  }


  public MatchAllOf(@NotNull String[] tagNames) {
    super(ALL_OF, tagNames);
  }


  public MatchAllOf(@NotNull String[] tagNames1, @NotNull String[] tagNames2) {
    super(ALL_OF, merge(tagNames1, tagNames2));
  }


  @Override
  public boolean match(@NotNull Collection<String> tagNames)
  {
    if (tagNames.isEmpty())
      return false;

    for(val tagName: getTagNames())
      if (!tagNames.contains(tagName))
        return false;

    return true;
  }


  @Override
  public String toString()
  {
    final String tagNames = tagNamesAsCSV();
    return getTagNames().length == 1 ? tagNames : ("allOf(" + tagNames + ')');
  }
}
