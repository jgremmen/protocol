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
import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor.MessageWithId;
import de.sayayi.lib.protocol.TagSelector;

import lombok.Getter;
import lombok.val;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;

import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;
import static de.sayayi.lib.protocol.Level.compare;
import static de.sayayi.lib.protocol.Level.min;
import static java.util.stream.Collectors.joining;


/**
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
final class ProtocolMessageEntry<M> extends AbstractGenericMessage<M> implements InternalProtocolEntry.Message<M>
{
  @Getter private final Level level;
  private final Set<String> tagNames;
  @Getter private final Throwable throwable;


  ProtocolMessageEntry(@NotNull Level level, @NotNull Set<String> tagNames, Throwable throwable,
                       @NotNull MessageWithId<M> messageWithId, @NotNull ParameterMap parentParameterMap)
  {
    super(messageWithId, parentParameterMap);

    this.level = level;
    this.tagNames = tagNames;
    this.throwable = throwable;
  }


  @Override
  public @NotNull Set<String> getTagNames() {
    return Collections.unmodifiableSet(tagNames);
  }


  @Override
  public boolean matches0(@NotNull Level levelLimit, @NotNull Level level, @NotNull TagSelector tagSelector) {
    return matches0(levelLimit, level) && tagSelector.match(tagNames);
  }


  @Override
  public boolean matches(@NotNull Level level, @NotNull TagSelector tagSelector) {
    return matches0(HIGHEST, level, tagSelector);
  }


  @Override
  public boolean matches0(@NotNull Level levelLimit, @NotNull Level level) {
    return compare(min(this.level, levelLimit), level) >= 0;
  }


  @Override
  public boolean matches(@NotNull Level level) {
    return compare(this.level, level) >= 0;
  }


  @Override
  public int getVisibleEntryCount0(@NotNull Level levelLimit, boolean recursive,
                                   @NotNull Level level, @NotNull TagSelector tagSelector) {
    return matches0(levelLimit, level, tagSelector) ? 1 : 0;
  }


  @Override
  public int getVisibleEntryCount(boolean recursive, @NotNull Level level, @NotNull TagSelector tagSelector) {
    return getVisibleEntryCount0(HIGHEST, recursive, level, tagSelector);
  }


  @Override
  public String toString()
  {
    val s = new StringBuilder("Message[level=").append(level).append(",tags={")
        .append(String.join(",", tagNames)).append("},id=").append(getMessageId())
        .append(",message=").append(getMessage());

    if (!parameterMap.isEmpty())
    {
      s.append(parameterMap.stream().map(Entry::toString).collect(
          joining(",", ",params={", "}")));
    }

    return s.append(']').toString();
  }
}
