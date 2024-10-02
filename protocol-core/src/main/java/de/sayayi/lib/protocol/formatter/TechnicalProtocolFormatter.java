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
package de.sayayi.lib.protocol.formatter;

import de.sayayi.lib.protocol.Protocol.GenericMessageWithLevel;
import de.sayayi.lib.protocol.Protocol.Message;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolFormatter.ConfiguredProtocolFormatter;
import de.sayayi.lib.protocol.matcher.MessageMatcher;
import de.sayayi.lib.protocol.matcher.MessageMatchers;

import org.jetbrains.annotations.NotNull;


/**
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public final class TechnicalProtocolFormatter<M> extends AbstractTreeProtocolFormatter<M>
    implements ConfiguredProtocolFormatter<M,String>
{
  private static final ConfiguredProtocolFormatter<?,String> INSTANCE = new TechnicalProtocolFormatter<>();


  private TechnicalProtocolFormatter() {
  }


  @Override
  public @NotNull MessageMatcher getMatcher(@NotNull ProtocolFactory<M> protocolFactory) {
    return MessageMatchers.any();
  }


  @Override
  protected String format(@NotNull GenericMessageWithLevel<M> message)
  {
    final StringBuilder s = new StringBuilder(super.format(message)).append("  {level=")
        .append(message.getLevel());

    if (message instanceof Message)
    {
      s.append(",tags=").append(((Message<M>)message).getTagNames().toString()
          .replace(", ", ","));
    }

    return s.append('}').toString();
  }


  @SuppressWarnings("unchecked")
  public static @NotNull <M> ConfiguredProtocolFormatter<M,String> getInstance() {
    return (ConfiguredProtocolFormatter<M,String>)INSTANCE;
  }
}
