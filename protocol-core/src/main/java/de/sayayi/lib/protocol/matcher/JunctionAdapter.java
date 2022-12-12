/*
 * Copyright 2022 Jeroen Gremmen
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
package de.sayayi.lib.protocol.matcher;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolEntry.Message;
import de.sayayi.lib.protocol.TagSelector;

import lombok.RequiredArgsConstructor;

import org.jetbrains.annotations.NotNull;

import static lombok.AccessLevel.PACKAGE;


/**
 * @author Jeroen Gremmen
 * @since 1.2.0
 */
@RequiredArgsConstructor(access = PACKAGE)
final class JunctionAdapter implements MessageMatcher.Junction
{
  private final MessageMatcher matcher;


  @Override
  public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
    return matcher.matches(levelLimit, message);
  }


  @Override
  public boolean isTagSelector() {
    return matcher.isTagSelector();
  }


  @Override
  public @NotNull TagSelector asTagSelector() {
    return matcher.asTagSelector();
  }


  @Override
  public String toString() {
    return matcher.toString();
  }
}