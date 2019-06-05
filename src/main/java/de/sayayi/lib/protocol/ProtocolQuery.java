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

import java.util.List;


/**
 * @author Jeroen Gremmen
 */
public interface ProtocolQuery<M>
{
  /**
   * Tells if this protocol object matches the given {@code level} and {@code tag}.
   *
   * @param level  requested protocol level, not {@code null}
   * @param tag    tag to query, not {@code null}
   *
   * @return  {@code true} if the protocol object matches, {@code false} otherwise
   */
  boolean isMatch(Level level, Tag tag);


  /**
   * Returns a list of protocol entries provided by this protocol object for the given {@code level} and {@code tag}.
   *
   * @param level  requested protocol level, not {@code null}
   * @param tag    tag to query, not {@code null}
   *
   * @return  a list of protocol entries, never {@code null}
   */
  List<ProtocolEntry<M>> getEntries(Level level, Tag tag);


  boolean hasVisibleElement(Level level, Tag tag);
}
