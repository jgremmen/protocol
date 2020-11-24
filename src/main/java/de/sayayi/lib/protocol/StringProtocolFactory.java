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

import de.sayayi.lib.protocol.spi.GenericProtocolFactory;

import org.jetbrains.annotations.NotNull;


/**
 * <p>
 *   Generic protocol factory for text messages where the messages are stored internally as
 *   {@code String} objects.
 * </p>
 *
 * @author Jeroen Gremmen
 */
public class StringProtocolFactory extends GenericProtocolFactory<String>
{
  public StringProtocolFactory()
  {
    super(new MessageProcessor<String>() {
      @Override
      public @NotNull String processMessage(@NotNull String message) {
        return message;
      }
    });
  }
}
