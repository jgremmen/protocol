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
package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;

import org.jetbrains.annotations.NotNull;


/**
 * Generic abstract builder class with a reference to the protocol instance that created the builder.
 *
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 */
abstract class AbstractBuilder<M,B extends ProtocolMessageBuilder<M>>
{
  protected final AbstractProtocol<M,B> protocol;


  protected AbstractBuilder(@NotNull AbstractProtocol<M,B> protocol) {
    this.protocol = protocol;
  }
}
