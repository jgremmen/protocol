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

import de.sayayi.lib.message.Message;
import de.sayayi.lib.message.MessageSupport;
import de.sayayi.lib.protocol.Protocol.GenericMessage;
import de.sayayi.lib.protocol.ProtocolFactory.MessageFormatter;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;


/**
 * @author Jeroen Gremmen
 * @since 0.7.0
 *
 * @see Message#format(MessageSupport.MessageAccessor, Message.Parameters)
 */
public final class MessageFormatFormatter implements MessageFormatter<Message>
{
  private final @NotNull MessageSupport messageSupport;


  public MessageFormatFormatter(@NotNull MessageSupport messageSupport) {
    this.messageSupport = requireNonNull(messageSupport);
  }


  @Override
  public @NotNull String formatMessage(@NotNull GenericMessage<Message> message)
  {
    return messageSupport
        .message(message.getMessage())
        .with(message.getParameterValues())
        .format();
  }
}
