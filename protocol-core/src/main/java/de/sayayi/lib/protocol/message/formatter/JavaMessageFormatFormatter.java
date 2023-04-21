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

import java.text.MessageFormat;
import java.util.Locale;


/**
 * @author Jeroen Gremmen
 * @since 0.7.0
 *
 * @see MessageFormat#format(String, Object...)
 */
public final class JavaMessageFormatFormatter extends AbstractIndexedMessageFormatter<String>
{
  public static final JavaMessageFormatFormatter INSTANCE =
      new JavaMessageFormatFormatter(Locale.getDefault());

  private final Locale locale;


  public JavaMessageFormatFormatter(@NotNull Locale locale) {
    this.locale = locale;
  }


  @Override
  protected @NotNull String formatMessage(@NotNull GenericMessage<String> message,
                                          @NotNull Object[] parameters) {
    return new MessageFormat(message.getMessage(), locale).format(parameters);
  }
}
