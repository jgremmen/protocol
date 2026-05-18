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
package de.sayayi.lib.protocol.message;

import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor.MessageWithId;

import org.jetbrains.annotations.NotNull;

import static java.util.UUID.randomUUID;


/**
 * Generic implementation of {@link MessageWithId} that pairs a message with its unique identifier.
 *
 * @param id       unique message identifier, not {@code null}
 * @param message  processed message object, not {@code null}
 *
 * @param <M>      internal message object type
 *
 * @author Jeroen Gremmen
 * @since 1.0.0  (refactored in 1.6.0, 1.7.0)
 */
public record GenericMessageWithId<M>(@NotNull String id, @NotNull M message) implements MessageWithId<M>
{
  /**
   * Creates a message with id using a randomly generated UUID as identifier.
   *
   * @param message  processed message object, not {@code null}
   */
  public GenericMessageWithId(@NotNull M message) {
    this(randomUUID().toString(), message);
  }


  @Override
  public @NotNull String toString() {
    return "MessageWithId(id=" + id + ",message=" + message + ')';
  }
}
