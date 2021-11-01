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

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.var;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import static lombok.AccessLevel.PRIVATE;


/**
 * @author Jeroen Gremmen
 * @since 1.0.0
 */
@SuppressWarnings("java:S1192")
public final class ParameterMap implements Iterable<Entry<String,Object>>
{
  private final ParameterMap parent;

  private ParameterEntry[] entries;
  private int size;
  private int modCount;


  public ParameterMap() {
    this(null);
  }


  public ParameterMap(ParameterMap parent)
  {
    this.parent = parent;

    entries = null;
    size = 0;
    modCount = 0;
  }


  @Contract(mutates = "this")
  public void put(@NotNull String parameter, Object value)
  {
    requireNonNull(parameter, "parameter must not be null");

    if (size == 0)
      entries = new ParameterEntry[] { new ParameterEntry(parameter, value), null };
    else
    {
      var low = 0;
      var high = size - 1;

      while(low <= high)
      {
        val mid = (low + high) >>> 1;
        val entry = entries[mid];
        val cmp = entry.key.compareTo(parameter);

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


  @Contract(pure = true)
  public boolean has(@NotNull String parameter) {
    return getEntry(requireNonNull(parameter, "parameter must not be null")) != null;
  }


  @Contract(pure = true)
  public Object get(@NotNull String parameter)
  {
    val entry = getEntry(requireNonNull(parameter, "parameter must not be null"));
    return entry == null ? null : entry.value;
  }


  @Contract(value = "-> new", pure = true)
  public @NotNull Iterator<Entry<String,Object>> iterator() {
    return new ParameterIterator();
  }


  @Override
  public Spliterator<Entry<String,Object>> spliterator() {
    return Spliterators.spliterator(iterator(), size(), ORDERED | SORTED | DISTINCT | NONNULL);
  }


  @Contract(value = "-> new", pure = true)
  public @NotNull Stream<Entry<String,Object>> stream() {
    return StreamSupport.stream(spliterator(), false);
  }


  @Contract(pure = true)
  private ParameterEntry getEntry(@NotNull String parameter)
  {
    var low = 0;
    var high = size - 1;

    while(low <= high)
    {
      val mid = (low + high) >>> 1;
      val entry = entries[mid];
      val cmp = entry.key.compareTo(parameter);

      if (cmp < 0)
        low = mid + 1;
      else if (cmp > 0)
        high = mid - 1;
      else
        return entry;
    }

    return parent == null ? null : parent.getEntry(parameter);
  }


  @Contract(pure = true)
  public int size()
  {
    var n = 0;

    for(Iterator<Entry<String,Object>> iterator = iterator(); iterator.hasNext(); iterator.next())
      n++;

    return n;
  }


  @Contract(pure = true)
  public boolean isEmpty() {
    return size == 0 && (parent == null || parent.isEmpty());
  }


  @Contract(value = "-> new", pure = true)
  public @NotNull Map<String,Object> unmodifyableMap() {
    return new UnmodifyableMap(this);
  }


  @Override
  public String toString()
  {
    val iterator = iterator();
    if (!iterator.hasNext())
      return "[]";

    val s = new StringJoiner(",", "[", "]");

    iterator.forEachRemaining(e -> s.add(e.toString()));

    return s.toString();
  }




  @RequiredArgsConstructor(access = PRIVATE)
  private static final class UnmodifyableMap implements Map<String,Object>
  {
    private final @NotNull ParameterMap map;

    private Set<String> keySet;
    private Set<Entry<String,Object>> entrySet;
    private Collection<Object> valueCollection;


    @Override
    public int size() {
      return map.size();
    }


    @Override
    public boolean isEmpty() {
      return map.isEmpty();
    }


    @Override
    public boolean containsKey(Object key) {
      return map.has((String)key);
    }


    @Override
    public boolean containsValue(Object value) {
      return values().contains(value);
    }


    @Override
    public Object get(Object key) {
      return map.get((String)key);
    }


    @Override
    public @Nullable Object put(String key, Object value) {
      throw new UnsupportedOperationException();
    }


    @Override
    public Object remove(Object key) {
      throw new UnsupportedOperationException();
    }


    @Override
    public void putAll(@NotNull Map<? extends String,?> m) {
      throw new UnsupportedOperationException();
    }


    @Override
    public void clear() {
      throw new UnsupportedOperationException();
    }


    @Override
    public @NotNull Set<String> keySet()
    {
      if (keySet == null)
        keySet = new UnmodifyableKeySet(map);

      return keySet;
    }


    @Override
    public @NotNull Collection<Object> values()
    {
      if (valueCollection == null)
        valueCollection = new UnmodifyableValueCollection(map);

      return valueCollection;
    }


    @Override
    public @NotNull Set<Entry<String,Object>> entrySet()
    {
      if (entrySet == null)
        entrySet = new UnmodifyableEntrySet(map);

      return entrySet;
    }
  }




  @EqualsAndHashCode(doNotUseGetters = true, onlyExplicitlyIncluded = true)
  private abstract static class AbstractUnmodifyableCollection<T> implements Collection<T>
  {
    @EqualsAndHashCode.Include
    protected final @NotNull ParameterMap map;


    protected AbstractUnmodifyableCollection(@NotNull ParameterMap map) {
      this.map = map;
    }


    @Override
    public int size() {
      return map.size();
    }


    @Override
    public boolean isEmpty() {
      return map.isEmpty();
    }


    @Override
    public boolean add(T e) {
      throw new UnsupportedOperationException();
    }


    @Override
    public boolean remove(Object o) {
      throw new UnsupportedOperationException();
    }


    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
      throw new UnsupportedOperationException();
    }


    @Override
    public void clear() {
      throw new UnsupportedOperationException();
    }


    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
      throw new UnsupportedOperationException();
    }


    @Override
    public boolean removeIf(Predicate<? super T> filter) {
      throw new UnsupportedOperationException();
    }


    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
      throw new UnsupportedOperationException();
    }


    public boolean containsAll(Collection<?> c)
    {
      for(val e: c)
        if (!contains(e))
          return false;

      return true;
    }


    public String toString()
    {
      val iterator = this.iterator();
      if (!iterator.hasNext())
        return "[]";

      val s = new StringJoiner(", ", "[", "]");

      iterator.forEachRemaining(e -> s.add(e.toString()));

      return s.toString();
    }
  }




