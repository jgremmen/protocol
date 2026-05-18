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
import de.sayayi.lib.message.exception.MessageParserException;
import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor;
import de.sayayi.lib.protocol.exception.ProtocolException;
import de.sayayi.lib.protocol.message.GenericMessageWithId;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;


/**
 * A {@link MessageProcessor} implementation that resolves protocol messages using a
 * {@link MessageAccessor}. Messages are looked up by code; if no message is found and parser
 * fallback is enabled, the input string is parsed as an inline message format instead.
 *
 * @author Jeroen Gremmen
 * @since 0.7.0 (renamed in 1.7.0)
 *
 * @see MessageAccessor
 */
public class MessageAccessorMessageProcessor implements MessageProcessor<Message>
{
  private final @NotNull MessageAccessor messageAccessor;
  private final boolean parserFallback;


  /**
   * Creates a new message processor with the given message accessor and parser fallback setting.
   *
   * @param messageAccessor  message accessor used for looking up messages by code,
   *                         not {@code null}
   * @param parserFallback   if {@code true}, the input string is parsed as a message format when
   *                         no message code match is found; if {@code false}, a
   *                         {@link ProtocolException} is thrown instead
   */
  public MessageAccessorMessageProcessor(@NotNull MessageAccessor messageAccessor, boolean parserFallback)
  {
    this.messageAccessor = requireNonNull(messageAccessor);
    this.parserFallback = parserFallback;
  }


  /**
   * Creates a new message processor with the given message accessor and parser fallback disabled.
   *
   * @param messageAccessor  message accessor used for looking up messages by code,
   *                         not {@code null}
   */
  @SuppressWarnings("unused")
  public MessageAccessorMessageProcessor(@NotNull MessageAccessor messageAccessor) {
    this(messageAccessor, false);
  }


  /**
   * Check whether the given {@code codeOrMessageFormat} is not a valid message code.
   * <p>
   * The default implementation returns {@code false} which is sufficient in most cases. If
   * message codes are easily identifiable (e.g. by regex) this method can be overridden to
   * prevent non-existing message codes from being parsed (only if {@link #parserFallback} is
   * set to {@code true}).
   *
   * @param codeOrMessageFormat  code or message to check, not {@code null}
   *
   * @return  {@code true} if the given code or message format is not a message code,
   *          {@code false} otherwise
   *
   * @since 1.0.0
   */
  protected boolean isInvalidMessageCode(@SuppressWarnings("unused") @NotNull String codeOrMessageFormat) {
    return false;
  }


  /**
   * {@inheritDoc}
   * <p>
   * Resolves the given {@code codeOrMessageFormat} by first attempting a message code lookup
   * via the message accessor. If no match is found and parser fallback is enabled, the input
   * is parsed as an inline message format.
   *
   * @param codeOrMessageFormat  message code or inline message format string, not {@code null}
   *
   * @return  resolved message paired with its identifier, never {@code null}
   *
   * @throws ProtocolException  if no message could be resolved and parser fallback is disabled,
   *                            or if parsing the message format fails
   */
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

    try {
      return new GenericMessageWithId<>(messageAccessor.getMessageFactory().parseMessage(codeOrMessageFormat));
    } catch(MessageParserException ex) {
      throw new ProtocolException("failed to process message: " + ex.getMessage(), ex);
    }
  }


  /**
   * {@inheritDoc}
   * <p>
   * Returns the message code if the message implements {@link Message.WithCode}; otherwise
   * a random UUID is generated as identifier.
   *
   * @param message  processed message, not {@code null}
   *
   * @return  message identifier, never {@code null}
   */
  @Override
  public @NotNull String getIdFromMessage(@NotNull Message message)
  {
    return message instanceof Message.WithCode messageWithCode
        ? messageWithCode.getCode()
        : randomUUID().toString();
  }
}
