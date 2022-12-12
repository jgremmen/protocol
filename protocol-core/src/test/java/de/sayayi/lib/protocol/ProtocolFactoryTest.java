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
package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import de.sayayi.lib.protocol.message.formatter.ToStringMessageFormatter;
import de.sayayi.lib.protocol.spi.GenericMessageWithId;
import de.sayayi.lib.protocol.spi.GenericProtocolFactory;
import org.junit.jupiter.api.Test;

import lombok.val;

import static de.sayayi.lib.protocol.Level.Shared.LOWEST;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * @author Jeroen Gremmen
 */
public class ProtocolFactoryTest
{
  @Test
  public void testProcessMessage()
  {
    val factory = new GenericProtocolFactory<>(message -> new GenericMessageWithId<>(message + "(ok)"),
        ToStringMessageFormatter.IDENTITY);
    val protocol = factory.createProtocol().debug().message("msg");
    val iterator = protocol.iterator(is(LOWEST));

    iterator.next();  // protocol start

    assertEquals("msg(ok)", ((MessageEntry<String>)iterator.next()).getMessage());
  }
}