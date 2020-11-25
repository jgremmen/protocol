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

import lombok.val;

import static de.sayayi.lib.protocol.Level.Shared.DEBUG;
import static de.sayayi.lib.protocol.Level.Shared.ERROR;
import static de.sayayi.lib.protocol.Level.Shared.INFO;
import static de.sayayi.lib.protocol.TagDef.MatchCondition.AT_LEAST;
import static org.junit.Assert.assertEquals;


/**
 * @author Jeroen Gremmen
 */
public class TagPropagationTest
{
  @Test
  public void testProtocolPropagation()
  {
    val factory = new StringProtocolFactory();
    val uiTagDef = factory.createTag("ui").match(AT_LEAST, INFO).getTagDef();

    val protocol = factory.createProtocol()
        .propagate(factory.getDefaultTag().asSelector()).to("ui");

    protocol.debug().message("debug")
            .warn().message("error");

    assertEquals(0, protocol.getVisibleEntryCount(false, ERROR, uiTagDef.asSelector()));
    assertEquals(1, protocol.getVisibleEntryCount(false, DEBUG, uiTagDef.asSelector()));
  }
}
