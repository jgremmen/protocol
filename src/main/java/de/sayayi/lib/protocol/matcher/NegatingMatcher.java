/*
 * Copyright 2021 Jeroen Gremmen
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
package de.sayayi.lib.protocol.matcher;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol.Message;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static de.sayayi.lib.protocol.matcher.BooleanMatcher.ANY;
import static de.sayayi.lib.protocol.matcher.BooleanMatcher.NONE;
import static lombok.AccessLevel.PRIVATE;


/**
 * @author Jeroen Gremmen
 * @since 1.0.0
 */
@RequiredArgsConstructor(access = PRIVATE)
@EqualsAndHashCode(callSuper = false)
final class NegatingMatcher extends AbstractJunction
{
  final MessageMatcher matcher;


  @Override
  public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
    return !matcher.matches(levelLimit, message);
  }


  @Override
  public String toString() {
    return "not(" + matcher + ')';
  }


  @Contract(pure = true)
  static Junction of(@NotNull MessageMatcher matcher)
  {
    if (matcher == ANY)
      return NONE;
    else if (matcher == NONE)
      return ANY;
    else if (matcher instanceof NegatingMatcher)
      return ((NegatingMatcher)matcher).matcher.asJunction();
    else
      return new NegatingMatcher(matcher);
  }
}
