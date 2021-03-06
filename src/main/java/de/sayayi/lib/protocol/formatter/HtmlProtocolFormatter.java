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
import de.sayayi.lib.protocol.Protocol.GenericMessageWithLevel;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolFactory.MessageFormatter;
import de.sayayi.lib.protocol.ProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolIterator.GroupEndEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupStartEntry;
import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import de.sayayi.lib.protocol.TagSelector;

import lombok.val;
import lombok.var;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static de.sayayi.lib.protocol.Level.SORT_DESCENDING;
import static de.sayayi.lib.protocol.Level.compare;
import static org.unbescape.html.HtmlEscape.escapeHtml5;


/**
 * <p>
 *   This formatter has a dependency with
 *   <a href="https://mvnrepository.com/artifact/org.unbescape/unbescape">unbescape</a>
 *   version &gt;= {@code 0.4}.
 * </p>
 *
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.2.0
 */
@SuppressWarnings("unused")
public class HtmlProtocolFormatter<M> implements ProtocolFormatter<M,String>
{
  private final StringBuilder html = new StringBuilder();
  private MessageFormatter<M> messageFormatter;


  @Override
  public void init(@NotNull ProtocolFactory<M> factory, @NotNull Level level, @NotNull TagSelector tagSelector,
                   int estimatedGroupDepth)
  {
    messageFormatter = factory.getMessageFormatter();
    html.delete(0, html.length());
  }


  @Contract(pure = true)
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


  @Contract(pure = true)
  protected String protocolStartDivClass() {
    return null;
  }


  @Contract(pure = true)
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


  @Contract(pure = true)
  protected String messageLiClass(@NotNull MessageEntry<M> message) {
    return null;
  }


  @Contract(pure = true)
  protected String messageSpanClass(@NotNull MessageEntry<M> message) {
    return null;
  }


  @Contract(pure = true)
  protected @NotNull String messagePrefixHtml(@NotNull MessageEntry<M> message) {
    return "";
  }


  @Contract(pure = true)
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


  @Contract(pure = true)
  protected String groupHeaderLiClass(@NotNull GenericMessageWithLevel<M> message) {
    return null;
  }


  @Contract(pure = true)
  protected String groupHeaderLiSpanClass(@NotNull GenericMessageWithLevel<M> message) {
    return null;
  }


  @Contract(pure = true)
  protected @NotNull String groupHeaderPrefixHtml(@NotNull GenericMessageWithLevel<M> message) {
    return "";
  }


  @Contract(pure = true)
  protected @NotNull String groupHeaderSuffixHtml(@NotNull GenericMessageWithLevel<M> message) {
    return "";
  }


  @Contract(pure = true)
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


  @Contract(pure = true)
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


  @Contract(pure = true)
  protected void indent(int depth)
  {
    val spaces = new char[(depth + 2) * 2];
    Arrays.fill(spaces, ' ');

    html.append(spaces);
  }




  /**
   * Html protocol formatter that produces list bullets with font awesome icons.
   *
   * @param <M>  internal message object type
   *
   * @since 0.7.0
   */
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
     *   &lt;link rel="stylesheet"
     *         href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css"
     *         integrity="sha256-eZrrJcwDc/3uDhsdt61sL2oOBY362qM3lon1gyExkL0="
     *         crossorigin="anonymous"/&gt;
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
     *   &lt;link rel="stylesheet"
     *         href="https://use.fontawesome.com/releases/v5.6.3/css/all.css"
     *         integrity="sha384-UHRtZLI+pbxtHCWp1t77Bi1L4ZtiqrqD80Kn4Z8NTSRyMA2Fd33n5dQ8lWUE00s/"
     *         crossorigin="anonymous"&gt;
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


    public WithFontAwesome(@NotNull Map<Level,String> levelIconMap)
    {
      this.levelIconMap = new TreeMap<>(SORT_DESCENDING);
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
    protected @NotNull String groupHeaderPrefixHtml(@NotNull GenericMessageWithLevel<M> message) {
      return htmlPart(getIconClassName(message));
    }


    @Contract(pure = true)
    protected @NotNull String htmlPart(String iconClassName) {
      return "<span class=\"fa-li\"><i" + classFromArray(iconClassName) + "></i></span>";
    }


    @Contract(pure = true)
    protected String getIconClassName(@NotNull GenericMessageWithLevel<M> message)
    {
      val level = message.getLevel();
      val iconClassName = levelIconMap.get(level);

      if (iconClassName != null)
        return iconClassName;

      for(val levelIconClassEntry: levelIconMap.entrySet())
        if (compare(level, levelIconClassEntry.getKey()) >= 0)
          return levelIconClassEntry.getValue();

      return null;
    }
  }
}
