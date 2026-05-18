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
 * Provides {@link de.sayayi.lib.protocol.ProtocolFormatter ProtocolFormatter} implementations
 * for rendering protocols into various output formats.
 * <p>
 * Available formatters:
 * <ul>
 *   <li>{@link de.sayayi.lib.protocol.formatter.AbstractTreeProtocolFormatter AbstractTreeProtocolFormatter}
 *       abstract base for ASCII tree representations</li>
 *   <li>{@link de.sayayi.lib.protocol.formatter.TechnicalProtocolFormatter TechnicalProtocolFormatter}
 *       renders all messages as an ASCII tree with technical metadata (level, tags)</li>
 *   <li>{@link de.sayayi.lib.protocol.formatter.JsonProtocolFormatter JsonProtocolFormatter}
 *       renders the protocol as JSON</li>
 * </ul>
 *
 * @since 0.1.0
 */
package de.sayayi.lib.protocol.formatter;
