/*
 * Copyright 2019 Jeroen Gremmen
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
package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.Protocol.GenericMessage;
import de.sayayi.lib.protocol.Protocol.MessageWithLevel;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;


/**
 *
 * @param <M>  Internal message object type. Messages are added by providing a string. The factory converts this
 *             string in the appropriate internal format (see {@link #processMessage(String)}), allowing for various
 *             message retrieval/formatting libraries to be used.
 *
 * @author Jeroen Gremmen
 */
@SuppressWarnings("squid:S1214")
public interface ProtocolFactory<M>
{
  interface Constants
  {
    /**
     * Name of the default tag.
     *
     * @see #getDefaultTag()
     */
    String DEFAULT_TAG_NAME = "default";


    String TICKET_PARAMETER_NAME = "ticket";
  }


  /**
   * Create a new protocol instance.
   *
   * @return  new protocol instance, never {@code null}.
   */
  @Contract("-> new")
  @NotNull Protocol<M> createProtocol();


  @Contract("_ -> new")
  @NotNull TagBuilder createTag(@NotNull @Pattern("\\p{Alpha}\\p{Graph}*") String name);


  @Contract(pure = true, value = "_ -> new")
  @NotNull TagBuilder modifyTag(@NotNull @Pattern("\\p{Alpha}\\p{Graph}*") String name);


  @Contract(pure = true)
  Tag getTagByName(@NotNull @Pattern("\\p{Alpha}\\p{Graph}*") String name);


  @Contract(pure = true)
  boolean hasTag(@NotNull @Pattern("\\p{Alpha}\\p{Graph}*") String name);


  @Contract(pure = true)
  boolean isRegisteredTag(@NotNull Tag tag);


  @Contract(pure = true, value = "-> new")
  @NotNull Set<Tag> getTags();


  /**
   * Returns the default tag which is used for each message protocolled.
   *
   * @return  default tag
   */
  @Contract(pure = true)
  @NotNull Tag getDefaultTag();


  @Contract(pure = true)
  @NotNull Map<String,Object> getDefaultParameterValues();


  /**
   * Transform the given message into its internal representation.
   * <p>
   * The simplest implementation would be to return the message as is. However this method provides a way to
   * integrate more complex message retrieval and/or formatting strategies:
   *
   * <ul>
   *   <li>The {@code message} could be a resource key which is used to lookup the actual message text</li>
   *   <li>The {@code message} could be a Spring Expression and the returned object would be a compiled expression</li>
   *   <li>Syntax analysis can be performed on the message</li>
   * </ul>
   *
   * @param message  message, not {@code null}
   *
   * @return  internal representation for {@code message}
   */
  @Contract(pure = true)
  M processMessage(@NotNull String message);


  /**
   * <p>
   *   Create a unique ticket number.
   * </p>
   * <p>
   *   The provided {@code message} can be examined in order to generate a more fine grained ticket number.
   *   Please note however, that required message parameters may not be available yet.
   * </p>
   *
   * @param message  message to be used for ticket generation, not {@code null}
   *
   * @return  a new unique ticket number, never {@code null}
   *
   * @see GenericMessage#getParameterValues()
   */
  @Contract(pure = true, value = "_ -> new")
  @NotNull String createTicketFor(@NotNull MessageWithLevel<M> message);


  /**
   *
   * @param <M>  internal message object type
   */
  @SuppressWarnings("UnusedReturnValue")
  interface TagBuilder<M> extends ProtocolFactory<M>
  {
    @Contract("_ -> this")
    @NotNull TagBuilder dependsOn(@NotNull String ... tags);


    @Contract("_ -> this")
    @NotNull TagBuilder implies(@NotNull String ... tags);


    @Contract("_, _ -> this")
    @NotNull TagBuilder match(@NotNull Tag.MatchCondition matchCondition, @NotNull Level matchLevel);


    /**
     * Returns the tag instance build by this builder.
     *
     * @return  tag instance
     */
    @Contract(pure = true)
    @NotNull Tag getTag();
  }
}
