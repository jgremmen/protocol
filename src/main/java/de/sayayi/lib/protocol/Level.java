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

import org.jetbrains.annotations.Contract;


/**
 * @author Jeroen Gremmen
 */
public interface Level
{
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
   * @see Shared
   */
  @Contract(pure = true)
  int severity();


  /**
   * Level constants
   */
  enum Shared implements Level
  {
    /** Constant representing a level with the lowest possible severity */
    ALL(Integer.MIN_VALUE),

    /** Constant representing DEBUG level (severity = 100) */
    DEBUG(100),

    /** Constant representing INFO level (severity = 200) */
    INFO(200),

    /** Constant representing WARNING level (severity = 300) */
    WARN(300),

    /** Constant representing ERROR level (severity = 400) */
    ERROR(400);


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
