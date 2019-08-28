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
package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Protocol.GenericMessage;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * {@inheritDoc}
 *
 * @author Jeroen Gremmen
 */
abstract class AbstractGenericMessage<M> implements GenericMessage<M>
{
  @Getter final M message;
  final Map<String,Object> parameterValues;


  AbstractGenericMessage(@NotNull M message, @NotNull Map<String,Object> defaultParameterValues)
  {
    this.message = message;
    this.parameterValues = new HashMap<String,Object>(defaultParameterValues);
  }


  @Override
  public @NotNull Map<String,Object> getParameterValues() {
    return Collections.unmodifiableMap(parameterValues);
  }
}
