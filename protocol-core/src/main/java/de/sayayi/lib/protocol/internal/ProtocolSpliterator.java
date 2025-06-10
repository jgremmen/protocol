/*
 * Copyright 2022 Jeroen Gremmen
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
package de.sayayi.lib.protocol.internal;

import de.sayayi.lib.protocol.ProtocolIterator.DepthEntry;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.lang.Long.MAX_VALUE;


/**
 * Spliterator implementation
 *
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 *
 * @since 1.1.0
 */
final class ProtocolSpliterator<M> implements Spliterator<DepthEntry<M>>
{
  private final @NotNull Iterator<DepthEntry<M>> iterator;


  ProtocolSpliterator(@NotNull Iterator<DepthEntry<M>> iterator) {
    this.iterator = iterator;
  }


  @Override
  public boolean tryAdvance(@NotNull Consumer<? super DepthEntry<M>> action)
  {
    if (iterator.hasNext())
    {
      action.accept(iterator.next());
      return true;
    }

    return false;
  }


  @Override
  public void forEachRemaining(@NotNull Consumer<? super DepthEntry<M>> action) {
    iterator.forEachRemaining(action);
  }


  @Override
  public Spliterator<DepthEntry<M>> trySplit() {
    return null;
  }


  @Override
  public long estimateSize() {
    return MAX_VALUE;
  }


  @Override
  public long getExactSizeIfKnown() {
    return -1;
  }


  @Override
  public int characteristics() {
    return DISTINCT | NONNULL | ORDERED | IMMUTABLE;
  }
}
