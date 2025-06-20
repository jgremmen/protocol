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
 * A tag selector filters protocol entries by tag name.
 *
 * @author Jeroen Gremmen
 * @since 0.6.0
 */
public interface TagSelector
{
  /**
   * Tells whether the selector matches for the provided {@code tagNames}.
   *
   * @param tagNames  a collection of tag names, not {@code null}
   *
   * @return  {@code true} if the selector matches for the collection provided,
   *          {@code false} otherwise
   */
  @Contract(pure = true)
  boolean match(@NotNull Iterable<String> tagNames);


  /**
   * Converts this tag selector into a message matcher.
   *
   * @return  message matcher instance, never {@code null}
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