/*
 * Copyright 2021 Jeroen Gremmen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.sayayi.lib.protocol.matcher;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolEntry.Message;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.matcher.MessageMatcher.Junction;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;


/**
 * @author Jeroen Gremmen
 * @since 1.0.0
 */
final class BooleanMatcher implements Junction, TagSelector
{
  static final BooleanMatcher ANY = new BooleanMatcher(true);
  static final BooleanMatcher NONE = new BooleanMatcher(false);

  private final boolean matches;


  private BooleanMatcher(boolean matches) {
    this.matches = matches;
  }


  @Override
  public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
    return matches;
  }


  @Override
  public boolean match(@NotNull Collection<String> tagNames) {
    return matches;
  }


  @Override
  public boolean isTagSelector() {
    return true;
  }


  @Override
  public @NotNull TagSelector asTagSelector() {
    return this;
  }


  @Override
  public @NotNull MessageMatcher asMessageMatcher() {
    return this;
  }


  @Override
  public boolean equals(Object o) {
    return this == o || o instanceof BooleanMatcher && matches == ((BooleanMatcher)o).matches;
  }


  @Override
  public int hashCode() {
    return Boolean.hashCode(matches);
  }


  @Override
  public String toString() {
    return matches ? "any" : "none";
  }
}
