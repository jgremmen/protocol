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
package de.sayayi.lib.protocol.spi;

import org.junit.Test;

import lombok.val;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;


/**
 * @author Jeroen Gremmen
 */
public class AbstractGenericMessageTest
{
  @Test
  public void testDefaultParameters()
  {
    val parameters = Collections.<String,Object>singletonMap("key", "value123");
    val message = new TestMessage("msg", parameters);

    assertNotSame(parameters, message.getParameterValues());
    assertEquals("value123", message.getParameterValues().get("key"));
  }


  @Test(expected = UnsupportedOperationException.class)
  public void testParametersNotModifyable()
  {
    val message = new TestMessage("msg", Collections.singletonMap("key", "value123"));

    message.getParameterValues().put("key2", "test");
  }


  @Test
  public void testTime()
  {
    val message = new TestMessage("msg", Collections.emptyMap());
    assertTrue(System.currentTimeMillis() >= message.getTimeMillis());
  }


  static final class TestMessage extends AbstractGenericMessage<String>
  {
    TestMessage(@NotNull String message, @NotNull Map<String, Object> defaultParameterValues) {
      super(message, defaultParameterValues);
    }
  }
}
