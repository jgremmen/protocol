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
package de.sayayi.lib.protocol.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;
import static java.util.Collections.emptyIterator;
import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SORTED;


/**
 * Lightweight parameter map optimized for protocol message parameters.
 * <p>
 * The map stores parameter names in sorted order and supports optional parent chaining.
 * Lookups first inspect the current map and then continue with the parent map, while
 * iteration returns a merged view where entries in this map override equally named entries
 * from the parent.
 * <p>
 * This type is mutable and not thread-safe.
 *
 * @author Jeroen Gremmen
 * @since 1.0.0  (refactored in 1.6.0)
 */
public final class ParameterMap implements Iterable<Entry<String,Object>>
{
  private final ParameterMap parent;

  private ParameterEntry[] entries;
  private int size;
  private int modCount;


  /**
   * Creates an empty parameter map without a parent.
   */
  public ParameterMap() {
    this(null);
  }


  /**
   * Creates an empty parameter map with an optional parent map.
   *
   * @param parent  parent parameter map or {@code null}
   */
  public ParameterMap(@Nullable ParameterMap parent)
  {
    this.parent = parent;

    entries = null;
    size = 0;
    modCount = 0;
  }


  /**
   * Adds or replaces a parameter value in this map.
   * <p>
   * If the parameter exists in this map, only the local value is updated.
   * Parent entries are not modified.
   *
   * @param parameter  parameter name, not {@code null} or empty
   * @param value      parameter value, may be {@code null}
   *
   * @throws NullPointerException      if {@code parameter} is {@code null}
   * @throws IllegalArgumentException  if {@code parameter} is empty
   */
  @Contract(mutates = "this")
  public void put(@NotNull String parameter, Object value)
  {
    if (requireNonNull(parameter, "parameter must not be null").isEmpty())
      throw new IllegalArgumentException("parameter must not be empty");

    if (size == 0)
      entries = new ParameterEntry[] { new ParameterEntry(parameter, value), null };
    else
    {
      int low = 0;
      int high = size - 1;

      while(low <= high)
      {
        final int mid = (low + high) >>> 1;
        final var entry = entries[mid];
        final int cmp = entry.key.compareTo(parameter);

        if (cmp < 0)
          low = mid + 1;
        else if (cmp > 0)
          high = mid - 1;
        else
        {
          if (!Objects.equals(entry.value, value))
          {
            modCount++;
            entry.value = value;
          }

          return;
        }
      }

      if (entries.length == size)
        entries = copyOf(entries, size + 2);

      arraycopy(entries, low, entries, low + 1, size - low);
      entries[low] = new ParameterEntry(parameter, value);
    }

    size++;
    modCount++;
  }


  /**
   * Checks whether a parameter is present in this map or any parent map.
   *
   * @param parameter  parameter name, not {@code null}
   *
   * @return  {@code true} if the parameter exists, {@code false} otherwise
   */
  @Contract(pure = true)
  public boolean has(@NotNull String parameter) {
    return getEntry(requireNonNull(parameter, "parameter must not be null")) != null;
  }


  /**
   * Returns a parameter value from this map or any parent map.
   *
   * @param parameter  parameter name, not {@code null}
   *
   * @return  parameter value, or {@code null} if the parameter is not present
   */
  @Contract(pure = true)
  public Object get(@NotNull String parameter)
  {
    final var entry = getEntry(requireNonNull(parameter, "parameter must not be null"));

    return entry == null ? null : entry.value;
  }


  /**
   * Returns an iterator over merged parameters from this map and its parent chain.
   * <p>
   * Parameters are ordered by key and local entries override parent entries with the same key.
   *
   * @return  iterator over parameter entries, never {@code null}
   */
  @Contract(value = "-> new", pure = true)
  public @NotNull Iterator<Entry<String,Object>> iterator() {
    return new ParameterIterator();
  }


  /**
   * Returns a spliterator for the merged parameter entries.
   *
   * @return  spliterator over parameter entries, never {@code null}
   */
  @Override
  public @NotNull Spliterator<Entry<String,Object>> spliterator() {
    return Spliterators.spliterator(iterator(), size(), ORDERED | SORTED | DISTINCT | NONNULL);
  }


  /**
   * Returns a sequential stream over the merged parameter entries.
   *
   * @return  stream of parameter entries, never {@code null}
   */
  @Contract(value = "-> new", pure = true)
  public @NotNull Stream<Entry<String,Object>> stream() {
    return StreamSupport.stream(spliterator(), false);
  }


