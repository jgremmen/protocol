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
package de.sayayi.lib.protocol.formatter;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.Protocol.GenericMessage;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolFormatter.ConfiguredProtocolFormatter;
import de.sayayi.lib.protocol.TagSelector;

import org.jetbrains.annotations.NotNull;

import static de.sayayi.lib.protocol.Level.Shared.LOWEST;


/**
 * @author Jeroen Gremmen
 */
@SuppressWarnings("unused")
public class TechnicalProtocolFormatter<M> extends TreeProtocolFormatter<M>
    implements ConfiguredProtocolFormatter<M,String>
{
  @SuppressWarnings("WeakerAccess")
  public TechnicalProtocolFormatter() {
    setMessageFormatter(new MessageFormatter<M>() {
      @Override
      public String format(@NotNull GenericMessage<M> message) {
        return message.toString();
      }
    });
  }


  @Override
  public @NotNull Level getLevel() {
    return LOWEST;
  }


  @Override
  public @NotNull
  TagSelector getTagSelector(@NotNull ProtocolFactory<M> protocolFactory) {
    return protocolFactory.getDefaultTag().asSelector();
  }


  public static @NotNull <M> String format(@NotNull Protocol<M> protocol) {
    return protocol.format(new TechnicalProtocolFormatter<M>());
  }
}
