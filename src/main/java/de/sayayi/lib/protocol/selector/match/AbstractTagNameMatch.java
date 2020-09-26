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

import de.sayayi.lib.protocol.TagSelector.TagReference;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;
import lombok.var;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static java.lang.System.arraycopy;
import static java.util.Arrays.binarySearch;


/**
 * @author Jeroen Gremmen
 */
@EqualsAndHashCode(callSuper = false)
abstract class AbstractTagNameMatch extends AbstractTagSelectorBuilder implements TagReference
{
  @Getter private final String[] tagNames;


  protected AbstractTagNameMatch(@NotNull MatchType type, @NotNull String[] tagNames)
  {
    super(type);

    if (tagNames.length <= 1)
      this.tagNames = tagNames;
    else
    {
      val uniqueTagNames = new String[tagNames.length];
      var length = 0;
      int idx;

      for(val tagName: tagNames)
        if ((idx = binarySearch(uniqueTagNames, 0, length, tagName)) < 0)
        {
          idx = -(idx + 1);

          if (idx < length)
            arraycopy(uniqueTagNames, idx, uniqueTagNames, idx + 1, length - idx);

          uniqueTagNames[idx] = tagName;
          length++;
        }

      this.tagNames = length < uniqueTagNames.length ? Arrays.copyOf(uniqueTagNames, length) : uniqueTagNames;
    }
  }


  @Contract(value = "null -> false", pure = true)
  public boolean contains(String tagName) {
    return binarySearch(this.tagNames, tagName) >= 0;
  }


  protected String tagNamesAsCSV()
  {
    if (tagNames.length == 1)
      return tagNames[0];

    val s = new StringBuilder();
    var first = true;

    for(val tagName: tagNames)
    {
      if (first)
        first = false;
      else
        s.append(',');

      s.append(tagName);
    }

    return s.toString();
  }


  protected static @NotNull String[] merge(@NotNull String[] tagNames1, @NotNull String[] tagNames2)
  {
    val len1 = tagNames1.length;
    if (len1 == 0)
      return tagNames2;

    val len2 = tagNames2.length;
    if (len2 == 0)
      return tagNames1;

    val merged = new String[len1 + len2];

    arraycopy(tagNames1, 0, merged, 0, len1);
    arraycopy(tagNames2, 0, merged, len1, len2);

    return merged;
  }
}
