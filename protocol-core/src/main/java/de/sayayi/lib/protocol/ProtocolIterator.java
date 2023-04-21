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

import de.sayayi.lib.protocol.Protocol.GenericMessageWithLevel;
import de.sayayi.lib.protocol.ProtocolGroup.Visibility;
import de.sayayi.lib.protocol.ProtocolIterator.DepthEntry;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;


/**
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
public interface ProtocolIterator<M> extends Iterator<DepthEntry<M>>
{
  /**
   * <p>
   *   This class is the basis for every entry produced by the protocol iterator. It provides the
   *   group depth (starting with {@code 0}) for each entry. Every {@link GroupStartEntry}
   *   increases the depth and every {@link GroupEndEntry} decreases the depth. This information
   *   can be used by {@linkplain ProtocolFormatter ProtocolFormatters} to format the protocol in
   *   a structural form (eg. a tree).
   * </p>
   *
   * @param <M>  internal message object type
   */
  @SuppressWarnings({"unused", "squid:S2326"})
  interface DepthEntry<M>
  {
    /**
     * <p>
     *   Returns the depth for this entry.
     * </p>
     * <p>
     *   The first entry (message or group entry) returned by a protocol iterator starts at depth
     *   0. For each group with group message, the messages belonging to that group have an
     *   incremented depth:
     * </p>
     * <ul>
     *   <li>Message 1 (depth = 0)</li>
     *   <li>
     *     Group message (depth = 1)
     *     <ul>
     *       <li>Message 2 (depth = 1)</li>
     *       <li>Message 3 (depth = 1)</li>
     *     </ul>
     *   </li>
     *   <li>Message 4 (depth = 0)</li>
     * </ul>
     *
     * @return  entry depth &gt;= 0
     */
    @Contract(pure = true)
    int getDepth();
  }




  /**
   * <p>
   *   In addition to {@link DepthEntry} this type provides information about the position, with
   *   respect to its depth, it is listed in.
   * </p>
   *
   * @param <M>  internal message object type
   */
  interface BoundedDepthEntry<M> extends DepthEntry<M>
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




  /**
   * This entry marks the start of a protocol iteration. It is generated unconditionally.
   *
   * @param <M>  internal message object type
   */
  interface ProtocolStart<M> extends DepthEntry<M> {
  }




  /**
   * This entry marks the end of a protocol iteration. It is generated unconditionally.
   *
   * @param <M>  internal message object type
   */
  interface ProtocolEnd<M> extends DepthEntry<M> {
  }




  /**
   * Message entry.
   *
   * @param <M>  internal message object type
   */
  interface MessageEntry<M> extends BoundedDepthEntry<M>, Protocol.Message<M>
  {
    /**
     * Tells if this message is a group header message.
     *
     * @return  {@code true} if this is a group header message, {@code false} otherwise
     *
     * @see GroupMessageEntry
     */
    @Contract(pure = true)
    boolean isGroupMessage();
  }




  /**
   * Group message entry. This entry is generated for groups which have no visible entries
   * themselves but have a visible group header message. Eg. for visibility
   * {@link Visibility#SHOW_HEADER_ONLY}.
   *
   * @param <M>  internal message object type
   *
   * @see ProtocolGroup#setVisibility(Visibility)
   * @see GroupStartEntry
   */
  interface GroupMessageEntry<M> extends MessageEntry<M>
  {
    /**
     * Returns the unique name for this group.
     *
     * @return  unique name for this group or {@code null} if no name is set.
     */
    @Contract(pure = true)
    String getName();


    /**
     * {@inheritDoc}
     *
     * @return  always {@code null}
     */
    @Override
    @Contract(value = "-> null", pure = true)
    default Throwable getThrowable() {
      return null;
    }


    /**
     * {@inheritDoc}
     *
     * @return  always {@code true}
     */
    @Override
    @Contract(value = "-> true", pure = true)
    default boolean isGroupMessage() {
      return true;
    }
  }




  /**
   * <p>
   *   Marks the beginning of a protocol group.
   * </p>
   * <p>
   *   This entry is generated only if the protocol group has a visible group header message and
   *   at least 1 containing visible entry.
   * </p>
   *
   * @param <M>  internal message object type
   *
   * @see GroupMessageEntry
   * @see GroupEndEntry
   */
  interface GroupStartEntry<M> extends BoundedDepthEntry<M>, Protocol.Group<M>
  {
    /**
     * {@inheritDoc}
     *
     * @return  group header message, never {@code null}
     */
    @Override
    @Contract(pure = true)
    @NotNull GenericMessageWithLevel<M> getGroupMessage();


    /**
     * <p>
     *   Returns the number of visible messages in this group. Only messages with the same depth
     *   are counted.
     * </p>
     *
     * @return  number of messages in the group (at least 1)
     *
     * @see #getDepth()
     */
    @Contract(pure = true)
    int getMessageCount();
  }




  /**
   * <p>
   *   Marks the end of a protocol group.
   * </p>
   *
   * @param <M>  internal message object type
   *
   * @see GroupStartEntry
   */
  interface GroupEndEntry<M> extends DepthEntry<M> {
  }
}
