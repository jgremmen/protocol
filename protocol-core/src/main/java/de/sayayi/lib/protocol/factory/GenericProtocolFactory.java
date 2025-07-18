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
package de.sayayi.lib.protocol.factory;

import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolMessageMatcher;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.exception.MessageMatcherException;
import de.sayayi.lib.protocol.internal.ProtocolImpl;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;


/**
 * @author Jeroen Gremmen
 * @since 1.6.0
 */
public class GenericProtocolFactory<M> implements ProtocolFactory<M>
{
  private static final AtomicInteger FACTORY_ID = new AtomicInteger(0);

  private final int id;

  private final @NotNull MessageProcessor<M> messageProcessor;
  private final @NotNull MessageFormatter<M> messageFormatter;

  private @NotNull ProtocolMessageMatcher messageMatcher;


  public GenericProtocolFactory(@NotNull MessageProcessor<M> messageProcessor,
                                @NotNull MessageFormatter<M> messageFormatter) {
    this(messageProcessor, messageFormatter, currentThread().getContextClassLoader());
  }


  public GenericProtocolFactory(@NotNull MessageProcessor<M> messageProcessor,
                                @NotNull MessageFormatter<M> messageFormatter,
                                ClassLoader classLoader) {
    this(messageProcessor, messageFormatter, detectMessageMatcher(classLoader));
  }


  public GenericProtocolFactory(@NotNull MessageProcessor<M> messageProcessor,
                                @NotNull MessageFormatter<M> messageFormatter,
                                @NotNull ProtocolMessageMatcher messageMatcher)
  {
    this.messageProcessor = requireNonNull(messageProcessor, "messageProcessor must not be null");
    this.messageFormatter = requireNonNull(messageFormatter, "messageFormatter must not be null");
    this.messageMatcher = requireNonNull(messageMatcher, "messageMatcher must not be null");

    id = FACTORY_ID.incrementAndGet();
  }


  @Override
  public @NotNull MessageProcessor<M> getMessageProcessor() {
    return messageProcessor;
  }


  @Override
  public @NotNull MessageFormatter<M> getMessageFormatter() {
    return messageFormatter;
  }


  /**
   * Associate a protocol message matcher with this factory,
   * overriding the previous message matcher.
   *
   * @param messageMatcher   protocol message matcher instance, not {@code null}
   *
   * @since 1.2.1
   */
  public void setMessageMatcher(@NotNull ProtocolMessageMatcher messageMatcher) {
    this.messageMatcher = requireNonNull(messageMatcher, "messageMatcher must not be null");
  }


  @Override
  public @NotNull MessageMatcher parseMessageMatcher(@NotNull String messageMatcherExpression)
  {
    return messageMatcher.parseMessageMatcher(
        requireNonNull(messageMatcherExpression, "messageMatcherExpression must not be null"));
  }


  @Override
  public @NotNull TagSelector parseTagSelector(@NotNull String tagSelectorExpression)
  {
    return messageMatcher.parseTagSelector(
        requireNonNull(tagSelectorExpression, "tagSelectorExpression must not be null"));
  }


  @Override
  public @NotNull Protocol<M> createProtocol() {
    return new ProtocolImpl<>(this);
  }


  @Override
  public @NotNull String toString() {
    return "ProtocolFactory(id=" + id + ')';
  }


  @Contract(pure = true)
  protected static @NotNull ProtocolMessageMatcher detectMessageMatcher(ClassLoader classLoader)
  {
    return ServiceLoader
        .load(ProtocolMessageMatcher.class, classLoader)
        .findFirst()
        .orElseGet(NotSupportedMessageMatcher::new);
  }




  protected static final class NotSupportedMessageMatcher implements ProtocolMessageMatcher
  {
    private NotSupportedMessageMatcher() {
    }


    @Override
    public @NotNull MessageMatcher parseMessageMatcher(@NotNull String messageMatcherExpression) {
      throw new MessageMatcherException("parseMessageMatcher not supported");
    }


    @Override
    public @NotNull TagSelector parseTagSelector(@NotNull String tagSelectorExpression) {
      throw new MessageMatcherException("parseTagSelector not supported");
    }


    @Override
    public String toString() {
      return "not supported";
    }
  }
}
