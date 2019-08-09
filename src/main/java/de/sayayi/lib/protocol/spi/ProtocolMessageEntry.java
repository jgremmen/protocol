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
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.Tag;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;


/**
 * @author Jeroen Gremmen
 */
class ProtocolMessageEntry<M> extends AbstractGenericMessage<M> implements ProtocolEntry.Message<M>
{
  @Getter private final Level level;
  private final Set<Tag> tags;
  @Getter private final Throwable throwable;


  ProtocolMessageEntry(@NotNull Level level, @NotNull Set<Tag> tags, Throwable throwable, @NotNull M message,
                       @NotNull Map<String,Object> defaultParameterValues)
  {
    super(message, defaultParameterValues);

    this.level = level;
    this.tags = tags;
    this.throwable = throwable;
  }


  @Override
  public @NotNull Set<Tag> getTags() {
    return Collections.unmodifiableSet(tags);
  }


  @Override
  public boolean matches(@NotNull Level level, @NotNull Tag tag) {
    return matches(level) && tags.contains(tag) && tag.matches(level);
  }


  @Override
  public boolean matches(@NotNull Level level) {
    return this.level.severity() >= level.severity();
  }


  @Override
  public int getVisibleEntryCount(@NotNull Level level, @NotNull Tag tag) {
    return matches(level, tag) ? 1 : 0;
  }


  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder("Message[level=").append(level).append(",tags={");
    boolean first = true;

    for(Tag tag: tags)
    {
      if (first)
        first = false;
      else
        s.append(',');

      s.append(tag.getName());
    }

    s.append("},message=").append(message);

    if (!parameterValues.isEmpty())
      s.append(",params=").append(parameterValues);

    return s.append(']').toString();
  }
}
