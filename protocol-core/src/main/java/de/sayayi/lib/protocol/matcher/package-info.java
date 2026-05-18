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

/**
 * Provides the {@link de.sayayi.lib.protocol.matcher.MessageMatcher MessageMatcher} contract
 * and a rich set of factory methods in
 * {@link de.sayayi.lib.protocol.matcher.MessageMatchers MessageMatchers} for filtering protocol
 * messages by level, tags, parameters, throwables, message identity, and protocol structure.
 * <p>
 * Matchers can be composed using logical operators through the
 * {@link de.sayayi.lib.protocol.matcher.MessageMatcher.Junction Junction} interface, and can be
 * converted to or from {@link de.sayayi.lib.protocol.TagSelector TagSelector} instances.
 *
 * @since 1.0.0
 */
package de.sayayi.lib.protocol.matcher;
