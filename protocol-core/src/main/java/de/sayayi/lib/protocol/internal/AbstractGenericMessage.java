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
package de.sayayi.lib.protocol.internal;

import de.sayayi.lib.protocol.Protocol.GenericMessage;
import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor.MessageWithId;
import de.sayayi.lib.protocol.util.ParameterMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;

import static java.lang.System.currentTimeMillis;


/**
 * Base class for protocol messages that implements common {@link GenericMessage} functionality such as message
 * identity, parameter storage, and creation time tracking.
 * <p>
 * Each instance records the creation time at construction and maintains its own {@link ParameterMap} that inherits
 * entries from a parent parameter map.
 *
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
abstract class AbstractGenericMessage<M> implements GenericMessage<M>
{
  final long timeMillis;

  protected final @NotNull MessageWithId<M> messageWithId;
  protected final @NotNull ParameterMap parameterMap;


  /**
   * Creates a new message with the given message identity and a parameter map that inherits from the given parent.
   * The creation time is recorded at construction.
   *
   * @param messageWithId       message with its identifier, not {@code null}
   * @param parentParameterMap  parent parameter map to inherit from, not {@code null}
   */
  protected AbstractGenericMessage(@NotNull MessageWithId<M> messageWithId,
                                   @NotNull ParameterMap parentParameterMap)
  {
    this.messageWithId = messageWithId;

    timeMillis = currentTimeMillis();
    parameterMap = new ParameterMap(parentParameterMap);
  }


  /** {@inheritDoc} */
  @Override
  public long getTimeMillis() {
    return timeMillis;
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull String getMessageId() {
    return messageWithId.id();
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull M getMessage() {
    return messageWithId.message();
  }


  /** {@inheritDoc} */
  @Override
  @UnmodifiableView
  public @NotNull Map<String,Object> getParameterValues() {
    return parameterMap.unmodifyableMap();
  }
}
