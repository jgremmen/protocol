/*
 * Copyright 2020 Jeroen Gremmen
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
package de.sayayi.lib.protocol.message.processor;

import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor;
import de.sayayi.lib.protocol.exception.ProtocolException;
import de.sayayi.lib.protocol.spi.GenericMessageWithId;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.TreeMap;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;


/**
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.7.0
 */
public final class MapMessageProcessor<M> implements MessageProcessor<M>
{
  private final Map<String,M> map;


  public MapMessageProcessor(@NotNull Map<String,M> map) {
    this.map = unmodifiableMap(new TreeMap<>(map));
  }


  @Override
  public @NotNull MessageWithId<M> processMessage(@NotNull String key)
  {
    final M message = map.get(requireNonNull(key, "key must not be null"));
    if (message == null)
      throw new ProtocolException("missing mapped message for key '" + key + "'");

    return new GenericMessageWithId<>(key, message);
  }
}
