/*
 * Copyright 2020 Jeroen Gremmen
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
package de.sayayi.lib.protocol.message.formatter;

import de.sayayi.lib.protocol.Protocol.GenericMessage;
import org.junit.jupiter.api.Test;

import lombok.val;

import java.util.HashMap;

import static java.util.Collections.singletonMap;
import static java.util.Locale.ROOT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author Jeroen Gremmen
 * @since 0.7.0
 */
public class JavaStringFormatFormatterTest
{
  @Test
  public void testFormatMessageSimple()
  {
    val formatter = new JavaStringFormatFormatter(ROOT);

    @SuppressWarnings("unchecked")
    val message = (GenericMessage<String>)mock(GenericMessage.class);
    when(message.getMessage()).thenReturn("this is a %s");

    assertEquals("this is a message", formatter.formatMessage(message, new Object[] { "message" }));

    when(message.getParameterValues()).thenReturn(singletonMap("0", "test"));

    assertEquals("this is a test", formatter.formatMessage(message));
  }


  @Test
  public void testFormatMessageComplex()
  {
    val formatter = new JavaStringFormatFormatter(ROOT);

    @SuppressWarnings("unchecked")
    val message = (GenericMessage<String>)mock(GenericMessage.class);
    when(message.getMessage()).thenReturn("%4$s = %1$6.4f");

    val parameters = new HashMap<String,Object>();
    parameters.put("0", 3.141592);
    parameters.put("dummy", "lorem ipsum");  // this parameter should be ignored properly
    parameters.put("3", "pi");

    when(message.getParameterValues()).thenReturn(parameters);

    assertEquals("pi = 3.1416", formatter.formatMessage(message));
  }
}
