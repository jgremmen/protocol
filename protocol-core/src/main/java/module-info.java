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
 * Core module of the Protocol library for collecting, querying, and formatting structured messages.
 * <p>
 * The protocol API allows you to record messages with a severity {@link de.sayayi.lib.protocol.Level level}, optional
 * tags, parameters, and throwables. Messages can be organized into nested
 * {@link de.sayayi.lib.protocol.ProtocolGroup protocol groups} and later queried or rendered into various output
 * formats.
 *
 * <h2>Main concepts</h2>
 * <ul>
 *   <li>{@link de.sayayi.lib.protocol.ProtocolFactory ProtocolFactory} creates
 *       {@link de.sayayi.lib.protocol.Protocol Protocol} instances and defines how message strings are processed and
 *       formatted.</li>
 *   <li>{@link de.sayayi.lib.protocol.Protocol Protocol} is the central interface for adding messages using a fluent
 *       builder API.</li>
 *   <li>{@link de.sayayi.lib.protocol.ProtocolGroup ProtocolGroup} extends Protocol with group headers and visibility
 *       control.</li>
 *   <li>{@link de.sayayi.lib.protocol.ProtocolFormatter ProtocolFormatter} renders the collected messages into a result
 *       such as plain text, JSON, or a custom data structure.</li>
 *   <li>{@link de.sayayi.lib.protocol.matcher.MessageMatcher MessageMatcher} and
 *       {@link de.sayayi.lib.protocol.TagSelector TagSelector} provide flexible filtering of protocol messages by
 *       level, tags, or other criteria.</li>
 * </ul>
 *
 * <h2>Packages</h2>
 * <ul>
 *   <li>{@code de.sayayi.lib.protocol} contains the core API interfaces and enumerations.</li>
 *   <li>{@code de.sayayi.lib.protocol.exception} defines exception types thrown by the protocol API.</li>
 *   <li>{@code de.sayayi.lib.protocol.factory} provides ready to use
 *       {@link de.sayayi.lib.protocol.ProtocolFactory ProtocolFactory} implementations.</li>
 *   <li>{@code de.sayayi.lib.protocol.formatter} provides
 *       {@link de.sayayi.lib.protocol.ProtocolFormatter ProtocolFormatter} implementations for rendering protocols
 *       (e.g. ASCII tree, JSON).</li>
 *   <li>{@code de.sayayi.lib.protocol.matcher} provides the message matcher contract and factory methods for composing
 *       match expressions.</li>
 *   <li>{@code de.sayayi.lib.protocol.message} contains the generic message type used internally by protocol
 *       entries.</li>
 *   <li>{@code de.sayayi.lib.protocol.message.formatter} provides
 *       {@link de.sayayi.lib.protocol.ProtocolFactory.MessageFormatter MessageFormatter} implementations for rendering
 *       internal message objects into strings.</li>
 *   <li>{@code de.sayayi.lib.protocol.message.processor} provides
 *       {@link de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor MessageProcessor} implementations for resolving
 *       messages from various sources such as resource bundles, property files, or maps.</li>
 *   <li>{@code de.sayayi.lib.protocol.util} contains utility classes supporting the protocol API.</li>
 * </ul>
 *
 * @author Jeroen Gremmen
 * @since 1.6.0
 */
module de.sayayi.lib.protocol
{
  requires static org.jetbrains.annotations;
  requires static de.sayayi.lib.message;

  exports de.sayayi.lib.protocol;
  exports de.sayayi.lib.protocol.exception;
  exports de.sayayi.lib.protocol.factory;
  exports de.sayayi.lib.protocol.formatter;
  exports de.sayayi.lib.protocol.matcher;
  exports de.sayayi.lib.protocol.matcher.internal to de.sayayi.lib.protocol.message.matcher;
  exports de.sayayi.lib.protocol.message;
  exports de.sayayi.lib.protocol.message.formatter;
  exports de.sayayi.lib.protocol.message.processor;
  exports de.sayayi.lib.protocol.util;

  uses de.sayayi.lib.protocol.ProtocolMessageMatcher;
}