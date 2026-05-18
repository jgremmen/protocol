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
 * Provides {@link de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor MessageProcessor}
 * implementations for resolving protocol messages from various sources.
 * <p>
 * The following processors are available:
 * <ul>
 *   <li>{@link de.sayayi.lib.protocol.message.processor.StringMessageProcessor StringMessageProcessor}
 *       uses the message string as-is</li>
 *   <li>{@link de.sayayi.lib.protocol.message.processor.MapMessageProcessor MapMessageProcessor}
 *       resolves messages from a {@link java.util.Map}</li>
 *   <li>{@link de.sayayi.lib.protocol.message.processor.PropertiesMessageProcessor PropertiesMessageProcessor}
 *       resolves messages from a {@link java.util.Properties} instance</li>
 *   <li>{@link de.sayayi.lib.protocol.message.processor.ResourceBundleMessageProcessor ResourceBundleMessageProcessor}
 *       resolves messages from a {@link java.util.ResourceBundle}</li>
 *   <li>{@link de.sayayi.lib.protocol.message.processor.MessageAccessorMessageProcessor MessageAccessorMessageProcessor}
 *       resolves messages using a message accessor with optional parser fallback</li>
 * </ul>
 *
 * @since 0.7.0
 */
package de.sayayi.lib.protocol.message.processor;
