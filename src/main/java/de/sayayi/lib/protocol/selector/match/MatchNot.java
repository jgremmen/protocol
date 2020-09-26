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

import lombok.EqualsAndHashCode;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static de.sayayi.lib.protocol.TagSelector.MatchType.NOT;


/**
 * @author Jeroen Gremmen
 */
@EqualsAndHashCode(callSuper = false)
public final class MatchNot extends AbstractTagSelectorBuilder implements SelectorReference
{
  private final TagSelector selector;


  public MatchNot(@NotNull TagSelector selector)
  {
    super(NOT);
    this.selector = selector;
  }


  @NotNull
  @Override
  public TagSelector[] getSelectors() {
    return new TagSelector[] { selector };
  }


  @Override
  public boolean match(@NotNull Collection<String> tagNames) {
    return !selector.match(tagNames);
  }


  @Override
  public String toString() {
    return "not(" + selector + ')';
  }
}
