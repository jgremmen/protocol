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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


/**
 * This interface provides methods for querying protocol objects in a quantitative matter.
 *
 * @author Jeroen Gremmen
 */
public interface ProtocolQuery
{
  /**
   * Tells if this protocol object matches the given {@code level} and at least one of {@code tags}.
   *
   * @param level  requested protocol level, not {@code null}
   * @param tags   tags to query, not {@code null}
   *
   * @return  {@code true} if the protocol object matches, {@code false} otherwise
   */
  @Contract(pure = true)
  boolean matches(@NotNull Level level, @NotNull Tag ... tags);


  /**
   * Tells if this protocol object matches the given {@code level}.
   *
   * @param level  requested protocol level, not {@code null}
   *
   * @return  {@code true} if the protocol object matches, {@code false} otherwise
   */
  @Contract(pure = true)
  boolean matches(@NotNull Level level);


  /**
   * Returns the number of visible entries for the given {@code level} and {@code tags}.
   *
   * @param level  requested protocol level, not {@code null}
   * @param tags   tags to query, not {@code null}
   * @param recursive  {@code false} returns the number of visible entries for the current depth only,
   *                   {@code true} returns the number of visible entries for all depths starting at the current one
   *
   * @return  number of visible entries
   */
  @Contract(pure = true)
  int getVisibleEntryCount(boolean recursive, @NotNull Level level, @NotNull Tag ... tags);
}
