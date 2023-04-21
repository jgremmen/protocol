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
package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Protocol.GenericMessage;
import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor.MessageWithId;

import org.jetbrains.annotations.NotNull;

import java.util.Map;


/**
 * {@inheritDoc}
 *
 * @author Jeroen Gremmen
 */
abstract class AbstractGenericMessage<M> implements GenericMessage<M>
{
  final long timeMillis;

  protected final @NotNull MessageWithId<M> messageWithId;
  protected final @NotNull ParameterMap parameterMap;


  protected AbstractGenericMessage(@NotNull MessageWithId<M> messageWithId,
                                   @NotNull ParameterMap parentParameterMap)
  {
    this.messageWithId = messageWithId;

    timeMillis = System.currentTimeMillis();
    parameterMap = new ParameterMap(parentParameterMap);
  }


  @Override
  public long getTimeMillis() {
    return timeMillis;
  }


  @Override
  public @NotNull String getMessageId() {
    return messageWithId.getId();
  }


  @Override
  public @NotNull M getMessage() {
    return messageWithId.getMessage();
  }


  @Override
  public @NotNull Map<String,Object> getParameterValues() {
    return parameterMap.unmodifyableMap();
  }
}
