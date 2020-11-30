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
package de.sayayi.lib.protocol.message.structure;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolFormatter.ConfiguredProtocolFormatter;
import de.sayayi.lib.protocol.TagSelector;

import lombok.AllArgsConstructor;

import org.jetbrains.annotations.NotNull;

import static de.sayayi.lib.protocol.Level.Shared.LOWEST;
import static lombok.AccessLevel.PRIVATE;


/**
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
@SuppressWarnings("unused")
@AllArgsConstructor(access = PRIVATE)
public final class TechnicalProtocolFormatter<M> extends AbstractTreeProtocolFormatter<M>
    implements ConfiguredProtocolFormatter<M,String>
{
  private static final ConfiguredProtocolFormatter<?,String> INSTANCE = new TechnicalProtocolFormatter<Object>();


  @Override
  public @NotNull Level getLevel() {
    return LOWEST;
  }


  @Override
  public @NotNull TagSelector getTagSelector(@NotNull ProtocolFactory<M> protocolFactory) {
    return protocolFactory.getDefaultTag().asSelector();
  }


  @SuppressWarnings("unchecked")
  public static @NotNull <M> ConfiguredProtocolFormatter<M,String> getInstance() {
    return (ConfiguredProtocolFormatter<M,String>)INSTANCE;
  }
}
