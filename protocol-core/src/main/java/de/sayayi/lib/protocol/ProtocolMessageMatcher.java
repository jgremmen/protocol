/*
 * Copyright 2022 Jeroen Gremmen
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


/**
 * @author Jeroen Gremmen
 * @since 1.2.1
 */
public interface ProtocolMessageMatcher
{
  /**
   * Create a message matcher based on a text expression.
   *
   * @param messageMatcherExpression  message matcher expression, not {@code null}
   *
   * @return  message matcher instance representing the expression, never {@code null}
   */
  @Contract(pure = true)
  @NotNull MessageMatcher parseMessageMatcher(@NotNull String messageMatcherExpression);


  /**
   * Create a tag selector based on a text expression.
   *
   * @param tagSelectorExpression  tag selector expression, not {@code null}
   *
   * @return  tag selector instance representing the expression, never {@code null}
   */
  @Contract(pure = true)
  @NotNull TagSelector parseTagSelector(@NotNull String tagSelectorExpression);
}