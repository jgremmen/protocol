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

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolEntry;
import de.sayayi.lib.protocol.ProtocolGroup.Visibility;
import de.sayayi.lib.protocol.Tag;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * @author Jeroen Gremmen
 */
interface InternalProtocolEntry<M> extends ProtocolEntry<M>, InternalProtocolQuery
{
  interface Message<M> extends ProtocolEntry.Message<M>, InternalProtocolEntry<M> {
  }


  interface Group<M> extends ProtocolEntry.Group<M>, InternalProtocolEntry<M>
  {
    @Contract(pure = true)
    int getId();


    @Contract(pure = true)
    Visibility getVisibility();


    @Contract(pure = true)
    Visibility getEffectiveVisibility();


    @Contract(pure = true, value = "_, _, _ -> new")
    @NotNull List<ProtocolEntry<M>> getEntries0(@NotNull Level levelLimit, @NotNull Level level, @NotNull Tag ... tags);


    @Contract(pure = true)
    boolean isHeaderVisible0(@NotNull Level levelLimit, @NotNull Level level, @NotNull Tag ... tags);


    @Contract(pure = true)
    @NotNull Level getHeaderLevel0(@NotNull Level levelLimit, @NotNull Level level, @NotNull Tag ... tags);


    @Contract(pure = true)
    int getVisibleGroupEntryMessageCount0(@NotNull Level levelLimit, @NotNull Level level, @NotNull Tag ... tags);
  }
}