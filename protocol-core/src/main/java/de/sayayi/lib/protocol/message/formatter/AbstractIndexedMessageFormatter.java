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
import de.sayayi.lib.protocol.ProtocolFactory.MessageFormatter;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map.Entry;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.copyOf;


/**
 * <p>
 *   Abstract class for message formatters that require the parameters to be provided in an
 *   {@code Object[]} instead of a {@code Map}.
 * </p>
 *
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.7.0
 */
public abstract class AbstractIndexedMessageFormatter<M> implements MessageFormatter<M>
{
  @Override
  public final @NotNull String formatMessage(@NotNull GenericMessage<M> message)
  {
    Object[] parameters = new Object[4];

    for(final Entry<String,Object> parametersEntry: message.getParameterValues().entrySet())
    {
      int i = -1;

      try {
        i = parseInt(parametersEntry.getKey());
      } catch(NumberFormatException ignored) {
      }

      if (i >> 6 == 0)  // 0..31
      {
        if (i >= parameters.length)
        {
          i |= i >> 1;
          i |= i >> 2;
          i |= i >> 4;

          parameters = copyOf(parameters, i + 1);
        }

        parameters[i] = parametersEntry.getValue();
      }
    }

    return formatMessage(message, parameters);
  }


  /**
   * <p>
   *   This method replaces {@code formatMessage(GenericMessage)}.
   * </p>
   * <p>
   *   Message parameters are collected into an {@code Object[]} by using the parameter name as an
   *   index in the array. The resulting array has a size equal to the largest index number
   *   found + 1. In order to prevent large numbers leading to allocating huge amounts of memory,
   *   the maximum index taken into account is {@code 31}.
   *   <br>
   *   Missing indices will be initialized with {@code null} in the resulting array.
   * </p>
   *
   * @param message     message to format, never {@code null}
   * @param parameters  indexed message parameters, never {@code null}
   *
   * @return  formatted message
   */
  @Contract(pure = true)
  protected abstract @NotNull String formatMessage(@NotNull GenericMessage<M> message,
                                                   @NotNull Object[] parameters);
}
