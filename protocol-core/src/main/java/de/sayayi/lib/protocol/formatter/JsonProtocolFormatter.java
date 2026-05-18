/*
 * Copyright 2025 Jeroen Gremmen
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
package de.sayayi.lib.protocol.formatter;

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolFactory.MessageFormatter;
import de.sayayi.lib.protocol.ProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolIterator.GroupEndEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupStartEntry;
import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import de.sayayi.lib.protocol.matcher.MessageMatcher;
import org.intellij.lang.annotations.Language;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static de.sayayi.lib.protocol.ProtocolFactory.DEFAULT_TAG_NAME;
import static java.time.Instant.ofEpochMilli;


/**
 * A {@link ProtocolFormatter} that renders the protocol as a JSON array of message objects.
 * Each message object contains properties like {@code level}, {@code message}, {@code group},
 * {@code creation-time}, {@code message-id} and optionally {@code tags}. Group entries are
 * nested objects with a {@code messages} array containing their child messages.
 * <p>
 * The output can be pretty-printed with indentation or compact without whitespace, controlled
 * via the constructor parameter.
 * <p>
 * Subclasses can customize the generated JSON by overriding {@link #decorateMessageEntries},
 * {@link #decorateGroupEntries}, {@link #extractTagNames} and {@link #levelToString}.
 *
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 1.6.1
 */
public class JsonProtocolFormatter<M> implements ProtocolFormatter<M,String>
{
  private final StringBuilder json = new StringBuilder();
  private final boolean prettyFormat;

  private MessageFormatter<M> messageFormatter;
  private State[] stateStack;
  private int stateStackTopIdx;
  private String nameBeforeValue;


  /**
   * Creates a new JSON formatter with pretty-printing enabled.
   */
  public JsonProtocolFormatter() {
    this(true);
  }


  /**
   * Creates a new JSON formatter.
   *
   * @param prettyFormat  {@code true} for indented output, {@code false} for compact output
   */
  public JsonProtocolFormatter(boolean prettyFormat) {
    this.prettyFormat = prettyFormat;
  }


  /** {@inheritDoc} */
  @Override
  @MustBeInvokedByOverriders
  public void init(@NotNull ProtocolFactory<M> factory, @NotNull MessageMatcher matcher, int estimatedGroupDepth)
  {
    messageFormatter = factory.getMessageFormatter();

    json.setLength(0);

    stateStack = new State[(estimatedGroupDepth + 1) * 3 + 2];
    stateStack[0] = State.DOCUMENT_0;
    stateStackTopIdx = 0;

    nameBeforeValue = null;
  }


  /** {@inheritDoc} */
  @Override
  public final void protocolStart() {
    beginArray();
  }


  /** {@inheritDoc} */
  @Override
  public final void protocolEnd() {
    endArray();
  }


  /** {@inheritDoc} */
  @Override
  public final void groupStart(@NotNull GroupStartEntry<M> group)
  {
    beginObject();

    final var message = group.getGroupMessage();
    final var groupEntries = new TreeMap<String,Object>();

    groupEntries.put("level", levelToString(message.getLevel()));
    groupEntries.put("message", messageFormatter.formatMessage(message));
    groupEntries.put("group", true);

    decorateGroupEntries(group, groupEntries);

    groupEntries.forEach((key,value) -> name(key).value(value));

    name("messages");
    beginArray();
  }


