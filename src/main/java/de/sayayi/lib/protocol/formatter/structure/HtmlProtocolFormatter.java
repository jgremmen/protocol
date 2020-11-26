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
package de.sayayi.lib.protocol.formatter.structure;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol.MessageWithLevel;
import de.sayayi.lib.protocol.ProtocolFormatter.InitializableProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolIterator.GroupEndEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupStartEntry;
import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.formatter.MessageFormatter;

import lombok.val;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static de.sayayi.lib.protocol.Level.SORT_DESCENDING;
import static org.unbescape.html.HtmlEscape.escapeHtml5;


/**
 * @author Jeroen Gremmen
 */
@SuppressWarnings("unused")
public class HtmlProtocolFormatter<M> implements InitializableProtocolFormatter<M,String>
{
  private final MessageFormatter<M> messageFormatter;
  private final StringBuilder html;


  public HtmlProtocolFormatter(@NotNull MessageFormatter<M> messageFormatter)
  {
    this.messageFormatter = messageFormatter;
    this.html = new StringBuilder();
  }


  @SuppressWarnings("WeakerAccess")
  protected String levelToHtmlClass(@NotNull Level level) {
    return level.toString().toLowerCase();
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
    val msg = messageFormatter.formatMessage(message);

    indent(message.getDepth());

    html.append("<li class=\"level-").append(levelToHtmlClass(message.getLevel()))
        .append("\"><span class=\"")
        .append(message.isGroupMessage() ? "group-message " : "").append("message\">")
        .append(messagePrefixHtml(message)).append(escapeHtml5(msg)).append("</span></li>\n");
  }


  @SuppressWarnings("java:S3400")
  protected String messagePrefixHtml(@NotNull MessageEntry<M> message) {
    return "";
  }


  @Override
  public void groupStart(@NotNull GroupStartEntry<M> group)
  {
    val depth = group.getDepth();
    val message = group.getGroupMessage();
    val msg = messageFormatter.formatMessage(group.getGroupMessage());

    indent(depth - 1);

    html.append("<li class=\"level-").append(levelToHtmlClass(message.getLevel()))
        .append("\"><span class=\"group\">").append(groupPrefixHtml(group))
        .append(escapeHtml5(msg)).append("</span></li>\n");

    indent(depth - 1);

    html.append("<ul class=\"depth-").append(depth).append(" group\">\n");
  }


  @SuppressWarnings("java:S3400")
  protected String groupPrefixHtml(@NotNull GroupStartEntry<M> group) {
    return "";
  }


  @Override
  public void groupEnd(@NotNull GroupEndEntry<M> groupEnd)
  {
    indent(groupEnd.getDepth() - 1);

    html.append("</ul>\n");
  }


  @Override
  public @NotNull String getResult() {
    return html.toString();
  }


  @Override
  public void init(@NotNull Level level, @NotNull TagSelector tagSelector, int estimatedGroupDepth) {
    html.delete(0, html.length());
  }


  private void indent(int depth)
  {
    val spaces = new char[(depth + 2) * 2];
    Arrays.fill(spaces, ' ');

    html.append(spaces);
  }




  public static class WithFontAwesome<M> extends HtmlProtocolFormatter<M>
  {
    /**
     * Font Awesome 4 default icons.
     *
     * <pre>
     * </pre>
     */
    public static final Map<Level,String> FA4_LEVEL_ICON_CLASSES;


    /**
     * Font Awesome 5 default icons.
     *
     * <pre>
     *   <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.6.3/css/all.css" integrity="sha384-UHRtZLI+pbxtHCWp1t77Bi1L4ZtiqrqD80Kn4Z8NTSRyMA2Fd33n5dQ8lWUE00s/" crossorigin="anonymous">
     * </pre>
     */
    public static final Map<Level,String> FA5_LEVEL_ICON_CLASSES;


    static
    {
      val fa4LevelIconClassMap = new TreeMap<Level,String>(SORT_DESCENDING);
      fa4LevelIconClassMap.put(Level.Shared.ERROR, "fa fa-times-circle");
      fa4LevelIconClassMap.put(Level.Shared.WARN, "fa fa-exclamation-triangle");
      fa4LevelIconClassMap.put(Level.Shared.INFO, "fa fa-info-circle");
      fa4LevelIconClassMap.put(Level.Shared.DEBUG, "fa fa-comment-o");
      fa4LevelIconClassMap.put(Level.Shared.LOWEST, "fa fa-wrench");
      FA4_LEVEL_ICON_CLASSES = Collections.unmodifiableMap(fa4LevelIconClassMap);

      val fa5LevelIconClassMap = new TreeMap<Level,String>(SORT_DESCENDING);
      fa5LevelIconClassMap.put(Level.Shared.ERROR, "fas fa-times-circle");
      fa5LevelIconClassMap.put(Level.Shared.WARN, "fas fa-exclamation-triangle");
      fa5LevelIconClassMap.put(Level.Shared.INFO, "fas fa-info-circle");
      fa5LevelIconClassMap.put(Level.Shared.DEBUG, "far fa-comment");
      fa5LevelIconClassMap.put(Level.Shared.LOWEST, "fas fa-wrench");
      FA5_LEVEL_ICON_CLASSES = Collections.unmodifiableMap(fa5LevelIconClassMap);
    }


    private final SortedMap<Level,String> levelIconMap;


    public WithFontAwesome(@NotNull MessageFormatter<M> messageFormatter, @NotNull Map<Level,String> levelIconMap)
    {
      super(messageFormatter);

      this.levelIconMap = new TreeMap<Level,String>(SORT_DESCENDING);
      this.levelIconMap.putAll(levelIconMap);
    }


    @Override
    protected String messagePrefixHtml(@NotNull MessageEntry<M> message) {
      return htmlPart(getIconClassName(message));
    }


    @Override
    protected String groupPrefixHtml(@NotNull GroupStartEntry<M> group) {
      return htmlPart(getIconClassName(group.getGroupMessage()));
    }


    protected @NotNull String htmlPart(String iconClassName) {
      return iconClassName == null ? "<i></i>" : ("<i class=\"" + iconClassName.trim() + "\"></i>");
    }


    protected String getIconClassName(@NotNull MessageWithLevel<M> message)
    {
      val level = message.getLevel();
      val iconClassName = levelIconMap.get(level);

      if (iconClassName != null)
        return iconClassName;

      val severity = level.severity();

      for(val levelIconClassEntry: levelIconMap.entrySet())
        if (severity >= levelIconClassEntry.getKey().severity())
          return levelIconClassEntry.getValue();

      return "";
    }
  }
}
