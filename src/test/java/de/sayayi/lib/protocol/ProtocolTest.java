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
package de.sayayi.lib.protocol;

import org.junit.Test;

import static de.sayayi.lib.protocol.Level.Shared.DEBUG;
import static de.sayayi.lib.protocol.Level.Shared.INFO;
import static de.sayayi.lib.protocol.Tag.MatchCondition.AT_LEAST;


/**
 * @author Jeroen Gremmen
 */
public class ProtocolTest
{
  @SuppressWarnings("squid:S2699")
  @Test
  public void testBasics()
  {
    GenericProtocolFactory factory = new GenericProtocolFactory();

    factory.createTag("ui").match(AT_LEAST, INFO)
           .createTag("technical").dependsOn("ui").implies("system");

    Tag ui = factory.getTagByName("ui");

    Protocol protocol = factory.createProtocol();

    protocol.add(DEBUG).message("Just sayin'")
            .error(new NullPointerException()).forTags(ui).message("MSG-048");

    ProtocolGroup gp = protocol.createGroup().setGroupMessage("Huhu");

    gp.error().message("MSG-104").with("test", true)
      .setGroupMessage("GRP-771").with("idx", 45);
  }
}
