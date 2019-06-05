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

import java.util.Map;
import java.util.Set;


/**
 * @author Jeroen Gremmen
 */
public interface ProtocolEntry<M> extends ProtocolQuery<M>
{
  interface BasicMessage<M> extends ProtocolEntry<M>
  {
    M getMessage();


    Map<String,Object> getParameterValues();
  }


  interface Message<M> extends BasicMessage<M>
  {
    Level getLevel();


    Set<Tag> getTags();


    /**
     * Returns the throwable associated with the message.
     *
     * @return  throwable/exception or {@code null}
     */
    Throwable getThrowable();
  }


  interface Group<M> extends ProtocolEntry<M>
  {
    BasicMessage<M> getGroupMessage();
  }
}
