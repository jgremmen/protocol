/*
 * Copyright 2020 Jeroen Gremmen
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
package de.sayayi.lib.protocol.processor;

import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor;
import de.sayayi.lib.protocol.exception.ProtocolException;

import lombok.AllArgsConstructor;
import lombok.val;

import org.jetbrains.annotations.NotNull;

import java.util.Properties;


/**
 * @author Jeroen Gremmen
 */
@AllArgsConstructor
public class PropertiesMessageProcessor implements MessageProcessor<String>
{
  private final Properties properties;


  @Override
  public @NotNull String processMessage(@NotNull String key)
  {
    val message = properties.getProperty(key);
    if (message == null)
      throw new ProtocolException("missing property message for key '" + key + "'");

    return message;
  }
}
