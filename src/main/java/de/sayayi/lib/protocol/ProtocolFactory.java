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

import de.sayayi.lib.protocol.Tag.LevelMatch;

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
public interface ProtocolFactory<M>
{
  /**
   * Name of the default tag.
   *
   * @see #getDefaultTag()
   */
  String DEFAULT_TAG_NAME = "default";


  /**
   * Create a new protocol instance.
   *
   * @return  new protocol instance
   */
  Protocol<M> createProtocol();


  TagBuilder createTag(String name);


  TagBuilder modifyTag(String name);


  Tag getTagByName(String name);


  boolean hasTag(String name);


  boolean isRegisteredTag(Tag tag);


  Set<Tag> getTags();


  /**
   * Returns the default tag which is used for each message protocolled.
   *
   * @return  default tag
   */
  Tag getDefaultTag();


  Map<String,Object> getDefaultParameterValues();


  /**
   * Transform the given message into its internal representation.
   * <p>
   * The simplest implementation would be to return the message as is. However this method provides a way to
   * integrate more complex message retrieval and/or formatting strategies:
   *
   * <ul>
   *   <li>The {@code message} could be a resource key which is used to lookup the actual message text</li>
   *   <li>Syntax analysis can be performed on the message</li>
   *   <li>The {@code message} could be a Spring Expression and the returned object would be a compiled expression</li>
   * </ul>
   *
   * @param message  message
   *
   * @return  internal representation for {@code message}
   */
  M processMessage(String message);


  @SuppressWarnings("UnusedReturnValue")
  interface TagBuilder<M> extends ProtocolFactory<M>
  {
    TagBuilder dependsOn(String ... tags);


    TagBuilder implies(String ... tags);


    TagBuilder match(LevelMatch match, Level level);


    /**
     * Returns the tag instance build by this builder.
     *
     * @return  tag instance
     */
    Tag getTag();
  }
}
