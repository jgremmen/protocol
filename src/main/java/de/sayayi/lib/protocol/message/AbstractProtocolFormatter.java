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
package de.sayayi.lib.protocol.message;

import de.sayayi.lib.protocol.ProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolIterator;

import org.jetbrains.annotations.NotNull;


/**
 * @author Jeroen Gremmen
 * @since 0.3.0
 */
@Deprecated
public abstract class AbstractProtocolFormatter<M,R> implements ProtocolFormatter<M,R>
{
  @Override
  public void protocolStart() {
  }


  @Override
  public void protocolEnd() {
  }


  @Override
  public void groupStart(@NotNull ProtocolIterator.GroupStartEntry<M> group) {
  }


  @Override
  public void groupEnd(@NotNull ProtocolIterator.GroupEndEntry<M> groupEnd) {
  }
}
