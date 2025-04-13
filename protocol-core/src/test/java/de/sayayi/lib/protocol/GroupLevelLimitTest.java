/*
 * Copyright 2020 Jeroen Gremmen
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

import de.sayayi.lib.protocol.factory.StringProtocolFactory;
import org.junit.jupiter.api.Test;

import lombok.val;

import static de.sayayi.lib.protocol.Level.Shared.ERROR;
import static de.sayayi.lib.protocol.Level.Shared.INFO;
import static de.sayayi.lib.protocol.Level.Shared.LOWEST;
import static de.sayayi.lib.protocol.Level.Shared.WARN;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.FLATTEN_ON_SINGLE_ENTRY;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.is;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.isError;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.isInfo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;


/**
 * @author Jeroen Gremmen
 */
public class GroupLevelLimitTest
{
  @Test
  public void testLevelLimit1()
  {
    val factory = StringProtocolFactory.createPlainTextFactory();
    val gp = factory.createProtocol().createGroup();

    gp.setGroupMessage("Test");
    gp.debug().message("Msg #1");
    gp.info().message("Msg #2");
    gp.error().message("Msg #3");

    gp.setLevelLimit(WARN);

    assertFalse(gp.matches(isError()));

    val iterator = gp.iterator(isInfo());

    assertInstanceOf(ProtocolIterator.ProtocolStart.class, iterator.next());
    assertInstanceOf(ProtocolIterator.GroupStartEntry.class, iterator.next());
    assertMessageWithLevel(iterator.next(), INFO, true, false);
    assertMessageWithLevel(iterator.next(), WARN, false, true);
    assertInstanceOf(ProtocolIterator.GroupEndEntry.class, iterator.next());
    assertInstanceOf(ProtocolIterator.ProtocolEnd.class, iterator.next());
  }


  @Test
  public void testLevelLimitPropagation()
  {
    val factory = StringProtocolFactory.createPlainTextFactory();
    val p = factory.createProtocol();
    val gp1 = p.createGroup().createGroup();
    val gp2 = gp1.createGroup().createGroup();

    gp2.setVisibility(FLATTEN_ON_SINGLE_ENTRY).error().message("msg").setLevelLimit(ERROR);
    gp1.setLevelLimit(WARN);

    val iterator = p.iterator(is(LOWEST));

    assertInstanceOf(ProtocolIterator.ProtocolStart.class, iterator.next());
    assertMessageWithLevel(iterator.next(), WARN, true, true);
    assertInstanceOf(ProtocolIterator.ProtocolEnd.class, iterator.next());
  }


  private <M> void assertMessageWithLevel(ProtocolIterator.DepthEntry<M> entry, Level level, boolean first, boolean last)
  {
    assertInstanceOf(ProtocolIterator.MessageEntry.class, entry);

    val message = (ProtocolIterator.MessageEntry<M>)entry;

    assertEquals(level, message.getLevel());
    assertEquals(first, message.isFirst());
    assertEquals(last, message.isLast());
  }
}