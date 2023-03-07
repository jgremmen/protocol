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
package de.sayayi.lib.protocol.matcher;

import de.sayayi.lib.protocol.message.formatter.ToStringMessageFormatter;
import de.sayayi.lib.protocol.message.processor.StringMessageProcessor;
import de.sayayi.lib.protocol.spi.GenericProtocolFactory;
import org.junit.jupiter.api.Test;

import lombok.val;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Jeroen Gremmen
 * @since 1.2.1
 */
class ServiceTest extends AbstractMatcherParserTest
{
  @Test
  void testCompoundOr()
  {
    val factory = new GenericProtocolFactory<>(StringMessageProcessor.INSTANCE,
        ToStringMessageFormatter.IDENTITY);
    val matcher = factory.parseTagSelector("system or not(test-ticket)");

    assertTrue(matcher.match(asTagNameSet("mytag", "system")));
    assertFalse(matcher.match(asTagNameSet("test-ticket")));
    assertTrue(matcher.match(asTagNameSet("mytag")));
  }
}