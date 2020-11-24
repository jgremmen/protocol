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
import de.sayayi.lib.message.MessageFactory;
import de.sayayi.lib.message.exception.MessageParserException;
import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor;
import de.sayayi.lib.protocol.exception.ProtocolException;

import org.jetbrains.annotations.NotNull;


/**
 * @author Jeroen Gremmen
 *
 * @see MessageBundleMessageProcessor
 */
public enum MessageFormatMessageProcessor implements MessageProcessor<Message>
{
  INSTANCE;


  @Override
  public @NotNull Message processMessage(@NotNull String messageFormat)
  {
    try {
      return MessageFactory.parse(messageFormat);
    } catch(MessageParserException ex) {
      throw new ProtocolException("failed to process message: " + ex.getMessage(), ex);
    }
  }
}