  /**
   * Resolves a parameter entry from this map or any parent map.
   *
   * @param parameter  parameter name, not {@code null}
   *
   * @return  matching entry or {@code null}
   */
  @Contract(pure = true)
  private ParameterEntry getEntry(@NotNull String parameter)
  {
    for(int low = 0, high = size - 1; low <= high;)
    {
      final int mid = (low + high) >>> 1;
      final var entry = entries[mid];
      final int cmp = entry.key.compareTo(parameter);

      if (cmp < 0)
        low = mid + 1;
      else if (cmp > 0)
        high = mid - 1;
      else
        return entry;
    }

    return parent == null ? null : parent.getEntry(parameter);
  }


  /**
   * Returns the number of visible parameters in the merged view.
   * <p>
   * Parameters shadowed by local entries are counted once.
   *
   * @return  number of merged parameters
   */
  @Contract(pure = true)
  public int size()
  {
    var n = 0;

    for(var iterator = iterator(); iterator.hasNext(); iterator.next())
      n++;

    return n;
  }


  /**
   * Checks whether this map and its parent chain contain no parameters.
   *
   * @return  {@code true} if there are no visible parameters, {@code false} otherwise
   */
  @Contract(pure = true)
  public boolean isEmpty() {
    return size == 0 && (parent == null || parent.isEmpty());
  }


  /**
   * Returns an unmodifiable {@link Map} view of the merged parameters.
   *
   * @return  unmodifiable map view, never {@code null}
   */
  @Contract(value = "-> new", pure = true)
  @UnmodifiableView
  public @NotNull Map<String,Object> unmodifyableMap() {
    return new UnmodifyableMap(this);
  }


  /**
   * Returns a string representation of the merged parameters.
   *
   * @return  string representation in bracket notation
   */
  @Override
  public String toString()
  {
    var iterator = iterator();
    if (!iterator.hasNext())
      return "[]";

    final var s = new StringJoiner(",", "[", "]");

    iterator.forEachRemaining(e -> s.add(e.toString()));

    return s.toString();
  }




  /**
   * Unmodifiable {@link Map} view backed by a {@link ParameterMap}.
   */
  private static final class UnmodifyableMap implements Map<String,Object>
  {
    private final @NotNull ParameterMap map;

    private Set<String> keySet;
    private Set<Entry<String,Object>> entrySet;
    private Collection<Object> valueCollection;


    /**
     * Creates a new unmodifiable map view.
     *
     * @param map  backing parameter map, not {@code null}
     */
    private UnmodifyableMap(@NotNull ParameterMap map) {
      this.map = map;
    }


    /** {@inheritDoc} */
    @Override
    public int size() {
      return map.size();
    }


    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
      return map.isEmpty();
    }


    /** {@inheritDoc} */
    @Override
    public boolean containsKey(Object key) {
      return map.has((String)key);
    }


    /** {@inheritDoc} */
    @Override
    public boolean containsValue(Object value) {
      return values().contains(value);
    }


