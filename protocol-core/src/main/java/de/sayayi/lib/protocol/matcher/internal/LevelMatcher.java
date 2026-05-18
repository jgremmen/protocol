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
import de.sayayi.lib.protocol.Level.Shared;
import de.sayayi.lib.protocol.ProtocolEntry.Message;
import de.sayayi.lib.protocol.matcher.MessageMatcher.Junction;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static de.sayayi.lib.protocol.Level.Shared.LOWEST;
import static de.sayayi.lib.protocol.Level.compare;
import static de.sayayi.lib.protocol.Level.min;
import static de.sayayi.lib.protocol.matcher.internal.BooleanMatcher.ANY;


/**
 * A {@link Junction} that matches messages whose effective level (the minimum of the message level and the level
 * limit) is at least as severe as a configured threshold.
 * <p>
 * Pre-built instances for the standard levels are available as {@link #DEBUG}, {@link #INFO}, {@link #WARN}, and
 * {@link #ERROR}. Custom instances can be obtained via {@link #of(Level)}.
 *
 * @author Jeroen Gremmen
 * @since 1.0.0  (refactored in 1.6.0)
 */
public final class LevelMatcher implements Junction
{
  /** Matcher for messages with level &gt;= {@code DEBUG}. */
  public static final Junction DEBUG = new LevelMatcher(Shared.DEBUG);

  /** Matcher for messages with level &gt;= {@code INFO}. */
  public static final Junction INFO = new LevelMatcher(Shared.INFO);

  /** Matcher for messages with level &gt;= {@code WARN}. */
  public static final Junction WARN = new LevelMatcher(Shared.WARN);

  /** Matcher for messages with level &gt;= {@code ERROR}. */
  public static final Junction ERROR = new LevelMatcher(Shared.ERROR);

  private final Level level;


  /**
   * Creates a level matcher with the given minimum level threshold.
   *
   * @param level  minimum level to match, not {@code null}
   */
  private LevelMatcher(@NotNull Level level) {
    this.level = level;
  }


  /** {@inheritDoc} */
  @Override
  public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
    return compare(min(message.getLevel(), levelLimit), level) >= 0;
  }


  /** {@inheritDoc} */
  @Override
  public boolean isTagSelector() {
    return false;
  }


  @Override
  public boolean equals(Object o) {
    return this == o || o instanceof LevelMatcher levelMatcher && level.equals(levelMatcher.level);
  }


  @Override
  public int hashCode() {
    return level.hashCode();
  }


  @Override
  public String toString() {
    return "level(" + level + ')';
  }


  /**
   * Returns a level matcher for the given minimum level. For standard shared levels, cached singleton instances are
   * returned.
   *
   * @param level  minimum level to match, not {@code null}
   *
   * @return  level matcher, never {@code null}
   */
  @Contract(pure = true)
  public static Junction of(@NotNull Level level)
  {
    if (level == Shared.DEBUG)
      return DEBUG;
    else if (level == Shared.INFO)
      return INFO;
    else if (level == Shared.WARN)
      return WARN;
    else if (level == Shared.ERROR)
      return ERROR;
    else if (level.severity() == LOWEST.severity())
      return ANY;
    else
      return new LevelMatcher(level);
  }
}
