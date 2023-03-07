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
 *
 * @param <M>  Internal message object type. Messages are added by providing a string. The factory
 *             converts this string in the appropriate internal format
 *             (see {@link MessageProcessor}), allowing for various message retrieval/formatting
 *             libraries to be used.
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
@SuppressWarnings("squid:S1214")
public interface ProtocolFactory<M> extends ProtocolMessageMatcher
{
  /**
   * Name of the default tag.
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
       */
      @Contract(pure = true)
      @NotNull String getId();


      /**
       * Returns the processed message.
       *
       * @return  processed message, never {@code null}
       */
      @Contract(pure = true)
      @NotNull M getMessage();
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