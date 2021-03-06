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
package de.sayayi.lib.protocol.exception;

import lombok.Getter;


/**
 * <p>
 *   Tag selector parser related exception.
 * </p>
 * <p>
 *   Methods {@link #getStartIndex()} and {@link #getEndIndex()} provide information on which part of
 *   the tag selector has a syntactical problem.
 * </p>
 *
 * @author Jeroen Gremmen
 * @since 0.6.0
 */
@Getter
public final class TagSelectorParserException extends ProtocolException
{
  private final int startIndex;
  private final int endIndex;


  public TagSelectorParserException(int startIndex, int endIndex, String message)
  {
    super(message);

    this.startIndex = startIndex;
    this.endIndex = endIndex;
  }
}
