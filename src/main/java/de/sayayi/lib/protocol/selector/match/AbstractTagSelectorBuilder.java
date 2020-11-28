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

import de.sayayi.lib.protocol.Tag;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.exception.ProtocolException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;


/**
 * @author Jeroen Gremmen
 * @since 0.6.0
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractTagSelectorBuilder implements TagSelector.Builder
{
  @Getter private final MatchType type;


  @NotNull
  @Override
  public TagSelector.Builder and(@NotNull TagSelector tagSelector) {
    return Tag.and(this, tagSelector);
  }


  @NotNull
  @Override
  public TagSelector.Builder and(@NotNull String tagName) {
    return Tag.and(this, Tag.of(tagName));
  }


  @NotNull
  @Override
  public TagSelector.Builder or(@NotNull TagSelector tagSelector) {
    return Tag.or(this, tagSelector);
  }


  @NotNull
  @Override
  public TagSelector.Builder or(@NotNull String tagName) {
    return Tag.or(this, Tag.of(tagName));
  }


  public static TagSelector.Builder wrap(@NotNull TagSelector tagSelector)
  {
    if (tagSelector instanceof TagSelector.Builder)
      return (TagSelector.Builder)tagSelector;

    if (tagSelector instanceof TagReference)
      return new TagReferenceBuilder(tagSelector);

    if (tagSelector instanceof SelectorReference)
      return new SelectorReferenceBuilder(tagSelector);

    throw new ProtocolException("malformed tag selector: " + tagSelector);
  }




  private static final class TagReferenceBuilder extends AbstractTagSelectorBuilder implements TagReference
  {
    private final TagSelector selector;


    private TagReferenceBuilder(@NotNull TagSelector selector)
    {
      super(selector.getType());
      this.selector = selector;
    }


    @Override
    public String[] getTagNames() {
      return ((TagReference)selector).getTagNames();
    }


    @Override
    public boolean match(@NotNull Collection<String> tagNames) {
      return selector.match(tagNames);
    }


    @Override
    public String toString() {
      return selector.toString();
    }
  }




  private static final class SelectorReferenceBuilder extends AbstractTagSelectorBuilder implements SelectorReference
  {
    private final TagSelector selector;


    private SelectorReferenceBuilder(@NotNull TagSelector selector)
    {
      super(selector.getType());
      this.selector = selector;
    }


    @NotNull
    @Override
    public TagSelector[] getSelectors() {
      return ((SelectorReference)selector).getSelectors();
    }


    @Override
    public boolean match(@NotNull Collection<String> tagNames) {
      return selector.match(tagNames);
    }


    @Override
    public String toString() {
      return selector.toString();
    }
  }
}
