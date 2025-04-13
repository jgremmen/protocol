/*
 * Copyright 2019 Jeroen Gremmen
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
package de.sayayi.lib.protocol.internal;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolFactory.MessageProcessor.MessageWithId;
import de.sayayi.lib.protocol.matcher.MessageMatcher;
import de.sayayi.lib.protocol.util.ParameterMap;

import org.jetbrains.annotations.NotNull;

import java.util.Map.Entry;
import java.util.Set;

import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.joining;


/**
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
final class ProtocolMessageEntry<M> extends AbstractGenericMessage<M> implements InternalProtocolEntry.Message<M>
{
  private final @NotNull Protocol<M> protocol;
  private final @NotNull Level level;
  private final @NotNull Set<String> tagNames;
  private final Throwable throwable;


  ProtocolMessageEntry(@NotNull Protocol<M> protocol, @NotNull Level level,
                       @NotNull Set<String> tagNames, Throwable throwable,
                       @NotNull MessageWithId<M> messageWithId,
                       @NotNull ParameterMap parentParameterMap)
  {
    super(messageWithId, parentParameterMap);

    this.protocol = protocol;
    this.level = level;
    this.tagNames = tagNames;
    this.throwable = throwable;
  }


  @Override
  public @NotNull Protocol<M> getProtocol() {
    return protocol;
  }


  @Override
  public @NotNull Level getLevel() {
    return level;
  }


  @Override
  public @NotNull Set<String> getTagNames() {
    return unmodifiableSet(tagNames);
  }


  @Override
  public Throwable getThrowable() {
    return throwable;
  }


  @Override
  public boolean hasTag(@NotNull String tagName) {
    return tagNames.contains(tagName);
  }


  @Override
  public boolean matches0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher, boolean messageOnly) {
    return matcher.matches(levelLimit, this);
  }


  @Override
  public boolean matches(@NotNull MessageMatcher matcher) {
    return matches0(HIGHEST, matcher, true);
  }


  @Override
  public int getVisibleEntryCount0(@NotNull Level levelLimit, @NotNull MessageMatcher matcher) {
    return matches0(levelLimit, matcher, false) ? 1 : 0;
  }


  @Override
  public int getVisibleEntryCount(@NotNull MessageMatcher matcher) {
    return getVisibleEntryCount0(HIGHEST, matcher);
  }


  @Override
  public String toString()
  {
    var s = new StringBuilder("Message(level=").append(level).append(",tags={")
        .append(String.join(",", tagNames)).append("},id=").append(getMessageId())
        .append(",message=").append(getMessage());

    if (!parameterMap.isEmpty())
    {
      s.append(parameterMap.stream().map(Entry::toString).collect(
          joining(",", ",params={", "}")));
    }

    return s.append(')').toString();
  }
}
