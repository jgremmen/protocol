/*
 * Copyright 2019 Jeroen Gremmen
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
package de.sayayi.lib.protocol.formatter.html;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol.GenericMessageWithLevel;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolFactory.MessageFormatter;
import de.sayayi.lib.protocol.ProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolIterator.GroupEndEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupStartEntry;
import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static de.sayayi.lib.protocol.Level.SORT_DESCENDING;
import static de.sayayi.lib.protocol.Level.Shared.DEBUG;
import static de.sayayi.lib.protocol.Level.Shared.ERROR;
import static de.sayayi.lib.protocol.Level.Shared.INFO;
import static de.sayayi.lib.protocol.Level.Shared.LOWEST;
import static de.sayayi.lib.protocol.Level.Shared.WARN;
import static de.sayayi.lib.protocol.Level.compare;
import static java.util.Arrays.fill;
import static java.util.Collections.unmodifiableMap;


/**
 * A {@link ProtocolFormatter} that renders protocol entries as an HTML unordered list structure. Messages and group
 * headers are output as {@code <li>} elements within nested {@code <ul>} lists, with CSS classes for level and depth.
 * <p>
 * Subclasses can override the various hook methods ({@code *Class}, {@code *PrefixHtml}, {@code *SuffixHtml}) to
 * customize the generated HTML without changing the overall structure.
 *
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 0.2.0  (refactored in 1.6.0)
 *
 * @see WithFontAwesome
 */
@SuppressWarnings("unused")
public class HtmlProtocolFormatter<M> implements ProtocolFormatter<M,String>
{
  private final HtmlEncoder encoder;
  private final StringBuilder html = new StringBuilder();
  private MessageFormatter<M> messageFormatter;


  /**
   * Creates a new HTML formatter using the default {@link HtmlEncoder}.
   */
  public HtmlProtocolFormatter() {
    this(HtmlEncoder.getInstance());
  }


  /**
   * Creates a new HTML formatter using the given {@link HtmlEncoder}.
   *
   * @param encoder  HTML encoder to use for escaping text, not {@code null}
   */
  public HtmlProtocolFormatter(HtmlEncoder encoder) {
    this.encoder = encoder;
  }


  /** {@inheritDoc} */
  @Override
  @MustBeInvokedByOverriders
  public void init(@NotNull ProtocolFactory<M> factory, @NotNull MessageMatcher matcher, int estimatedGroupDepth)
  {
    messageFormatter = factory.getMessageFormatter();
    html.setLength(0);
  }


  /**
   * Converts a level to a CSS class name. By default this returns the lowercased level name.
   *
   * @param level  message level, not {@code null}
   *
   * @return  CSS class name for the level, or {@code null}
   */
  @Contract(pure = true)
  protected String levelToHtmlClass(@NotNull Level level) {
    return level.toString().toLowerCase();
  }


  /** {@inheritDoc} */
  @Override
  public void protocolStart()
  {
    final var divClasses = new String[] { "protocol", protocolStartDivClass() };

    html.append("<div").append(classFromArray(divClasses)).append(">\n")
        .append("  <ul").append(classFromArray("depth-0", protocolStartUlClass())).append(">\n");
  }


  /**
   * Returns an additional CSS class for the protocol wrapper {@code <div>} element.
   *
   * @return  CSS class name, or {@code null} for none
   */
  @Contract(pure = true)
  protected String protocolStartDivClass() {
    return null;
  }


  /**
   * Returns an additional CSS class for the top-level {@code <ul>} element.
   *
   * @return  CSS class name, or {@code null} for none
   */
  @Contract(pure = true)
  protected String protocolStartUlClass() {
    return null;
  }


  /** {@inheritDoc} */
  @Override
  public void protocolEnd()
  {
    html.append("  </ul>\n")
        .append("</div>\n");
  }


  /** {@inheritDoc} */
  @Override
  public void message(@NotNull MessageEntry<M> message)
  {
    indent(message.getDepth());

    final var liClasses = new String[] { "level-" + levelToHtmlClass(message.getLevel()), messageLiClass(message) };
    final var liSpanClasses = new String[] {
        message.isGroupMessage() ? "group-message" : null, "message", messageSpanClass(message)
    };

    html.append("<li").append(classFromArray(liClasses)).append('>')
        .append(messagePrefixHtml(message))
        .append("<span").append(classFromArray(liSpanClasses)).append('>')
        .append(encoder.encodeHtml(messageFormatter.formatMessage(message))).append("</span>")
        .append(messageSuffixHtml(message))
        .append("</li>\n");
  }


