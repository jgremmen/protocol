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

import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor;
import de.sayayi.lib.protocol.ProtocolFormatter.ConfiguredProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolIterator.DepthEntry;
import de.sayayi.lib.protocol.formatter.TechnicalProtocolFormatter;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;


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
 * @since 0.1.0
 */
public interface Protocol<M> extends ProtocolQueryable
{
  /**
   * Returns the protocol factory which was used to create this protocol instance.
   *
   * @return  protocol factory
   */
  @Contract(pure = true)
  @NotNull ProtocolFactory<M> getFactory();


  /**
   * Returns the parent protocol instance.
   *
   * @return  parent protocol or {@code null} if this is the root protocol
   */
  @Contract(pure = true)
  Protocol<M> getParent();


  /**
   * Returns the id for this protocol. Protocol ids are unique.
   *
   * @return  protocol id
   *
   * @since 0.7.0
   */
  @Contract(pure = true)
  int getId();


  /**
   * <p>
   *   Prepare a tag propagation definition for this protocol.
   * </p>
   * <p>
   *   Tag propagation means that a source tag automatically implies a target tag for each message
   *   added to this protocol or its underlying protocol groups.
   * </p>
   * <p>
   *   If a message is added with tag X and a propagation definition exists for X -&gt; Y then the
   *   message will have both tags X and Y (as long as it matches the message level) as well as the
   *   tags implicated by X and Y as defined for each tag itself.
   * </p>
   *
   * @param tagSelector  tag selector
   *
   * @return  propagation target tag builder instance, never {@code null}
   */
  @Contract("_ -> new")
  @NotNull TargetTagBuilder<M> propagate(@NotNull TagSelector tagSelector);


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
  default @NotNull ProtocolMessageBuilder<M> debug() {
    return add(Level.Shared.DEBUG);
  }


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
  default @NotNull ProtocolMessageBuilder<M> info() {
    return add(Level.Shared.INFO);
  }


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
  default @NotNull ProtocolMessageBuilder<M> warn() {
    return add(Level.Shared.WARN);
  }


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
  default @NotNull ProtocolMessageBuilder<M> error() {
    return add(Level.Shared.ERROR);
  }


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
  default @NotNull ProtocolMessageBuilder<M> error(@NotNull Throwable throwable) {
    return add(Level.Shared.ERROR).withThrowable(throwable);
  }


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
   * <p>
   *   Returns a group iterator for this protocol.
   * </p>
   * <p>
   *   The protocol groups returned by the iterator are direct descendants of this protocol.
   * </p>
   *
   * @return  group iterator for this protocol, never {@code null}
   *
   * @since 0.7.0
   */
  @NotNull Iterator<ProtocolGroup<M>> groupIterator();


  /**
   * @since 1.0.0
   */
  @NotNull Spliterator<ProtocolGroup<M>> groupSpliterator();


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
  default <R> R format(@NotNull ProtocolFormatter<M,R> formatter, @NotNull Level level) {
    return format(formatter, level, Tag.any());
  }


  /**
   * Formats this protocol using the given {@code formatter} iterating over all elements matching {@code level} and
   * {@code tagSelector}.
   *
   * @param formatter    protocol formatter to use for formatting this protocol
   * @param level        level to match
   * @param tagSelector  selector to match tags
   * @param <R>          result type
   *
   * @return  formatted protocol, or {@code null}
   */
  @SuppressWarnings("unused")
  @Contract(pure = true)
  <R> R format(@NotNull ProtocolFormatter<M,R> formatter, @NotNull Level level, @NotNull TagSelector tagSelector);


  /**
   * Formats this protocol using the given {@code formatter}.
   *
   * @param formatter  formatter, not {@code null}
   *
   * @param <R>  formatting result type
   *
   * @return  formatted protocol, or {@code null}
   *
   * @see #format(ProtocolFormatter, Level, TagSelector)
   */
  @Contract(pure = true)
  default <R> R format(@NotNull ConfiguredProtocolFormatter<M,R> formatter) {
    return format(formatter, formatter.getLevel(), formatter.getTagSelector(getFactory()));
  }


  @Contract(pure = true, value = "_, _ -> new")
  @NotNull ProtocolIterator<M> iterator(@NotNull Level level, @NotNull TagSelector tagSelector);


  /**
   * @since 1.0.0
   */
  @Contract(pure = true, value = "_, _ -> new")
  default @NotNull Spliterator<DepthEntry<M>> spliterator(@NotNull Level level, @NotNull TagSelector tagSelector) {
    return spliteratorUnknownSize(iterator(level, tagSelector), ORDERED | DISTINCT | NONNULL);
  }


