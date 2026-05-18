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
package de.sayayi.lib.protocol.message.processor;

import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor;
import de.sayayi.lib.protocol.message.GenericMessageWithId;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;


/**
 * A {@link MessageProcessor} implementation that uses the message string as-is, without any
 * lookup or transformation. The message string itself serves as both the message and its
 * identifier.
 *
 * @author Jeroen Gremmen
 * @since 0.7.0
 */
public enum StringMessageProcessor implements MessageProcessor<String>
{
  /** Singleton instance. */
  INSTANCE;


  /**
   * {@inheritDoc}
   * <p>
   * Returns the given message string directly, using it as both the message content and its id.
   *
   * @param message  message string, not {@code null}
   *
   * @return  the message paired with its id, never {@code null}
   */
  @Override
  public @NotNull MessageWithId<String> processMessage(@NotNull String message) {
    return new GenericMessageWithId<>(requireNonNull(message, "message must not be null"));
  }
}