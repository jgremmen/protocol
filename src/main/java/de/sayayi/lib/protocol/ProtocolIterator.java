/**
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

import de.sayayi.lib.protocol.ProtocolEntry.Group;
import de.sayayi.lib.protocol.ProtocolEntry.Message;
import de.sayayi.lib.protocol.ProtocolIterator.DepthEntry;

import java.util.Iterator;


/**
 * @author Jeroen Gremmen
 */
public interface ProtocolIterator<M> extends Iterator<DepthEntry>
{
  Level getLevel();

  Tag getTag();


  interface DepthEntry
  {
    /**
     * <p>
     *   Returns the depth for this entry.
     * </p>
     * <p>
     *
     * </p>
     *
     * @return  entry depth
     */
    int getDepth();

    boolean isFirst();

    boolean isLast();
  }


  interface MessageEntry<M> extends DepthEntry, Message<M> {
  }


  interface GroupEntry<M> extends DepthEntry, Group<M>
  {
    boolean hasEntryAfterGroup();
  }
}
