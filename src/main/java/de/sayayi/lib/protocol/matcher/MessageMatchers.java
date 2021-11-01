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
import de.sayayi.lib.protocol.matcher.MessageMatcher.Junction;

import lombok.NoArgsConstructor;
import lombok.val;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;

import static de.sayayi.lib.protocol.Level.Shared.DEBUG;
import static de.sayayi.lib.protocol.Level.Shared.ERROR;
import static de.sayayi.lib.protocol.Level.Shared.INFO;
import static de.sayayi.lib.protocol.Level.Shared.WARN;
import static de.sayayi.lib.protocol.ProtocolFactory.DEFAULT_TAG_NAME;
import static de.sayayi.lib.protocol.matcher.BooleanMatcher.ANY;
import static de.sayayi.lib.protocol.matcher.BooleanMatcher.NONE;
import static java.util.Arrays.asList;
import static lombok.AccessLevel.PRIVATE;


/**
 * @author Jeroen Gremmen
 * @since 1.0.0
 */
@NoArgsConstructor(access = PRIVATE)
public final class MessageMatchers
{
  @Contract(pure = true)
  public static @NotNull Junction any() {
    return ANY;
  }


  @Contract(pure = true)
  public static @NotNull Junction none() {
    return NONE;
  }


  @Contract(pure = true)
  public static @NotNull Junction not(@NotNull MessageMatcher matcher) {
    return Negation.of(matcher);
  }


  @Contract(pure = true)
  public static @NotNull Junction hasThrowable() {
    return HasThrowableMatcher.INSTANCE;
  }


  @Contract(pure = true)
  public static @NotNull Junction hasThrowable(@NotNull Class<? extends Throwable> type) {
    return HasThrowableMatcher.of(type);
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Junction hasTag(@NotNull TagDef tag) {
    return hasTag(tag.getName());
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Junction hasTag(@NotNull String tagName)
  {
    if (tagName.length() == 0)
      return NONE;
    else if (DEFAULT_TAG_NAME.equals(tagName))
      return ANY;

    return new Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
        return message.hasTag(tagName);
      }


      @Override
      public String toString() {
        return "hasTag(" + tagName + ')';
      }
    };
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Junction hasAnyOf(@NotNull Collection<String> tagNames)
  {
    val uniqueTagNames = new TreeSet<>(tagNames);
    uniqueTagNames.remove("");

    if (uniqueTagNames.remove(DEFAULT_TAG_NAME))
      return ANY;
    else if (uniqueTagNames.isEmpty())
      return NONE;

    return Disjunction.of(uniqueTagNames.stream()
        .map(MessageMatchers::hasTag)
        .toArray(MessageMatcher[]::new));
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Junction hasAnyOf(@NotNull String... tagNames) {
    return hasAnyOf(asList(tagNames));
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Junction hasAllOf(@NotNull Collection<String> tagNames)
  {
    val uniqueTagNames = new TreeSet<>(tagNames);
    val hasDefaultTag = uniqueTagNames.remove(DEFAULT_TAG_NAME);

    if (uniqueTagNames.remove(""))
      return NONE;
    else if (uniqueTagNames.isEmpty())
      return hasDefaultTag ? ANY : NONE;

    return Conjunction.of(uniqueTagNames.stream()
        .map(MessageMatchers::hasTag)
        .toArray(MessageMatcher[]::new));
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Junction hasAllOf(@NotNull String... tagNames) {
    return hasAllOf(asList(tagNames));
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Junction hasNoneOf(@NotNull Collection<String> tagNames) {
    return not(hasAnyOf(tagNames));
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Junction hasNoneOf(@NotNull String... tagNames) {
    return hasNoneOf(asList(tagNames));
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Junction is(@NotNull TagSelector tagSelector)
  {
    return new Junction()
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
  public static @NotNull Junction hasParam(@NotNull String parameterName)
  {
    if (parameterName.isEmpty())
      return NONE;

    return new Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
        return message.getParameterValues().containsKey(parameterName);
      }


      @Override
      public String toString() {
        return "hasParam(" + parameterName + ')';
      }
    };
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Junction hasParamValue(@NotNull String parameterName)
  {
    if (parameterName.length() == 0)
      return NONE;

    return new Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
        return message.getParameterValues().get(parameterName) != null;
      }


      @Override
      public String toString() {
        return "hasParamValue(" + parameterName + ')';
      }
    };
  }


  @Contract(value = "_, _ -> new", pure = true)
  public static @NotNull Junction hasParamValue(@NotNull String parameterName, Object value)
  {
    if (parameterName.length() == 0)
      return NONE;

    return new Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message)
      {
        val parameterValues = message.getParameterValues();

        if (value == null)
        {
          return parameterValues.containsKey(parameterName) &&
                 parameterValues.get(parameterName) == null;
        }
        else
          return Objects.equals(parameterValues.get(parameterName), value);
      }


      @Override
      public String toString() {
        return "hasParamValue(" + parameterName + ',' + value + ')';
      }
    };
  }


  /**
   * Create a matcher which checks for messages with a level which is at least {@code DEBUG}.
   *
   * @return  matcher instance which checks for messages with a level &gt;= {@code DEBUG}
   *
   * @see #is(Level)
   * @see Level.Shared#DEBUG
   */
  @Contract(value = "-> new", pure = true)
  public static @NotNull Junction isDebug() {
    return is(DEBUG);
  }


  /**
   * Create a matcher which checks for messages with a level which is at least {@code INFO}.
   *
   * @return  matcher instance which checks for messages with a level &gt;= {@code INFO}
   *
   * @see #is(Level)
   * @see Level.Shared#INFO
   */
  @Contract(value = "-> new", pure = true)
  public static @NotNull Junction isInfo() {
    return is(INFO);
  }


  /**
   * Create a matcher which checks for messages with a level which is at least {@code WARN}.
   *
   * @return  matcher instance which checks for messages with a level &gt;= {@code WARN}
   *
   * @see #is(Level)
   * @see Level.Shared#WARN
   */
  @Contract(value = "-> new", pure = true)
  public static @NotNull Junction isWarn() {
    return is(WARN);
  }


  /**
   * Create a matcher which checks for messages with a level which is at least {@code ERROR}.
   *
   * @return  matcher instance which checks for messages with a level &gt;= {@code ERROR}
   *
   * @see #is(Level)
   * @see Level.Shared#ERROR
   */
  @Contract(value = "-> new", pure = true)
  public static @NotNull Junction isError() {
    return is(ERROR);
  }


  /**
   * Create a matcher which checks for messages with a level which is at least {@code level}.
   *
   * @param level  lowest level to match, not {@code null}
   *
   * @return  matcher instance which checks for messages with a level &gt;= {@code level}
   */
  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Junction is(@NotNull Level level) {
    return LevelMatcher.of(level);
  }


  /**
   * Create a matcher which checks for messages with the given {@code messageId}.
   *
   * @param messageId  message id to match, not {@code null}
   *
   * @return  matcher instance which checks for messages with the given {@code messageId}
   */
  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Junction hasMessage(@NotNull String messageId)
  {
    if (messageId.length() == 0)
      return NONE;

    return new Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
        return message.getMessageId().equals(messageId);
      }


      @Override
      public String toString() {
        return "hasMessage(" + messageId + ')';
      }
    };
  }
}
