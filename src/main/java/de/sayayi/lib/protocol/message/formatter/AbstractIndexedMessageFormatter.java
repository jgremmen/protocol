/*
 * Copyright 2020 Jeroen Gremmen
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
package de.sayayi.lib.protocol.message.formatter;

import de.sayayi.lib.protocol.Protocol.GenericMessage;
import de.sayayi.lib.protocol.ProtocolFactory.MessageFormatter;

import lombok.val;
import lombok.var;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static java.util.Arrays.copyOf;


/**
 * <p>
 *   Abstract class for message formatters that require the parameters to be provided in an {@code Object[]}
 *   instead of a {@code Map}.
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
  @SuppressWarnings("java:S108")
  public final @NotNull String formatMessage(@NotNull GenericMessage<M> message)
  {
    var parameters = new Object[0];

    for(val parametersEntry: message.getParameterValues().entrySet())
    {
      var i = -1;

      try {
        i = Integer.parseInt(parametersEntry.getKey());
      } catch(NumberFormatException ignored) {
      }

      if (i >> 6 == 0)  // 0..31
      {
        if (i >= parameters.length)
          parameters = copyOf(parameters, i + 1);

        parameters[i] = parametersEntry.getValue();
      }
    }

    return formatMessage(message, parameters);
  }


  /**
   * <p>
   *   This method replaces {@link MessageFormatter#formatMessage(GenericMessage)}.
   * </p>
   * <p>
   *   Message parameters are collected into an {@code Object[]} by using the parameter name as an index in
   *   the array. The resulting array has a size equal to the largest index number found + 1. In order to
   *   prevent large numbers leading to allocating huge amounts of memory, the maximum index taken into
   *   account is {@code 63}.
   *   <br>
   *   Missing indices will be initialized as {@code null} in the resulting array.
   * </p>
   *
   * @param message     message to format, never {@code null}
   * @param parameters  indexed message parameters, never {@code null}
   *
   * @return  formatted message
   */
  @Contract(pure = true)
  protected abstract @NotNull String formatMessage(@NotNull GenericMessage<M> message, @NotNull Object[] parameters);
}
