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

/**
 * Provides {@link de.sayayi.lib.protocol.ProtocolFactory.MessageFormatter MessageFormatter}
 * implementations for rendering internal message objects into strings.
 * <p>
 * The following formatters are available:
 * <ul>
 *   <li>{@link de.sayayi.lib.protocol.message.formatter.ToStringMessageFormatter ToStringMessageFormatter}
 *       formats messages using {@code toString()} or returns string messages as-is</li>
 *   <li>{@link de.sayayi.lib.protocol.message.formatter.JavaMessageFormatFormatter JavaMessageFormatFormatter}
 *       formats messages using {@link java.text.MessageFormat}</li>
 *   <li>{@link de.sayayi.lib.protocol.message.formatter.JavaStringFormatFormatter JavaStringFormatFormatter}
 *       formats messages using {@link java.lang.String#format(java.util.Locale, String, Object...)}</li>
 *   <li>{@link de.sayayi.lib.protocol.message.formatter.MessageFormatFormatter MessageFormatFormatter}
 *       formats messages using the message-format library</li>
 *   <li>{@link de.sayayi.lib.protocol.message.formatter.AbstractIndexedMessageFormatter AbstractIndexedMessageFormatter}
 *       abstract base for formatters that use indexed parameters</li>
 * </ul>
 *
 * @since 0.7.0
 */
package de.sayayi.lib.protocol.message.formatter;
