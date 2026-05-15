/*
 * Copyright 2022 Jeroen Gremmen
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
package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


/**
 * Interface for parsing text expressions into {@link MessageMatcher} and {@link TagSelector}
 * instances. It is implemented by {@link ProtocolFactory}, making parsed matchers and
 * selectors available directly from the factory wherever the protocol API accepts them.
 * <p>
 * Full parsing support requires the {@code protocol-message-matcher} module to be present on
 * the classpath. If it is absent, both parse methods will throw a
 * {@link de.sayayi.lib.protocol.exception.MessageMatcherException}.
 *
 * @author Jeroen Gremmen
 * @since 1.2.1
 *
 * @see ProtocolFactory
 * @see MessageMatcher
 * @see TagSelector
 */
public interface ProtocolMessageMatcher
{
  /**
   * Creates a message matcher from a text expression.
   *
   * @param messageMatcherExpression  message matcher expression, not {@code null}
   *
   * @return  message matcher instance representing the expression, never {@code null}
   *
   * @throws de.sayayi.lib.protocol.exception.MessageMatcherException  if the expression is
   *         invalid or if no parsing support is available
   */
  @Contract(pure = true)
  @NotNull MessageMatcher parseMessageMatcher(@NotNull String messageMatcherExpression);


  /**
   * Creates a tag selector from a text expression.
   *
   * @param tagSelectorExpression  tag selector expression, not {@code null}
   *
   * @return  tag selector instance representing the expression, never {@code null}
   *
   * @throws de.sayayi.lib.protocol.exception.MessageMatcherException  if the expression is
   *         invalid or if no parsing support is available
   */
  @Contract(pure = true)
  @NotNull TagSelector parseTagSelector(@NotNull String tagSelectorExpression);
}