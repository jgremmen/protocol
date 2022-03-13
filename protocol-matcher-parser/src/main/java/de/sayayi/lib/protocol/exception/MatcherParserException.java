/*
 * Copyright 2022 Jeroen Gremmen
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

import org.antlr.v4.runtime.RecognitionException;

import lombok.Getter;

import org.jetbrains.annotations.NotNull;


/**
 * <p>
 *   Matcher parser related exception.
 * </p>
 * <p>
 *   Methods {@link #getStartIndex()} and {@link #getEndIndex()} provide information on which part
 *   of the matcher has a syntactical problem.
 * </p>
 *
 * @author Jeroen Gremmen
 * @since 1.2.0
 */
@Getter
public final class MatcherParserException extends ProtocolException
{
  private final @NotNull String matcher;
  private final int startIndex;
  private final int endIndex;


  public MatcherParserException(@NotNull String matcher, int startIndex, int endIndex,
                                @NotNull String formattedMessage, RecognitionException ex)
  {
    super(formattedMessage, ex);

    this.matcher = matcher;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
  }


  @Override
  public synchronized RecognitionException getCause() {
    return (RecognitionException)super.getCause();
  }
}