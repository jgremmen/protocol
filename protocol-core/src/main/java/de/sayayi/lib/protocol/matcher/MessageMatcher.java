/*
 * Copyright 2021 Jeroen Gremmen
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
package de.sayayi.lib.protocol.matcher;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolEntry.Message;
import de.sayayi.lib.protocol.TagSelector;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;


/**
 * @author Jeroen Gremmen
 * @since 1.0.0
 */
public interface MessageMatcher
{
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
   * Convert this message matcher into a tag only selector.
   *
   * @return  tag selector, never {@code null}
   *
   * @throws UnsupportedOperationException  in case this message matcher is not a tag only matcher
   *
   * @see #isTagSelector()
   *
   * @since 1.2.0
   */
  @Contract(pure = true)
  default @NotNull TagSelector asTagSelector()
  {
    if (!isTagSelector())
      throw new UnsupportedOperationException();

    return new TagSelector() {
      @Override
      public boolean match(@NotNull Collection<String> tagNames) {
        return matches(Level.Shared.HIGHEST, new TagSelectorMessageAdapter(tagNames));
      }


      @Override
      public String toString() {
        return MessageMatcher.this.toString();
      }
    };
  }


  /**
   * <p>
   *   Converts a message matcher to a matcher which implements the {@link Junction} interface.
   * </p>
   * <p>
   *   If this message matcher already implements the {@code Junction} interface, it will return
   *   same object. Otherwise it will wrap the matcher.
   * </p>
   *
   * @return  message matcher which implements {@code Junction} interface, never {@code null}
   */
  @Contract(pure = true)
  default @NotNull Junction asJunction() {
    return this instanceof Junction ? (Junction)this : new JunctionAdapter(this);
  }




  /**
   * This interface allows for message matchers to be combined (conjunction and disjunction).
   */
  interface Junction extends MessageMatcher
  {
    @Contract(pure = true)
    default @NotNull Junction and(@NotNull MessageMatcher other) {
      return Conjunction.of(this, other);
    }


    @Contract(pure = true)
    default @NotNull Junction or(@NotNull MessageMatcher other) {
      return Disjunction.of(this, other);
    }
  }
}