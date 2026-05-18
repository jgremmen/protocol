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
package de.sayayi.lib.protocol.matcher.internal;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolEntry.Message;
import de.sayayi.lib.protocol.exception.MessageMatcherException;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static de.sayayi.lib.protocol.ProtocolFactory.DEFAULT_TAG_NAME;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableSet;


/**
 * A minimal {@link Message} adapter that only provides tag names. This is used by
 * {@link de.sayayi.lib.protocol.matcher.MessageMatcher#asTagSelector()} to evaluate tag-based matchers against a set
 * of tag names without requiring a full message instance.
 * <p>
 * Only {@link #getTagNames()}, {@link #hasTag(String)}, {@link #getParameterValues()}, and
 * {@link #getVisibleEntryCount(MessageMatcher)} are supported. All other methods throw a
 * {@link MessageMatcherException}.
 *
 * @author Jeroen Gremmen
 * @since 1.2.0  (refactored in 1.6.0)
 */
@SuppressWarnings("ClassCanBeRecord")
public final class TagNamesMessageAdapter implements Message<Object>
{
  private final Set<String> tagNames;


  /**
   * Creates a tag names adapter from the given tag names. The
   * {@linkplain de.sayayi.lib.protocol.ProtocolFactory#DEFAULT_TAG_NAME default tag} is always included; empty and
   * {@code null} values are removed.
   *
   * @param tagNames  tag names to wrap, not {@code null}
   */
  public TagNamesMessageAdapter(@NotNull Iterable<String> tagNames)
  {
    final var tagNameSet = new HashSet<String>();

    tagNameSet.add(DEFAULT_TAG_NAME);
    tagNames.iterator().forEachRemaining(tagNameSet::add);

    tagNameSet.remove("");
    tagNameSet.remove(null);

    this.tagNames = unmodifiableSet(tagNameSet);
  }


  /** {@inheritDoc} */
  @Override
  @Contract("-> fail")
  public @NotNull Protocol<Object> getProtocol() {
    throw new MessageMatcherException("getProtocol not supported");
  }


  /** {@inheritDoc} */
  @Override
  @Contract("-> fail")
  public @NotNull String getMessageId() {
    throw new MessageMatcherException("getMessageId not supported");
  }


  /** {@inheritDoc} */
  @Override
  @Contract("-> fail")
  public @NotNull Object getMessage() {
    throw new MessageMatcherException("getMessage not supported");
  }


  /** {@inheritDoc} */
  @Override
  @Contract("-> fail")
  public long getTimeMillis() {
    throw new MessageMatcherException("getTimeMillis not supported");
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull Map<String,Object> getParameterValues() {
    return emptyMap();
  }


  /** {@inheritDoc} */
  @Override
  @Contract("-> fail")
  public @NotNull Level getLevel() {
    throw new MessageMatcherException("getLevel not supported");
  }


  /** {@inheritDoc} */
  @Override
  @Contract("-> fail")
  public Throwable getThrowable() {
    throw new MessageMatcherException("getThrowable not supported");
  }


  /** {@inheritDoc} */
  @Override
  @Unmodifiable
  public @NotNull Set<String> getTagNames() {
    return tagNames;
  }


  /** {@inheritDoc} */
  @Override
  public boolean hasTag(@NotNull String tagName) {
    return tagNames.contains(tagName);
  }


  /** {@inheritDoc} */
  @Override
  @Contract("_ -> fail")
  public boolean matches(@NotNull MessageMatcher matcher) {
    throw new MessageMatcherException("matches not supported");
  }


  /** {@inheritDoc} */
  @Override
  public int getVisibleEntryCount(@NotNull MessageMatcher matcher) {
    return 0;
  }


  @Override
  public String toString() {
    return "Message(tags={" + String.join(",", tagNames) + "})";
  }
}
