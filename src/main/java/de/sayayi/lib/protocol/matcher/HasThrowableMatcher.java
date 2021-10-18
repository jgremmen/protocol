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
import de.sayayi.lib.protocol.matcher.MessageMatcher.Junction;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static lombok.AccessLevel.PRIVATE;


/**
 * @author Jeroen Gremmen
 * @since 1.0.0
 */
@RequiredArgsConstructor(access = PRIVATE)
@EqualsAndHashCode(callSuper = false)
final class HasThrowableMatcher implements Junction
{
  static final HasThrowableMatcher INSTANCE = new HasThrowableMatcher(Throwable.class);

  private final Class<? extends Throwable> type;


  @Override
  public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
    return type.isInstance(message.getThrowable());
  }


  @Override
  public String toString() {
    return "hasThrowable(" + (type == Throwable.class ? "" : type.getName()) + ')' ;
  }


  @Contract(pure = true)
  static Junction of(@NotNull Class<? extends Throwable> type) {
    return type == Throwable.class ? INSTANCE : new HasThrowableMatcher(type);
  }
}