    /** {@inheritDoc} */
    @Override
    public Object get(Object key) {
      return map.get((String)key);
    }


    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public @Nullable Object put(String key, Object value) {
      throw new UnsupportedOperationException("put");
    }


    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public Object remove(Object key) {
      throw new UnsupportedOperationException("remove");
    }


    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void putAll(@NotNull Map<? extends String,?> m) {
      throw new UnsupportedOperationException("putAll");
    }


    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void clear() {
      throw new UnsupportedOperationException("clear");
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull Set<String> keySet()
    {
      if (keySet == null)
        keySet = new UnmodifyableKeySet(map);

      return keySet;
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull Collection<Object> values()
    {
      if (valueCollection == null)
        valueCollection = new UnmodifyableValueCollection(map);

      return valueCollection;
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull Set<Entry<String,Object>> entrySet()
    {
      if (entrySet == null)
        entrySet = new UnmodifyableEntrySet(map);

      return entrySet;
    }
  }




  /**
   * Base class for the unmodifiable collection views backed by a {@link ParameterMap}.
   *
   * @param <T>  collection element type
   */
  private abstract static class AbstractUnmodifyableCollection<T> implements Collection<T>
  {
    protected final @NotNull ParameterMap map;


    /**
     * Creates a new collection view.
     *
     * @param map  backing parameter map, not {@code null}
     */
    protected AbstractUnmodifyableCollection(@NotNull ParameterMap map) {
      this.map = map;
    }


    /** {@inheritDoc} */
    @Override
    public int size() {
      return map.size();
    }


    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
      return map.isEmpty();
    }


    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean add(T e) {
      throw new UnsupportedOperationException("add");
    }


    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean remove(Object o) {
      throw new UnsupportedOperationException("remove");
    }


    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
      throw new UnsupportedOperationException("addAll");
    }


    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void clear() {
      throw new UnsupportedOperationException("clear");
    }


    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
      throw new UnsupportedOperationException("removeAll");
    }


    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean removeIf(@NotNull Predicate<? super T> filter) {
      throw new UnsupportedOperationException("removeIf");
    }


    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
      throw new UnsupportedOperationException("retainAll");
    }


    /** {@inheritDoc} */
    public boolean containsAll(Collection<?> c)
    {
      for(var e: c)
        if (!contains(e))
          return false;

      return true;
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o)
    {
      return this == o || o instanceof AbstractUnmodifyableCollection &&
                          map.equals(((AbstractUnmodifyableCollection<?>)o).map);
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
      return map.hashCode();
    }


    /** {@inheritDoc} */
    public String toString()
    {
      var iterator = this.iterator();
      if (!iterator.hasNext())
        return "[]";

      final var s = new StringJoiner(", ", "[", "]");

      iterator.forEachRemaining(e -> s.add(e.toString()));

      return s.toString();
    }
  }




  /**
   * Unmodifiable key set view backed by a {@link ParameterMap}.
   */
  private static final class UnmodifyableKeySet
      extends AbstractUnmodifyableCollection<String>
      implements Set<String>
  {
    /**
     * Creates a new key set view.
     *
     * @param map  backing parameter map, not {@code null}
     */
    private UnmodifyableKeySet(@NotNull ParameterMap map) {
      super(map);
    }


    /** {@inheritDoc} */
    @Override
    public boolean contains(Object o) {
      return o instanceof String && map.has((String)o);
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull Iterator<String> iterator()
    {
      final var iterator = map.iterator();

      return new Iterator<>() {
        @Override
        public boolean hasNext() {
          return iterator.hasNext();
        }


        @Override
        public String next() {
          return iterator.next().getKey();
        }
      };
    }


    /** {@inheritDoc} */
    @Override
    public Object @NotNull [] toArray() {
      return map.stream().map(Entry::getKey).toArray();
    }


    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T @NotNull [] toArray(T @NotNull [] a)
    {
      final var size = map.size();

      if (a.length < size)
        a = (T[])Array.newInstance(a.getClass().getComponentType(), size);
      else if (a.length > size)
        a[size] = null;

      var n = 0;
      for(var entry: map)
        a[n++] = (T)entry.getKey();

      return a;
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull Spliterator<String> spliterator() {
      return Spliterators.spliterator(iterator(), super.size(), ORDERED | SORTED | DISTINCT | NONNULL);
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
      return super.equals(o) && o instanceof UnmodifyableKeySet;
    }
  }




  /**
   * Unmodifiable entry set view backed by a {@link ParameterMap}.
   */
  private static final class UnmodifyableEntrySet
      extends AbstractUnmodifyableCollection<Entry<String,Object>>
      implements Set<Entry<String,Object>>
  {
    /**
     * Creates a new entry set view.
     *
     * @param map  backing parameter map, not {@code null}
     */
    private UnmodifyableEntrySet(@NotNull ParameterMap map) {
      super(map);
    }


    /** {@inheritDoc} */
    @Override
    public boolean contains(Object o)
    {
      if (o instanceof Entry)
        for(var entry: map)
          if (Objects.equals(entry, o))
            return true;

      return false;
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull Iterator<Entry<String,Object>> iterator() {
      return map.iterator();
    }


    /** {@inheritDoc} */
    @Override
    public Object @NotNull [] toArray() {
      return map.stream().toArray();
    }


    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T @NotNull [] toArray(T @NotNull [] a)
    {
      final var size = map.size();

      if (a.length < size)
        a = (T[])Array.newInstance(a.getClass().getComponentType(), size);
      else if (a.length > size)
        a[size] = null;

      var n = 0;
      for(var entry: map)
        a[n++] = (T)entry;

      return a;
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull Spliterator<Entry<String,Object>> spliterator() {
      return map.spliterator();
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
      return super.equals(o) && o instanceof UnmodifyableEntrySet;
    }
  }




  /**
   * Unmodifiable values collection view backed by a {@link ParameterMap}.
   */
  private static final class UnmodifyableValueCollection
      extends AbstractUnmodifyableCollection<Object>
      implements Collection<Object>
  {
    /**
     * Creates a new values collection view.
     *
     * @param map  backing parameter map, not {@code null}
     */
    private UnmodifyableValueCollection(@NotNull ParameterMap map) {
      super(map);
    }


    /** {@inheritDoc} */
    @Override
    public boolean contains(Object o)
    {
      for(var entry: map)
        if (Objects.equals(entry.getValue(), o))
          return true;

      return false;
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull Iterator<Object> iterator()
    {
      final var iterator = map.iterator();

      return new Iterator<>() {
        @Override
        public boolean hasNext() {
          return iterator.hasNext();
        }


        @Override
        public Object next() {
          return iterator.next().getValue();
        }
      };
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull Spliterator<Object> spliterator() {
      return Spliterators.spliterator(iterator(), super.size(), ORDERED);
    }


    /** {@inheritDoc} */
    @Override
    public Object @NotNull [] toArray() {
      return map.stream().map(Entry::getValue).toArray();
    }


    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public <T> T @NotNull [] toArray(T @NotNull [] a) {
      throw new UnsupportedOperationException("toArray");
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
      return super.equals(o) && o instanceof UnmodifyableValueCollection;
    }
  }




  /**
   * Immutable key-value entry used internally by {@link ParameterMap}.
   */
  private static final class ParameterEntry implements Entry<String,Object>
  {
    private final @NotNull String key;
    private Object value;


    /**
     * Creates a new parameter entry.
     *
     * @param key    parameter key, not {@code null}
     * @param value  parameter value
     */
    private ParameterEntry(@NotNull String key, Object value)
    {
      this.key = key;
      this.value = value;
    }


    /** {@inheritDoc} */
    @Contract(pure = true)
    public @NotNull String getKey() {
      return key;
    }


    /** {@inheritDoc} */
    @Contract(pure = true)
    public Object getValue() {
      return value;
    }


    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public Object setValue(Object value) {
      throw new UnsupportedOperationException("setValue");
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
      return this == o || o instanceof ParameterEntry that && key.equals(that.key) && Objects.equals(value, that.value);
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
      return key.hashCode();
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
      return key + '=' + value;
    }
  }




  /**
   * Iterator over the merged parameter view of this map and its parent chain.
   * <p>
   * The iterator is fail-fast with respect to structural modifications of the local map.
   */
  private final class ParameterIterator implements Iterator<Entry<String,Object>>
  {
    private final @NotNull Iterator<Entry<String,Object>> parentIterator;
    private final int expectedModCount;

    private Entry<String,Object> nextParentEntry;
    private Entry<String,Object> nextEntry;
    private int n = 0;


    /**
     * Creates a new iterator.
     */
    private ParameterIterator()
    {
      parentIterator = parent == null ? emptyIterator() : parent.iterator();
      expectedModCount = modCount;
      nextParentEntry = null;

      prepareNext();
    }


    /**
     * Advances the iterator to the next merged entry.
     */
    private void prepareNext()
    {
      nextEntry = null;

      if (nextParentEntry == null && parentIterator.hasNext())
        nextParentEntry = parentIterator.next();

      if (n >= size)
      {
        nextEntry = nextParentEntry;
        nextParentEntry = null;
      }
      else
      {
        final var entry = entries[n];
        var cmp = 1;

        if (nextParentEntry != null && (cmp = nextParentEntry.getKey().compareTo(entry.key)) < 0)
        {
          nextEntry = nextParentEntry;
          nextParentEntry = null;
        }
        else
        {
          nextEntry = entry;
          n++;

          if (cmp == 0)
            nextParentEntry = null;
        }
      }
    }


    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
      return nextEntry != null;
    }


    /** {@inheritDoc} */
    @Override
    public @NotNull Entry<String,Object> next()
    {
      if (expectedModCount != modCount)
        throw new ConcurrentModificationException();

      if (nextEntry == null)
        throw new NoSuchElementException();

      final var next = nextEntry;

      prepareNext();

      return next;
    }
  }
}
