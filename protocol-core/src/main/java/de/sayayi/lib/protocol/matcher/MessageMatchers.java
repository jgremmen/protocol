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
package de.sayayi.lib.protocol.matcher;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolEntry.Message;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.matcher.MessageMatcher.Junction;
import de.sayayi.lib.protocol.matcher.internal.Conjunction;
import de.sayayi.lib.protocol.matcher.internal.Disjunction;
import de.sayayi.lib.protocol.matcher.internal.LevelMatcher;
import de.sayayi.lib.protocol.matcher.internal.Negation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;
import static de.sayayi.lib.protocol.Level.compare;
import static de.sayayi.lib.protocol.Level.min;
import static de.sayayi.lib.protocol.ProtocolFactory.DEFAULT_TAG_NAME;
import static de.sayayi.lib.protocol.matcher.internal.BooleanMatcher.ANY;
import static de.sayayi.lib.protocol.matcher.internal.BooleanMatcher.NONE;
import static java.util.Objects.requireNonNull;


/**
 * A comprehensive collection of message matchers.
 *
 * @author Jeroen Gremmen
 * @since 1.0.0
 */
public final class MessageMatchers
{
  private MessageMatchers() {
  }


  /**
   * Return a message matcher which matches every message.
   *
   * @return  matcher instance which matches every message, never {@code null}
   */
  @Contract(pure = true)
  public static @NotNull Junction any() {
    return ANY;
  }


  /**
   * Return a message matcher which matches no message.
   *
   * @return  matcher instance which matches no message, never {@code null}
   */
  @Contract(pure = true)
  public static @NotNull Junction none() {
    return NONE;
  }


  @Contract(pure = true)
  public static @NotNull Junction not(@NotNull MessageMatcher matcher) {
    return Negation.of(requireNonNull(matcher));
  }


  static final Junction HAS_THROWABLE_MATCHER = new Junction() {
    @Override
    public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
      return message.getThrowable() != null;
    }


