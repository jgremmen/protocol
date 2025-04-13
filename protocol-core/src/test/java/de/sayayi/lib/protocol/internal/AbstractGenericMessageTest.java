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
package de.sayayi.lib.protocol.internal;

import de.sayayi.lib.protocol.message.GenericMessageWithId;
import de.sayayi.lib.protocol.util.ParameterMap;
import org.junit.jupiter.api.Test;

import lombok.val;

import org.jetbrains.annotations.NotNull;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Jeroen Gremmen
 */
public class AbstractGenericMessageTest
{
  @Test
  public void testParentParameters()
  {
    val parameters = new ParameterMap();
    parameters.put("key", "value123");

    val message = new TestMessage("msg", parameters);

    assertEquals("value123", message.getParameterValues().get("key"));
  }


  @Test
  public void testParametersNotModifyable()
  {
    val parameters = new ParameterMap();
    parameters.put("key", "value123");

    assertThrows(UnsupportedOperationException.class,
        () -> new TestMessage("msg", parameters).getParameterValues().put("key2", "test"));
  }


  @Test
  public void testTime()
  {
    val message = new TestMessage("msg", null);
    assertTrue(System.currentTimeMillis() >= message.getTimeMillis());
  }


  static final class TestMessage extends AbstractGenericMessage<String>
  {
    TestMessage(@NotNull String message, ParameterMap parentParameterMap) {
      super(new GenericMessageWithId<>(message), parentParameterMap);
    }
  }
}