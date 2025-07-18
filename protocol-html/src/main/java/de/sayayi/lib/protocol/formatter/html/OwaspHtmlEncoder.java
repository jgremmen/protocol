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
package de.sayayi.lib.protocol.formatter.html;

import org.jetbrains.annotations.NotNull;

import static org.owasp.encoder.Encode.forHtml;


/**
 * @author Jeroen Gremmen
 * @since 1.6.0
 */
@SuppressWarnings("unused")
public final class OwaspHtmlEncoder extends HtmlEncoder
{
  @Override
  public @NotNull String encodeHtml(@NotNull String text) {
    return forHtml(text);
  }


  @Override
  public String toString() {
    return "Owasp";
  }
}
