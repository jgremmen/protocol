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

import org.jetbrains.annotations.NotNull;


/**
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.7.0
 */
public final class ToStringMessageFormatter<M> implements MessageFormatter<M>
{
  private static final MessageFormatter<Object> INSTANCE = new ToStringMessageFormatter<>();


  /**
   * This formatter returns the internal string message as is.
   */
  public static final MessageFormatter<String> IDENTITY = GenericMessage::getMessage;


  private ToStringMessageFormatter() {
  }


  @Override
  public @NotNull String formatMessage(@NotNull GenericMessage<M> message) {
    return message.getMessage().toString();
  }


  @SuppressWarnings("unchecked")
  public static @NotNull <T> MessageFormatter<T> getInstance() {
    return (MessageFormatter<T>)INSTANCE;
  }
}
