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

import java.util.Map;
import java.util.Set;


/**
 * @author Jeroen Gremmen
 */
public interface ProtocolEntry<M> extends ProtocolQuery<M>
{
  /**
   * The simplest representation of a formattable protocol message.
   *
   * @param <M>  Internal message object type.
   */
  interface FormattableMessage<M> extends ProtocolEntry<M>
  {
    /**
     * Returns the internal representation of the message.
     *
     * @return  message, never {@code null}
     */
    M getMessage();


    /**
     * Returns a map with parameter names and values to be used for formatting the message.
     * <ul>
     *   <li>a parameter name (map key) must have a length of at least 1</li>
     *   <li>additional parameters are allowed to those which are required to format the message</li>
     *   <li>message formatters cannot rely on the order of the parameters</li>
     *   <li>
     *     if a parameter required by the message is missing, the behaviour depends on the message formatter
     *     implementation; it may choose a default or throw an exception
     *   </li>
     * </ul>
     *
     * @return  parameter values, never {@code null}
     *
     * @see #getMessage()
     */
    Map<String,Object> getParameterValues();
  }


  interface Message<M> extends FormattableMessage<M>
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
    /**
     * Returns the group message.
     *
     * @return  group message or {@code null}
     */
    FormattableMessage<M> getGroupMessage();
  }
}