    @Override
    public String toString() {
      return "throwable";
    }
  };


  /**
   * Return a message matcher which matches every message having a throwable associated with it.
   *
   * @return  throwable message matcher, never {@code null}
   */
  @Contract(pure = true)
  public static @NotNull Junction hasThrowable() {
    return HAS_THROWABLE_MATCHER;
  }


  /**
   * Return a message matcher which matches every message having a throwable of the given
   * {@code type} associated with it.
   *
   * @param type  throwable type to check for, not {@code null}
   *
   * @return  throwable message matcher, never {@code null}
   */
  @Contract(pure = true)
  public static @NotNull Junction hasThrowable(@NotNull Class<? extends Throwable> type)
  {
    if (requireNonNull(type) == Throwable.class)
      return HAS_THROWABLE_MATCHER;

    return new Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
        return type.isInstance(message.getThrowable());
      }


      @Override
      public String toString() {
        return "throwable(" + type.getName() + ')';
      }
    };
  }


  @Contract(pure = true)
  public static @NotNull Junction hasTag(@NotNull String tagName)
  {
    if (tagName.isEmpty())
      return NONE;
    else if (DEFAULT_TAG_NAME.equals(tagName))
      return ANY;

    return new Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
        return message.hasTag(tagName);
      }


      @Override
      public boolean isTagSelector() {
        return true;
      }


      @Override
      public String toString() {
        return "tag(" + tagName + ')';
      }
    };
  }


  @Contract(pure = true)
  public static @NotNull Junction hasAnyOf(@NotNull Collection<String> tagNames)
  {
    final var uniqueTagNames = new TreeSet<>(tagNames);
    uniqueTagNames.remove("");

    if (uniqueTagNames.remove(DEFAULT_TAG_NAME))
      return ANY;
    else if (uniqueTagNames.isEmpty())
      return NONE;

    return Disjunction.of(uniqueTagNames.stream()
        .map(MessageMatchers::hasTag)
        .toArray(MessageMatcher[]::new));
  }


  @Contract(pure = true)
  public static @NotNull Junction hasAnyOf(@NotNull String... tagNames) {
    return hasAnyOf(List.of(tagNames));
  }


  @Contract(pure = true)
  public static @NotNull Junction hasAllOf(@NotNull Collection<String> tagNames)
  {
    final var uniqueTagNames = new TreeSet<>(tagNames);
    final var hasDefaultTag = uniqueTagNames.remove(DEFAULT_TAG_NAME);

    if (uniqueTagNames.remove(""))
      return NONE;
    else if (uniqueTagNames.isEmpty())
      return hasDefaultTag ? ANY : NONE;

    return Conjunction.of(uniqueTagNames.stream()
        .map(MessageMatchers::hasTag)
        .toArray(MessageMatcher[]::new));
  }


  @Contract(pure = true)
  public static @NotNull Junction hasAllOf(@NotNull String... tagNames) {
    return hasAllOf(List.of(tagNames));
  }


  @Contract(pure = true)
  public static @NotNull Junction hasNoneOf(@NotNull Collection<String> tagNames) {
    return not(hasAnyOf(tagNames));
  }


  @Contract(pure = true)
  public static @NotNull Junction hasNoneOf(@NotNull String... tagNames) {
    return hasNoneOf(List.of(tagNames));
  }


  @Contract(pure = true)
  public static @NotNull Junction is(@NotNull TagSelector tagSelector) {
    return tagSelector.asMessageMatcher().asJunction();
  }


  @Contract(pure = true)
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
        return "has-param(" + parameterName + ')';
      }
    };
  }


  @Contract(pure = true)
  public static @NotNull Junction hasParamValue(@NotNull String parameterName)
  {
    if (parameterName.isEmpty())
      return NONE;

    return new Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
        return message.getParameterValues().get(parameterName) != null;
      }


      @Override
      public String toString() {
        return "has-param-value(" + parameterName + ')';
      }
    };
  }


  @Contract(pure = true)
  public static @NotNull Junction hasParamValue(@NotNull String parameterName, Object value)
  {
    if (parameterName.isEmpty())
      return NONE;

    return new Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message)
      {
        var parameterValues = message.getParameterValues();

        return value == null
            ? parameterValues.containsKey(parameterName) && parameterValues.get(parameterName) == null
            : Objects.equals(parameterValues.get(parameterName), value);
      }


      @Override
      public String toString() {
        return "has-param-value(" + parameterName + ',' + value + ')';
      }
    };
  }


  /**
   * Create a matcher which checks for messages with a level which is at least {@code DEBUG}.
   *
   * @return  matcher instance which checks for messages with a level &gt;= {@code DEBUG},
   *          never {@code null}
   *
   * @see #is(Level)
   * @see Level.Shared#DEBUG
   */
  @Contract(pure = true)
  public static @NotNull Junction isDebug() {
    return LevelMatcher.DEBUG;
  }


  /**
   * Create a matcher which checks for messages with a level which is at least {@code INFO}.
   *
   * @return  matcher instance which checks for messages with a level &gt;= {@code INFO},
   *          never {@code null}
   *
   * @see #is(Level)
   * @see Level.Shared#INFO
   */
  @Contract(pure = true)
  public static @NotNull Junction isInfo() {
    return LevelMatcher.INFO;
  }


  /**
   * Create a matcher which checks for messages with a level which is at least {@code WARN}.
   *
   * @return  matcher instance which checks for messages with a level &gt;= {@code WARN},
   *          never {@code null}
   *
   * @see #is(Level)
   * @see Level.Shared#WARN
   */
  @Contract(pure = true)
  public static @NotNull Junction isWarn() {
    return LevelMatcher.WARN;
  }


  /**
   * Create a matcher which checks for messages with a level which is at least {@code ERROR}.
   *
   * @return  matcher instance which checks for messages with a level &gt;= {@code ERROR},
   *          never {@code null}
   *
   * @see #is(Level)
   * @see Level.Shared#ERROR
   */
  @Contract(pure = true)
  public static @NotNull Junction isError() {
    return LevelMatcher.ERROR;
  }


  /**
   * Create a matcher which checks for messages with a level which is at least {@code level}.
   *
   * @param level  lowest level to match, not {@code null}
   *
   * @return  matcher instance which checks for messages with level &gt;= {@code level},
   *          never {@code null}
   */
  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Junction is(@NotNull Level level) {
    return LevelMatcher.of(requireNonNull(level));
  }


  /**
   * Create a matcher which checks for messages with a level between {@code levelLow} and
   * {@code levelHigh}.
   *
   * @param levelLow   lowest level to match, not {@code null}
   * @param levelHigh  highest level to match, not {@code null}
   *
   * @return  matcher instance which checks for messages with level between {@code levelLow}
   *          and {@code levelHigh}
   */
  @Contract(pure = true)
  public static @NotNull Junction between(@NotNull Level levelLow, @NotNull Level levelHigh)
  {
    if (Level.equals(levelHigh, HIGHEST))
      return is(levelLow);

    return new Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message)
      {
        var messageLevel = min(message.getLevel(), levelLimit);
        return compare(messageLevel, levelLow) >= 0 && compare(messageLevel, levelHigh) <= 0;
      }


      @Override
      public String toString() {
        return "between(" + levelLow + "," + levelHigh + ')';
      }
    };
  }


  /**
   * Create a matcher which checks for messages with the given {@code messageId}.
   *
   * @param messageId  message id to match, not {@code null}
   *
   * @return  matcher instance which checks for messages with the given {@code messageId},
   *          never {@code null}
   */
  @Contract(pure = true)
  public static @NotNull Junction hasMessage(@NotNull String messageId)
  {
    if (messageId.isEmpty())
      return NONE;

    return new Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
        return message.getMessageId().equals(messageId);
      }


      @Override
      public String toString() {
        return "message(" + messageId + ')';
      }
    };
  }


  private static final Junction IN_GROUP_MATCHER = new Junction() {
    @Override
    public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
      return message.getProtocol().isProtocolGroup();
    }


    @Override
    public String toString() {
      return "in-group";
    }
  };


  /**
   * Create a matcher which checks for messages that are contained in a protocol group.
   *
   * @return  matcher instance which checks for messages that are contained in a protocol group,
   *          never {@code null}
   *
   * @see #inGroup(String)
   *
   * @since 1.1.0
   */
  @Contract(pure = true)
  public static @NotNull Junction inGroup() {
    return IN_GROUP_MATCHER;
  }


  /**
   * <p>
   *   Create a matcher which checks for messages that are contained in a protocol group with
   *   name equal to {@code groupName}.
   * </p>
   * <p>
   *   If {@code groupName} is empty, any protocol group will match, regardless of its name.
   * </p>
   *
   * @param groupName  name of the protocol group name to match, not {@code null}
   *
   * @return  matcher instance which checks for messages that are contained in a named
   *          protocol group, never {@code null}
   *
   * @see #inGroup()
   *
   * @since 1.1.0
   */
  @Contract(pure = true)
  public static @NotNull Junction inGroup(@NotNull String groupName)
  {
    if (requireNonNull(groupName).isEmpty())
      return IN_GROUP_MATCHER;

    return new Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message)
      {
        var protocol = message.getProtocol();
        return protocol.isProtocolGroup() && groupName.equals(((ProtocolGroup<M>)protocol).getName());
      }


      @Override
      public String toString() {
        return "in-group('" + groupName + "')";
      }
    };
  }


  /**
   * <p>
   *   Create a matcher which checks for messages that are contained in a protocol group with
   *   a name that matches {@code groupNameRegex}.
   * </p>
   *
   * @param groupNameRegex  regular expression for protocol group name to match, not {@code null}
   *
   * @return  matcher instance which checks for messages that are contained in a named
   *          protocol group, never {@code null}
   *
   * @since 1.1.0
   */
  @Contract(pure = true)
  public static @NotNull Junction inGroupRegex(@NotNull String groupNameRegex)
  {
    final var pattern = Pattern.compile(requireNonNull(groupNameRegex));

    return new Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message)
      {
        var protocol = message.getProtocol();
        if (protocol.isProtocolGroup())
        {
          var groupName = ((ProtocolGroup<M>)protocol).getName();
          return groupName != null && pattern.matcher(groupName).matches();
        }

        return false;
      }


      @Override
      public String toString() {
        return "in-group-regex('" + groupNameRegex + "')";
      }
    };
  }


  static final Junction IN_ROOT_MATCHER = new Junction() {
    @Override
    public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
      return message.getProtocol().getParent() == null;
    }


    @Override
    public String toString() {
      return "in-root";
    }
  };


  /**
   * Create a matcher which checks for messages that are in the root protocol.
   *
   * @return  matcher instance which checks for messages that are in the root protocol,
   *          never {@code null}
   *
   * @since 1.1.0
   */
  @Contract(pure = true)
  public static @NotNull Junction inRoot() {
    return IN_ROOT_MATCHER;
  }


  /**
   * Create a matcher which checks for messages which are contained in a specific protocol instance.
   *
   * @param protocol  protocol instance to match, not {@code null}
   *
   * @return  matcher instance which checks for messages that are contained in a specific
   *          protocol instance, never {@code null}
   *
   * @since 1.1.0
   */
  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Junction inProtocol(@NotNull Protocol<?> protocol)
  {
    final var protocolFactory = protocol.getFactory();
    final var protocolId = protocol.getId();

    return new Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message)
      {
        var protocol = message.getProtocol();
        return protocol.getFactory() == protocolFactory && protocol.getId() == protocolId;
      }


      @Override
      public String toString() {
        return "in-protocol(" + protocolId + ')';
      }
    };
  }
}
