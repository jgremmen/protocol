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

import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolMessageMatcher;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import lombok.Getter;
import lombok.val;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;


/**
 * {@inheritDoc}
 *
 * @author Jeroen Gremmen
 */
public class GenericProtocolFactory<M> implements ProtocolFactory<M>
{
  private static final AtomicInteger FACTORY_ID = new AtomicInteger(0);

  private final int id;

  @Getter private final @NotNull MessageProcessor<M> messageProcessor;
  @Getter private final @NotNull MessageFormatter<M> messageFormatter;
  @Getter private final @NotNull ProtocolMessageMatcher messageMatcher;


  public GenericProtocolFactory(@NotNull MessageProcessor<M> messageProcessor,
                                @NotNull MessageFormatter<M> messageFormatter) {
    this(messageProcessor, messageFormatter, detectMessageMatcher());
  }


  public GenericProtocolFactory(@NotNull MessageProcessor<M> messageProcessor,
                                @NotNull MessageFormatter<M> messageFormatter,
                                @NotNull ProtocolMessageMatcher messageMatcher)
  {
    this.messageProcessor =
        requireNonNull(messageProcessor, "messageProcessor must not be null");
    this.messageFormatter =
        requireNonNull(messageFormatter, "messageFormatter must not be null");
    this.messageMatcher =
        requireNonNull(messageMatcher, "messageMatcher must not be null");

    id = FACTORY_ID.incrementAndGet();
  }


  @Override
  public @NotNull MessageMatcher parseMessageMatcher(@NotNull String messageMatcherText) {
    return messageMatcher.parseMessageMatcher(messageMatcherText);
  }


  @Override
  public @NotNull TagSelector parseTagSelector(@NotNull String tagSelectorText) {
    return messageMatcher.parseTagSelector(tagSelectorText);
  }


  @Override
  public @NotNull Protocol<M> createProtocol() {
    return new ProtocolImpl<>(this);
  }


  @Override
  public @NotNull String toString() {
    return "ProtocolFactory[id=" + id + ']';
  }


  @Contract(pure = true)
  protected static @NotNull ProtocolMessageMatcher detectMessageMatcher()
  {
    val messageMatcherIterator = ServiceLoader
        .load(ProtocolMessageMatcher.class)
        .iterator();

    if (messageMatcherIterator.hasNext())
      return messageMatcherIterator.next();

    return new ProtocolMessageMatcher()
    {
      @Override
      public @NotNull MessageMatcher parseMessageMatcher(@NotNull String messageMatcherText) {
        throw new UnsupportedOperationException();
      }


      @Override
      public @NotNull TagSelector parseTagSelector(@NotNull String tagSelectorText) {
        throw new UnsupportedOperationException();
      }
    };
  }
}