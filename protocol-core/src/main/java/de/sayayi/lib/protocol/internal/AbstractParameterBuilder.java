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
import de.sayayi.lib.protocol.Protocol.MessageParameterBuilder;
import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.ProtocolIterator;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;


/**
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
@SuppressWarnings({ "unchecked" })
abstract class AbstractParameterBuilder
    <M,P extends MessageParameterBuilder<M>,B extends ProtocolMessageBuilder<M>>
    extends AbstractBuilder<M,B>
    implements MessageParameterBuilder<M>
{
  private final @NotNull AbstractGenericMessage<M> message;


  protected AbstractParameterBuilder(@NotNull AbstractProtocol<M,B> protocol,
                                     @NotNull AbstractGenericMessage<M> message)
  {
    super(protocol);

    this.message = message;
  }


  @Override
  public @NotNull P with(@NotNull Map<String,Object> parameterValues)
  {
    requireNonNull(parameterValues, "parameterValues must not be null");

    for(var entry: parameterValues.entrySet())
    {
      var key = entry.getKey();
      if (key != null && !key.isEmpty())
        with(key, entry.getValue());
    }

    return (P)this;
  }


  @Override
  public @NotNull P with(@NotNull String parameter, Object value)
  {
    if (requireNonNull(parameter, "parameter must not be null").isEmpty())
      throw new IllegalArgumentException("parameter must not be empty");

    message.parameterMap.put(parameter, value);

    return (P)this;
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
    return message.timeMillis;
  }


  @Override
  @UnmodifiableView
  public @NotNull Map<String,Object> getParameterValues() {
    return message.getParameterValues();
  }


  @Override
  public @NotNull ProtocolFactory<M> getFactory() {
    return protocol.getFactory();
  }


  @Override
  public Protocol<M> getParent() {
    return protocol.getParent();
  }


  @Override
  public int getId() {
    return protocol.getId();
  }


  @Override
  public boolean matches(@NotNull String matcher) {
    return protocol.matches(matcher);
  }


  @Override
  public @NotNull B add(@NotNull Level level) {
    return protocol.add(level);
  }


  @Override
  public @NotNull ProtocolGroup<M> createGroup() {
    return protocol.createGroup();
  }


  @Override
  public @NotNull Iterator<ProtocolGroup<M>> groupIterator() {
    return protocol.groupIterator();
  }


  @Override
  public @NotNull Spliterator<ProtocolGroup<M>> groupSpliterator() {
    return protocol.groupSpliterator();
  }


  @Override
  public <R> R format(@NotNull ProtocolFormatter<M,R> formatter, @NotNull MessageMatcher matcher) {
    return protocol.format(formatter, matcher);
  }


  @Override
  public boolean matches(@NotNull MessageMatcher matcher) {
    return protocol.matches(matcher);
  }


  @Override
  public int getVisibleEntryCount(@NotNull MessageMatcher matcher) {
    return protocol.getVisibleEntryCount(matcher);
  }


  @Override
  public @NotNull ProtocolIterator<M> iterator(@NotNull MessageMatcher matcher) {
    return protocol.iterator(matcher);
  }


  @Override
  public @NotNull Spliterator<ProtocolIterator.DepthEntry<M>> spliterator(@NotNull MessageMatcher matcher) {
    return protocol.spliterator(matcher);
  }


  @Override
  public @NotNull TargetTagBuilder<M> propagate(@NotNull TagSelector tagSelector) {
    return protocol.propagate(tagSelector);
  }


  @Override
  public @NotNull TargetTagBuilder<M> propagate(@NotNull String tagSelectorExpression) {
    return protocol.propagate(tagSelectorExpression);
  }


  @Override
  public @NotNull Optional<ProtocolGroup<M>> getGroupByName(@NotNull String name) {
    return protocol.getGroupByName(name);
  }


  @Override
  public void forEachGroupByRegex(@NotNull String regex, @NotNull Consumer<ProtocolGroup<M>> action) {
    protocol.forEachGroupByRegex(regex, action);
  }
}
