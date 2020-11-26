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
package de.sayayi.lib.protocol;

import lombok.val;

import org.jetbrains.annotations.Contract;

import java.util.Comparator;


/**
 * <p>
 *   Describes the level at which messages are protocolled.
 * </p>
 * <p>
 *   Similar to various logging libraries, the {@linkplain Shared Shared} enumeration provides regularly used protocol
 *   levels like {@code DEBUG} and {@code ERROR}. In addition to that (or even as a replacement) you can define your own
 *   set of protocol levels and use them to {@linkplain Protocol#add(Level) protocol} messages.
 * </p>
 *
 * @author Jeroen Gremmen
 */
@SuppressWarnings("java:S1214")
public interface Level
{
  Comparator<Level> SORT_ASCENDING = new Comparator<Level>() {
    @Override
    @SuppressWarnings("java:S3358")
    public int compare(Level o1, Level o2)
    {
      val s1 = o1.severity();
      val s2 = o2.severity();

      return s1 < s2 ? -1 : (s1 == s2 ? 0 : 1);
    }
  };


  Comparator<Level> SORT_DESCENDING = new Comparator<Level>() {
    @Override
    @SuppressWarnings("java:S3358")
    public int compare(Level o1, Level o2)
    {
      val s1 = o1.severity();
      val s2 = o2.severity();

      return s1 < s2 ? 1 : (s1 == s2 ? 0 : -1);
    }
  };


  /**
   * <p>
   *   Returns the severity for this level.
   * </p>
   * <p>
   *   A higher severity number indicates a more severe problem.
   * </p>
   *
   * @return  severity number
   *
   * @see Shared Shared
   */
  @Contract(pure = true)
  int severity();


  /**
   * Level constants for the {@linkplain Protocol#debug() debug()}, {@linkplain Protocol#info() info()},
   * {@linkplain Protocol#warn() warn()} and {@linkplain Protocol#error() error()} protocol methods.
   */
  enum Shared implements Level
  {
    /** Constant representing a level with the lowest possible severity */
    LOWEST(Integer.MIN_VALUE),

    /** Constant representing DEBUG level (severity = 100) */
    DEBUG(100),

    /** Constant representing INFO level (severity = 200) */
    INFO(200),

    /** Constant representing WARNING level (severity = 300) */
    WARN(300),

    /** Constant representing ERROR level (severity = 400) */
    ERROR(400),

    /** Constant representing a level with the highest possible severity */
    HIGHEST(Integer.MAX_VALUE);


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
