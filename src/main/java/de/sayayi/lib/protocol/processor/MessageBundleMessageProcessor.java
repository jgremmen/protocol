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
package de.sayayi.lib.protocol.processor;

import de.sayayi.lib.message.Message;
import de.sayayi.lib.message.MessageBundle;
import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor;
import de.sayayi.lib.protocol.exception.ProtocolException;

import org.jetbrains.annotations.NotNull;


/**
 * @author Jeroen Gremmen
 * @since 0.7.0
 *
 * @see MessageFormatMessageProcessor
 */
public class MessageBundleMessageProcessor implements MessageProcessor<Message>
{
  private final MessageBundle messageBundle;
  private final boolean parserFallback;


  public MessageBundleMessageProcessor(@NotNull MessageBundle messageBundle) {
    this(messageBundle, false);
  }


  public MessageBundleMessageProcessor(@NotNull MessageBundle messageBundle, boolean parserFallback)
  {
    this.messageBundle = messageBundle;
    this.parserFallback = parserFallback;
  }


  @Override
  public @NotNull Message processMessage(@NotNull String codeOrMessageFormat)
  {
    Message message = messageBundle.getByCode(codeOrMessageFormat);

    if (message == null)
    {
      if (!parserFallback)
        throw new ProtocolException("missing message in bundle for code '" + codeOrMessageFormat + "'");

      message = MessageFormatMessageProcessor.INSTANCE.processMessage(codeOrMessageFormat);
    }

    return message;
  }
}