  /**
   * Returns an additional CSS class for a message {@code <li>} element.
   *
   * @param message  message entry, not {@code null}
   *
   * @return  CSS class name, or {@code null} for none
   */
  @Contract(pure = true)
  protected String messageLiClass(@NotNull MessageEntry<M> message) {
    return null;
  }


  /**
   * Returns an additional CSS class for the message text {@code <span>} element.
   *
   * @param message  message entry, not {@code null}
   *
   * @return  CSS class name, or {@code null} for none
   */
  @Contract(pure = true)
  protected String messageSpanClass(@NotNull MessageEntry<M> message) {
    return null;
  }


  /**
   * Returns HTML to insert before the message text span.
   *
   * @param message  message entry, not {@code null}
   *
   * @return  prefix HTML, never {@code null}
   */
  @Contract(pure = true)
  protected @NotNull String messagePrefixHtml(@NotNull MessageEntry<M> message) {
    return "";
  }


  /**
   * Returns HTML to insert after the message text span.
   *
   * @param message  message entry, not {@code null}
   *
   * @return  suffix HTML, never {@code null}
   */
  @Contract(pure = true)
  protected @NotNull String messageSuffixHtml(@NotNull MessageEntry<M> message) {
    return "";
  }


  /** {@inheritDoc} */
  @Override
  public void groupStart(@NotNull GroupStartEntry<M> group)
  {
    final var depth = group.getDepth();
    final var message = group.getGroupMessage();
    final var msg = messageFormatter.formatMessage(group.getGroupMessage());

    indent(depth - 1);

    final var liClasses = new String[] { "level-" + levelToHtmlClass(message.getLevel()), groupHeaderLiClass(message) };

    html.append("<li").append(classFromArray(liClasses)).append('>')
        .append(groupHeaderPrefixHtml(message))
        .append("<span").append(classFromArray("group", groupHeaderLiSpanClass(message))).append('>')
        .append(encoder.encodeHtml(msg)).append("</span>")
        .append(groupHeaderSuffixHtml(message))
        .append("</li>\n");

    indent(depth - 1);

    html.append("<ul")
        .append(classFromArray("depth-" + depth, "group", groupStartUlClass(group)))
        .append(">\n");
  }


  /**
   * Returns an additional CSS class for a group header {@code <li>} element.
   *
   * @param message  group header message, not {@code null}
   *
   * @return  CSS class name, or {@code null} for none
   */
  @Contract(pure = true)
  protected String groupHeaderLiClass(@NotNull GenericMessageWithLevel<M> message) {
    return null;
  }


  /**
   * Returns an additional CSS class for the group header text {@code <span>} element.
   *
   * @param message  group header message, not {@code null}
   *
   * @return  CSS class name, or {@code null} for none
   */
  @Contract(pure = true)
  protected String groupHeaderLiSpanClass(@NotNull GenericMessageWithLevel<M> message) {
    return null;
  }


  /**
   * Returns HTML to insert before the group header text span.
   *
   * @param message  group header message, not {@code null}
   *
   * @return  prefix HTML, never {@code null}
   */
  @Contract(pure = true)
  protected @NotNull String groupHeaderPrefixHtml(@NotNull GenericMessageWithLevel<M> message) {
    return "";
  }


  /**
   * Returns HTML to insert after the group header text span.
   *
   * @param message  group header message, not {@code null}
   *
   * @return  suffix HTML, never {@code null}
   */
  @Contract(pure = true)
  protected @NotNull String groupHeaderSuffixHtml(@NotNull GenericMessageWithLevel<M> message) {
    return "";
  }


  /**
   * Returns an additional CSS class for a group's {@code <ul>} element.
   *
   * @param group  group start entry, not {@code null}
   *
   * @return  CSS class name, or {@code null} for none
   */
  @Contract(pure = true)
  protected String groupStartUlClass(@NotNull GroupStartEntry<M> group) {
    return null;
  }


  /** {@inheritDoc} */
  @Override
  public void groupEnd(@NotNull GroupEndEntry<M> groupEnd)
  {
    indent(groupEnd.getDepth() - 1);

    html.append("</ul>\n");
  }


  /** {@inheritDoc} */
  @Override
  public @NotNull String getResult() {
    return html.toString();
  }


