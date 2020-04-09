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

import de.sayayi.lib.protocol.ProtocolFormatter.ConfiguredProtocolFormatter;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


/**
 * <p>
 *   This interface is the core instance for protocolling messages.
 * </p>
 * <p>
 *   A protocol is created by a protocol factory or by creating a group protocol using another protocol. Group
 *   protocols have additional functionality but share the same core functions defined in this interface.
 * </p>
 *
 * <pre>
 *   public void validate(Protocol protocol) {
 *     try {
 *       ...
 *
 *       if (!check1())
 *         protocol.info().forTag("ui").message("validation 1 failed");
 *
 *       ...
 *     } catch(Exception ex) {
 *       String ticket = createTicket();
 *       protocol.warn().forTags("ui", "support").message("Ticket {0} created. Please contact support").with("0", ticket)
 *               .error(ex).forTag("support").message("Unexpected validation error occurred");
 *     }
 *   }
 * </pre>
 * <p>
 *   Protocol instances are not thread safe.
 * </p>
 *
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 */
public interface Protocol<M> extends ProtocolQuery
{
  /**
   * Returns the protocol factory which was used to create this protocol instance.
   *
   * @return  protocol factory
   */
  @Contract(pure = true)
  @NotNull ProtocolFactory<M> getFactory();


  /**
   * Returns the parent protocol.
   *
   * @return  parent protocol or {@code null} if this is the root protocol
   */
  @Contract(pure = true)
  Protocol<M> getGroupParent();


  /**
   * <p>
   *   Prepares a new debug level message.
   * </p>
   * <p>
   *   This method is a convenience function and is identical to {@code add(Level.Shared.DEBUG)}
   * </p>
   *
   * @return  message builder instance for the debug message, never {@code null}
   *
   * @see Level.Shared#DEBUG DEBUG
   */
  @Contract(pure = true, value = "-> new")
  @NotNull ProtocolMessageBuilder<M> debug();


  /**
   * <p>
   *   Prepares a new info level message.
   * </p>
   * <p>
   *   This method is a convenience function and is identical to {@code add(Level.Shared.INFO)}
   * </p>
   *
   * @return  message builder instance for the info message, never {@code null}
   *
   * @see Level.Shared#INFO INFO
   */
  @Contract(pure = true, value = "-> new")
  @NotNull ProtocolMessageBuilder<M> info();


  /**
   * <p>
   *   Prepares a new warning level message.
   * </p>
   * <p>
   *   This method is a convenience function and is identical to {@code add(Level.Shared.WARN)}
   * </p>
   *
   * @return  message builder instance for the warning message, never {@code null}
   *
   * @see Level.Shared#WARN WARN
   */
  @Contract(pure = true, value = "-> new")
  @NotNull ProtocolMessageBuilder<M> warn();


  /**
   * <p>
   *   Prepares a new error level message.
   * </p>
   * <p>
   *   This method is a convenience function and is identical to {@code add(Level.Shared.ERROR)}
   * </p>
   *
   * @return  message builder instance for the error message, never {@code null}
   *
   * @see Level.Shared#ERROR ERROR
   */
  @Contract(pure = true, value = "-> new")
  @NotNull ProtocolMessageBuilder<M> error();


  /**
   * <p>
   *   Prepares a new error level message with throwable.
   * </p>
   * <p>
   *   This method is a convenience function and is identical to
   *   {@code add(Level.Shared.ERROR).withThrowable(throwable)}
   * </p>
   *
   * @param throwable  throwable associated with message
   *
   * @return  message builder instance for the error message, never {@code null}
   *
   * @see Level.Shared#ERROR ERROR
   */
  @Contract(pure = true, value = "_ -> new")
  @NotNull ProtocolMessageBuilder<M> error(Throwable throwable);


  /**
   * <p>
   *   Prepares a new message with the given protocol {@code level}.
   * </p>
   *
   * @param level  protocol level, never {@code null}
   *
   * @return  new message builder instance, never {@code null}
   */
  @Contract(pure = true, value = "_ -> new")
  @NotNull ProtocolMessageBuilder<M> add(@NotNull Level level);


  /**
   * Create a new protocol group.
   *
   * @return  new protocol group
   */
  @Contract("-> new")
  @NotNull ProtocolGroup<M> createGroup();


