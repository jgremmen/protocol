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

import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.Protocol.TargetTagBuilder;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.exception.ProtocolException;

import lombok.val;
import lombok.var;

import org.jetbrains.annotations.NotNull;

import java.util.TreeSet;


/**
 * @author Jeroen Gremmen
 * @since 0.4.0
 */
abstract class AbstractPropagationBuilder<M,B extends ProtocolMessageBuilder<M>>
    extends AbstractBuilder<M,B>
    implements TargetTagBuilder<M>
{
  private final TagSelector tagSelector;


  protected AbstractPropagationBuilder(@NotNull AbstractProtocol<M,B> protocol, @NotNull TagSelector tagSelector)
  {
    super(protocol);

    this.tagSelector = tagSelector;
  }


  @Override
  public @NotNull Protocol<M> to(@NotNull String targetTagName)
  {
    if (!protocol.factory.isValidTagName(targetTagName))
      throw new IllegalArgumentException("invalid target tag name '" + targetTagName + "'");

    var propagationSet = protocol.tagPropagationMap.get(tagSelector);
    if (propagationSet == null)
    {
      propagationSet = new TreeSet<String>();
      protocol.tagPropagationMap.put(tagSelector, propagationSet);
    }

    propagationSet.add(targetTagName);

    return protocol;
  }


  @Override
  @SuppressWarnings({ "java:S2583", "java:S2589", "ConstantConditions" })
  public @NotNull Protocol<M> to(@NotNull String... targetTagNames)
  {
    if (targetTagNames == null || targetTagNames.length == 0)
      throw new ProtocolException("targetTagNames must not be empty");

    for(val targetTagName: targetTagNames)
      to(targetTagName);

    return protocol;
  }
}
