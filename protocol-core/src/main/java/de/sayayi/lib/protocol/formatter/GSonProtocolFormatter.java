/*
 * Copyright 2022 Jeroen Gremmen
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

import com.google.gson.stream.JsonWriter;
import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol.GenericMessageWithLevel;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolFactory.MessageFormatter;
import de.sayayi.lib.protocol.ProtocolFormatter;
import de.sayayi.lib.protocol.ProtocolIterator.GroupEndEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupMessageEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupStartEntry;
import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import de.sayayi.lib.protocol.exception.ProtocolException;
import de.sayayi.lib.protocol.matcher.MessageMatcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringWriter;


/**
 * @param <M>  internal message object type
 *
 * @author Jeroen Gremmen
 * @since 1.0.2
 */
@SuppressWarnings("UnstableApiUsage")
public class GSonProtocolFormatter<M> implements ProtocolFormatter<M,String>
{
  private final StringWriter json;
  private final boolean prettyPrint;

  private JsonWriter jsonWriter;
  protected MessageFormatter<M> messageFormatter;


  public GSonProtocolFormatter() {
    this(false);
  }


  public GSonProtocolFormatter(boolean prettyPrint)
  {
    json = new StringWriter();
    this.prettyPrint = prettyPrint;
  }


  @Override
  @SuppressWarnings("deprecation")
  public void init(@NotNull ProtocolFactory<M> factory, @NotNull MessageMatcher matcher, int estimatedGroupDepth)
  {
    messageFormatter = factory.getMessageFormatter();

    if (jsonWriter != null)
    {
      try {
        jsonWriter.close();
      } catch(IOException ignored) {
      }
    }

    final StringBuffer buffer = json.getBuffer();
    buffer.setLength(0);

    jsonWriter = new JsonWriter(json);

    if (prettyPrint)
      jsonWriter.setIndent("  ");

    jsonWriter.setHtmlSafe(true);
    jsonWriter.setLenient(true);
  }


  @Override
  public void protocolStart()
  {
    try {
      jsonWriter.beginArray();
    } catch(IOException ex) {
      throw new ProtocolException("failed to generate json", ex);
    }
  }


  @Override
  public void protocolEnd()
  {
    try {
      jsonWriter.endArray();
    } catch(IOException ex) {
      throw new ProtocolException("failed to generate json", ex);
    }
  }


  @Override
  public void message(@NotNull MessageEntry<M> message)
  {
    try {
      jsonWriter.beginObject();

      jsonWriter.name("id").value(message.getMessageId());
      formatTimestamp(jsonWriter.name("timestamp"), message.getTimeMillis());

      // message
      if (message.isGroupMessage())
      {
        jsonWriter.name("group-name").value(((GroupMessageEntry<M>)message).getName());
        jsonWriter.name("group-message").value(formatMessage(message));
      }
      else
      {
        jsonWriter.name("message").value(formatMessage(message));

        // tags (all in one line)
        jsonWriter.name("tags").beginArray();
        for(final String tag: message.getTagNames())
          jsonWriter.value(tag);
        jsonWriter.endArray();
      }

      // level
      final Level level = message.getLevel();
      jsonWriter.name("level-severity").value(level.severity());
      jsonWriter.name("level-name").value(levelName(level));

      jsonWriter.endObject();
    } catch(IOException ex) {
      throw new ProtocolException("failed to generate json", ex);
    }
  }


  @Contract(mutates = "param1")
  private void formatTimestamp(@NotNull JsonWriter writer, long timestamp) throws IOException
  {
    final Object value = formatTimestamp(timestamp);

    if (value instanceof Number)
      writer.value((Number)value);
    else if (value instanceof String)
      writer.value((String)value);
    else
      throw new IllegalStateException("formatted timestamp must be Number or String");
  }


  @Contract(pure = true)
  protected @NotNull Object formatTimestamp(long timestamp) {
    return timestamp;
  }


  @Contract(pure = true)
  protected @NotNull String formatMessage(@NotNull GenericMessageWithLevel<M> message) {
    return messageFormatter.formatMessage(message);
  }


  @Override
  public void groupStart(@NotNull GroupStartEntry<M> group)
  {
    try {
      jsonWriter.beginObject();

      final GenericMessageWithLevel<M> message = group.getGroupMessage();

      jsonWriter.name("id").value(message.getMessageId());
      formatTimestamp(jsonWriter.name("timestamp"), message.getTimeMillis());

      // group message
      jsonWriter.name("group-name").value(group.getName());
      jsonWriter.name("group-message").value(formatMessage(message));

      // level
      final Level level = message.getLevel();
      jsonWriter.name("level-severity").value(level.severity());
      jsonWriter.name("level-name").value(levelName(level));

      jsonWriter.name("messages").beginArray();
    } catch(IOException ex) {
      throw new ProtocolException("failed to generate json", ex);
    }
  }


  @Override
  public void groupEnd(@NotNull GroupEndEntry<M> groupEnd)
  {
    try {
      jsonWriter.endArray();
      jsonWriter.endObject();
    } catch(IOException ex) {
      throw new ProtocolException("failed to generate json", ex);
    }
  }


  @Override
  public String getResult()
  {
    try {
      jsonWriter.flush();
    } catch(IOException ex) {
      throw new ProtocolException("failed to generate json", ex);
    }

    return json.toString();
  }


  protected String levelName(@NotNull Level level) {
    return level.toString();
  }
}
