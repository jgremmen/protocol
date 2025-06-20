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
import de.sayayi.lib.message.MessageSupport.MessageAccessor;
import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor;
import de.sayayi.lib.protocol.exception.ProtocolException;
import de.sayayi.lib.protocol.message.GenericMessageWithId;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;


/**
 * @author Jeroen Gremmen
 * @since 0.7.0 (renamed in 1.3.3)
 *
 * @see MessageFormatMessageProcessor
 */
public class MessageSupportMessageProcessor implements MessageProcessor<Message>
{
  private final @NotNull MessageAccessor messageAccessor;
  private final boolean parserFallback;


  public MessageSupportMessageProcessor(@NotNull MessageAccessor messageAccessor, boolean parserFallback)
  {
    this.messageAccessor = requireNonNull(messageAccessor);
    this.parserFallback = parserFallback;
  }


  @SuppressWarnings("unused")
  public MessageSupportMessageProcessor(@NotNull MessageAccessor messageAccessor) {
    this(messageAccessor, false);
  }


  /**
   * <p>
   *   Check whether the given {@code codeOrMessageFormat} is not a valid message code.
   * </p>
   * <p>
   *   The default implementation returns {@code false} which is sufficient in most cases. If
   *   message codes are easily identifiable (e.g. by regex) this method can be overridden to
   *   prevent non-existing message codes from being parsed (only if {@link #parserFallback} is
   *   set to {@code true}).
   * </p>
   *
   * @param codeOrMessageFormat  code or message to check, never {@code null}
   *
   * @return  {@code true} if the given code or message format is not a message code,
   *          {@code false} otherwise
   *
   * @since 1.0.0
   */
  protected boolean isInvalidMessageCode(@SuppressWarnings("unused") @NotNull String codeOrMessageFormat) {
    return false;
  }


  @Override
  public @NotNull MessageWithId<Message> processMessage(@NotNull String codeOrMessageFormat)
  {
    requireNonNull(codeOrMessageFormat, "codeOrMessageFormat must not be null");

    var message = isInvalidMessageCode(codeOrMessageFormat)
        ? null : messageAccessor.getMessageByCode(codeOrMessageFormat);
    if (message != null)
      return new GenericMessageWithId<>(message.getCode(), message);

    if (!parserFallback)
      throw new ProtocolException("missing message in bundle for code '" + codeOrMessageFormat + '\'');

    return MessageFormatMessageProcessor.INSTANCE.processMessage(codeOrMessageFormat);
  }


  @Override
  public @NotNull String getIdFromMessage(@NotNull Message message) {
    return MessageFormatMessageProcessor.INSTANCE.getIdFromMessage(message);
  }
}
