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

import de.sayayi.lib.protocol.ProtocolIterator.DepthEntry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;


/**
 * @author Jeroen Gremmen
 */
public interface ProtocolIterator<M> extends Iterator<DepthEntry<M>>
{
  /**
   * Returns the level used for iteration.
   *
   * @return  iteration level, never {@code null}
   */
  @Contract(pure = true)
  @NotNull Level getLevel();


  /**
   * Returns the tag used for iteration.
   *
   * @return  iteration tag, never {@code null}
   */
  @Contract(pure = true)
  @NotNull Tag getTag();


  @SuppressWarnings("unused")
  interface DepthEntry<M>
  {
    /**
     * <p>
     *   Returns the depth for this entry.
     * </p>
     * <p>
     *   The first entry (message or group entry) returned by a protocol iterator starts at depth 0. For each group with
     *   group message, the messages belonging to that group have an incremented depth:
     * </p>
     * <ul>
     *   <li>Message 1 (depth = 0)</li>
     *   <li>
     *     Group message (depth = 0)
     *     <ul>
     *       <li>Message 2 (depth = 1)</li>
     *       <li>Message 3 (depth = 1)</li>
     *     </ul>
     *   </li>
     *   <li>Message 4 (depth = 0)</li>
     * </ul>
     *
     * @return  entry depth
     */
    @Contract(pure = true)
    int getDepth();
  }


  interface VisibleDepthEntry<M> extends DepthEntry<M>
  {
    /**
     * Tells if this is the first entry with respect to its depth.
     *
     * @return  {@code true} if this is the first entry, {@code false} otherwise.
     *
     * @see #getDepth()
     * @see #isLast()
     */
    @Contract(pure = true)
    boolean isFirst();


    /**
     * Tells if this is the last entry with respect to its depth.
     *
     * @return  {@code true} if this is the last entry, {@code false} otherwise.
     *
     * @see #getDepth()
     * @see #isFirst()
     */
    @Contract(pure = true)
    boolean isLast();
  }


  interface MessageEntry<M> extends VisibleDepthEntry<M>, Protocol.Message<M> {
  }


  interface GroupMessageEntry<M> extends MessageEntry<M>
  {
    /**
     * {@inheritDoc}
     *
     * @return  always {@code null}
     */
    @Override
    Throwable getThrowable();
  }


  interface GroupEntry<M> extends VisibleDepthEntry<M>, Protocol.Group<M>
  {
    /**
     * {@inheritDoc}
     *
     * @return  group header message, never {@code null}
     */
    @Contract(pure = true)
    @Override
    @NotNull Protocol.Message<M> getGroupHeader();


    /**
     * <p>
     *   Returns the number of visible messages in this group.
     * </p>
     * <p>
     *   Only messages with the same depth are counted. So if this group has a depth of {@code 2}, this method
     *   returns the number of messages with depth {@code 3}.
     * </p>
     *
     * @return  number of messages in the group
     *
     * @see #getDepth()
     */
    @Contract(pure = true)
    int getMessageCount();
  }
}