  /**
   * Formats this protocol using the given {@code formatter} iterating over all elements matching {@code level}.
   *
   * @param formatter  protocol formatter to use for formatting this protocol
   * @param level      level to match
   * @param <R>        result type
   *
   * @return  formatted protocol, or {@code null}
   */
  @SuppressWarnings("unused")
  @Contract(pure = true)
  <R> R format(@NotNull ProtocolFormatter<M,R> formatter, @NotNull Level level);


  /**
   * Formats this protocol using the given {@code formatter} iterating over all elements matching {@code level} and at
   * least one of the {@code tags}.
   *
   * @param formatter  protocol formatter to use for formatting this protocol
   * @param level      level to match
   * @param tags       tag or tags to match
   * @param <R>        result type
   *
   * @return  formatted protocol, or {@code null}
   */
  @SuppressWarnings("unused")
  @Contract(pure = true)
  <R> R format(@NotNull ProtocolFormatter<M,R> formatter, @NotNull Level level, @NotNull Tag ... tags);


  /**
   * Formats this protocol using the given {@code formatter} iterating over all elements matching {@code level} and at
   * least one of the {@code tags}.
   *
   * @param formatter  protocol formatter to use for formatting this protocol
   * @param level      level to match
   * @param tagNames   tag or tags to match
   * @param <R>        result type
   *
   * @return  formatted protocol, or {@code null}
   *
   * @throws IllegalArgumentException  if at least one of the {@code tagNames} is not registered by the same
   *                                   protocol factory
   */
  @SuppressWarnings("unused")
  @Contract(pure = true)
  <R> R format(@NotNull ProtocolFormatter<M,R> formatter, @NotNull Level level, @NotNull String ... tagNames);


  /**
   * Formats this protocol using the given {@code formatter}.
   *
   * @param formatter  formatter, not {@code null}
   *
   * @param <R>  formatting result type
   *
   * @return  formatted protocol, or {@code null}
   *
   * @see #format(ProtocolFormatter, Level, Tag[])
   */
  @Contract(pure = true)
  <R> R format(@NotNull ConfiguredProtocolFormatter<M,R> formatter);


  @Contract(pure = true, value = "_, _ -> new")
  @NotNull ProtocolIterator<M> iterator(@NotNull Level level, @NotNull Tag ... tags);


  /**
   * Tells if any entry in this protocol matches the given {@code level} and {@code tags}.
   *
   * @param level     requested protocol level, not {@code null}
   * @param tagNames  tags to query, not {@code null}
   *
   * @return  {@code true} if at least 1 entry in the protocol matches, {@code false} otherwise
   *
   * @see #matches(Level, Tag[])
   * @see ProtocolFactory#getTagByName(String)
   */
  @Contract(pure = true)
  boolean matches(@NotNull Level level, @NotNull String ... tagNames);


  /**
   * <p>
   *   Builder pattern for creating a protocol message.
   * </p>
   * <p>
   *   The builder collects tag and throwable information to be associated with the message. The message itself is
   *   added to the protocol when the {@link #message(String)} method is invoked.
   * </p>
   *
   * @param <M>  internal message object type
   */
  @SuppressWarnings({ "UnusedReturnValue", "unused" })
  interface ProtocolMessageBuilder<M>
  {
    /**
     * <p>
     *   Adds a tag to the protocol message.
     * </p>
     *
     * @param tag  tag to associate with the new message, never {@code null}
     *
     * @return  this instance
     *
     * @throws IllegalArgumentException  if {@code tag} is not registered by the same protocol factory
     *
     * @see ProtocolFactory#isRegisteredTag(Tag)
     */
    @Contract("_ -> this")
    @NotNull ProtocolMessageBuilder<M> forTag(@NotNull Tag tag);


    /**
     * <p>
     *   Adds a tag to the protocol message.
     * </p>
     *
     * @param tagName  tag to associate with the new message, never {@code null}
     *
     * @return  this instance
     *
     * @throws IllegalArgumentException  if {@code tagName} is not registered by the same protocol factory
     *
     * @see ProtocolFactory#getTagByName(String)
     */
    @Contract("_ -> this")
    @NotNull ProtocolMessageBuilder<M> forTag(@NotNull String tagName);


