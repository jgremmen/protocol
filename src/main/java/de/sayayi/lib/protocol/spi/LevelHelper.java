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
import de.sayayi.lib.protocol.Tag;


/**
 * @author Jeroen Gremmen
 */
abstract class LevelHelper
{
  private LevelHelper() {
  }


  static Level max(Level l1, Level l2) {
    return (l1.severity() >= l2.severity()) ? l1 : l2;
  }


  static boolean matchLevelAndTags(Level level, Tag... tags)
  {
    for(Tag tag: tags)
      if (tag.matches(level))
        return true;

    return false;
  }
}
