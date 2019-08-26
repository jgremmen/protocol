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
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.Protocol.MessageParameterBuilder;
import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolFormatter.ConfiguredProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.ProtocolIterator;
import de.sayayi.lib.protocol.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Map.Entry;


/**
 * @author Jeroen Gremmen
 */
@SuppressWarnings({ "unchecked", "PatternValidation" })
abstract class AbstractParameterBuilder<M,P extends MessageParameterBuilder<M>,B extends ProtocolMessageBuilder<M>>
    implements MessageParameterBuilder<M>
{
  private final AbstractProtocol<M,B> protocol;
  private final AbstractGenericMessage<M> message;


  AbstractParameterBuilder(AbstractProtocol<M,B> protocol, @NotNull AbstractGenericMessage<M> message)
  {
    this.protocol = protocol;
    this.message = message;
  }


  @Override
  public @NotNull P with(@NotNull Map<String, Object> parameterValues)
  {
    for(Entry<String,Object> entry: parameterValues.entrySet())
      with(entry.getKey(), entry.getValue());

    return (P)this;
  }


  @Override
  public @NotNull P with(@NotNull String parameter, boolean value) {
    return with(parameter, Boolean.valueOf(value));
  }


  @Override
  public @NotNull P with(@NotNull String parameter, int value) {
    return with(parameter, Integer.valueOf(value));
  }


  @Override
  public @NotNull P with(@NotNull String parameter, long value) {
    return with(parameter, Long.valueOf(value));
  }


  @Override
  public @NotNull P with(@NotNull String parameter, float value) {
    return with(parameter, Float.valueOf(value));
  }


  @Override
  public @NotNull P with(@NotNull String parameter, double value) {
    return with(parameter, Double.valueOf(value));
  }


  @SuppressWarnings("squid:S2589")
  @Override
  public @NotNull P with(@NotNull String parameter, Object value)
  {
    //noinspection ConstantConditions
    if (parameter == null || parameter.isEmpty())
      throw new IllegalArgumentException("parameter must not be empty");

    message.parameterValues.put(parameter, value);

    return (P)this;
  }


  @Override
  public @NotNull ProtocolFactory<M> getFactory() {
    return protocol.getFactory();
  }


  @Override
  public Protocol<M> getGroupParent() {
    return protocol.getGroupParent();
  }


  @Override
  public @NotNull B debug() {
    return protocol.debug();
  }


  @Override
  public @NotNull B info() {
    return protocol.info();
  }


  @Override
  public @NotNull B warn() {
    return protocol.warn();
  }


  @Override
  public @NotNull B error() {
    return protocol.error();
  }


  @Override
  public @NotNull B error(Throwable throwable) {
    return protocol.error(throwable);
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
  public <R> R format(@NotNull ProtocolFormatter<M,R> formatter, @NotNull Level level, @NotNull Tag ... tags) {
    return protocol.format(formatter, level, tags);
  }


  @Override
  public <R> R format(@NotNull ConfiguredProtocolFormatter<M,R> formatter) {
    return protocol.format(formatter);
  }


  @Override
  public boolean matches(@NotNull Level level, @NotNull Tag ... tags) {
    return protocol.matches(level, tags);
  }


  @Override
  public boolean matches(@NotNull Level level, @NotNull String ... tagNames) {
    return protocol.matches(level, tagNames);
  }


  @Override
  public boolean matches(@NotNull Level level) {
    return protocol.matches(level);
  }


  @Override
  public int getVisibleEntryCount(@NotNull Level level, @NotNull Tag ... tags) {
    return protocol.getVisibleEntryCount(level, tags);
  }


  @Override
  public @NotNull ProtocolIterator<M> iterator(@NotNull Level level, @NotNull Tag ... tags) {
    return protocol.iterator(level, tags);
  }
}
