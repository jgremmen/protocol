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
package de.sayayi.lib.protocol;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;


/**
 * Describes the level at which messages are protocolled.
 * <p>
 * Similar to various logging libraries, the {@linkplain Shared Shared} enumeration provides
 * regularly used protocol levels like {@code DEBUG} and {@code ERROR}. In addition to that
 * (or even as a replacement) you can define your own set of protocol levels and use them to
 * {@linkplain Protocol#add(Level) protocol} messages.
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("java:S1214")
public interface Level
{
  /**
   * Sort levels ascending: {@link Shared#LOWEST LOWEST} -&gt; {@link Shared#HIGHEST HIGHEST}
   *
   * @since 0.7.0
   */
  Comparator<Level> SORT_ASCENDING = Comparator.comparingInt(Level::severity);


  /**
   * Sort levels descending: {@link Shared#HIGHEST HIGHEST} -&gt; {@link Shared#LOWEST LOWEST}
   *
   * @since 0.7.0
   */
  Comparator<Level> SORT_DESCENDING = SORT_ASCENDING.reversed();


  /**
   * Returns the severity for this level.
   * <p>
   * A higher severity number indicates a more severe problem.
   *
   * @return  severity number
   *
   * @see Shared Shared
   */
  @Contract(pure = true)
  int severity();


  /**
   * Return the level with the highest severity.
   *
   * @param l1  1st level, not {@code null}
   * @param l2  2nd level, not {@code null}
   *
   * @return  level with the highest severity. If both levels have the same severity, {@code l1} is returned.
   */
  @Contract(pure = true)
  static @NotNull Level max(@NotNull Level l1, @NotNull Level l2) {
    return l1.severity() >= l2.severity() ? l1 : l2;
  }


  /**
   * Return the level with the lowest severity.
   *
   * @param l1  1st level, not {@code null}
   * @param l2  2nd level, not {@code null}
   *
   * @return  level with the lowest severity. If both levels have the same severity, {@code l1} is returned.
   */
  @Contract(pure = true)
  static @NotNull Level min(@NotNull Level l1, @NotNull Level l2) {
    return l1.severity() <= l2.severity() ? l1 : l2;
  }


  /**
   * Compare levels by severity.
   *
   * @param l1  1st level, not {@code null}
   * @param l2  2nd level, not {@code null}
   *
   * @return  {@code -1} if {@code l1} has a lower severity than {@code l2},
   *          {@code 1} if {@code l1} has a higher severity than {@code l2},
   *          {@code 0} if both levels have the same severity
   */
  @Contract(pure = true)
  static int compare(@NotNull Level l1, @NotNull Level l2) {
    return Integer.compare(l1.severity(), l2.severity());
  }


  /**
   * Tells if levels {@code l1} and {@code l2} have the same severity.
   *
   * @param l1  1st level, not {@code null}
   * @param l2  2nd level, not {@code null}
   *
   * @return  {@code true} if both levels have the same severity, {@code false} otherwise
   */
  @Contract(pure = true)
  static boolean equals(@NotNull Level l1, @NotNull Level l2) {
    return l1.severity() == l2.severity();
  }




  /**
   * Level constants for the {@linkplain Protocol#debug() debug()},
   * {@linkplain Protocol#info() info()}, {@linkplain Protocol#warn() warn()} and
   * {@linkplain Protocol#error() error()} protocol methods.
   *
   * @since 0.1.0
   */
  enum Shared implements Level
  {
    /** Constant representing a level with the lowest possible severity */
    LOWEST(MIN_VALUE),

    /** Constant representing DEBUG level (severity = 100) */
    DEBUG(100),

    /** Constant representing INFO level (severity = 200) */
    INFO(200),

    /** Constant representing WARNING level (severity = 300) */
    WARN(300),

    /** Constant representing ERROR level (severity = 400) */
    ERROR(400),

    /** Constant representing a level with the highest possible severity */
    HIGHEST(MAX_VALUE);


    private final int severity;


    Shared(int severity) {
      this.severity = severity;
    }


    @Override
    public int severity() {
      return severity;
    }
  }
}
