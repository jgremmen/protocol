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
import de.sayayi.lib.protocol.matcher.MessageMatcher.Junction;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static de.sayayi.lib.protocol.Level.Shared.LOWEST;
import static de.sayayi.lib.protocol.Level.compare;
import static de.sayayi.lib.protocol.Level.min;
import static de.sayayi.lib.protocol.matcher.BooleanMatcher.ANY;
import static lombok.AccessLevel.PRIVATE;


/**
 * @author Jeroen Gremmen
 * @since 1.0.0
 */
@RequiredArgsConstructor(access = PRIVATE)
@EqualsAndHashCode(callSuper = false)
final class LevelMatcher implements Junction
{
  static final Junction DEBUG = new LevelMatcher(Level.Shared.DEBUG);
  static final Junction INFO = new LevelMatcher(Level.Shared.INFO);
  static final Junction WARN = new LevelMatcher(Level.Shared.WARN);
  static final Junction ERROR = new LevelMatcher(Level.Shared.ERROR);

  private final Level level;


  @Override
  public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
    return compare(min(message.getLevel(), levelLimit), level) >= 0;
  }


  @Override
  public String toString() {
    return "is(" + level + ')';
  }


  @Contract(pure = true)
  static Junction of(@NotNull Level level)
  {
    if (level == Level.Shared.DEBUG)
      return DEBUG;
    else if (level == Level.Shared.INFO)
      return INFO;
    else if (level == Level.Shared.WARN)
      return WARN;
    else if (level == Level.Shared.ERROR)
      return ERROR;
    else if (level.severity() == LOWEST.severity())
      return ANY;
    else
      return new LevelMatcher(level);
  }
}
