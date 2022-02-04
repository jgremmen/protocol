/*
 * Copyright 2019 Jeroen Gremmen
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
package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import static java.lang.Integer.MAX_VALUE;


/**
 * This interface provides methods for querying protocol objects.
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
public interface ProtocolQueryable
{
  /**
   * Tells if this protocol object matches the given {@code matcher}.
   *
   * @param matcher  message matcher, not {@code null}
   *
   * @return  {@code true} if the protocol object matches, {@code false} otherwise
   *
   * @since 1.0.0
   */
  @Contract(pure = true)
  boolean matches(@NotNull MessageMatcher matcher);


  /**
   * Returns the number of visible entries for the given message {@code matcher}.
   *
   * @param matcher    message matcher, not {@code null}
   *
   * @return  number of visible entries
   *
   * @since 1.0.0
   */
  @Contract(pure = true)
  @Range(from = 0, to = MAX_VALUE)
  int getVisibleEntryCount(@NotNull MessageMatcher matcher);
}