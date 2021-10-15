/*
 * Copyright 2021 Jeroen Gremmen
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
package de.sayayi.lib.protocol.matcher;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol.Message;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.val;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static de.sayayi.lib.protocol.matcher.BooleanMatcher.ANY;
import static de.sayayi.lib.protocol.matcher.BooleanMatcher.NONE;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;


/**
 * @author Jeroen Gremmen
 * @since 1.0.0
 */
@RequiredArgsConstructor(access = PRIVATE)
@EqualsAndHashCode(callSuper = false)
class Conjunction extends AbstractJunction
{
  private final Set<MessageMatcher> matchers;


  public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> target)
  {
    for(val matcher: matchers)
      if (!matcher.matches(levelLimit, target))
        return false;

    return true;
  }


  @Override
  public String toString()
  {
    return matchers.stream()
        .map(MessageMatcher::toString)
        .collect(joining(" and ", "(", ")"));
  }


  @Contract(pure = true)
  static @NotNull Junction of(@NotNull MessageMatcher... matcher)
  {
    if (matcher.length == 0)
      throw new IllegalArgumentException("matcher must not be empty");

    val matchers = new LinkedHashSet<>(Arrays.asList(matcher));
    if (matchers.contains(NONE))
      return NONE;

    boolean matchersChanged;
    do {
      matchersChanged = false;

      for(final Iterator<MessageMatcher> matcherIterator = matchers.iterator(); matcherIterator.hasNext();)
      {
        val m = matcherIterator.next();
        if (m instanceof Conjunction)
        {
          matcherIterator.remove();
          matchers.addAll(((Conjunction)m).matchers);
          matchersChanged = true;
          break;
        }
      }
    } while(matchersChanged);

    if (matchers.size() > 1)
      matchers.remove(ANY);

    return matchers.size() == 1 ? matchers.iterator().next().asJunction() : new Conjunction(matchers);
  }
}