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

import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolIterator.GroupStartEntry;
import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import de.sayayi.lib.protocol.factory.StringProtocolFactory;
import de.sayayi.lib.protocol.message.formatter.JavaStringFormatFormatter;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.gson.ToNumberPolicy.LONG_OR_DOUBLE;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ALWAYS;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.any;
import static java.util.Locale.US;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * @author Jeroen Gremmen
 * @version 1.6.1
 */
@DisplayName("Json protocol formatter")
class JsonProtocolFormatterTest
{
  private static ProtocolFactory<String> protocolFactory;

  private List<Object> deserialized;


  @BeforeAll
  static void beforeAll() {
    protocolFactory = new StringProtocolFactory(new JavaStringFormatFormatter(US));
  }


  @BeforeEach
  @SuppressWarnings("unchecked")
  void beforeEach()
  {
    final var protocol = protocolFactory.createProtocol();

    protocol
        .info()
        .forTags("structure", "first")
        .message("Start");

    protocol
        .createGroup("process-1")
        .setVisibility(SHOW_HEADER_ALWAYS)
        .setGroupMessage("Process %d")
        .with("0", 1);

    var group2 = protocol
        .createGroup("process-2")
        .setGroupMessage("Finalizing €-tasks");

    group2
        .debug()
        .message("Checking\tpre-requisites");

    group2
        .error(new IllegalArgumentException(new RuntimeException("error", new NullPointerException())))
        .message("%d of %d tasks failed")
        .with("0", 3)
        .with("1", 7);

    protocol
        .info()
        .forTags("structure", "last")
        .message("End");

    @Language("JSON")
    final var jsonString = protocol.format(new MyJsonProtocolFormatter(), any());

    deserialized = new GsonBuilder()
        .setObjectToNumberStrategy(LONG_OR_DOUBLE)
        .setStrictness(Strictness.STRICT)
        .create()
        .fromJson(jsonString, ArrayList.class);
  }


  @Test
  @DisplayName("Json structure")
  @SuppressWarnings("unchecked")
  void structure()
  {
    assertEquals(4, deserialized.size());

    final var groupMessages = (List<Object>)((Map<String,Object>)deserialized.get(2)).get("messages");
    assertEquals(2, groupMessages.size());
  }




  private static final class MyJsonProtocolFormatter extends JsonProtocolFormatter<String>
  {
    public MyJsonProtocolFormatter() {
      super(true);
    }


    @Override
    protected void decorateMessageEntries(@NotNull MessageEntry<String> message, @NotNull Map<String, Object> messageEntries)
    {
      super.decorateMessageEntries(message, messageEntries);

      messageEntries.remove("message-id");
      messageEntries.put("creation-time", toTime(message.getTimeMillis()));
    }


    @Override
    protected void decorateGroupEntries(@NotNull GroupStartEntry<String> group, @NotNull Map<String, Object> groupEntries)
    {
      super.decorateGroupEntries(group, groupEntries);

      groupEntries.remove("message-id");
      groupEntries.put("creation-time", toTime(group.getGroupMessage().getTimeMillis()));
    }


    @Contract(pure = true)
    private @NotNull String toTime(long millis) {
      return LocalTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()).toString();
    }
  }
}