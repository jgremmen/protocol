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
import de.sayayi.lib.protocol.TagDef;
import de.sayayi.lib.protocol.TagSelector;

import lombok.NoArgsConstructor;
import lombok.val;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;

import static de.sayayi.lib.protocol.Level.Shared.DEBUG;
import static de.sayayi.lib.protocol.Level.Shared.ERROR;
import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;
import static de.sayayi.lib.protocol.Level.Shared.INFO;
import static de.sayayi.lib.protocol.Level.Shared.LOWEST;
import static de.sayayi.lib.protocol.Level.Shared.WARN;
import static de.sayayi.lib.protocol.ProtocolFactory.DEFAULT_TAG_NAME;
import static lombok.AccessLevel.PRIVATE;


/**
 * @author Jeroen Gremmen
 * @since 1.0.0
 */
@NoArgsConstructor(access = PRIVATE)
public final class MessageMatchers
{
  @Contract(pure = true)
  public static @NotNull MessageMatcher.Junction any() {
    return BooleanMatcher.TRUE;
  }


  @Contract(pure = true)
  public static @NotNull MessageMatcher.Junction none() {
    return BooleanMatcher.FALSE;
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MessageMatcher.Junction not(MessageMatcher matcher)
  {
    if (matcher == BooleanMatcher.FALSE)
      return BooleanMatcher.TRUE;
    else if (matcher == BooleanMatcher.TRUE)
      return BooleanMatcher.FALSE;
    else
      return new NegatingMatcher(matcher);
  }


  @Contract(pure = true)
  public static @NotNull MessageMatcher.Junction hasThrowable() {
    return HasThrowableMatcher.INSTANCE;
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MessageMatcher.Junction hasTag(@NotNull TagDef tag) {
    return hasTag(tag.getName());
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MessageMatcher.Junction hasTag(@NotNull String tagName)
  {
    if (tagName.length() == 0)
      return BooleanMatcher.FALSE;
    else if (DEFAULT_TAG_NAME.equals(tagName))
      return BooleanMatcher.TRUE;

    return new MessageMatcher.Junction.AbstractBase() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
        return message.getTagNames().contains(tagName);
      }


      @Override
      public String toString() {
        return "hasTag(" + tagName + ')';
      }
    };
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MessageMatcher.Junction hasAnyOf(@NotNull Collection<String> tagNames)
  {
    val uniqueTagNames = new TreeSet<>(tagNames);
    uniqueTagNames.remove("");

    if (uniqueTagNames.remove(DEFAULT_TAG_NAME))
      return BooleanMatcher.TRUE;
    else if (uniqueTagNames.isEmpty())
      return BooleanMatcher.FALSE;

    return new MessageMatcher.Disjunction(uniqueTagNames.stream()
        .map(MessageMatchers::hasTag)
        .toArray(MessageMatcher[]::new));
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MessageMatcher.Junction hasAnyOf(@NotNull String... tagNames) {
    return hasAnyOf(Arrays.asList(tagNames));
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MessageMatcher.Junction hasAllOf(@NotNull Collection<String> tagNames)
  {
    val uniqueTagNames = new TreeSet<>(tagNames);
    val hasDefaultTag = uniqueTagNames.remove(DEFAULT_TAG_NAME);

    if (uniqueTagNames.remove(""))
      return BooleanMatcher.FALSE;
    else if (uniqueTagNames.isEmpty())
      return hasDefaultTag ? BooleanMatcher.TRUE : BooleanMatcher.FALSE;

    return new MessageMatcher.Conjunction(uniqueTagNames.stream()
        .map(MessageMatchers::hasTag)
        .toArray(MessageMatcher[]::new));
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MessageMatcher.Junction hasAllOf(@NotNull String... tagNames) {
    return hasAllOf(Arrays.asList(tagNames));
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MessageMatcher.Junction forSelector(@NotNull TagSelector tagSelector)
  {
    return new MessageMatcher.Junction.AbstractBase()
    {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
        return tagSelector.match(message.getTagNames());
      }


      @Override
      public String toString() {
        return tagSelector.toString();
      }
    };
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MessageMatcher.Junction hasParameter(@NotNull String parameterName)
  {
    if (parameterName.length() == 0)
      return BooleanMatcher.FALSE;

    return new MessageMatcher.Junction.AbstractBase() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
        return message.getParameterValues().containsKey(parameterName);
      }


      @Override
      public String toString() {
        return "hasParameter(" + parameterName + ')';
      }
    };
  }


  @Contract(value = "_, _ -> new", pure = true)
  public static @NotNull MessageMatcher.Junction hasValue(@NotNull String parameterName, Object value)
  {
    if (parameterName.length() == 0)
      return BooleanMatcher.FALSE;

    return new MessageMatcher.Junction.AbstractBase() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message)
      {
        val parameterValues = message.getParameterValues();

        return parameterValues.containsKey(parameterName) &&
               Objects.equals(parameterValues.get(parameterName), value);
      }


      @Override
      public String toString() {
        return "hasValue(" + parameterName + ',' + value + ')';
      }
    };
  }


  @Contract(pure = true)
  public static @NotNull MessageMatcher.Junction isLowest() {
    return isLevel(LOWEST);
  }


  @Contract(value = "-> new", pure = true)
  public static @NotNull MessageMatcher.Junction isDebug() {
    return isLevel(DEBUG);
  }


  @Contract(value = "-> new", pure = true)
  public static @NotNull MessageMatcher.Junction isInfo() {
    return isLevel(INFO);
  }


  @Contract(value = "-> new", pure = true)
  public static @NotNull MessageMatcher.Junction isWarn() {
    return isLevel(WARN);
  }


  @Contract(value = "-> new", pure = true)
  public static @NotNull MessageMatcher.Junction isError() {
    return isLevel(ERROR);
  }


  @Contract(value = "-> new", pure = true)
  public static @NotNull MessageMatcher.Junction isHighest() {
    return isLevel(HIGHEST);
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MessageMatcher.Junction isLevel(@NotNull Level level) {
    return LOWEST.severity() == level.severity() ? BooleanMatcher.TRUE : new LevelMatcher(level);
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MessageMatcher.Junction hasMessageId(@NotNull String messageId)
  {
    if (messageId.length() == 0)
      return BooleanMatcher.FALSE;

    return new MessageMatcher.Junction.AbstractBase() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
        return message.getMessageId().equals(messageId);
      }


      @Override
      public String toString() {
        return "hasMessageId(" + messageId + ')';
      }
    };
  }
}
