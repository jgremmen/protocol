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
import de.sayayi.lib.protocol.TagDef;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;


/**
 * @author Jeroen Gremmen
 */
abstract class AbstractPropagationTargetTagBuilder<M,B extends ProtocolMessageBuilder<M>>
    extends AbstractBuilder<M,B>
    implements TargetTagBuilder<M>
{
  private final TagDef sourceTagDef;


  protected AbstractPropagationTargetTagBuilder(@NotNull AbstractProtocol<M,B> protocol, @NotNull TagDef sourceTagDef)
  {
    super(protocol);

    this.sourceTagDef = sourceTagDef;
  }


  @Override
  public @NotNull Protocol<M> to(@NotNull String targetTagName)
  {
    final TagDef targetTagDef = protocol.factory.getTagByName(targetTagName);
    Set<TagDef> propagationSet = protocol.tagPropagationMap.get(sourceTagDef);

    if (propagationSet == null)
    {
      propagationSet = new HashSet<TagDef>(4);
      protocol.tagPropagationMap.put(sourceTagDef, propagationSet);
    }

    propagationSet.add(targetTagDef);

    return protocol;
  }


  @Override
  @SuppressWarnings({ "java:S2583", "java:S2589", "ConstantConditions" })
  public @NotNull Protocol<M> to(@NotNull String... targetTagNames)
  {
    if (targetTagNames == null || targetTagNames.length == 0)
      throw new NullPointerException("targetTagNames must not be empty");

    for(String targetTagName: targetTagNames)
      to(targetTagName);

    return protocol;
  }
}
