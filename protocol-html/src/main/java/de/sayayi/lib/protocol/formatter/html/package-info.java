/*
 * Copyright 2026 Jeroen Gremmen
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
 * HTML formatting support for protocols. The
 * {@link de.sayayi.lib.protocol.formatter.html.HtmlProtocolFormatter HtmlProtocolFormatter}
 * renders protocol entries as an HTML list structure with CSS classes for levels and depth.
 * A {@link de.sayayi.lib.protocol.formatter.html.HtmlProtocolFormatter.WithFontAwesome
 * Font Awesome variant} adds icon support.
 * <p>
 * HTML encoding is handled by {@link de.sayayi.lib.protocol.formatter.html.HtmlEncoder
 * HtmlEncoder}, which auto-detects a supported encoding library on the classpath.
 *
 * @since 0.2.0
 */
package de.sayayi.lib.protocol.formatter.html;
