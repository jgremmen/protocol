/*
 * Copyright 2020 Jeroen Gremmen
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
package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static de.sayayi.lib.protocol.Level.compare;
import static java.util.stream.Collectors.joining;


/**
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.4.1
 */
final class ProtocolMessageEntryAdapter<M> implements ProtocolEntry.Message<M>
{
  private final @NotNull Level levelLimit;
  private final @NotNull InternalProtocolEntry.Message<M> message;


  private ProtocolMessageEntryAdapter(@NotNull Level levelLimit, @NotNull InternalProtocolEntry.Message<M> message)
  {
    this.levelLimit = levelLimit;
    this.message = message;
  }


  @Override
  public @NotNull String getMessageId() {
    return message.getMessageId();
  }


  @Override
  public @NotNull M getMessage() {
    return message.getMessage();
  }


  @Override
  public long getTimeMillis() {
    return message.getTimeMillis();
  }


  @Override
  public @NotNull Map<String,Object> getParameterValues() {
    return message.getParameterValues();
  }


  @Override
  public @NotNull Protocol<M> getProtocol() {
    return message.getProtocol();
  }


  @Override
  public @NotNull Level getLevel() {
    return levelLimit;
  }


  @Override
  public Throwable getThrowable() {
    return message.getThrowable();
  }


  @Override
  public @NotNull Set<String> getTagNames() {
    return message.getTagNames();
  }


  @Override
  public boolean matches(@NotNull MessageMatcher matcher) {
    return message.matches0(levelLimit, matcher, true);
  }


  @Override
  public int getVisibleEntryCount(@NotNull MessageMatcher matcher) {
    return message.getVisibleEntryCount0(levelLimit, matcher);
  }


  @Override
  public String toString()
  {
    var s = new StringBuilder("Message(level=").append(levelLimit)
        .append(",tags={").append(String.join(",", getTagNames())).append("},id=")
        .append(getMessageId()).append(",message=").append(message.getMessage());

    var parameterValues = getParameterValues();
    if (!parameterValues.isEmpty())
    {
      s.append(parameterValues.entrySet().stream().map(Entry::toString).collect(
          joining(",", ",params={", "}")));
    }

    return s.append(')').toString();
  }


  static <M> ProtocolEntry.Message<M> from(@NotNull Level levelLimit,
                                           @NotNull InternalProtocolEntry.Message<M> messageEntry)
  {
    return compare(levelLimit, messageEntry.getLevel()) < 0
        ? new ProtocolMessageEntryAdapter<>(levelLimit, messageEntry) : messageEntry;
  }
}