  /**
   * Tells if any entry in this protocol matches the given {@code level} and {@code tagSelector}.
   *
   * @param level        requested protocol level, not {@code null}
   * @param tagSelector  tag selector, not {@code null}
   *
   * @return  {@code true} if at least 1 entry in the protocol matches, {@code false} otherwise
   *
   * @see #matches(Level, TagSelector)
   * @see ProtocolFactory#getTagByName(String)
   */
  @Contract(pure = true)
  boolean matches(@NotNull Level level, @NotNull TagSelector tagSelector);


  /**
   * <p>
   *   Performs {@code action} on a group with the given unique {@code name}.
   * </p>
   * <p>
   *   The search probes every descendant group starting from this protocol until a matching group is found.
   * </p>
   *
   * @param name  group name to perform the action on, not {@code null}
   * @param action  action to perform on the group, not {@code null}
   *
   * @return  {@code true} if a group with the name has been found, {@code false} otherwise
   *
   * @since 1.0.0
   */
  boolean forGroupWithName(@NotNull String name, @NotNull Consumer<ProtocolGroup<M>> action);


  /**
   * <p>
   *   Performs {@code action} on all groups with names that match the given regular expression {@code regex}.
   * </p>
   * <p>
   *   The search probes every descendant group starting from this protocol for matching groups.
   * </p>
   *
   * @param regex  regular expression for matching group names, not {@code null} or empty
   * @param action  action to perform on matching groups, not {@code null}
   *
   * @since 1.0.0
   */
  void forEachGroupByRegex(@NotNull String regex, @NotNull Consumer<ProtocolGroup<M>> action);


  /**
   * Formats this protocol using the {@link TechnicalProtocolFormatter}.
   *
   * @return  technical representation of this protocol
   *
   * @since 0.7.0
   */
  @Contract(pure = true)
  default @NotNull String toStringTree() {
    return format(TechnicalProtocolFormatter.getInstance());
  }




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
    @NotNull ProtocolMessageBuilder<M> withThrowable(@NotNull Throwable throwable);


    /**
     * <p>
     *   Creates a new protocol message based on the builder settings and adds the message to the protocol.
     * </p>
     * <p>
     *   The {@code message} parameter is converted into an internal representation of type {@code M} using
     *   the {@link MessageProcessor} assigned to the protocol factory.
     * </p>
     *
     * @param message  message text, resource key or any other message identifier, never {@code null}
     *
     * @return  parameter builder instance for the newly created message
     */
    @Contract("_ -> new")
    @NotNull MessageParameterBuilder<M> message(@NotNull String message);


    /**
     * <p>
     *   Creates a new protocol message based on the builder settings and adds the message to the protocol.
     * </p>
     * <p>
     *   This method differs from {@link #message(String)} in that it bypasses the {@link MessageProcessor}
     *   and directly adds the internal message representation to the protocol.
     * </p>
     *
     * @param message  internal message instance, never {@code null}
     *
     * @return  parameter builder instance for the newly created message
     *
     * @since 0.4.0
     */
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
    default @NotNull MessageParameterBuilder<M> with(@NotNull String parameter, boolean value) {
      return with(parameter, Boolean.valueOf(value));
    }


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
    default @NotNull MessageParameterBuilder<M> with(@NotNull String parameter, int value) {
      return with(parameter, Integer.valueOf(value));
    }


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
    default @NotNull MessageParameterBuilder<M> with(@NotNull String parameter, long value) {
      return with(parameter, Long.valueOf(value));
    }


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
    default @NotNull MessageParameterBuilder<M> with(@NotNull String parameter, float value) {
      return with(parameter, Float.valueOf(value));
    }


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
    default @NotNull MessageParameterBuilder<M> with(@NotNull String parameter, double value) {
      return with(parameter, Double.valueOf(value));
    }


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
     *
     * @since 0.6.0
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
  interface GenericMessageWithLevel<M> extends GenericMessage<M>
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
   * A protocol message with level, optional throwable and tags.
   *
   * @param <M>  internal message object type
   */
  interface Message<M> extends GenericMessageWithLevel<M>
  {
    /**
     * Returns the throwable associated with the message.
     *
     * @return  throwable/exception or {@code null}
     */
    @Contract(pure = true)
    Throwable getThrowable();


    /**
     * Returns a set containing all tag names defined for this message.
     *
     * @return  set containing all tag names, never {@code null}
     *
     * @since 0.7.0
     */
    @Contract(pure = true, value = "-> new")
    @NotNull Set<String> getTagNames();
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
    GenericMessage<M> getGroupMessage();
  }




  /**
   * @param <M>  internal message object type
   *
   * @since 0.5.0
   */
  interface TargetTagBuilder<M>
  {
    @NotNull Protocol<M> to(@NotNull String targetTagName);


    @NotNull Protocol<M> to(@NotNull String ... targetTagNames);
  }
}
