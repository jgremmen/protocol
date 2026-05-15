/*
 * Copyright 2021 Jeroen Gremmen
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
package de.sayayi.lib.protocol.matcher;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolEntry.Message;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.exception.MessageMatcherException;
import de.sayayi.lib.protocol.matcher.internal.Conjunction;
import de.sayayi.lib.protocol.matcher.internal.Disjunction;
import de.sayayi.lib.protocol.matcher.internal.JunctionAdapter;
import de.sayayi.lib.protocol.matcher.internal.TagNamesMessageAdapter;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;


/**
 * Predicate-like contract for deciding whether a protocol message is included in querying,
 * iteration, or formatting operations.
 * <p>
 * Matchers can evaluate message level and message metadata, and may optionally represent a
 * pure tag-based selector that can be converted to a {@link TagSelector}. For fluent
 * composition using logical {@code and}/{@code or}, use {@link #asJunction()}.
 *
 * @author Jeroen Gremmen
 * @since 1.0.0
 *
 * @see de.sayayi.lib.protocol.Protocol#iterator(MessageMatcher)
 * @see de.sayayi.lib.protocol.Protocol#format(de.sayayi.lib.protocol.ProtocolFormatter, MessageMatcher)
 */
public interface MessageMatcher
{
  /**
   * Checks whether this matcher matches the given {@code message}.
   *
   * @param levelLimit  the maximum level to be considered when matching, not {@code null}. This
   *                    level takes precedence over the level provided by the message
   * @param message     message to check, not {@code null}
   *
   * @param <M>         internal message object type
   *
   * @return  {@code true} if the given message matches, {@code false} otherwise
   */
  @Contract(pure = true)
  <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message);


  /**
   * Tells whether this message matcher is a tag only selector.
   *
   * @return  {@code true} if this matcher is a tag selector, {@code false} otherwise
   *
   * @see #asTagSelector()
   *
   * @since 1.2.0
   */
  @Contract(pure = true)
  default boolean isTagSelector() {
    return false;
  }


  /**
   * Converts this message matcher into a tag-only selector.
   *
   * @return  tag selector, never {@code null}
   *
   * @throws MessageMatcherException  if this message matcher is not a pure tag selector
   *
   * @see #isTagSelector()
   *
   * @since 1.2.0
   */
  @Contract(pure = true)
  default @NotNull TagSelector asTagSelector()
  {
    if (!isTagSelector())
      throw new MessageMatcherException("matcher is not a pure tag selector");

    return new TagSelector() {
      @Override
      public boolean match(@NotNull Iterable<String> tagNames) {
        return matches(HIGHEST, new TagNamesMessageAdapter(tagNames));
      }


      @Override
      public @NotNull MessageMatcher asMessageMatcher() {
        return MessageMatcher.this;
      }


      @Override
      public String toString() {
        return MessageMatcher.this.toString();
      }
    };
  }


  /**
   * Converts this matcher to a matcher that implements {@link Junction}.
   * <p>
   * If this matcher already implements {@code Junction}, this method returns the same instance.
   * Otherwise, it returns an adapter that delegates to this matcher.
   *
   * @return  matcher implementing {@code Junction}, never {@code null}
   */
  @Contract(pure = true)
  default @NotNull Junction asJunction() {
    return this instanceof Junction ? (Junction)this : new JunctionAdapter(this);
  }




  /**
   * Specialised matcher type that supports fluent logical composition.
   * <p>
   * A junction can be combined with other matchers using conjunction ({@link #and(MessageMatcher)})
   * and disjunction ({@link #or(MessageMatcher)}).
   */
  interface Junction extends MessageMatcher
  {
    /** {@inheritDoc} */
    @Override
    default @NotNull Junction asJunction() {
      return this;
    }


    /**
     * Creates a matcher that matches when both this matcher and {@code other} match.
     *
     * @param other  matcher to combine with, not {@code null}
     *
     * @return  conjunction matcher, never {@code null}
     */
    @Contract(pure = true)
    default @NotNull Junction and(@NotNull MessageMatcher other) {
      return Conjunction.of(this, other);
    }


    /**
     * Creates a matcher that matches when either this matcher or {@code other} matches.
     *
     * @param other  matcher to combine with, not {@code null}
     *
     * @return  disjunction matcher, never {@code null}
     */
    @Contract(pure = true)
    default @NotNull Junction or(@NotNull MessageMatcher other) {
      return Disjunction.of(this, other);
    }
  }
}
