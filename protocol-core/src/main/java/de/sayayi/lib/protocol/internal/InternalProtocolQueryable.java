/*
 * Copyright 2020 Jeroen Gremmen
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
package de.sayayi.lib.protocol.internal;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolQueryable;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


/**
 * Internal extension of {@link ProtocolQueryable} that adds level-limited variants of the matching and counting
 * operations. These methods allow the internal implementation to evaluate queries against a specific level ceiling
 * rather than the unrestricted maximum.
 *
 * @author Jeroen Gremmen
 * @since 0.4.1
 */
interface InternalProtocolQueryable extends ProtocolQueryable
{
  /**
   * Tells whether this protocol object has at least one entry matching the given matcher, constrained by the specified
   * level limit.
   *
   * @param levelLimit   maximum level to consider, not {@code null}
   * @param matcher      message matcher, not {@code null}
   * @param messageOnly  {@code true} to match only message entries, {@code false} to include group structure
   *
   * @return  {@code true} if at least one matching entry exists, {@code false} otherwise
   */
  @Contract(pure = true)
  boolean matches0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher, boolean messageOnly);


  /**
   * Returns the number of visible entries matching the given matcher, constrained by the specified level limit.
   *
   * @param levelLimit  maximum level to consider, not {@code null}
   * @param matcher     message matcher, not {@code null}
   *
   * @return  number of visible entries &gt;= 0
   */
  @Contract(pure = true)
  int getVisibleEntryCount0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher);
}
