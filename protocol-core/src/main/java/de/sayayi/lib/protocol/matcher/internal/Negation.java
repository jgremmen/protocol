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
import de.sayayi.lib.protocol.matcher.MessageMatcher;
import de.sayayi.lib.protocol.matcher.MessageMatcher.Junction;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static de.sayayi.lib.protocol.matcher.internal.BooleanMatcher.ANY;
import static de.sayayi.lib.protocol.matcher.internal.BooleanMatcher.NONE;


/**
 * A {@link Junction} that inverts the result of another matcher (logical NOT). Double negation is automatically
 * eliminated, and negating {@link BooleanMatcher#ANY ANY} or {@link BooleanMatcher#NONE NONE} returns the opposite
 * constant.
 *
 * @author Jeroen Gremmen
 * @since 1.0.0  (refactored in 1.6.0)
 */
public final class Negation implements Junction
{
  private final MessageMatcher matcher;


  /**
   * Creates a negation wrapping the given matcher.
   *
   * @param matcher  matcher to negate, not {@code null}
   */
  private Negation(@NotNull MessageMatcher matcher) {
    this.matcher = matcher;
  }


  /** {@inheritDoc} */
  @Override
  public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
    return !matcher.matches(levelLimit, message);
  }


  /** {@inheritDoc} */
  @Override
  public boolean isTagSelector() {
    return matcher.isTagSelector();
  }


  @Override
  public boolean equals(Object o) {
    return this == o || o instanceof Negation negation && matcher.equals(negation.matcher);
  }


  @Override
  public int hashCode() {
    return matcher.hashCode();
  }


  @Override
  public String toString() {
    return "not(" + matcher + ')';
  }


  /**
   * Creates a negation of the given matcher. Double negation is eliminated and boolean constants are inverted directly.
   *
   * @param matcher  matcher to negate, not {@code null}
   *
   * @return  negated matcher, never {@code null}
   */
  @Contract(pure = true)
  public static @NotNull Junction of(@NotNull MessageMatcher matcher)
  {
    if (matcher == ANY)
      return NONE;
    else if (matcher == NONE)
      return ANY;
    else if (matcher instanceof Negation negation)
      return negation.matcher.asJunction();
    else
      return new Negation(matcher);
  }
}
