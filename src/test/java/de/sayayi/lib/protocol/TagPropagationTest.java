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
package de.sayayi.lib.protocol;

import org.junit.Test;

import static de.sayayi.lib.protocol.Level.Shared.DEBUG;
import static de.sayayi.lib.protocol.Level.Shared.ERROR;
import static de.sayayi.lib.protocol.Level.Shared.INFO;
import static de.sayayi.lib.protocol.Tag.MatchCondition.AT_LEAST;
import static org.junit.Assert.assertEquals;


/**
 * @author Jeroen Gremmen
 */
public class TagPropagationTest
{
  @Test
  public void testProtocolPropagation()
  {
    GenericProtocolFactory factory = new GenericProtocolFactory();
    Tag uiTag = factory.createTag("ui").match(AT_LEAST, INFO).getTag();

    Protocol<String> protocol = factory.createProtocol()
        .propagate(factory.getDefaultTag()).to(uiTag);

    protocol.debug().message("debug")
            .warn().message("error");

    assertEquals(0, protocol.getVisibleEntryCount(false, ERROR, uiTag));
    assertEquals(1, protocol.getVisibleEntryCount(false, DEBUG, uiTag));
  }
}