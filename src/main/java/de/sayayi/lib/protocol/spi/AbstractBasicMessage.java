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
package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolEntry.BasicMessage;
import de.sayayi.lib.protocol.Tag;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Jeroen Gremmen
 */
abstract class AbstractBasicMessage<M> implements BasicMessage<M>
{
  final M message;
  final Map<String,Object> parameterValues;


  AbstractBasicMessage(M message, Map<String,Object> defaultParameterValues)
  {
    this.message = message;
    this.parameterValues = new HashMap<String,Object>(defaultParameterValues);
  }


  @Override
  public M getMessage() {
    return message;
  }


  @Override
  public Map<String,Object> getParameterValues() {
    return Collections.unmodifiableMap(parameterValues);
  }


  @Override
  public List<ProtocolEntry<M>> getEntries(Level level, Tag tag)
  {
    return isMatch(level, tag)
        ? Collections.<ProtocolEntry<M>>singletonList(this)
        : Collections.<ProtocolEntry<M>>emptyList();
  }
}