  /**
   * This method provides the json object entries for a group start and can be overridden to customize the
   * values generated in the json object.
   * <p>
   * On method entry, the {@code groupEntries} map already contains values for {@code level}, {@code message}
   * and {@code group}. The default implementation adds {@code group-message}, {@code creation-time},
   * {@code message-id}, {@code level-severity} and optionally {@code name}.
   * <p>
   * By overriding this method, additional entries can be added and existing ones can be overridden or deleted.
   * <p>
   * Entry values are limited to string, number and boolean. Arrays and complex objects are not converted properly.
   *
   * @param group         group start entry, not {@code null}
   * @param groupEntries  modifiable map containing key/value entries for the json object representing the group,
   *                      not {@code null}
   */
  @Contract(mutates = "param2")
  protected void decorateGroupEntries(@NotNull GroupStartEntry<M> group, @NotNull Map<String,Object> groupEntries)
  {
    final var name = group.getName();
    if (name != null)
      groupEntries.put("name", name);

    final var message = group.getGroupMessage();

    groupEntries.put("group-message", true);
    groupEntries.put("creation-time", ofEpochMilli(message.getTimeMillis()).toString());
    groupEntries.put("message-id", message.getMessageId());
    groupEntries.put("level-severity", message.getLevel().severity());
  }


  /** {@inheritDoc} */
  @Override
  public final void groupEnd(@NotNull GroupEndEntry<M> groupEnd)
  {
    endArray();
    endObject();
  }


  /** {@inheritDoc} */
  @Override
  public final void message(@NotNull MessageEntry<M> message)
  {
    beginObject();

    final var messageEntries = new TreeMap<String,Object>();

    messageEntries.put("level", levelToString(message.getLevel()));
    messageEntries.put("message", messageFormatter.formatMessage(message));
    messageEntries.put("group", false);

    decorateMessageEntries(message, messageEntries);

    messageEntries.forEach((key,value) -> name(key).value(value));

    if (!message.isGroupMessage())
    {
      final var tagNames = extractTagNames(message);

      if (tagNames != null)
      {
        name("tags");
        beginArray();
        tagNames.forEach(this::value);
        endArray();
      }
    }

    endObject();
  }


  /**
   * This method provides the json object entries for a message and can be overridden to customize the
   * values generated in the json object.
   * <p>
   * On method entry, the {@code messageEntries} map already contains values for {@code level}, {@code message}
   * and {@code group}. The default implementation adds {@code group-message}, {@code creation-time},
   * {@code message-id} and {@code level-severity}.
   * <p>
   * By overriding this method, additional entries can be added and existing ones can be overridden or deleted.
   * <p>
   * Entry values are limited to string, number and boolean. Arrays and complex objects are not converted properly.
   *
   * @param message         message entry, not {@code null}
   * @param messageEntries  modifiable map containing key/value entries for the json object
   *                        representing the message, not {@code null}
   */
  @Contract(mutates = "param2")
  protected void decorateMessageEntries(@NotNull MessageEntry<M> message, @NotNull Map<String,Object> messageEntries)
  {
    messageEntries.put("message-id", message.getMessageId());
    messageEntries.put("creation-time", ofEpochMilli(message.getTimeMillis()).toString());
    messageEntries.put("group-message", message.isGroupMessage());
    messageEntries.put("level-severity", message.getLevel().severity());
    messageEntries.put("throwable", message.getThrowable() != null);
  }


  /**
   * Extract tag names from {@code message}. This method is invoked during formatting to decorate the {@code tags}
   * property of a json object representing a message.
   * <p>
   * If this method returns {@code null}, the {@code tags} property will be omitted. Otherwise, it will produce
   * something similar to:
   *
   * <pre>
   *   {
   *     ...
   *     "tags": [ "my-tag1", "my-tag2" ]
   *   }
   * </pre>
   *
   * The default implementation returns the tag names from the message without the default tag or {@code null} if
   * the default tag is the only tag
   *
   * @param message  message entry, not {@code null}
   *
   * @return  set containing tag names or {@code null}
   */
  @Contract(pure = true)
  protected Set<String> extractTagNames(@NotNull MessageEntry<M> message)
  {
    final var tagNames = new TreeSet<>(message.getTagNames());

    tagNames.remove(DEFAULT_TAG_NAME);

    return tagNames.isEmpty() ? null : tagNames;
  }


  /**
   * Translates {@code level} to its string representation.
   *
   * @param level  level, not {@code null}
   *
   * @return  string representing the level, never {@code null}
   */
  @Contract(pure = true)
  protected @NotNull String levelToString(@NotNull Level level) {
    return level.toString().toLowerCase();
  }


