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
package de.sayayi.lib.protocol.message.formatter;

import de.sayayi.lib.protocol.Protocol.GenericMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import static java.util.Objects.requireNonNull;


/**
 * @author Jeroen Gremmen
 * @since 0.7.0
 *
 * @see String#format(Locale, String, Object...)
 */
public final class JavaStringFormatFormatter extends AbstractIndexedMessageFormatter<String>
{
  public static final JavaStringFormatFormatter INSTANCE = new JavaStringFormatFormatter(Locale.getDefault());

  private final Locale locale;


  public JavaStringFormatFormatter(@NotNull Locale locale) {
    this.locale = requireNonNull(locale);
  }


  @Override
  protected @NotNull String formatMessage(@NotNull GenericMessage<String> message, @NotNull Object[] parameters) {
    return String.format(locale, message.getMessage(), parameters);
  }
}
