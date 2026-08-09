/*
 * Copyright 2021 Jeroen Gremmen
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
package de.sayayi.lib.protocol.matcher;

import de.sayayi.lib.protocol.ProtocolEntry.Message;
import org.junit.jupiter.api.Test;

import lombok.val;

import static de.sayayi.lib.protocol.Level.Shared.DEBUG;
import static de.sayayi.lib.protocol.Level.Shared.ERROR;
import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;
import static de.sayayi.lib.protocol.Level.Shared.INFO;
import static de.sayayi.lib.protocol.Level.Shared.LOWEST;
import static de.sayayi.lib.protocol.Level.Shared.WARN;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.is;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.isDebug;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.isError;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.isInfo;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.isWarn;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author Jeroen Gremmen
 */
@SuppressWarnings("unchecked")
class LevelMatcherTest
{
  @Test
  void testLevelLowest()
  {
    val message = (Message<Object>)mock(Message.class);
    when(message.getLevel()).thenReturn(LOWEST);

    assertTrue(is(LOWEST).matches(HIGHEST, message));
    assertFalse(isDebug().matches(HIGHEST, message));
    assertFalse(isInfo().matches(HIGHEST, message));
    assertFalse(isWarn().matches(HIGHEST, message));
    assertFalse(isError().matches(HIGHEST, message));
    assertFalse(is(HIGHEST).matches(HIGHEST, message));
  }


  @Test
  void testLevelDebug()
  {
    val message = (Message<Object>)mock(Message.class);
    when(message.getLevel()).thenReturn(DEBUG);

    assertTrue(is(LOWEST).matches(HIGHEST, message));
    assertTrue(isDebug().matches(HIGHEST, message));
    assertFalse(isInfo().matches(HIGHEST, message));
    assertFalse(isWarn().matches(HIGHEST, message));
    assertFalse(isError().matches(HIGHEST, message));
    assertFalse(is(HIGHEST).matches(HIGHEST, message));
  }


  @Test
  void testLevelInfo()
  {
    val message = (Message<Object>)mock(Message.class);
    when(message.getLevel()).thenReturn(INFO);

    assertTrue(is(LOWEST).matches(HIGHEST, message));
    assertTrue(isDebug().matches(HIGHEST, message));
    assertTrue(isInfo().matches(HIGHEST, message));
    assertFalse(isWarn().matches(HIGHEST, message));
    assertFalse(isError().matches(HIGHEST, message));
    assertFalse(is(HIGHEST).matches(HIGHEST, message));
  }


  @Test
  void testLevelWarn()
  {
    val message = (Message<Object>)mock(Message.class);
    when(message.getLevel()).thenReturn(WARN);

    assertTrue(is(LOWEST).matches(HIGHEST, message));
    assertTrue(isDebug().matches(HIGHEST, message));
    assertTrue(isInfo().matches(HIGHEST, message));
    assertTrue(isWarn().matches(HIGHEST, message));
    assertFalse(isError().matches(HIGHEST, message));
    assertFalse(is(HIGHEST).matches(HIGHEST, message));
  }


  @Test
  void testLevelError()
  {
    val message = (Message<Object>)mock(Message.class);
    when(message.getLevel()).thenReturn(ERROR);

    assertTrue(is(LOWEST).matches(HIGHEST, message));
    assertTrue(isDebug().matches(HIGHEST, message));
    assertTrue(isInfo().matches(HIGHEST, message));
    assertTrue(isWarn().matches(HIGHEST, message));
    assertTrue(isError().matches(HIGHEST, message));
    assertFalse(is(HIGHEST).matches(HIGHEST, message));

    assertFalse(isWarn().matches(INFO, message));
  }


  @Test
  void testLevelHighest()
  {
    val message = (Message<Object>)mock(Message.class);
    when(message.getLevel()).thenReturn(HIGHEST);

    assertTrue(is(LOWEST).matches(HIGHEST, message));
    assertTrue(isDebug().matches(HIGHEST, message));
    assertTrue(isInfo().matches(HIGHEST, message));
    assertTrue(isWarn().matches(HIGHEST, message));
    assertTrue(isError().matches(HIGHEST, message));
    assertTrue(is(HIGHEST).matches(HIGHEST, message));
  }
}