  /** {@inheritDoc} */
  @Override
  @Language("JSON")
  public final String getResult() {
    return json.toString();
  }


  /**
   * Opens a new JSON array in the output.
   */
  protected final void beginArray()
  {
    writeNameBeforeValue();
    json.append('[');

    stateStack[++stateStackTopIdx] = State.ARRAY_0;
  }


  /**
   * Closes the current JSON array in the output.
   */
  protected final void endArray()
  {
    if (stateStack[stateStackTopIdx--] == State.ARRAY_N)
      newline();

    json.append(']');
  }


  /**
   * Opens a new JSON object in the output.
   */
  protected final void beginObject()
  {
    writeNameBeforeValue();
    json.append('{');

    stateStack[++stateStackTopIdx] = State.OBJECT_0;
  }


  /**
   * Closes the current JSON object in the output.
   */
  protected final void endObject()
  {
    if (stateStack[stateStackTopIdx--] == State.OBJECT_N)
      newline();

    json.append('}');
  }


  /**
   * Sets the property name for the next value to be written.
   *
   * @param name  JSON property name, not {@code null}
   *
   * @return  this formatter, for method chaining
   */
  protected final @NotNull JsonProtocolFormatter<M> name(@NotNull String name)
  {
    nameBeforeValue = name;
    return this;
  }


  /**
   * Writes a JSON value. Supported types are {@code null}, {@link Boolean}, {@link CharSequence}
   * and {@link Number} (written as long).
   *
   * @param value  value to write, or {@code null}
   */
  protected final void value(Object value)
  {
    writeNameBeforeValue();

    if (value == null || value instanceof Boolean)
      json.append(value);
    else if (value instanceof CharSequence)
      string(value.toString());
    else if (value instanceof Number number)
      json.append(number.longValue());
  }


  private void newline()
  {
    if (prettyFormat)
      json.append('\n').repeat("  ", stateStackTopIdx);
  }


  private void string(@NotNull String value)
  {
    json.append('"');

    for(var c: value.toCharArray())
      json.append(string_escape(c));

    json.append('"');
  }


  @Contract(pure = true)
  private @NotNull String string_escape(char c)
  {
    return switch(c) {
      case '"' -> "\\\"";
      case '\'' -> "\\'";
      case '\\' -> "\\\\";
      case '\b' -> "\\b";
      case '\f' -> "\\f";
      case '\n' -> "\\n";
      case '\r' -> "\\r";
      case '\t' -> "\\t";
      case '<' -> "\\u003c";
      case '>' -> "\\u003e";
      case '&' -> "\\u0026";
      case '=' -> "\\u003d";

      default ->
        // make sure we're producing us-ascii compatible output
        c >= ' ' && c < '\u0080'
            ? Character.toString(c)
            : String.format("\\u%04x", (int)c);
    };
  }


  private void writeNameBeforeValue()
  {
    if (nameBeforeValue != null)
    {
      if (stateStack[stateStackTopIdx] == State.OBJECT_N)
        json.append(',');

      newline();
      string(nameBeforeValue);

      stateStack[stateStackTopIdx] = State.NAME_WITHOUT_VALUE;
      nameBeforeValue = null;
    }

    switch(stateStack[stateStackTopIdx])
    {
      case DOCUMENT_0:
        stateStack[stateStackTopIdx] = State.DOCUMENT_N;
        break;

      case ARRAY_0:
        stateStack[stateStackTopIdx] = State.ARRAY_N;
        newline();
        break;

      case ARRAY_N:
        json.append(',');
        newline();
        break;

      case NAME_WITHOUT_VALUE:
        json.append(prettyFormat ? ": " : ":");
        stateStack[stateStackTopIdx] = State.OBJECT_N;
        break;
    }
  }




  private enum State {
    DOCUMENT_0, DOCUMENT_N, ARRAY_0, ARRAY_N, OBJECT_0, OBJECT_N, NAME_WITHOUT_VALUE
  }
}