    /**
     * <p>
     *   Adds multiple tags to the protocol message.
     * </p>
     *
     * @param tags  tags to associate with the new message, never {@code null}
     *
     * @return  this instance
     *
     * @throws IllegalArgumentException  if at least one tag is not registered by the same protocol factory
     *
     * @see ProtocolFactory#isRegisteredTag(Tag)
     */
    @Contract("_ -> this")
    @NotNull ProtocolMessageBuilder<M> forTags(@NotNull Tag ... tags);


    /**
     * <p>
     *   Adds multiple tags to the protocol message.
     * </p>
     *
     * @param tagNames  names of the tags to associate with the new message, never {@code null}
     *
     * @return  this instance
     *
     * @throws IllegalArgumentException  if at least one tagName is not registered by the protocol factory
     *
     * @see ProtocolFactory#getTagByName(String)
     */
    @Contract("_ -> this")
    @NotNull ProtocolMessageBuilder<M> forTags(@NotNull String ... tagNames);


    /**
     * <p>
     *   Sets a throwable for the new protocol message.
     * </p>
     *
     * @param throwable  throwable to associate with the new message
     *
     * @return  this instance
     *
     * @see Protocol#error(Throwable)
     */
    @Contract("_ -> this")
    @NotNull ProtocolMessageBuilder<M> withThrowable(Throwable throwable);


    /**
     * <p>
     *   Creates a new protocol message based on the builder settings and adds the message to the protocol.
     * </p>
     * <p>
     *   The {@code message} parameter is converted into an internal representation of type {@code M} using
     *   factory method {@link ProtocolFactory#processMessage(String)}.
     * </p>
     *
     * @param message  message text, resource key or any other message identifier, never {@code null}
     *
     * @return  parameter builder instance for the newly created message
     */
    @Contract("_ -> new")
    @NotNull MessageParameterBuilder<M> message(@NotNull String message);


    @Contract("_ -> new")
    @NotNull MessageParameterBuilder<M> withMessage(@NotNull M message);
  }


  /**
   * <p>
   *   Builder pattern for setting message parameter values.
   * </p>
   *
   * @param <M>  internal message object type
   */
  interface MessageParameterBuilder<M> extends Protocol<M>, GenericMessage<M>
  {
    /**
     * <p>
     *   Associate the provided {@code parameterValues} with this message. New parameters are added, existing
     *   parameters are overridden.
     * </p>
     * <p>
     *   Any restrictions on parameter name and value are handled by {@link #with(String, Object)}.
     * </p>
     *
     * @param parameterValues  map with parameter values. the parameter name must not be {@code null} or empty.
     *
     * @return  paramter builder instance for the current message
     */
    @Contract("_ -> this")
    @NotNull MessageParameterBuilder<M> with(@NotNull Map<String,Object> parameterValues);


    /**
     * <p>
     *   Associate the provided {@code parameter} and boolean {@code value} with this message. If a parameter with
     *   the same name already has a value, it will be overridden.
     * </p>
     *
     * @param parameter  parameter name, not {@code null} or empty. although there are no restrictions, it is
     *                   recommended that the parameter name matches regular expression {@code \p{Alnum}\p{Graph}*}.
     * @param value      parameter value
     *
     * @return  paramter builder instance for the current message
     */
    @Contract("_, _ -> this")
    @NotNull MessageParameterBuilder<M> with(@NotNull String parameter, boolean value);


    /**
     * <p>
     *   Associate the provided {@code parameter} and int {@code value} with this message. If a parameter with
     *   the same name already has a value, it will be overridden.
     * </p>
     *
     * @param parameter  parameter name, not {@code null} or empty. although there are no restrictions, it is
     *                   recommended that the parameter name matches regular expression {@code \p{Alnum}\p{Graph}*}.
     * @param value      parameter value
     *
     * @return  paramter builder instance for the current message
     */
    @Contract("_, _ -> this")
    @NotNull MessageParameterBuilder<M> with(@NotNull String parameter, int value);


