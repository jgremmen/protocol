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
package de.sayayi.lib.protocol.formatter;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolFactory.MessageFormatter;
import de.sayayi.lib.protocol.ProtocolIterator.GroupStartEntry;
import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import de.sayayi.lib.protocol.TagSelector;

import lombok.val;

import org.jetbrains.annotations.NotNull;


/**
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
public abstract class AbstractTreeProtocolFormatter<M> extends AbstractProtocolFormatter<M,String>
{
  private static final String GRAPH_ROOT_NODE_PREFIX = "\u25a0\u2500\u2500";
  private static final String GRAPH_MIDDLE_NODE_PREFIX = "\u251c\u2500\u2500";
  private static final String GRAPH_LAST_NODE_PREFIX = "\u2514\u2500\u2500";

  private static final String GRAPH_VERTICAL_BAR = "\u2502\n";

  private static final String GRAPH_LEVEL_SEPARATOR_BAR = "\u2502  ";
  private static final String GRAPH_LEVEL_SEPARATOR_EMPTY = "   ";

  private final StringBuilder result;

  protected MessageFormatter<M> messageFormatter;
  private String[] prefixes;


  @SuppressWarnings("WeakerAccess")
  protected AbstractTreeProtocolFormatter() {
    this.result = new StringBuilder();
  }


  @Override
  public void init(@NotNull ProtocolFactory<M> factory, @NotNull Level level, @NotNull TagSelector tagSelector,
                   int estimatedGroupDepth)
  {
    result.delete(0, result.length());

    messageFormatter = factory.getMessageFormatter();

    prefixes = new String[estimatedGroupDepth + 1];
    prefixes[0] = "";
  }


  @Override
  public void message(@NotNull MessageEntry<M> message)
  {
    val depth = message.getDepth();

    if (depth == 0 && message.isFirst())
      result.append(GRAPH_ROOT_NODE_PREFIX);
    else
    {
      val prefix = prefixes[depth];

      result.append(prefix).append(GRAPH_VERTICAL_BAR)
            .append(prefix).append(message.isLast() ? GRAPH_LAST_NODE_PREFIX : GRAPH_MIDDLE_NODE_PREFIX);
    }

    result.append(messageFormatter.formatMessage(message)).append('\n');
  }


  @Override
  public void groupStart(@NotNull GroupStartEntry<M> group)
  {
    val depth = group.getDepth();
    val prefix = prefixes[depth - 1];

    if (depth == 1 && group.isFirst())
      result.append(GRAPH_ROOT_NODE_PREFIX);
    else
    {
      result.append(prefix).append(GRAPH_VERTICAL_BAR)
            .append(prefix).append(group.isLast() ? GRAPH_LAST_NODE_PREFIX : GRAPH_MIDDLE_NODE_PREFIX);
    }

    result.append(messageFormatter.formatMessage(group.getGroupMessage())).append('\n');

    prefixes[depth] = prefix + (group.isLast() ? GRAPH_LEVEL_SEPARATOR_EMPTY : GRAPH_LEVEL_SEPARATOR_BAR);
  }


  @Override
  public @NotNull String getResult() {
    return result.toString();
  }
}
