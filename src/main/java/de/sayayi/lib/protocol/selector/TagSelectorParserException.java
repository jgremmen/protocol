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
package de.sayayi.lib.protocol.selector;

import lombok.Getter;


/**
 * @author Jeroen Gremmen
 */
public final class TagSelectorParserException extends RuntimeException
{
  @Getter private final int startIndex;
  @Getter private final int endIndex;


  public TagSelectorParserException(int startIndex, int endIndex, String message)
  {
    super(message);

    this.startIndex = startIndex;
    this.endIndex = endIndex;
  }
}
