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
package de.sayayi.lib.protocol.matcher.internal;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolEntry.Message;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.matcher.MessageMatcher;
import de.sayayi.lib.protocol.matcher.MessageMatcher.Junction;

import org.jetbrains.annotations.NotNull;


/**
 * A constant matcher that unconditionally matches or rejects all messages. The two singleton instances {@link #ANY}
 * and {@link #NONE} also implement {@link TagSelector}, making them usable in both matcher and tag selector contexts.
 *
 * @author Jeroen Gremmen
 * @since 1.0.0  (refactored in 1.6.0)
 */
public final class BooleanMatcher implements Junction, TagSelector
{
  /** Matcher that matches every message. */
  public static final BooleanMatcher ANY = new BooleanMatcher(true);

  /** Matcher that matches no message. */
  public static final BooleanMatcher NONE = new BooleanMatcher(false);

  private final boolean matches;


  /**
   * Creates a boolean matcher with the given match result.
   *
   * @param matches  {@code true} to match all, {@code false} to match none
   */
  private BooleanMatcher(boolean matches) {
    this.matches = matches;
  }


  /** {@inheritDoc} */
  @Override
  public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
    return matches;
  }


  /** {@inheritDoc} */
  @Override
  public boolean match(@NotNull Iterable<String> tagNames) {
    return matches;
  }


  /** {@inheritDoc} */
  @Override
  public boolean isTagSelector() {
    return true;
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull TagSelector asTagSelector() {
    return this;
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull MessageMatcher asMessageMatcher() {
    return this;
  }


  @Override
  public boolean equals(Object o) {
    return this == o || o instanceof BooleanMatcher booleanMatcher && matches == booleanMatcher.matches;
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
