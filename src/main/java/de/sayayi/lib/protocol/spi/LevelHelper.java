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
package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;


/**
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
@NoArgsConstructor(access = PRIVATE)
abstract class LevelHelper
{
  static Level max(Level l1, Level l2) {
    return l1.severity() >= l2.severity() ? l1 : l2;
  }


  static Level min(Level l1, Level l2) {
    return l1.severity() <= l2.severity() ? l1 : l2;
  }
}
