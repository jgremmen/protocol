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

import de.sayayi.lib.protocol.ProtocolIterator.GroupEntry;
import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


/**
 * @param <M>  internal message type
 * @param <R>  formatting result type
 *
 * @author Jeroen Gremmen
 */
public interface ProtocolFormatter<M,R>
{
  void protocolStart();


  void protocolEnd();


  void message(@NotNull MessageEntry<M> message);


  void groupStart(@NotNull GroupEntry<M> group);


  void groupEnd();


  /**
   * Returns the formatted result.
   *
   * @return  formatted result
   */
  @Contract(pure = true)
  R getResult();


  /**
   * <p>
   *   A formatter of this type is automatically initialized by {@link Protocol#format(Level, Tag, ProtocolFormatter)}.
   * </p>
   * <p>
   *   Implementing classes must make sure, that the formatter is reusable after invoking
   *   {@link #init(Level, Tag, int)}. Thread safety however is not a requirement.
   * </p>
   *
   * @param <M>  internal message type
   * @param <R>  formatting result type
   */
  interface InitializableProtocolFormatter<M,R> extends ProtocolFormatter<M,R>
  {
    /**
     * This method is invoked before any other formatting methods are invoked and must initialize the formatter in
     * such way that it can be reused.
     *
     * @param level  matching protocol level
     * @param tag  matching protocol tag
     * @param estimatedGroupDepth  the estimated depth of nested protocol groups ({@code 0} means the protocol contains
     *                             no groups). The real depth depends on {@code level}, {@code tag} and group visibility
     *                             settings but is never greater than the estimated depth.
     *
     * @see Protocol#format(Level, Tag, ProtocolFormatter)
     */
    void init(@NotNull Level level, @NotNull Tag tag, int estimatedGroupDepth);
  }


  /**
   *
   * @param <M>  internal message type
   * @param <R>  formatting result type
   */
  interface ConfiguredProtocolFormatter<M,R> extends ProtocolFormatter<M,R>
  {
    @Contract(pure = true)
    @NotNull Level getLevel();


    @Contract(pure = true)
    @NotNull Tag getTag();
  }
}
