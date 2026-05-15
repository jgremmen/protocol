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
package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.ProtocolEntry.Message;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


/**
 * A tag selector determines whether a protocol message matches based on its set of associated
 * tag names. It is used in two main contexts:
 * <ul>
 *   <li>
 *     <b>Tag propagation</b> – as the source criterion in
 *     {@link Protocol#propagate(TagSelector)}, so that matching messages automatically receive
 *     an additional target tag.
 *   </li>
 *   <li>
 *     <b>Message filtering</b> – by converting a tag selector to a {@link de.sayayi.lib.protocol.matcher.MessageMatcher}
 *     via {@link #asMessageMatcher()} and passing it to formatting or querying methods.
 *   </li>
 * </ul>
 * Instances are typically created by parsing a tag selector expression via
 * {@link ProtocolMessageMatcher#parseTagSelector(String)}. Supported expression forms include
 * {@code any}, {@code none}, {@code tag(name)}, {@code any-of(...)}, {@code all-of(...)},
 * {@code none-of(...)}, as well as {@code and}, {@code or} and {@code not} combinators.
 *
 * @author Jeroen Gremmen
 * @since 0.6.0
 *
 * @see ProtocolMessageMatcher#parseTagSelector(String)
 * @see Protocol#propagate(TagSelector)
 */
public interface TagSelector
{
  /**
   * Tells whether this selector matches the given set of tag names.
   *
   * @param tagNames  the tag names associated with the message being tested, not {@code null}
   *
   * @return  {@code true} if the selector matches for the collection provided,
   *          {@code false} otherwise
   */
  @Contract(pure = true)
  boolean match(@NotNull Iterable<String> tagNames);


  /**
   * Returns a {@link de.sayayi.lib.protocol.matcher.MessageMatcher} that matches any message
   * whose tag set is accepted by this tag selector.
   * <p>
   * This allows a tag selector to be used wherever a {@code MessageMatcher} is required, such
   * as in {@link Protocol#format(ProtocolFormatter, de.sayayi.lib.protocol.matcher.MessageMatcher)
   * Protocol#format} or {@link Protocol#iterator(de.sayayi.lib.protocol.matcher.MessageMatcher)
   * Protocol#iterator}.
   *
   * @return  message matcher backed by this tag selector, never {@code null}
   *
   * @since 1.2.1
   */
  @Contract(pure = true)
  default @NotNull MessageMatcher asMessageMatcher()
  {
    return new MessageMatcher.Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
        return TagSelector.this.match(message.getTagNames());
      }


      @Override
      public boolean isTagSelector() {
        return true;
      }


      @Override
      public @NotNull TagSelector asTagSelector() {
        return TagSelector.this;
      }


      @Override
      public String toString() {
        return TagSelector.this.toString();
      }
    };
  }
}