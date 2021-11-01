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
import de.sayayi.lib.protocol.Protocol.Message;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


/**
 * @author Jeroen Gremmen
 * @since 1.0.0
 */
public interface MessageMatcher
{
  @Contract(pure = true)
  <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message);


  @Contract(pure = true)
  default @NotNull Junction asJunction()
  {
    if (this instanceof Junction)
      return (Junction)this;

    return new Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
        return MessageMatcher.this.matches(levelLimit, message);
      }
    };
  }




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
