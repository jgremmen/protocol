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

import de.sayayi.lib.protocol.TagDef.MatchCondition;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;


/**
 *
 * @param <M>  Internal message object type. Messages are added by providing a string. The factory converts this
 *             string in the appropriate internal format (see {@link MessageProcessor}), allowing for various
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


  @Contract(pure = true)
  @NotNull MessageProcessor<M> getMessageProcessor();


  void setMessageProcessor(@NotNull MessageProcessor<M> messageProcessor);


  /**
   * Create a new protocol instance.
   *
   * @return  new protocol instance, never {@code null}.
   */
  @Contract("-> new")
  @NotNull Protocol<M> createProtocol();


  @Contract(value = "null -> false", pure = true)
  boolean isValidTagName(String tagName);


  @Contract("_ -> new")
  @NotNull TagBuilder<M> createTag(@NotNull String name);


  @Contract(pure = true, value = "_ -> new")
  @NotNull TagBuilder<M> modifyTag(@NotNull String name);


  @Contract(pure = true)
  @NotNull TagDef getTagByName(@NotNull String name);


  @Contract(value = "null -> false", pure = true)
  boolean hasTag(String name);


  /**
   * Returns a set of all tag names registered with this factory.
   *
   * @return  set of all tag names registered with this factory, never {@code null}
   */
  @Contract(pure = true, value = "-> new")
  @NotNull Set<String> getTagNames();


  /**
   * Returns a set of all tags registered with this factory.
   *
   * @return  set of all tags registered with this factory, never {@code null}
   */
  @Contract(pure = true, value = "-> new")
  @NotNull Set<TagDef> getTagDefs();


  /**
   * Returns the default tag which is used for each message protocolled.
   *
   * @return  default tag, never {@code null}
   */
  @Contract(pure = true)
  @NotNull TagDef getDefaultTag();


  /**
   * returns a map with parameter name/value entries which will be available to every message created by protocol
   * instances belonging directly or indirectly to this factory.
   *
   * @return  map with default name/value entries, never {@code null}
   */
  @Contract(pure = true)
  @NotNull Map<String,Object> getDefaultParameterValues();




  /**
   * @param <M>  internal message object type
   */
  @SuppressWarnings("UnusedReturnValue")
  interface TagBuilder<M> extends ProtocolFactory<M>
  {
    @Contract("_ -> this")
    @NotNull TagBuilder<M> dependsOn(@NotNull String ... tagNames);


    @Contract("_ -> this")
    @NotNull TagBuilder<M> implies(@NotNull String ... tagNames);


    @Contract("_, _ -> this")
    @NotNull TagBuilder<M> match(@NotNull MatchCondition matchCondition, @NotNull Level matchLevel);


    /**
     * Returns the tag definition instance build by this builder.
     *
     * @return  tag definition instance
     */
    @Contract(pure = true)
    @NotNull TagDef getTagDef();
  }




  /**
   * @param <M>  internal message object type
   */
  interface MessageProcessor<M>
  {
    @Contract(pure = true)
    @NotNull M processMessage(@NotNull String message);
  }
}
