/*
 * Copyright 2019 Jeroen Gremmen
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
package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.Tag.MatchCondition;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;


/**
 *
 * @param <M>  Internal message object type. Messages are added by providing a string. The factory converts this
 *             string in the appropriate internal format (see {@link #processMessage(String)}), allowing for various
 *             message retrieval/formatting libraries to be used.
 *
 * @author Jeroen Gremmen
 */
@SuppressWarnings("squid:S1214")
public interface ProtocolFactory<M>
{
  interface Constant
  {
    /**
     * Name of the default tag.
     *
     * @see #getDefaultTag()
     */
    String DEFAULT_TAG_NAME = "default";
  }


  /**
   * Create a new protocol instance.
   *
   * @return  new protocol instance, never {@code null}.
   */
  @Contract("-> new")
  @NotNull Protocol<M> createProtocol();


  @Contract("_ -> new")
  @NotNull TagBuilder<M> createTag(@NotNull String name);


  @Contract(pure = true, value = "_ -> new")
  @NotNull TagBuilder<M> modifyTag(@NotNull String name);


  @Contract(pure = true)
  Tag getTagByName(@NotNull String name);


  @Contract(pure = true)
  boolean hasTag(@NotNull String name);


  /**
   * Tells whether {@code tag} is registered with this factory.
   *
   * @param tag  tag to check
   *
   * @return  {@code true} if this tag is registered with this factory, {@code false} otherwise
   */
  @Contract(pure = true)
  boolean isRegisteredTag(@NotNull Tag tag);


  /**
   * Returns a set of all tags registered with this factory.
   *
   * @return  set of all tags registered with this factory, never {@code null}
   */
  @Contract(pure = true, value = "-> new")
  @NotNull Set<Tag> getTags();


  /**
   * Returns the default tag which is used for each message protocolled.
   *
   * @return  default tag, never {@code null}
   */
  @Contract(pure = true)
  @NotNull Tag getDefaultTag();


  /**
   * returns a map with parameter name/value entries which will be available to every message created by protocol
   * instances belonging directly or indirectly to this factory.
   *
   * @return  map with default name/value entries, never {@code null}
   */
  @Contract(pure = true)
  @NotNull Map<String,Object> getDefaultParameterValues();


  /**
   * Transforms the given message into its internal representation.
   * <p>
   * The simplest implementation would be to return the message as is. However this method provides a way to
   * integrate more complex message retrieval and/or formatting strategies:
   *
   * <ul>
   *   <li>The {@code message} could be a resource key which is used to lookup the actual message text</li>
   *   <li>The {@code message} could be a Spring Expression and the returned object would be a compiled expression</li>
   *   <li>Syntax analysis can be performed on the message</li>
   * </ul>
   *
   * @param message  message, not {@code null}
   *
   * @return  internal representation for {@code message}, never {@code null}
   */
  @Contract(pure = true)
  @NotNull M processMessage(@NotNull String message);




  /**
   *
   * @param <M>  internal message object type
   */
  @SuppressWarnings("UnusedReturnValue")
  interface TagBuilder<M> extends ProtocolFactory<M>
  {
    @Contract("_ -> this")
    @NotNull TagBuilder<M> dependsOn(@NotNull String ... tags);


    @Contract("_ -> this")
    @NotNull TagBuilder<M> implies(@NotNull String ... tags);


    @Contract("_, _ -> this")
    @NotNull TagBuilder<M> match(@NotNull MatchCondition matchCondition, @NotNull Level matchLevel);


    /**
     * Returns the tag instance build by this builder.
     *
     * @return  tag instance
     */
    @Contract(pure = true)
    @NotNull Tag getTag();
  }
}