  @EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
  private static final class UnmodifyableKeySet
      extends AbstractUnmodifyableCollection<String>
      implements Set<String>
  {
    private UnmodifyableKeySet(@NotNull ParameterMap map) {
      super(map);
    }


    @Override
    public boolean contains(Object o) {
      return o instanceof String && map.has((String)o);
    }


    @Override
    public @NotNull Iterator<String> iterator()
    {
      val iterator = map.iterator();

      return new Iterator<String>() {
        @Override
        public boolean hasNext() {
          return iterator.hasNext();
        }


        @Override
        @SuppressWarnings("java:S2272")
        public String next() {
          return iterator.next().getKey();
        }
      };
    }


    @Override
    public Object @NotNull [] toArray() {
      return map.stream().map(Entry::getKey).toArray();
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> T @NotNull [] toArray(T @NotNull [] a)
    {
      val size = map.size();

      if (a.length < size)
        a = (T[])Array.newInstance(a.getClass(), size);
      else if (a.length > size)
        a[size] = null;

      int n = 0;
      for(final Entry<String,Object> entry: map)
        a[n++] = (T)entry.getKey();

      return a;
    }


    @Override
    public Spliterator<String> spliterator() {
      return Spliterators.spliterator(iterator(), super.size(), ORDERED | SORTED | DISTINCT | NONNULL);
    }
  }




  @EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
  private static final class UnmodifyableEntrySet
      extends AbstractUnmodifyableCollection<Entry<String,Object>>
      implements Set<Entry<String,Object>>
  {
    private UnmodifyableEntrySet(@NotNull ParameterMap map) {
      super(map);
    }


    @Override
    public boolean contains(Object o)
    {
      if (o instanceof Entry)
        for(val entry: map)
          if (Objects.equals(entry, o))
            return true;

      return false;
    }


    @Override
    public @NotNull Iterator<Entry<String,Object>> iterator() {
      return map.iterator();
    }


    @Override
    public Object @NotNull [] toArray() {
      return map.stream().toArray();
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> T @NotNull [] toArray(T @NotNull [] a)
    {
      val size = map.size();

      if (a.length < size)
        a = (T[])Array.newInstance(a.getClass(), size);
      else if (a.length > size)
        a[size] = null;

      int n = 0;
      for(final Entry<String,Object> entry: map)
        a[n++] = (T)entry;

      return a;
    }


    @Override
    public Spliterator<Entry<String,Object>> spliterator() {
      return map.spliterator();
    }
  }




  @EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
  private static final class UnmodifyableValueCollection
      extends AbstractUnmodifyableCollection<Object>
      implements Collection<Object>
  {
    private UnmodifyableValueCollection(@NotNull ParameterMap map) {
      super(map);
    }


    @Override
    public boolean contains(Object o)
    {
      for(val entry: map)
        if (Objects.equals(entry.getValue(), o))
          return true;

      return false;
    }


    @Override
    public @NotNull Iterator<Object> iterator()
    {
      val iterator = map.iterator();

      return new Iterator<Object>() {
        @Override
        public boolean hasNext() {
          return iterator.hasNext();
        }


        @Override
        @SuppressWarnings("java:S2272")
        public Object next() {
          return iterator.next().getValue();
        }
      };
    }


    @Override
    public Spliterator<Object> spliterator() {
      return Spliterators.spliterator(iterator(), super.size(), ORDERED);
    }


    @Override
    public Object @NotNull [] toArray() {
      return map.stream().map(Entry::getValue).toArray();
    }


    @Override
    public <T> T @NotNull [] toArray(T @NotNull [] a) {
      throw new UnsupportedOperationException();
    }
  }




  @AllArgsConstructor(access = PRIVATE)
  private static final class ParameterEntry implements Entry<String,Object>
  {
    @Getter final @NotNull String key;
    @Getter Object value;


    @Override
    public Object setValue(Object value) {
      throw new UnsupportedOperationException();
    }


    @Override
    public boolean equals(Object o)
    {
      if (this == o)
        return true;
      if (!(o instanceof ParameterEntry))
        return false;

      val that = (ParameterEntry)o;

      return key.equals(that.key) && Objects.equals(value, that.value);
    }


    @Override
    public int hashCode() {
      return key.hashCode();
    }


    @Override
    public String toString() {
      return key + '=' + value;
    }
  }




  @AllArgsConstructor(access = PRIVATE)
  private final class ParameterIterator implements Iterator<Entry<String,Object>>
  {
    private final @NotNull Iterator<Entry<String,Object>> parentIterator;
    private final int expectedModCount;

    private Entry<String,Object> nextParentEntry;
    private Entry<String,Object> nextEntry;
    private int n = 0;


    private ParameterIterator()
    {
      parentIterator = parent == null ? emptyIterator() : parent.iterator();
      expectedModCount = modCount;
      nextParentEntry = null;

      prepareNext();
    }


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
        val entry = entries[n];
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


    @Override
    public boolean hasNext() {
      return nextEntry != null;
    }


    @Override
    public @NotNull Entry<String,Object> next()
    {
      if (expectedModCount != modCount)
        throw new ConcurrentModificationException();

      if (nextEntry == null)
        throw new NoSuchElementException();

      val next = nextEntry;

      prepareNext();

      return next;
    }
  }
}