    /**
     * <p>
     *   Associate the provided {@code parameter} and long {@code value} with this message. If a parameter with
     *   the same name already has a value, it will be overridden.
     * </p>
     *
     * @param parameter  parameter name, not {@code null} or empty. although there are no restrictions, it is
     *                   recommended that the parameter name matches regular expression {@code \p{Alnum}\p{Graph}*}.
     * @param value      parameter value
     *
     * @return  paramter builder instance for the current message
     */
    @Contract("_, _ -> this")
    @NotNull MessageParameterBuilder<M> with(@NotNull String parameter, long value);


    /**
     * <p>
     *   Associate the provided {@code parameter} and float {@code value} with this message. If a parameter with
     *   the same name already has a value, it will be overridden.
     * </p>
     *
     * @param parameter  parameter name, not {@code null} or empty. although there are no restrictions, it is
     *                   recommended that the parameter name matches regular expression {@code \p{Alnum}\p{Graph}*}.
     * @param value      parameter value
     *
     * @return  paramter builder instance for the current message
     */
    @Contract("_, _ -> this")
    @NotNull MessageParameterBuilder<M> with(@NotNull String parameter, float value);


    /**
     * <p>
     *   Associate the provided {@code parameter} and double {@code value} with this message. If a parameter with
     *   the same name already has a value, it will be overridden.
     * </p>
     *
     * @param parameter  parameter name, not {@code null} or empty. although there are no restrictions, it is
     *                   recommended that the parameter name matches regular expression {@code \p{Alnum}\p{Graph}*}.
     * @param value      parameter value
     *
     * @return  paramter builder instance for the current message
     */
    @Contract("_, _ -> this")
    @NotNull MessageParameterBuilder<M> with(@NotNull String parameter, double value);


    /**
     * <p>
     *   Associate the provided {@code parameter} and {@code value} with this message. If a parameter with the same
     *   name already has a value, it will be overridden.
     * </p>
     *
     * @param parameter  parameter name, not {@code null} or empty. although there are no restrictions, it is
     *                   recommended that the parameter name matches regular expression {@code \p{Alnum}\p{Graph}*}.
     * @param value      parameter value
     *
     * @return  paramter builder instance for the current message
     */
    @Contract("_, _ -> this")
    @NotNull MessageParameterBuilder<M> with(@NotNull String parameter, Object value);
  }


  /**
   * The most generic representation of a message, providing the internal representation of the message and parameter
   * values to be used for formatting the message.
   *
   * @param <M>  internal message object type
   */
  interface GenericMessage<M>
  {
    /**
     * Returns the internal representation of the message.
     *
     * @return  message, never {@code null}
     */
    @Contract(pure = true)
    @NotNull M getMessage();


    /**
     * Returns the message creation time.
     *
     * @return  creation time measured in milliseconds since midnight, January 1, 1970 UTC
     */
    @Contract(pure = true)
    long getTimeMillis();


    /**
     * Returns a map with parameter names and values to be used for formatting the message.
     * <ul>
     *   <li>a parameter name (map key) must have a length of at least 1</li>
     *   <li>additional parameters are allowed to those which are required to format the message</li>
     *   <li>
     *     if a parameter required by the message is missing, the behaviour depends on the message formatter
     *     implementation; it may choose a default or throw an exception
     *   </li>
     * </ul>
     *
     * @return  parameter values, never {@code null}
     *
     * @see #getMessage()
     */
    @Contract(pure = true, value = "-> new")
    @NotNull Map<String,Object> getParameterValues();
  }


  /**
   * A protocol message with level.
   *
   * @param <M>  internal message object type
   */
  interface MessageWithLevel<M> extends GenericMessage<M>
  {
    /**
     * Returns the level for this message.
     *
     * @return  message level, never {@code null}
     */
    @Contract(pure = true)
    @NotNull Level getLevel();
  }


  /**
   * A protocol message with level and optional throwable.
   *
   * @param <M>  internal message object type
   */
  interface Message<M> extends MessageWithLevel<M>
  {
    /**
     * Returns the throwable associated with the message.
     *
     * @return  throwable/exception or {@code null}
     */
    @Contract(pure = true)
    Throwable getThrowable();
  }


  /**
   * A protocol group with optional group header message.
   *
   * @param <M>  internal message object type
   */
  interface Group<M>
  {
    /**
     * Returns the group header message.
     *
     * @return  group header message or {@code null}
     */
    @Contract(pure = true)
    GenericMessage<M> getGroupHeader();
  }
}
