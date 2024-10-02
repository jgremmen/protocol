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
package de.sayayi.lib.protocol.spi;

import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.Protocol.ProtocolMessageBuilder;
import de.sayayi.lib.protocol.Protocol.TargetTagBuilder;
import de.sayayi.lib.protocol.TagSelector;

import org.jetbrains.annotations.NotNull;

import java.util.TreeSet;

import static java.util.Objects.requireNonNull;


/**
 * @author Jeroen Gremmen
 * @since 0.5.0
 */
abstract class AbstractPropagationBuilder<M,B extends ProtocolMessageBuilder<M>>
    extends AbstractBuilder<M,B>
    implements TargetTagBuilder<M>
{
  private final @NotNull TagSelector tagSelector;


  protected AbstractPropagationBuilder(@NotNull AbstractProtocol<M,B> protocol, @NotNull TagSelector tagSelector)
  {
    super(protocol);

    this.tagSelector = tagSelector;
  }


  @Override
  public @NotNull Protocol<M> to(@NotNull String targetTagName)
  {
    protocol.tagPropagationMap
        .computeIfAbsent(tagSelector, k -> new TreeSet<>())
        .add(requireNonNull(targetTagName));

    return protocol;
  }


  @Override
  public @NotNull Protocol<M> to(@NotNull String... targetTagNames)
  {
    if (requireNonNull(targetTagNames, "targetTagNames must not be null").length == 0)
      throw new IllegalArgumentException("targetTagNames must not be empty");

    for(final String targetTagName: targetTagNames)
      to(targetTagName);

    return protocol;
  }
}
