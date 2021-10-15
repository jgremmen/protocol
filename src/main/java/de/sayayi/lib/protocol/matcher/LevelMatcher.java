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
import de.sayayi.lib.protocol.Protocol;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static de.sayayi.lib.protocol.Level.Shared.LOWEST;
import static de.sayayi.lib.protocol.Level.compare;
import static de.sayayi.lib.protocol.Level.min;
import static lombok.AccessLevel.PRIVATE;


/**
 * @author Jeroen Gremmen
 * @since 1.0.0
 */
@RequiredArgsConstructor(access = PRIVATE)
@EqualsAndHashCode(callSuper = false)
final class LevelMatcher extends AbstractJunction
{
  private final Level level;


  @Override
  public <M> boolean matches(@NotNull Level levelLimit, Protocol.@NotNull Message<M> message) {
    return compare(min(message.getLevel(), levelLimit), level) >= 0;
  }


  @Override
  public String toString() {
    return "isLevel(" + level + ')';
  }


  @Contract(pure = true)
  static MessageMatcher.Junction of(@NotNull Level level) {
    return level.severity() == LOWEST.severity() ? BooleanMatcher.TRUE : new LevelMatcher(level);
  }
}
