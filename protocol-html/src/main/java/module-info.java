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
 * HTML formatting module for the Protocol library.
 * <p>
 * This module provides {@link de.sayayi.lib.protocol.formatter.html.HtmlProtocolFormatter HtmlProtocolFormatter},
 * which renders protocol entries as nested HTML list structures with CSS classes for severity levels and group depth.
 * A {@link de.sayayi.lib.protocol.formatter.html.HtmlProtocolFormatter.WithFontAwesome Font Awesome variant} is
 * included for rendering level icons.
 * <p>
 * HTML encoding is handled by {@link de.sayayi.lib.protocol.formatter.html.HtmlEncoder HtmlEncoder}, which
 * automatically detects a supported encoding library on the classpath. Supported libraries are Spring Web, Google
 * Guava, Apache Commons Text, Unbescape and OWASP Java Encoder. Custom implementations can be registered via the
 * {@link java.util.ServiceLoader} mechanism.
 *
 * @author Jeroen Gremmen
 * @since 1.6.0
 */
module de.sayayi.lib.protocol.html
{
  requires de.sayayi.lib.protocol;

  requires static com.google.common;
  requires static org.apache.commons.text;
  requires static org.jetbrains.annotations;
  requires static owasp.encoder;
  requires static spring.web;
  requires static unbescape;

  exports de.sayayi.lib.protocol.formatter.html;

  uses de.sayayi.lib.protocol.formatter.html.HtmlEncoder;
}
