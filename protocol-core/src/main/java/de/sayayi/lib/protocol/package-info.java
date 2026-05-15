/*
 * Copyright 2019 Jeroen Gremmen
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
 * Core protocol API for collecting, querying, and formatting structured messages.
 * <p>
 * A {@link de.sayayi.lib.protocol.ProtocolFactory} creates {@link de.sayayi.lib.protocol.Protocol}
 * instances that collect messages with a {@link de.sayayi.lib.protocol.Level} and optional tags,
 * parameters, and throwables. Protocols can be structured with nested
 * {@link de.sayayi.lib.protocol.ProtocolGroup protocol groups}, queried using
 * {@link de.sayayi.lib.protocol.matcher.MessageMatcher message matchers} or
 * {@link de.sayayi.lib.protocol.TagSelector tag selectors}, and rendered through
 * {@link de.sayayi.lib.protocol.ProtocolFormatter formatters}.
 * <p>
 * The package also defines common abstractions for entries and iteration
 * ({@link de.sayayi.lib.protocol.ProtocolEntry},
 * {@link de.sayayi.lib.protocol.ProtocolIterator}) and utilities for matcher/tag-selector
 * parsing via {@link de.sayayi.lib.protocol.ProtocolMessageMatcher}.
 *
 * @since 0.1.0
 */
package de.sayayi.lib.protocol;
