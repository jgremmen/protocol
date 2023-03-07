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

import de.sayayi.lib.message.Message;
import de.sayayi.lib.message.MessageFactory;
import de.sayayi.lib.message.exception.MessageParserException;
import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor;
import de.sayayi.lib.protocol.exception.ProtocolException;
import de.sayayi.lib.protocol.spi.GenericMessageWithId;

import lombok.AllArgsConstructor;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static java.util.Objects.requireNonNull;


/**
 * @author Jeroen Gremmen
 * @since 0.7.0
 *
 * @see MessageBundleMessageProcessor
 */
@AllArgsConstructor
public class MessageFormatMessageProcessor implements MessageProcessor<Message>
{
  public static final MessageFormatMessageProcessor INSTANCE =
      new MessageFormatMessageProcessor(MessageFactory.NO_CACHE_INSTANCE);


  private final @NotNull MessageFactory messageFactory;


  @Override
  public @NotNull MessageWithId<Message> processMessage(@NotNull String messageFormat)
  {
    try {
      return new GenericMessageWithId<>(messageFactory.parse(
          requireNonNull(messageFormat, "messageFormat must not be null")));
    } catch(MessageParserException ex) {
      throw new ProtocolException("failed to process message: " + ex.getMessage(), ex);
    }
  }


  @Override
  public @NotNull String getIdFromMessage(@NotNull Message message)
  {
    return message instanceof Message.WithCode
        ? ((Message.WithCode)message).getCode()
        : UUID.randomUUID().toString();
  }
}