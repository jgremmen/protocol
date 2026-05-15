/*
 * Copyright 2019 Jeroen Gremmen
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
package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.Protocol.GenericMessage;
import de.sayayi.lib.protocol.message.processor.ResourceBundleMessageProcessor;
import de.sayayi.lib.protocol.message.processor.StringMessageProcessor;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


/**
 * Factory for creating {@link Protocol} instances. A factory encapsulates the strategy for
 * converting message strings into an internal representation via its {@link MessageProcessor},
 * and for formatting that representation back into a displayable string via its
 * {@link MessageFormatter}. Both components are shared across all protocols created by the
 * same factory.
 * <p>
 * As an implementation of {@link ProtocolMessageMatcher}, a factory can also parse message
 * matcher and tag selector expressions used throughout the protocol API.
 *
 * @param <M>  internal message object type. Messages are added by providing a string; the
 *             factory converts this string into the appropriate internal format via its
 *             {@link MessageProcessor}, allowing various message retrieval/formatting
 *             libraries to be plugged in.
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 *
 * @see Protocol
 * @see MessageProcessor
 * @see MessageFormatter
 */
public interface ProtocolFactory<M> extends ProtocolMessageMatcher
{
  /**
   * Name of the default tag. Every protocol factory recognizes this tag and it can be used as
   * a baseline when defining tag selectors or propagation rules.
   */
  String DEFAULT_TAG_NAME = "default";


  /**
   * Returns the message processor associated with this factory.
   *
   * @return  message processor, never {@code null}
   *
   * @since 0.7.0
   */
  @Contract(pure = true)
  @NotNull MessageProcessor<M> getMessageProcessor();


  /**
   * Returns the message formatter associated with this factory.
   *
   * @return  message formatter, never {@code null}
   *
   * @since 0.7.0
   */
  @Contract(pure = true)
  @NotNull MessageFormatter<M> getMessageFormatter();


  /**
   * Create a new protocol instance.
   *
   * @return  new protocol instance, never {@code null}.
   */
  @Contract("-> new")
  @NotNull Protocol<M> createProtocol();




  /**
   * A message processor takes care of translating the protocol string message into an internal
   * representation.
   *
   * @param <M>  internal message object type
   *
   * @see StringMessageProcessor
   * @see ResourceBundleMessageProcessor
   *
   * @since 0.7.0
   */
  interface MessageProcessor<M>
  {
    /**
     * Processes the given message string and converts it into the internal message
     * representation, paired with a unique identifier.
     *
     * @param message  message string to process, not {@code null}
     *
     * @return  message with id, never {@code null}
     */
    @Contract(pure = true)
    @NotNull MessageWithId<M> processMessage(@NotNull String message);


    /**
     * Returns the id from an already processed message. The default implementation generates a
     * unique UUID.
     *
     * @param message  processed message, not {@code null}
     *
     * @return  id, never {@code null}
     *
     * @since 1.0.0
     */
    @Contract(pure = true)
    default @NotNull String getIdFromMessage(@NotNull M message) {
      return UUID.randomUUID().toString();
    }




    /**
     * Pairs the internal message representation with its unique identifier, as produced
     * by {@link MessageProcessor#processMessage(String)}.
     *
     * @param <M>  internal message object type
     *
     * @since 1.0.0
     */
    interface MessageWithId<M>
    {
      /**
       * Returns the message id.
       *
       * @return  message id, never {@code null}
       *
       * @since 1.7.0  (refactoring)
       */
      @Contract(pure = true)
      @NotNull String id();


      /**
       * Returns the processed message.
       *
       * @return  processed message, never {@code null}
       *
       * @since 1.7.0  (refactoring)
       */
      @Contract(pure = true)
      @NotNull M message();
    }
  }




  /**
   * This class formats the internal message representation into a {@code String}.
   *
   * @param <M>  internal message object type
   *
   * @author Jeroen Gremmen
   * @since 0.7.0
   */
  @FunctionalInterface
  interface MessageFormatter<M>
  {
    /**
     * Formats the internal message representation into a {@code String}.
     *
     * @param message  Message to format
     *
     * @return  formatted message, never {@code null}
     */
    @Contract(pure = true)
    @NotNull String formatMessage(@NotNull GenericMessage<M> message);
  }
}
