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

import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor;
import de.sayayi.lib.protocol.ProtocolFormatter.ConfiguredProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolIterator.DepthEntry;
import de.sayayi.lib.protocol.formatter.TechnicalProtocolFormatter;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * <p>
 *   This interface is the core instance for protocolling messages.
 * </p>
 * <p>
 *   A protocol is created by a protocol factory or by creating a group protocol using another
 *   protocol. Group protocols have additional functionality but share the same core functions
 *   defined in this interface.
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
 *
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
   * Tells if this protocol is a protocol group.
   *
   * @return  {@code true} if this protocol is a protocol group, {@code false} otherwise
   *
   * @since 1.1.0
   */
  @Contract(pure = true)
  default boolean isProtocolGroup() {
    return this instanceof ProtocolGroup;
  }


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
   * Tells if this protocol object matches the given {@code matcher}.
   *
   * @param matcher  message matcher, not {@code null}
   *
   * @return  {@code true} if the protocol object matches, {@code false} otherwise
   *
   * @since 1.2.2
   */
  boolean matches(@NotNull String matcher);


  /**
   * Prepare a tag propagation definition for this protocol.
   * <p>
   * Tag propagation means that a source tag automatically implies a target tag for each message
   * added to this protocol or its underlying protocol groups.
   * <p>
   * If a message is added with tag X and a propagation definition exists for X -&gt; Y then the
   * message will have both tags X and Y.
   *
   * @param tagSelector  tag selector
   *
   * @return  propagation target tag builder instance, never {@code null}
   */
  @Contract(value = "_ -> new", pure = true)
  @NotNull TargetTagBuilder<M> propagate(@NotNull TagSelector tagSelector);


  /**
   * Prepare a tag propagation definition for this protocol.
   * <p>
   * Tag propagation means that a source tag automatically implies a target tag for each message
   * added to this protocol or its underlying protocol groups.
   * <p>
   * If a message is added with tag X and a propagation definition exists for X -&gt; Y then the
   * message will have both tags X and Y.
   *
   * @param tagSelectorExpression  tag selector expression
   *
   * @return  propagation target tag builder instance, never {@code null}
   *
   * @since 1.2.1
   */
  @Contract(value = "_ -> new", pure = true)
  @NotNull TargetTagBuilder<M> propagate(@NotNull String tagSelectorExpression);


  /**
   * Set a parameter value.
   * <p>
   * Parameter values set for this protocol are available for both messages and groups added to
   * this protocol.
   *
   * @param parameter  name of the parameter to set, never {@code null}
   * @param b          parameter value
   *
   * @return  current protocol instance
   *
   * @since 1.0.0
   */
  @Contract(value = "_, _ -> this", mutates = "this")
  default @NotNull Protocol<M> set(@NotNull String parameter, boolean b) {
    return set(parameter, Boolean.valueOf(b));
  }


  /**
   * Set a parameter value.
   * <p>
   * Parameter values set for this protocol are available for both messages and groups added to
   * this protocol.
   *
   * @param parameter  name of the parameter to set, never {@code null}
   * @param i          parameter value
   *
   * @return  current protocol instance
   *
   * @since 1.0.0
   */
  @Contract(value = "_, _ -> this", mutates = "this")
  default @NotNull Protocol<M> set(@NotNull String parameter, int i) {
    return set(parameter, Integer.valueOf(i));
  }


  /**
   * Set a parameter value.
   * <p>
   * Parameter values set for this protocol are available for both messages and groups added to
   * this protocol.
   *
   * @param parameter  name of the parameter to set, never {@code null}
   * @param l          parameter value
   *
   * @return  current protocol instance
   *
   * @since 1.0.0
   */
  @Contract(value = "_, _ -> this", mutates = "this")
  default @NotNull Protocol<M> set(@NotNull String parameter, long l) {
    return set(parameter, Long.valueOf(l));
  }


  /**
   * Set a parameter value.
   * <p>
   * Parameter values set for this protocol are available for both messages and groups added to
   * this protocol.
   *
   * @param parameter  name of the parameter to set, never {@code null}
   * @param f          parameter value
   *
   * @return  current protocol instance
   *
   * @since 1.0.0
   */
  @Contract(value = "_, _ -> this", mutates = "this")
  default @NotNull Protocol<M> set(@NotNull String parameter, float f) {
    return set(parameter, Float.valueOf(f));
  }


  /**
   * Set a parameter value.
   * <p>
   * Parameter values set for this protocol are available for both messages and groups added to
   * this protocol.
   *
   * @param parameter  name of the parameter to set, never {@code null}
   * @param d          parameter value
   *
   * @return  current protocol instance
   *
   * @since 1.0.0
   */
  @Contract(value = "_, _ -> this", mutates = "this")
  default @NotNull Protocol<M> set(@NotNull String parameter, double d) {
    return set(parameter, Double.valueOf(d));
  }


  /**
   * Set a parameter value.
   * <p>
   * Parameter values set for this protocol are available for both messages and groups added to
   * this protocol.
   *
   * @param parameter  name of the parameter to set, never {@code null}
   * @param value      parameter value
   *
   * @return  current protocol instance
   *
   * @since 1.0.0
   */
  @Contract(value = "_, _ -> this", mutates = "this")
  @NotNull Protocol<M> set(@NotNull String parameter, Object value);


  /**
   * Prepares a new debug level message.
   * <p>
   * This method is a convenience function and is identical to {@code add(Level.Shared.DEBUG)}
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
   * Prepares a new info level message.
   * <p>
   * This method is a convenience function and is identical to {@code add(Level.Shared.INFO)}
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
   * Prepares a new warning level message.
   * <p>
   * This method is a convenience function and is identical to {@code add(Level.Shared.WARN)}
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
   * Prepares a new error level message.
   * <p>
   * This method is a convenience function and is identical to {@code add(Level.Shared.ERROR)}
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
   * Prepares a new error level message with throwable.
   * <p>
   * This method is a convenience function and is identical to
   * {@code add(Level.Shared.ERROR).withThrowable(throwable)}
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
   * Prepares a new message with the given protocol {@code level}.
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
   * @return  new protocol group, never {@code null}
   */
  @Contract(value = "-> new", mutates = "this")
  @NotNull ProtocolGroup<M> createGroup();


  /**
   * Create a new protocol group with the given {@code name}.
   *
   * @param name  protocol group name or {@code null}
   *
   * @return  new protocol group, never {@code null}
   *
   * @see ProtocolGroup#setName(String)
   */
  @Contract(value = "_ -> new", mutates = "this")
  default @NotNull ProtocolGroup<M> createGroup(String name) {
    return createGroup().setName(name);
  }


  /**
   * Returns a group iterator for this protocol.
   * <p>
   * The protocol groups returned by the iterator are direct descendants of this protocol.
   *
   * @return  group iterator for this protocol, never {@code null}
   *
   * @since 0.7.0
   */
  @Contract(value = "-> new", pure = true)
  @NotNull Iterator<ProtocolGroup<M>> groupIterator();


  /**
   * Creates a {@code Spliterator} over all protocol groups which are direct descendants of this
   * protocol, with a rough initial size estimate.
   * <p>
   * The {@code Spliterator} reports {@link Spliterator#ORDERED}, {@link Spliterator#DISTINCT}
   * and {@link Spliterator#NONNULL}.
   *
   * @return  group spliterator for this protocol, never {@code null}
   *
   * @see #groupIterator()
   *
   * @since 1.0.0
   */
  @Contract(value = "-> new", pure = true)
  @NotNull Spliterator<ProtocolGroup<M>> groupSpliterator();


  /**
   * Formats this protocol using the given {@code formatter} iterating over all elements filtered by
   * {@code matcher}.
   *
   * @param formatter  protocol formatter to use for formatting this protocol
   * @param matcher    message matcher, never {@code null}
   * @param <R>        result type
   *
   * @return  formatted protocol, or {@code null}
   *
   * @since 1.0.0
   */
  <R> R format(@NotNull ProtocolFormatter<M,R> formatter, @NotNull MessageMatcher matcher);


  /**
   * Formats this protocol using the given {@code formatter} iterating over all elements filtered by
   * {@code matcher}.
   *
   * @param formatter          protocol formatter to use for formatting this protocol
   * @param matcherExpression  message matcher, never {@code null}
   * @param <R>                result type
   *
   * @return  formatted protocol, or {@code null}
   *
   * @since 1.2.1
   */
  default <R> R format(@NotNull ProtocolFormatter<M,R> formatter, @NotNull String matcherExpression) {
    return format(formatter, getFactory().parseMessageMatcher(matcherExpression));
  }


  /**
   * Formats this protocol using the given {@code formatter}.
   *
   * @param formatter  formatter, not {@code null}
   *
   * @param <R>  formatting result type
   *
   * @return  formatted protocol, or {@code null}
   *
   * @see #format(ProtocolFormatter, MessageMatcher)
   */
  default <R> R format(@NotNull ConfiguredProtocolFormatter<M,R> formatter) {
    return format(formatter, formatter.getMatcher(getFactory()));
  }


  /**
   * @param matcher  Message matcher, never {@code null}
   *
   * @return  protocol iterator over all matching elements, never {@code null}
   *
   * @since 1.0.0
   */
  @Contract(pure = true, value = "_ -> new")
  @NotNull ProtocolIterator<M> iterator(@NotNull MessageMatcher matcher);


  /**
   * Creates a {@code Spliterator} over the elements of this protocol matched by
   * {@code matcher}, with no initial size estimate.
   * <p>
   * The {@code Spliterator} reports {@link Spliterator#ORDERED},{@link Spliterator#DISTINCT},
   * {@link Spliterator#NONNULL} and {@link Spliterator#IMMUTABLE}.
   *
   * @param matcher  Message matcher, never {@code null}
   *
   * @return A spliterator from an iterator
   *
   * @since 1.0.0
   */
  @Contract(pure = true, value = "_ -> new")
  @NotNull Spliterator<DepthEntry<M>> spliterator(@NotNull MessageMatcher matcher);


  @Contract(pure = true, value = "_ -> new")
  default @NotNull Stream<DepthEntry<M>> stream(@NotNull MessageMatcher matcher) {
    return StreamSupport.stream(spliterator(matcher), false);
  }


  /**
   * Search for a group by name.
   * <p>
   * The search probes every descendant group starting from this protocol until a matching group
   * is found.
   *
   * @param name  group name to search for, not {@code null}
   *
   * @return  optional instance of the group pr empty if no matching protocol group was found
   *
   * @since 1.0.0
   */
  @Contract(pure = true)
  @NotNull Optional<ProtocolGroup<M>> getGroupByName(@NotNull String name);


  /**
   * Performs {@code action} on all groups with names that match the given regular expression
   * {@code regex}.
   * <p>
   * The search probes every descendant group starting from this protocol for matching groups.
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
   * Builder pattern for creating a protocol message.
   * <p>
   * The builder collects tag and throwable information to be associated with the message.
   * The message itself is added to the protocol when the {@link #message(String)} method is
   * invoked.
   *
   * @param <M>  internal message object type
   */
  @SuppressWarnings({ "UnusedReturnValue", "unused" })
  interface ProtocolMessageBuilder<M>
  {
    /**
     * Adds a tag to the protocol message.
     *
     * @param tagName  tag to associate with the new message, never {@code null}
     *
     * @return  this instance
     */
    @Contract("_ -> this")
    @NotNull ProtocolMessageBuilder<M> forTag(@NotNull String tagName);


    /**
     * Adds multiple tags to the protocol message.
     *
     * @param tagNames  names of the tags to associate with the new message, never {@code null}
     *
     * @return  this instance
     */
    @Contract("_ -> this")
    @NotNull ProtocolMessageBuilder<M> forTags(@NotNull String ... tagNames);


    /**
     * Sets a throwable for the new protocol message.
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
     * Creates a new protocol message based on the builder settings and adds the message to the
     * protocol.
     * <p>
     * The {@code message} parameter is converted into an internal representation of type
     * {@code M} using the {@link MessageProcessor} assigned to the protocol factory.
     *
     * @param message  message text, resource key or any other message identifier, never
     *                 {@code null}
     *
     * @return  parameter builder instance for the newly created message
     */
    @Contract("_ -> new")
    @NotNull MessageParameterBuilder<M> message(@NotNull String message);


    /**
     * Creates a new protocol message based on the builder settings and adds the message to
     * the protocol.
     * <p>
     * This method differs from {@link #message(String)} in that it bypasses the
     * {@link MessageProcessor} and directly adds the internal message representation to the
     * protocol.
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
   * Builder pattern for setting message parameter values.
   *
   * @param <M>  internal message object type
   */
  interface MessageParameterBuilder<M> extends Protocol<M>, GenericMessage<M>
  {
    /**
     * Associate the provided {@code parameterValues} with this message. New parameters are
     * added, existing parameters are overridden.
     * <p>
     * Any restrictions on parameter name and value are handled by {@link #with(String, Object)}.
     *
     * @param parameterValues  map with parameter values. the parameter name must not be
     *                         {@code null} or empty.
     *
     * @return  paramter builder instance for the current message
     */
    @Contract(value = "_ -> this", mutates = "this")
    @NotNull MessageParameterBuilder<M> with(@NotNull Map<String,Object> parameterValues);


    /**
     * Associate the provided {@code parameter} and boolean {@code value} with this message.
     * If a parameter with the same name already has a value, it will be overridden.
     *
     * @param parameter  parameter name, not {@code null} or empty. although there are no
     *                   restrictions, it is recommended that the parameter name matches regular
     *                   expression {@code \p{Alnum}\p{Graph}*}.
     * @param value      parameter value
     *
     * @return  paramter builder instance for the current message
     */
    @Contract(value = "_, _ -> this", mutates = "this")
    default @NotNull MessageParameterBuilder<M> with(@NotNull String parameter, boolean value) {
      return with(parameter, Boolean.valueOf(value));
    }


    /**
     * Associate the provided {@code parameter} and int {@code value} with this message.
     * If a parameter with the same name already has a value, it will be overridden.
     *
     * @param parameter  parameter name, not {@code null} or empty. although there are no
     *                   restrictions, it is recommended that the parameter name matches regular
     *                   expression {@code \p{Alnum}\p{Graph}*}.
     * @param value      parameter value
     *
     * @return  paramter builder instance for the current message
     */
    @Contract(value = "_, _ -> this", mutates = "this")
    default @NotNull MessageParameterBuilder<M> with(@NotNull String parameter, int value) {
      return with(parameter, Integer.valueOf(value));
    }


    /**
     * Associate the provided {@code parameter} and long {@code value} with this message.
     * If a parameter with the same name already has a value, it will be overridden.
     *
     * @param parameter  parameter name, not {@code null} or empty. although there are no
     *                   restrictions, it is recommended that the parameter name matches regular
     *                   expression {@code \p{Alnum}\p{Graph}*}.
     * @param value      parameter value
     *
     * @return  paramter builder instance for the current message
     */
    @Contract(value = "_, _ -> this", mutates = "this")
    default @NotNull MessageParameterBuilder<M> with(@NotNull String parameter, long value) {
      return with(parameter, Long.valueOf(value));
    }


    /**
     * Associate the provided {@code parameter} and float {@code value} with this message.
     * If a parameter with the same name already has a value, it will be overridden.
     *
     * @param parameter  parameter name, not {@code null} or empty. although there are no
     *                   restrictions, it is recommended that the parameter name matches regular
     *                   expression {@code \p{Alnum}\p{Graph}*}.
     * @param value      parameter value
     *
     * @return  paramter builder instance for the current message
     */
    @Contract(value = "_, _ -> this", mutates = "this")
    default @NotNull MessageParameterBuilder<M> with(@NotNull String parameter, float value) {
      return with(parameter, Float.valueOf(value));
    }


    /**
     * Associate the provided {@code parameter} and double {@code value} with this message.
     * If a parameter with the same name already has a value, it will be overridden.
     *
     * @param parameter  parameter name, not {@code null} or empty. although there are no
     *                   restrictions, it is recommended that the parameter name matches regular
     *                   expression {@code \p{Alnum}\p{Graph}*}.
     * @param value      parameter value
     *
     * @return  paramter builder instance for the current message
     */
    @Contract(value = "_, _ -> this", mutates = "this")
    default @NotNull MessageParameterBuilder<M> with(@NotNull String parameter, double value) {
      return with(parameter, Double.valueOf(value));
    }


    /**
     * Associate the provided {@code parameter} and {@code value} with this message.
     * If a parameter with the same name already has a value, it will be overridden.
     *
     * @param parameter  parameter name, not {@code null} or empty. although there are no
     *                   restrictions, it is recommended that the parameter name matches regular
     *                   expression {@code \p{Alnum}\p{Graph}*}.
     * @param value      parameter value
     *
     * @return  paramter builder instance for the current message
     */
    @Contract(value = "_, _ -> this", mutates = "this")
    @NotNull MessageParameterBuilder<M> with(@NotNull String parameter, Object value);
  }




  /**
   * The most generic representation of a message, providing the internal representation of the
   * message and parameter values to be used for formatting the message.
   *
   * @param <M>  internal message object type
   */
  interface GenericMessage<M>
  {
    /**
     * Returns the id for this message. The id must not be unique.
     *
     * @return  id for this message, never {@code null}
     *
     * @since 1.0.0
     */
    @Contract(pure = true)
    @NotNull String getMessageId();


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
     *     if a parameter required by the message is missing, the behaviour depends on the message
     *     formatter implementation; it may choose a default or throw an exception
     *   </li>
     * </ul>
     *
     * @return  unmodifyable map with parameter values, never {@code null}
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
     * @return  unmodifyable set containing all tag names, never {@code null}
     *
     * @since 0.7.0
     */
    @Contract(pure = true, value = "-> new")
    @NotNull Set<String> getTagNames();


    /**
     * Tells whether the message contains tag {@code tagName}.
     *
     * @param tagName  tag name to check for, not {@code null}
     *
     * @return  {@code true} if this message contains tag {@code tagName}, {@code false} otherwise
     */
    @Contract(pure = true)
    default boolean hasTag(@NotNull String tagName) {
      return getTagNames().contains(tagName);
    }
  }




  /**
   * A protocol group with optional group header message.
   *
   * @param <M>  internal message object type
   */
  interface Group<M>
  {
    /**
     * Returns the unique name for this group. The name can be used to find a group from a parent
     * protocol instance.
     *
     * @return  unique name for this group or {@code null} if no name is set.
     *
     * @see Protocol#getGroupByName(String)
     * @see Protocol#forEachGroupByRegex(String, Consumer)
     */
    @Contract(pure = true)
    String getName();


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
