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
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolFormatter.ConfiguredProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.ProtocolIterator;
import de.sayayi.lib.protocol.Tag;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * @author Jeroen Gremmen
 */
@SuppressWarnings("unchecked")
abstract class AbstractParameterBuilder<M,P extends MessageParameterBuilder<M>,B extends ProtocolMessageBuilder<M>>
    implements MessageParameterBuilder<M>
{
  private final AbstractProtocol<M,B> protocol;
  private final AbstractFormattableMessage<M> message;


  AbstractParameterBuilder(AbstractProtocol<M,B> protocol, AbstractFormattableMessage<M> message)
  {
    this.protocol = protocol;
    this.message = message;
  }


  @Override
  public P with(Map<String, Object> parameterValues)
  {
    for(Entry<String,Object> entry: parameterValues.entrySet())
      with(entry.getKey(), entry.getValue());

    return (P)this;
  }


  @Override
  public P with(String parameter, boolean value) {
    return with(parameter, Boolean.valueOf(value));
  }


  @Override
  public P with(String parameter, int value) {
    return with(parameter, Integer.valueOf(value));
  }


  @Override
  public P with(String parameter, long value) {
    return with(parameter, Long.valueOf(value));
  }


  @Override
  public P with(String parameter, float value) {
    return with(parameter, Float.valueOf(value));
  }


  @Override
  public P with(String parameter, double value) {
    return with(parameter, Double.valueOf(value));
  }


  @Override
  public P with(String parameter, Object value)
  {
    if (message != null)
    {
      if (parameter == null || parameter.isEmpty())
        throw new IllegalArgumentException("parameter must not be empty");

      message.parameterValues.put(parameter, value);
    }

    return (P)this;
  }


  @Override
  public ProtocolFactory<M> getFactory() {
    return protocol.getFactory();
  }


  @Override
  public Protocol<M> getGroupParent() {
    return protocol.getGroupParent();
  }


  @Override
  public B debug() {
    return protocol.debug();
  }


  @Override
  public B info() {
    return protocol.info();
  }


  @Override
  public B warn() {
    return protocol.warn();
  }


  @Override
  public B error() {
    return protocol.error();
  }


  @Override
  public B add(Level level) {
    return protocol.add(level);
  }


  @Override
  public ProtocolGroup<M> createGroup() {
    return protocol.createGroup();
  }


  @Override
  public <R> R format(Level level, Tag tag, ProtocolFormatter<M,R> formatter) {
    return protocol.format(level, tag, formatter);
  }


  @Override
  public <R> R format(ConfiguredProtocolFormatter<M, R> formatter) {
    return protocol.format(formatter);
  }


  @Override
  public boolean isMatch(Level level, Tag tag) {
    return protocol.isMatch(level, tag);
  }


  @Override
  public List<ProtocolEntry<M>> getEntries(Level level, Tag tag) {
    return protocol.getEntries(level, tag);
  }


  @Override
  public boolean hasVisibleElement(Level level, Tag tag) {
    return protocol.hasVisibleElement(level, tag);
  }


  @Override
  public ProtocolIterator<M> iterator(Level level, Tag tag) {
    return protocol.iterator(level, tag);
  }
}
