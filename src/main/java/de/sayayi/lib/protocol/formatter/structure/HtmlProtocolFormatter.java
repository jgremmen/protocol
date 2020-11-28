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
import lombok.var;

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
 * @since 0.2.0
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
    val divClasses = new String[] { "protocol", protocolStartDivClass() };

    html.append("<div").append(classFromArray(divClasses)).append(">\n")
        .append("  <ul").append(classFromArray("depth-0", protocolStartUlClass())).append(">\n");
  }


  protected String protocolStartDivClass() {
    return null;
  }


  protected String protocolStartUlClass() {
    return null;
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

    val liClasses = new String[] { "level-" + levelToHtmlClass(message.getLevel()), messageLiClass(message) };
    val liSpanClasses = new String[] {
        message.isGroupMessage() ? "group-message" : null, "message", messageSpanClass(message)
    };

    html.append("<li").append(classFromArray(liClasses)).append('>')
        .append(messagePrefixHtml(message))
        .append("<span").append(classFromArray(liSpanClasses)).append(">")
        .append(escapeHtml5(msg)).append("</span>")
        .append(messageSuffixHtml(message))
        .append("</li>\n");
  }


  protected String messageLiClass(@NotNull MessageEntry<M> message) {
    return null;
  }


  protected String messageSpanClass(@NotNull MessageEntry<M> message) {
    return null;
  }


  protected @NotNull String messagePrefixHtml(@NotNull MessageEntry<M> message) {
    return "";
  }


  protected @NotNull String messageSuffixHtml(@NotNull MessageEntry<M> message) {
    return "";
  }


  @Override
  public void groupStart(@NotNull GroupStartEntry<M> group)
  {
    val depth = group.getDepth();
    val message = group.getGroupMessage();
    val msg = messageFormatter.formatMessage(group.getGroupMessage());

    indent(depth - 1);

    val liClasses = new String[] { "level-" + levelToHtmlClass(message.getLevel()), groupHeaderLiClass(message) };
    val liSpanClasses = new String[] { "group", groupHeaderLiSpanClass(message) };

    html.append("<li").append(classFromArray(liClasses)).append('>')
        .append(groupHeaderPrefixHtml(message))
        .append("<span").append(classFromArray(liSpanClasses)).append('>')
        .append(escapeHtml5(msg)).append("</span>")
        .append(groupHeaderSuffixHtml(message))
        .append("</li>\n");

    indent(depth - 1);

    val ulClasses = new String[] { "depth-" + depth, "group", groupStartUlClass(group) };

    html.append("<ul").append(classFromArray(ulClasses)).append(">\n");
  }


  protected String groupHeaderLiClass(@NotNull MessageWithLevel<M> message) {
    return null;
  }


  protected String groupHeaderLiSpanClass(@NotNull MessageWithLevel<M> message) {
    return null;
  }


  protected @NotNull String groupHeaderPrefixHtml(@NotNull MessageWithLevel<M> message) {
    return "";
  }


  protected @NotNull String groupHeaderSuffixHtml(@NotNull MessageWithLevel<M> message) {
    return "";
  }


  protected String groupStartUlClass(@NotNull GroupStartEntry<M> group) {
    return null;
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


  protected @NotNull String classFromArray(String ... classNames)
  {
    if (classNames != null && classNames.length > 0)
    {
      val cls = new StringBuilder(" class=\"");
      var n = 0;

      for(val className: classNames)
        if (className != null && !className.trim().isEmpty())
        {
          if (n != 0)
            cls.append(' ');

          cls.append(className.trim());
          n++;
        }

      if (n > 0)
        return cls.append('"').toString();
    }

    return "";
  }


  protected void indent(int depth)
  {
    val spaces = new char[(depth + 2) * 2];
    Arrays.fill(spaces, ' ');

    html.append(spaces);
  }




  public static class WithFontAwesome<M> extends HtmlProtocolFormatter<M>
  {
    /**
     * <p>
     *   Font Awesome 4 default icons.
     * </p>
     * <p>
     *   Add the following link to your html page:
     * </p>
     * <br>
     * <pre>
     * </pre>
     */
    public static final Map<Level,String> FA4_LEVEL_ICON_CLASSES;


    /**
     * <p>
     *   Font Awesome 5 default icons.
     * </p>
     * <p>
     *   Add the following link to your html page:
     * </p>
     * <br>
     * <pre>
     *   &lt;link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.6.3/css/all.css" integrity="sha384-UHRtZLI+pbxtHCWp1t77Bi1L4ZtiqrqD80Kn4Z8NTSRyMA2Fd33n5dQ8lWUE00s/" crossorigin="anonymous"&gt;
     * </pre>
     */
    public static final Map<Level,String> FA5_LEVEL_ICON_CLASSES;


    static
    {
      val fa4LevelIconClassMap = new TreeMap<Level,String>(SORT_DESCENDING);
      fa4LevelIconClassMap.put(Level.Shared.ERROR, "fa fa-times");
      fa4LevelIconClassMap.put(Level.Shared.WARN, "fa fa-exclamation-triangle");
      fa4LevelIconClassMap.put(Level.Shared.INFO, "fa fa-info-circle");
      fa4LevelIconClassMap.put(Level.Shared.DEBUG, "fa fa-puzzle-piece");
      fa4LevelIconClassMap.put(Level.Shared.LOWEST, "fa fa-wrench");
      FA4_LEVEL_ICON_CLASSES = Collections.unmodifiableMap(fa4LevelIconClassMap);

      val fa5LevelIconClassMap = new TreeMap<Level,String>(SORT_DESCENDING);
      fa5LevelIconClassMap.put(Level.Shared.ERROR, "fas fa-times");
      fa5LevelIconClassMap.put(Level.Shared.WARN, "fas fa-exclamation-triangle");
      fa5LevelIconClassMap.put(Level.Shared.INFO, "fas fa-info-circle");
      fa5LevelIconClassMap.put(Level.Shared.DEBUG, "fas fa-puzzle-piece");
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
    protected @NotNull String protocolStartUlClass() {
      return "fa-ul";
    }


    @Override
    protected String groupStartUlClass(@NotNull GroupStartEntry<M> group) {
      return "fa-ul";
    }


    @Override
    protected @NotNull String messagePrefixHtml(@NotNull MessageEntry<M> message) {
      return htmlPart(getIconClassName(message));
    }


    @Override
    protected @NotNull String groupHeaderPrefixHtml(@NotNull MessageWithLevel<M> message) {
      return htmlPart(getIconClassName(message));
    }


    protected @NotNull String htmlPart(String iconClassName) {
      return "<span class=\"fa-li\"><i" + classFromArray(iconClassName) + "></i></span>";
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

      return null;
    }
  }
}
