/*
 * Copyright 2022 Jeroen Gremmen
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
package de.sayayi.lib.protocol.matcher;

import lombok.val;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static de.sayayi.lib.protocol.ProtocolFactory.DEFAULT_TAG_NAME;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;


/**
 * @author Jeroen Gremmen
 * @since 1.2.1
 */
abstract class AbstractMatcherParserTest
{
  protected static @NotNull Set<String> asTagNameSet(@NotNull String ... s)
  {
    val set = new HashSet<>(asList(s));
    set.add(DEFAULT_TAG_NAME);

    return unmodifiableSet(set);
  }
}
