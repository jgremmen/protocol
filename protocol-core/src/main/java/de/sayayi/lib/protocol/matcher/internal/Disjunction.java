/*
 * Copyright 2021 Jeroen Gremmen
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
package de.sayayi.lib.protocol.matcher.internal;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolEntry.Message;
import de.sayayi.lib.protocol.exception.MessageMatcherException;
import de.sayayi.lib.protocol.matcher.MessageMatcher;
import de.sayayi.lib.protocol.matcher.MessageMatcher.Junction;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static de.sayayi.lib.protocol.matcher.internal.BooleanMatcher.ANY;
import static de.sayayi.lib.protocol.matcher.internal.BooleanMatcher.NONE;
import static java.util.stream.Collectors.joining;


/**
 * @author Jeroen Gremmen
 * @since 1.0.0  (refactored in 1.6.0)
 */
public final class Disjunction implements Junction
{
  private final Set<MessageMatcher> matchers;


  private Disjunction(@NotNull Set<MessageMatcher> matchers) {
    this.matchers = matchers;
  }


  public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> target)
  {
    for(var matcher: matchers)
      if (matcher.matches(levelLimit, target))
        return true;

    return false;
  }


  @Override
  public boolean isTagSelector() {
    return matchers.stream().allMatch(MessageMatcher::isTagSelector);
  }


  @Override
  public boolean equals(Object o) {
    return this == o || o instanceof Disjunction && matchers.equals(((Disjunction)o).matchers);
  }


  @Override
  public int hashCode() {
    return matchers.hashCode();
  }


  @Override
  public String toString()
  {
    return matchers
        .stream()
        .map(MessageMatcher::toString)
        .collect(joining(" or ", "(", ")"));
  }


  @Contract(pure = true)
  public static @NotNull Junction of(@NotNull MessageMatcher... matcher)
  {
    if (matcher.length == 0)
      throw new MessageMatcherException("matcher must not be empty");

    var matchers = new LinkedHashSet<>(List.of(matcher));
    if (matchers.contains(ANY))
      return ANY;

    boolean matchersChanged;
    do {
      matchersChanged = false;

      for(var matcherIterator = matchers.iterator(); matcherIterator.hasNext();)
      {
        var m = matcherIterator.next();
        if (m instanceof Disjunction)
        {
          matcherIterator.remove();
          matchers.addAll(((Disjunction)m).matchers);
          matchersChanged = true;
          break;
        }
      }
    } while(matchersChanged);

    if (matchers.size() > 1)
      matchers.remove(NONE);

    return matchers.size() == 1 ? matchers.iterator().next().asJunction() : new Disjunction(matchers);
  }
}
