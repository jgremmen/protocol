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
import de.sayayi.lib.protocol.Protocol.MessageWithLevel;
import de.sayayi.lib.protocol.ProtocolFormatter.InitializableProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolIterator.GroupEndEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupEntry;
import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import de.sayayi.lib.protocol.Tag;
import org.jetbrains.annotations.NotNull;
import org.unbescape.html.HtmlEscape;

import java.util.Arrays;


/**
 * @author Jeroen Gremmen
 */
@SuppressWarnings("unused")
public abstract class HtmlProtocolFormatter<M> implements InitializableProtocolFormatter<M,String>
{
  private final StringBuilder html;


  protected HtmlProtocolFormatter() {
    this.html = new StringBuilder();
  }


  @SuppressWarnings("WeakerAccess")
  protected abstract String formatMessage(MessageWithLevel<M> message);


  @SuppressWarnings("WeakerAccess")
  protected String levelToHtmlClass(Level level)
  {
    if (level instanceof Enum)
      return ((Enum)level).name();

    return level.toString();
  }


  @Override
  public void protocolStart()
  {
    html.append("<div class=\"protocol\">\n")
        .append("  <ul class=\"depth-0\">\n");
  }


  @Override
  public void protocolEnd()
  {
    html.append("  </ul>\n")
        .append("</div>\n");
  }


  @Override
  public void message(@NotNull MessageEntry<M> message)
  {
    String msg = formatMessage(message);

    indent(message.getDepth());
    html.append("<li class=\"level-").append(levelToHtmlClass(message.getLevel()))
        .append("\"><span class=\"").append(message.isGroupMessage() ? "group-message " : "").append("message\">")
        .append(HtmlEscape.escapeHtml5(msg)).append("</span></li>\n");
  }


  @Override
  public void groupStart(@NotNull GroupEntry<M> group)
  {
    int depth = group.getDepth();
    MessageWithLevel<M> message = group.getGroupHeader();
    String msg = formatMessage(group.getGroupHeader());

    indent(depth - 1);
    html.append("<li class=\"level-").append(levelToHtmlClass(message.getLevel()))
        .append("\"><span class=\"group\">").append(HtmlEscape.escapeHtml5(msg)).append("</span></li>\n");

    indent(depth - 1);
    html.append("<ul class=\"depth-").append(depth).append(" group\">\n");
  }


  @Override
  public void groupEnd(GroupEndEntry<M> groupEnd)
  {
    indent(groupEnd.getDepth() - 1);
    html.append("</ul>\n");
  }


  @Override
  public @NotNull String getResult() {
    return html.toString();
  }


  @Override
  public void init(@NotNull Level level, @NotNull Tag tag, int estimatedGroupDepth) {
    html.delete(0, html.length());
  }


  private void indent(int depth)
  {
    char[] spaces = new char[(depth + 2) * 2];
    Arrays.fill(spaces, ' ');

    html.append(spaces);
  }
}