  /**
   * Builds a {@code class="..."} attribute string from the given class names. Null and blank entries are ignored.
   * Returns an empty string if no valid class names are provided.
   *
   * @param classNames  CSS class names to include
   *
   * @return  HTML class attribute string, never {@code null}
   */
  @Contract(pure = true)
  protected @NotNull String classFromArray(String ... classNames)
  {
    if (classNames != null && classNames.length > 0)
    {
      final var cls = new StringBuilder(" class=\"");
      int n = 0;

      for(var className: classNames)
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


  /**
   * Appends indentation whitespace to the HTML output for the given depth.
   *
   * @param depth  indentation depth
   */
  @Contract(pure = true)
  protected void indent(int depth)
  {
    final var spaces = new char[(depth + 2) * 2];

    fill(spaces, ' ');

    html.append(spaces);
  }




  /**
   * HTML protocol formatter that renders list item icons using <a href="https://fontawesome.com/">Font Awesome</a>.
   * Icons are selected based on the message level using a configurable level-to-icon-class mapping.
   * <p>
   * Pre-configured icon maps for Font Awesome 4 ({@link #FA4_LEVEL_ICON_CLASSES}) and Font Awesome 5
   * ({@link #FA5_LEVEL_ICON_CLASSES}) are provided.
   *
   * @param <M>  internal message object type
   *
   * @since 0.7.0
   */
  public static class WithFontAwesome<M> extends HtmlProtocolFormatter<M>
  {
    /**
     * Font Awesome 4 default icons.
     * <p>
     * Add the following link to your html page:
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
     * Font Awesome 5 default icons.
     * <p>
     * Add the following link to your html page:
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
      final var fa4LevelIconClassMap = new TreeMap<Level,String>(SORT_DESCENDING);
      fa4LevelIconClassMap.put(ERROR, "fa fa-times");
      fa4LevelIconClassMap.put(WARN, "fa fa-exclamation-triangle");
      fa4LevelIconClassMap.put(INFO, "fa fa-info-circle");
      fa4LevelIconClassMap.put(DEBUG, "fa fa-puzzle-piece");
      fa4LevelIconClassMap.put(LOWEST, "fa fa-wrench");
      FA4_LEVEL_ICON_CLASSES = unmodifiableMap(fa4LevelIconClassMap);

      final var fa5LevelIconClassMap = new TreeMap<Level,String>(SORT_DESCENDING);
      fa5LevelIconClassMap.put(ERROR, "fas fa-times");
      fa5LevelIconClassMap.put(WARN, "fas fa-exclamation-triangle");
      fa5LevelIconClassMap.put(INFO, "fas fa-info-circle");
      fa5LevelIconClassMap.put(DEBUG, "fas fa-puzzle-piece");
      fa5LevelIconClassMap.put(LOWEST, "fas fa-wrench");
      FA5_LEVEL_ICON_CLASSES = unmodifiableMap(fa5LevelIconClassMap);
    }


    private final SortedMap<Level,String> levelIconMap;


    /**
     * Creates a new Font Awesome HTML formatter with the given level-to-icon-class mapping.
     *
     * @param levelIconMap  mapping from severity level to Font Awesome CSS class names, not {@code null}
     */
    public WithFontAwesome(@NotNull Map<Level,String> levelIconMap)
    {
      this.levelIconMap = new TreeMap<>(SORT_DESCENDING);
      this.levelIconMap.putAll(levelIconMap);
    }


    /** {@inheritDoc} */
    @Override
    protected @NotNull String protocolStartUlClass() {
      return "fa-ul";
    }


    /** {@inheritDoc} */
    @Override
    protected String groupStartUlClass(@NotNull GroupStartEntry<M> group) {
      return "fa-ul";
    }


    /** {@inheritDoc} */
    @Override
    protected @NotNull String messagePrefixHtml(@NotNull MessageEntry<M> message) {
      return htmlPart(getIconClassName(message));
    }


    /** {@inheritDoc} */
    @Override
    protected @NotNull String groupHeaderPrefixHtml(@NotNull GenericMessageWithLevel<M> message) {
      return htmlPart(getIconClassName(message));
    }


    /**
     * Generates the Font Awesome icon HTML for the given icon class name.
     *
     * @param iconClassName  Font Awesome CSS class name, or {@code null}
     *
     * @return  HTML for the icon element, never {@code null}
     */
    @Contract(pure = true)
    protected @NotNull String htmlPart(String iconClassName) {
      return "<span class=\"fa-li\"><i" + classFromArray(iconClassName) + "></i></span>";
    }


    /**
     * Determines the Font Awesome icon class for the given message's level by finding the closest matching entry in
     * the level-to-icon map.
     *
     * @param message  message to get the icon class for, not {@code null}
     *
     * @return  Font Awesome CSS class name, or {@code null} if no mapping matches
     */
    @Contract(pure = true)
    protected String getIconClassName(@NotNull GenericMessageWithLevel<M> message)
    {
      final var level = message.getLevel();
      final var iconClassName = levelIconMap.get(level);

      if (iconClassName != null)
        return iconClassName;

      for(final var levelIconClassEntry: levelIconMap.entrySet())
        if (compare(level, levelIconClassEntry.getKey()) >= 0)
          return levelIconClassEntry.getValue();

      return null;
    }
  }
}
