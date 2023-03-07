/*
 * Copyright 2021 Jeroen Gremmen
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

import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor.MessageWithId;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.jetbrains.annotations.NotNull;

import static java.util.UUID.randomUUID;


/**
 * Generic message with id implementation.
 *
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public final class GenericMessageWithId<M> implements MessageWithId<M>
{
  private final @NotNull String id;
  private final @NotNull M message;


  public GenericMessageWithId(@NotNull M message) {
    this(randomUUID().toString(), message);
  }


  @Override
  public String toString() {
    return "MessageWithId[id=" + id + ",message=" + message + ']';
  }
}