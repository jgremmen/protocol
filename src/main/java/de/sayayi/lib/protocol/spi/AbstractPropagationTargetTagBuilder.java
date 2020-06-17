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
import de.sayayi.lib.protocol.Tag;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;


/**
 * @author Jeroen Gremmen
 */
abstract class AbstractPropagationTargetTagBuilder<M,B extends ProtocolMessageBuilder<M>>
    extends AbstractTargetTagBuilder<M,B>
{
  protected AbstractPropagationTargetTagBuilder(@NotNull AbstractProtocol<M,B> protocol,
                                                @NotNull Tag sourceTag) {
    super(protocol, sourceTag);
  }


  @Override
  protected @NotNull Protocol<M> to0(@NotNull Tag targetTag)
  {
    Set<Tag> propagationSet = protocol.tagPropagationMap.get(sourceTag);

    if (propagationSet == null)
    {
      propagationSet = new HashSet<Tag>(4);
      protocol.tagPropagationMap.put(sourceTag, propagationSet);
    }

    propagationSet.add(targetTag);

    return protocol;
  }
}
