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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static de.sayayi.lib.protocol.TagSelector.MatchType.FIX;


/**
 * @author Jeroen Gremmen
 */
@EqualsAndHashCode(callSuper = false)
public final class MatchFixResult extends AbstractTagSelectorBuilder
{
  public static final MatchFixResult TRUE = new MatchFixResult(true);
  public static final MatchFixResult FALSE = new MatchFixResult(false);

  private final boolean result;


  public MatchFixResult(boolean result)
  {
    super(FIX);
    this.result = result;
  }


  @Override
  public boolean match(@NotNull Collection<String> tagNames) {
    return result;
  }


  @Override
  public String toString() {
    return result ? "true()" : "false()";
  }


  @Contract(pure = true)
  public static MatchFixResult valueOf(boolean result) {
    return result ? TRUE : FALSE;
  }
}
