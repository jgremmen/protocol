/*
 * Copyright 2019 Jeroen Gremmen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.formatter.message.JavaMessageFormatFormatter;
import de.sayayi.lib.protocol.formatter.message.JavaStringFormatFormatter;
import de.sayayi.lib.protocol.formatter.message.ToStringMessageFormatter;
import de.sayayi.lib.protocol.processor.StringMessageProcessor;
import de.sayayi.lib.protocol.spi.GenericProtocolFactory;

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
   * @since 0.7.0
   */
  public static StringProtocolFactory createPlainTextFactory() {
    return new StringProtocolFactory(ToStringMessageFormatter.IDENTITY);
  }


  /**
   * <p>
   *   This string protocol factory formats messages using {@link MessageFormat#format(String, Object...)}.
   * </p>
   *
   * @since 0.7.0
   */
  public static StringProtocolFactory createJavaMessageFormatFactory() {
    return new StringProtocolFactory(JavaMessageFormatFormatter.INSTANCE);
  }


  /**
   * <p>
   *   This string protocol factory formats messages using {@link String#format(String, Object...)}.
   * </p>
   *
   * @since 0.7.0
   */
  public static StringProtocolFactory createJavaStringFormatFactory() {
    return new StringProtocolFactory(JavaStringFormatFormatter.INSTANCE);
  }
}
