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

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static de.sayayi.lib.protocol.TagSelector.MatchType.ANY;


/**
 * @author Jeroen Gremmen
 * @since 0.6.0
 */
@EqualsAndHashCode(callSuper = false)
public final class MatchAny extends AbstractTagSelectorBuilder implements TagReference
{
  public MatchAny() {
    super(ANY);
  }


  @SuppressWarnings("java:S1168")
  @Override
  public String[] getTagNames() {
    return null;
  }


  @Override
  public boolean match(@NotNull Collection<String> tagNames) {
    return !tagNames.isEmpty();
  }


  @Override
  public String toString() {
    return "any()";
  }
}
