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
 * A {@link Spliterator} adapter over a {@link DepthEntry} iterator, enabling stream-based traversal of protocol
 * entries. This spliterator is sequential (non-splittable), ordered, and does not report a size as calculating it
 * would be too expensive.
 *
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 1.1.0
 */
final class ProtocolSpliterator<M> implements Spliterator<DepthEntry<M>>
{
  private final @NotNull Iterator<DepthEntry<M>> iterator;


  /**
   * Creates a spliterator backed by the given depth entry iterator.
   *
   * @param iterator  protocol iterator to wrap, not {@code null}
   */
  ProtocolSpliterator(@NotNull Iterator<DepthEntry<M>> iterator) {
    this.iterator = iterator;
  }


  /**
   * {@inheritDoc}
   * <p>
   * Advances the underlying iterator by one entry and passes it to the given action.
   */
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


  /**
   * {@inheritDoc}
   * <p>
   * Consumes all remaining entries from the underlying iterator.
   */
  @Override
  public void forEachRemaining(@NotNull Consumer<? super DepthEntry<M>> action) {
    iterator.forEachRemaining(action);
  }


  /**
   * {@inheritDoc}
   * <p>
   * Always returns {@code null} as protocol iteration is inherently sequential and cannot be split.
   */
  @Override
  public Spliterator<DepthEntry<M>> trySplit() {
    return null;
  }


  /**
   * {@inheritDoc}
   * <p>
   * Returns {@link Long#MAX_VALUE} as calculating the exact number of entries would be too expensive.
   */
  @Override
  public long estimateSize() {
    return MAX_VALUE;
  }


  /**
   * {@inheritDoc}
   * <p>
   * Returns {@code -1} as calculating the exact size would be too expensive.
   */
  @Override
  public long getExactSizeIfKnown() {
    return -1;
  }


  /**
   * {@inheritDoc}
   * <p>
   * Reports {@link #DISTINCT}, {@link #NONNULL}, {@link #ORDERED} and {@link #IMMUTABLE}.
   */
  @Override
  public int characteristics() {
    return DISTINCT | NONNULL | ORDERED | IMMUTABLE;
  }
}
