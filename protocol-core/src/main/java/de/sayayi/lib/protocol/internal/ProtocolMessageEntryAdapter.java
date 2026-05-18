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
package de.sayayi.lib.protocol.internal;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static de.sayayi.lib.protocol.Level.compare;
import static java.util.stream.Collectors.joining;


/**
 * Adapter that wraps an {@link InternalProtocolEntry.Message} and caps its reported level at a given limit. This is
 * used when the effective level of a message needs to be constrained by a level limit that is lower than the message's
 * own level.
 * <p>
 * Instances are created via the {@link #from(Level, InternalProtocolEntry.Message)} factory method, which returns the
 * original message unchanged if no level capping is needed.
 *
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.4.1
 */
final class ProtocolMessageEntryAdapter<M> implements ProtocolEntry.Message<M>
{
  private final @NotNull Level levelLimit;
  private final @NotNull InternalProtocolEntry.Message<M> message;


  /**
   * Creates a new adapter that wraps the given message with a capped level.
   *
   * @param levelLimit  maximum level to report for this message, not {@code null}
   * @param message     internal message entry to wrap, not {@code null}
   */
  private ProtocolMessageEntryAdapter(@NotNull Level levelLimit, @NotNull InternalProtocolEntry.Message<M> message)
  {
    this.levelLimit = levelLimit;
    this.message = message;
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull String getMessageId() {
    return message.getMessageId();
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull M getMessage() {
    return message.getMessage();
  }


  /** {@inheritDoc} */
  @Override
  public long getTimeMillis() {
    return message.getTimeMillis();
  }


  /** {@inheritDoc} */
  @Override
  @UnmodifiableView
  public @NotNull Map<String,Object> getParameterValues() {
    return message.getParameterValues();
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull Protocol<M> getProtocol() {
    return message.getProtocol();
  }


  /**
   * {@inheritDoc}
   * <p>
   * Returns the capped level limit rather than the original message level.
   */
  @Override
  public @NotNull Level getLevel() {
    return levelLimit;
  }


  /** {@inheritDoc} */
  @Override
  public Throwable getThrowable() {
    return message.getThrowable();
  }


  /** {@inheritDoc} */
  @Override
  @UnmodifiableView
  public @NotNull Set<String> getTagNames() {
    return message.getTagNames();
  }


  /** {@inheritDoc} */
  @Override
  public boolean matches(@NotNull MessageMatcher matcher) {
    return message.matches0(levelLimit, matcher, true);
  }


  /** {@inheritDoc} */
  @Override
  public int getVisibleEntryCount(@NotNull MessageMatcher matcher) {
    return message.getVisibleEntryCount0(levelLimit, matcher);
  }


  @Override
  public String toString()
  {
    final var s = new StringBuilder("Message(level=").append(levelLimit)
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


  /**
   * Returns a {@link ProtocolEntry.Message} for the given message entry, applying a level cap if the given
   * {@code levelLimit} is lower than the message's own level. If no capping is needed, the original message entry is
   * returned as-is.
   *
   * @param levelLimit    maximum level to report, not {@code null}
   * @param messageEntry  internal message entry, not {@code null}
   *
   * @param <M>  internal message object type
   *
   * @return  message entry with level capped at {@code levelLimit}, never {@code null}
   */
  static <M> ProtocolEntry.Message<M> from(@NotNull Level levelLimit,
                                           @NotNull InternalProtocolEntry.Message<M> messageEntry)
  {
    return compare(levelLimit, messageEntry.getLevel()) < 0
        ? new ProtocolMessageEntryAdapter<>(levelLimit, messageEntry) : messageEntry;
  }
}
