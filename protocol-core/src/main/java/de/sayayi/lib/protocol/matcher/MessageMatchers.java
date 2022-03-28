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
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolEntry.Message;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.matcher.MessageMatcher.Junction;

import lombok.NoArgsConstructor;
import lombok.val;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static de.sayayi.lib.protocol.ProtocolFactory.DEFAULT_TAG_NAME;
import static de.sayayi.lib.protocol.matcher.BooleanMatcher.ANY;
import static de.sayayi.lib.protocol.matcher.BooleanMatcher.NONE;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
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


  static final Junction HAS_THROWABLE_MATCHER = new Junction() {
    @Override
    public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
      return message.getThrowable() != null;
    }


    @Override
    public @NotNull Junction asJunction() {
      return this;
    }


    @Override
    public String toString() {
      return "throwable";
    }
  };


  @Contract(pure = true)
  public static @NotNull Junction hasThrowable() {
    return HAS_THROWABLE_MATCHER;
  }


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
      public @NotNull Junction asJunction() {
        return this;
      }


      @Override
      public String toString() {
        return "throwable(" + type.getName() + ')';
      }
    };
  }


  @Contract(value = "_ -> new", pure = true)
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
      public @NotNull Junction asJunction() {
        return this;
      }


      @Override
      public String toString() {
        return "tag(" + tagName + ')';
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
  public static @NotNull Junction is(@NotNull TagSelector tagSelector) {
    return tagSelector.asMessageMatcher().asJunction();
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
      public @NotNull Junction asJunction() {
        return this;
      }


      @Override
      public String toString() {
        return "has-param(" + parameterName + ')';
      }
    };
  }


  @Contract(value = "_ -> new", pure = true)
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
      public @NotNull Junction asJunction() {
        return this;
      }


      @Override
      public String toString() {
        return "has-param-value(" + parameterName + ')';
      }
    };
  }


  @Contract(value = "_, _ -> new", pure = true)
  public static @NotNull Junction hasParamValue(@NotNull String parameterName, Object value)
  {
    if (parameterName.isEmpty())
      return NONE;

    return new Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message)
      {
        val parameterValues = message.getParameterValues();

        return value == null
            ? parameterValues.containsKey(parameterName) && parameterValues.get(parameterName) == null
            : Objects.equals(parameterValues.get(parameterName), value);
      }


      @Override
      public @NotNull Junction asJunction() {
        return this;
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
  @Contract(value = "-> new", pure = true)
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
  @Contract(value = "-> new", pure = true)
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
  @Contract(value = "-> new", pure = true)
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
  @Contract(value = "-> new", pure = true)
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
    return LevelMatcher.of(level);
  }


  /**
   * Create a matcher which checks for messages with the given {@code messageId}.
   *
   * @param messageId  message id to match, not {@code null}
   *
   * @return  matcher instance which checks for messages with the given {@code messageId},
   *          never {@code null}
   */
  @Contract(value = "_ -> new", pure = true)
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
      public @NotNull Junction asJunction() {
        return this;
      }


      @Override
      public String toString() {
        return "message(" + messageId + ')';
      }
    };
  }


  static final Junction IN_GROUP_MATCHER = new Junction() {
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
    if (groupName.isEmpty())
      return IN_GROUP_MATCHER;

    return new Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message)
      {
        val protocol = message.getProtocol();

        return protocol.isProtocolGroup() &&
               groupName.equals(((ProtocolGroup<M>)protocol).getName());
      }


      @Override
      public @NotNull Junction asJunction() {
        return this;
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
    val pattern = Pattern.compile(requireNonNull(groupNameRegex));

    return new Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message)
      {
        val protocol = message.getProtocol();
        if (protocol.isProtocolGroup())
        {
          val groupName = ((ProtocolGroup<M>)protocol).getName();
          return groupName != null && pattern.matcher(groupName).matches();
        }

        return false;
      }


      @Override
      public @NotNull Junction asJunction() {
        return this;
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
    public @NotNull Junction asJunction() {
      return this;
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
    val protocolId = protocol.getId();

    return new Junction() {
      @Override
      public <M> boolean matches(@NotNull Level levelLimit, @NotNull Message<M> message) {
        return message.getProtocol().getId() == protocolId;
      }


      @Override
      public @NotNull Junction asJunction() {
        return this;
      }


      @Override
      public String toString() {
        return "in-protocol(" + protocolId + ')';
      }
    };
  }
}