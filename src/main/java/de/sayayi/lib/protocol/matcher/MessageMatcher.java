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

import lombok.val;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;


/**
 * @author Jeroen Gremmen
 * @since 1.0.0
 */
public interface MessageMatcher
{
  <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message);


  default <M> boolean matches(@NotNull Message<M> message) {
    return matches(Level.Shared.HIGHEST, message);
  }




  interface Junction extends MessageMatcher
  {
    @NotNull Junction and(@NotNull MessageMatcher other);


    @NotNull Junction or(@NotNull MessageMatcher other);
  }




  abstract class AbstractBase implements Junction
  {
    @Override
    public @NotNull Junction and(@NotNull MessageMatcher other) {
      return new Conjunction(this, other);
    }


    @Override
    public @NotNull Junction or(@NotNull MessageMatcher other) {
      return new Disjunction(this, other);
    }
  }




  class Conjunction extends AbstractBase
  {
    private final List<MessageMatcher> matchers;


    Conjunction(MessageMatcher... matcher) {
      this(Arrays.asList(matcher));
    }


    Conjunction(List<MessageMatcher> matchers)
    {
      this.matchers = new ArrayList<>(matchers.size());

      for(val matcher: matchers)
      {
        if (matcher instanceof Conjunction)
          this.matchers.addAll(((Conjunction)matcher).matchers);
        else
          this.matchers.add(matcher);
      }
    }


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
  }




  class Disjunction extends AbstractBase
  {
    private final List<MessageMatcher> matchers;


    Disjunction(MessageMatcher... matcher) {
      this(Arrays.asList(matcher));
    }


    Disjunction(List<MessageMatcher> matchers)
    {
      this.matchers = new ArrayList<>(matchers.size());

      for(val matcher: matchers)
      {
        if (matcher instanceof Disjunction)
          this.matchers.addAll(((Disjunction)matcher).matchers);
        else
          this.matchers.add(matcher);
      }
    }


    public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> target)
    {
      for(val matcher: matchers)
        if (matcher.matches(levelLimit, target))
          return true;

      return false;
    }


    @Override
    public String toString()
    {
      return matchers.stream()
          .map(MessageMatcher::toString)
          .collect(joining(" or ", "(", ")"));
    }
  }
}
