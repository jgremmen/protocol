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
package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.Tag;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;


/**
 * @author Jeroen Gremmen
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class ProtocolMessageEntryAdapter<M> implements ProtocolEntry.Message<M>
{
  private final Level levelLimit;
  private final InternalProtocolEntry.Message<M> message;


  @NotNull
  @Override
  public Set<Tag> getTags() {
    return message.getTags();
  }


  @NotNull
  @Override
  public M getMessage() {
    return message.getMessage();
  }


  @Override
  public long getTimeMillis() {
    return message.getTimeMillis();
  }


  @NotNull
  @Override
  public Map<String,Object> getParameterValues() {
    return message.getParameterValues();
  }


  @NotNull
  @Override
  public Level getLevel() {
    return levelLimit;
  }


  @Override
  public Throwable getThrowable() {
    return message.getThrowable();
  }


  @Override
  public boolean matches(@NotNull Level level, @NotNull Tag... tags) {
    return message.matches0(levelLimit, level, tags);
  }


  @Override
  public boolean matches(@NotNull Level level) {
    return message.matches0(levelLimit, level);
  }


  @Override
  public int getVisibleEntryCount(boolean recursive, @NotNull Level level, @NotNull Tag... tags) {
    return message.getVisibleEntryCount0(levelLimit, recursive, level, tags);
  }


  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder("Message[level=").append(levelLimit).append(",tags={");
    boolean first = true;

    for(Tag tag: getTags())
    {
      if (first)
        first = false;
      else
        s.append(',');

      s.append(tag.getName());
    }

    s.append("},message=").append(message);

    final Map<String,Object> parameterValues = getParameterValues();
    if (!parameterValues.isEmpty())
      s.append(",params=").append(parameterValues);

    return s.append(']').toString();
  }


  static <M> ProtocolEntry.Message<M> from(@NotNull Level levelLimit,
                                           @NotNull InternalProtocolEntry.Message<M> messageEntry)
  {
    return levelLimit.severity() < messageEntry.getLevel().severity()
        ? new ProtocolMessageEntryAdapter<M>(levelLimit, messageEntry) : messageEntry;
  }
}
