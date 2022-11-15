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

import lombok.experimental.StandardException;


/**
 * <p>
 *   Matcher parser related exception.
 * </p>
 *
 * @author Jeroen Gremmen
 * @since 1.2.0
 */
@StandardException
public final class MessageMatcherParserException extends ProtocolException
{
  @Override
  public RecognitionException getCause() {
    return (RecognitionException)super.getCause();
  }
}