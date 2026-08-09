/*
 * Copyright 2025 Jeroen Gremmen
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

/**
 * This module provides an ANTLR based parser for message matcher and tag selector expressions. It converts textual
 * expressions into {@link de.sayayi.lib.protocol.matcher.MessageMatcher MessageMatcher} and
 * {@link de.sayayi.lib.protocol.TagSelector TagSelector} instances that can be used to filter protocol messages by
 * level, tag, parameter, throwable, group membership and other criteria.
 * <p>
 * The main entry point is {@link de.sayayi.lib.protocol.matcher.parser.MessageMatcherParser MessageMatcherParser},
 * which parses expression strings supporting logical operators ({@code and}, {@code or}, {@code not}), level
 * comparisons, tag matching ({@code any-of}, {@code all-of}, {@code none-of}), parameter checks and group filters.
 * <p>
 * {@link de.sayayi.lib.protocol.matcher.parser.MessageMatcherParserException MessageMatcherParserException} is thrown
 * when an expression contains syntax errors, providing both an error message and a visual indicator of the error
 * location.
 *
 * @author Jeroen Gremmen
 * @since 1.6.0
 */
module de.sayayi.lib.protocol.message.matcher
{
  requires de.sayayi.lib.antlr;
  requires de.sayayi.lib.protocol;

  requires org.antlr.antlr4.runtime;

  requires static org.jetbrains.annotations;

  exports de.sayayi.lib.protocol.matcher.parser;
}
