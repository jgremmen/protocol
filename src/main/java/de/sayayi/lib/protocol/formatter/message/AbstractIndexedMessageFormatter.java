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
package de.sayayi.lib.protocol.formatter.message;

import de.sayayi.lib.protocol.Protocol.GenericMessage;
import de.sayayi.lib.protocol.formatter.MessageFormatter;

import lombok.val;
import lombok.var;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


/**
 * @author Jeroen Gremmen
 */
public abstract class AbstractIndexedMessageFormatter<M> implements MessageFormatter<M>
{
  @Override
  @SuppressWarnings("java:S108")
  public final @NotNull String formatMessage(@NotNull GenericMessage<M> message)
  {
    val parameters = new ArrayList<Object>();

    for(val parametersEntry: message.getParameterValues().entrySet())
    {
      var i = -1;

      try {
        i = Integer.parseInt(parametersEntry.getKey());
      } catch(NumberFormatException ignored) {
      }

      if ((i >> 6) == 0)
        parameters.set(i, parametersEntry.getValue());
    }

    return formatMessage(message, parameters.toArray());
  }


  protected abstract @NotNull String formatMessage(@NotNull GenericMessage<M> message,
                                                   @NotNull Object[] parameters);
}
