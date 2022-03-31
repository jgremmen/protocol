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
package de.sayayi.lib.protocol.matcher;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolEntry.Message;

import lombok.val;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static de.sayayi.lib.protocol.ProtocolFactory.DEFAULT_TAG_NAME;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableSet;


/**
 * @author Jeroen Gremmen
 * @since 1.2.0
 */
final class TagNamesMessageAdapter implements Message<Object>
{
  private final Set<String> tagNames;


  TagNamesMessageAdapter(@NotNull Collection<String> tagNames)
  {
    val tagNameSet = new HashSet<>(tagNames);

    tagNameSet.add(DEFAULT_TAG_NAME);

    this.tagNames = unmodifiableSet(tagNameSet);
  }


  @Override
  public @NotNull Protocol<Object> getProtocol() {
    throw new UnsupportedOperationException();
  }


  @Override
  public @NotNull String getMessageId() {
    throw new UnsupportedOperationException();
  }


  @Override
  public @NotNull Object getMessage() {
    throw new UnsupportedOperationException();
  }


  @Override
  public long getTimeMillis() {
    throw new UnsupportedOperationException();
  }


  @Override
  public @NotNull Map<String,Object> getParameterValues() {
    return emptyMap();
  }


  @Override
  public @NotNull Level getLevel() {
    throw new UnsupportedOperationException();
  }


  @Override
  public Throwable getThrowable() {
    throw new UnsupportedOperationException();
  }


  @Override
  public @Unmodifiable @NotNull Set<String> getTagNames() {
    return tagNames;
  }


  @Override
  public boolean hasTag(@NotNull String tagName) {
    return tagNames.contains(tagName);
  }


  @Override
  public boolean matches(@NotNull MessageMatcher matcher) {
    throw new UnsupportedOperationException();
  }


  @Override
  public int getVisibleEntryCount(@NotNull MessageMatcher matcher) {
    return 0;
  }


  @Override
  public String toString() {
    return "Message[tags={" + String.join(",", tagNames) + "}]";
  }
}