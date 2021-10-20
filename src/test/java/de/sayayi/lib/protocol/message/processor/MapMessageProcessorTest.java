/*
 * Copyright 2021 Jeroen Gremmen
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
package de.sayayi.lib.protocol.message.processor;

import de.sayayi.lib.protocol.exception.ProtocolException;
import de.sayayi.lib.protocol.message.formatter.ToStringMessageFormatter;
import de.sayayi.lib.protocol.spi.GenericProtocolFactory;
import org.junit.Before;
import org.junit.Test;

import lombok.val;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;


/**
 * @author Jeroen Gremmen
 */
public class MapMessageProcessorTest
{
  private MapMessageProcessor<String> messageProcessor;


  @Before
  public void init()
  {
    val messageMap = new HashMap<String,String>();
    messageMap.put("001", "Message 1");
    messageMap.put("002", "Message 2");
    messageMap.put("003", "Message 3");

   messageProcessor = new MapMessageProcessor<>(messageMap);
  }


  @Test
  public void testProcessor()
  {
    val factory = new GenericProtocolFactory<>(messageProcessor, ToStringMessageFormatter.IDENTITY);
    val protocol = factory.createProtocol();

    assertEquals("Message 1", protocol.warn().message("001").getMessage());
    assertEquals("Message 2", protocol.debug().message("002").getMessage());
    assertEquals("003", protocol.info().message("003").getMessageId());
  }


  @Test(expected = ProtocolException.class)
  public void testUnknownMessage()
  {
    val factory = new GenericProtocolFactory<>(messageProcessor, ToStringMessageFormatter.IDENTITY);
    val protocol = factory.createProtocol();

    protocol.warn().message("004");
  }
}
