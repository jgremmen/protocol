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
package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.message.formatter.JavaMessageFormatFormatter;
import de.sayayi.lib.protocol.message.formatter.JavaStringFormatFormatter;
import de.sayayi.lib.protocol.message.formatter.ToStringMessageFormatter;
import de.sayayi.lib.protocol.message.processor.StringMessageProcessor;
import de.sayayi.lib.protocol.spi.GenericProtocolFactory;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;


/**
 * <p>
 *   Generic protocol factory for text messages where the messages are stored internally as
 *   {@code String} objects.
 * </p>
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
public class StringProtocolFactory extends GenericProtocolFactory<String>
{
  public StringProtocolFactory(@NotNull MessageFormatter<String> messageFormatter) {
    super(StringMessageProcessor.INSTANCE, messageFormatter);
  }


  /**
   * <p>
   *   This string protocol factory formats messages as is without parameter substitution.
   * </p>
   *
   * @return  plain text protocol factory, never {@code null}
   *
   * @since 0.7.0
   */
  @Contract(value = "-> new", pure = true)
  public static @NotNull StringProtocolFactory createPlainTextFactory() {
    return new StringProtocolFactory(ToStringMessageFormatter.IDENTITY);
  }


  /**
   * <p>
   *   This string protocol factory formats messages using
   *   {@link MessageFormat#format(String, Object...)}.
   * </p>
   *
   * @return  java message format protocol factory, never {@code null}
   *
   * @since 0.7.0
   */
  @Contract(value = "-> new", pure = true)
  public static @NotNull StringProtocolFactory createJavaMessageFormatFactory() {
    return new StringProtocolFactory(JavaMessageFormatFormatter.INSTANCE);
  }


  /**
   * <p>
   *   This string protocol factory formats messages using
   *   {@link String#format(String, Object...)}.
   * </p>
   *
   * @return  java string format protocol factory, never {@code null}
   *
   * @since 0.7.0
   */
  @Contract(value = "-> new", pure = true)
  public static @NotNull StringProtocolFactory createJavaStringFormatFactory() {
    return new StringProtocolFactory(JavaStringFormatFormatter.INSTANCE);
  }
}