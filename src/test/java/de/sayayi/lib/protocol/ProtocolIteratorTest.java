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

import de.sayayi.lib.protocol.ProtocolIterator.DepthEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupStartEntry;
import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import org.junit.Test;

import lombok.val;

import static de.sayayi.lib.protocol.Level.Shared.DEBUG;
import static de.sayayi.lib.protocol.Level.Shared.ERROR;
import static de.sayayi.lib.protocol.Level.Shared.WARN;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.FLATTEN_ON_SINGLE_ENTRY;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ALWAYS;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.isError;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.isLowest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * @author Jeroen Gremmen
 */
public class ProtocolIteratorTest
{
  @Test
  public void testDepth()
  {
    val factory = StringProtocolFactory.createPlainTextFactory();
    val protocol = factory.createProtocol();

    ProtocolGroup<String> grp1, grp2;

    protocol.debug().message("d0,msg1");

    grp1 = protocol.createGroup().setGroupMessage("d0,grp1");
    grp1.debug().message("d1,msg1");

    grp2 = grp1.createGroup().setGroupMessage("d1,grp1");
    grp2.debug().message("d2,msg1")
        .debug().message("d2,msg2");

    grp1.debug().message("d1,msg2");

    //String tree = protocol.format(new TechnicalProtocolFormatter<String>(factory));

    val iterator = protocol.iterator(isLowest());
    GroupStartEntry<String> grpEntry;
    MessageEntry<String> msgEntry;

    assertTrue(iterator.next() instanceof ProtocolIterator.ProtocolStart);

    msgEntry = (MessageEntry<String>)iterator.next();
    assertTrue(msgEntry.isFirst());
    assertFalse(msgEntry.isLast());
    assertEquals(0, msgEntry.getDepth());
    assertEquals("d0,msg1", msgEntry.getMessage());

    grpEntry = (GroupStartEntry<String>)iterator.next();
    assertFalse(grpEntry.isFirst());
    assertTrue(grpEntry.isLast());
    assertEquals(1, grpEntry.getDepth());
    assertEquals("d0,grp1", grpEntry.getGroupMessage().getMessage());
    assertEquals(3, grpEntry.getMessageCount());

    msgEntry = (MessageEntry<String>)iterator.next();
    assertTrue(msgEntry.isFirst());
    assertFalse(msgEntry.isLast());
    assertEquals(1, msgEntry.getDepth());
    assertEquals("d1,msg1", msgEntry.getMessage());

    grpEntry = (GroupStartEntry<String>)iterator.next();
    assertFalse(grpEntry.isFirst());
    assertFalse(grpEntry.isLast());
    assertEquals(2, grpEntry.getDepth());
    assertEquals("d1,grp1", grpEntry.getGroupMessage().getMessage());
    assertEquals(2, grpEntry.getMessageCount());

    msgEntry = (MessageEntry<String>)iterator.next();
    assertTrue(msgEntry.isFirst());
    assertFalse(msgEntry.isLast());
    assertEquals(2, msgEntry.getDepth());
    assertEquals("d2,msg1", msgEntry.getMessage());

    msgEntry = (MessageEntry<String>)iterator.next();
    assertFalse(msgEntry.isFirst());
    assertTrue(msgEntry.isLast());
    assertEquals(2, msgEntry.getDepth());
    assertEquals("d2,msg2", msgEntry.getMessage());

    assertTrue(iterator.next() instanceof ProtocolIterator.GroupEndEntry);

    msgEntry = (MessageEntry<String>)iterator.next();
    assertFalse(msgEntry.isFirst());
    assertTrue(msgEntry.isLast());
    assertEquals(1, msgEntry.getDepth());
    assertEquals("d1,msg2", msgEntry.getMessage());

    assertTrue(iterator.next() instanceof ProtocolIterator.GroupEndEntry);
    assertTrue(iterator.next() instanceof ProtocolIterator.ProtocolEnd);
  }


  @Test
  public void testGroupGroup()
  {
    val factory = StringProtocolFactory.createPlainTextFactory();
    val protocol = factory.createProtocol();

    protocol.createGroup().setGroupMessage("grp #1, header")
                          .debug().message("grp #1, msg #1")
            .getParent()
            .createGroup().setGroupMessage("grp #2, header").setVisibility(FLATTEN_ON_SINGLE_ENTRY)
                          .debug().message("grp #2, msg #1");

    val iterator = protocol.iterator(isLowest());
    GroupStartEntry<String> grpEntry;
    MessageEntry<String> msgEntry;

    assertTrue(iterator.next() instanceof ProtocolIterator.ProtocolStart);

    grpEntry = (GroupStartEntry<String>)iterator.next();
    assertTrue(grpEntry.isFirst());
    assertFalse(grpEntry.isLast());
    assertEquals(1, grpEntry.getDepth());
    assertEquals("grp #1, header", grpEntry.getGroupMessage().getMessage());

    msgEntry = (MessageEntry<String>)iterator.next();
    assertTrue(msgEntry.isFirst());
    assertTrue(msgEntry.isLast());
    assertEquals(1, msgEntry.getDepth());
    assertEquals("grp #1, msg #1", msgEntry.getMessage());

    assertTrue(iterator.next() instanceof ProtocolIterator.GroupEndEntry);

    msgEntry = (MessageEntry<String>)iterator.next();
    assertFalse(msgEntry.isFirst());
    assertTrue(msgEntry.isLast());
    assertEquals(0, msgEntry.getDepth());
    assertEquals("grp #2, msg #1", msgEntry.getMessage());

    assertTrue(iterator.next() instanceof ProtocolIterator.ProtocolEnd);
  }


  @Test
  public void testNoMessages()
  {
    val factory = StringProtocolFactory.createPlainTextFactory();
    val protocol = factory.createProtocol().debug().message("msg");
    val iterator = protocol.iterator(isError());

    assertTrue(iterator.next() instanceof ProtocolIterator.ProtocolStart);
    assertTrue(iterator.next() instanceof ProtocolIterator.ProtocolEnd);
  }


  @Test
  public void testSingleMessage()
  {
    val factory = StringProtocolFactory.createPlainTextFactory();
    val protocol = factory.createProtocol().debug().message("msg #1");
    val iterator = protocol.iterator(isLowest());

    DepthEntry<String> entry;
    MessageEntry<String> message;

    assertTrue(iterator.next() instanceof ProtocolIterator.ProtocolStart);

    // msg 1
    assertTrue(iterator.hasNext());
    entry = iterator.next();
    assertTrue(entry instanceof MessageEntry);
    message = (MessageEntry<String>)entry;
    assertEquals(0, message.getDepth());
    assertTrue(message.isFirst());
    assertTrue(message.isLast());
    assertEquals("msg #1", message.getMessage());
    assertEquals(DEBUG, message.getLevel());

    assertTrue(iterator.next() instanceof ProtocolIterator.ProtocolEnd);
  }


  @Test
  public void testMessagesOnly()
  {
    val factory = StringProtocolFactory.createPlainTextFactory();
    val protocol = factory.createProtocol();

    protocol.debug().message("msg #1")
            .warn().message("msg #2")
            .error().message("msg #3");

    val iterator = protocol.iterator(isLowest());

    DepthEntry<String> entry;
    MessageEntry<String> message;

    assertTrue(iterator.next() instanceof ProtocolIterator.ProtocolStart);

    // msg 1
    assertTrue(iterator.hasNext());
    entry = iterator.next();
    assertTrue(entry instanceof MessageEntry);
    message = (MessageEntry<String>)entry;
    assertEquals(0, message.getDepth());
    assertTrue(message.isFirst());
    assertFalse(message.isLast());
    assertEquals("msg #1", message.getMessage());
    assertEquals(DEBUG, message.getLevel());

    // msg 2
    assertTrue(iterator.hasNext());
    entry = iterator.next();
    assertTrue(entry instanceof MessageEntry);
    message = (MessageEntry<String>)entry;
    assertEquals(0, message.getDepth());
    assertFalse(message.isFirst());
    assertFalse(message.isLast());
    assertEquals("msg #2", message.getMessage());
    assertEquals(WARN, message.getLevel());

    // msg 3
    assertTrue(iterator.hasNext());
    entry = iterator.next();
    assertTrue(entry instanceof MessageEntry);
    message = (MessageEntry<String>)entry;
    assertEquals(0, message.getDepth());
    assertFalse(message.isFirst());
    assertTrue(message.isLast());
    assertEquals("msg #3", message.getMessage());
    assertEquals(ERROR, message.getLevel());

    assertTrue(iterator.next() instanceof ProtocolIterator.ProtocolEnd);
  }


  @Test
  public void testBug1()
  {
    val factory = StringProtocolFactory.createPlainTextFactory();
    val protocol = factory.createProtocol().createGroup();

    protocol.setVisibility(SHOW_HEADER_ALWAYS)
        .setGroupMessage("Group")
        .error().message("Error");

    val iterator = protocol.iterator(isLowest());

    assertTrue(iterator.next() instanceof ProtocolIterator.ProtocolStart);
    assertTrue(iterator.next() instanceof ProtocolIterator.GroupStartEntry);
    assertTrue(iterator.next() instanceof ProtocolIterator.MessageEntry);
    assertTrue(iterator.next() instanceof ProtocolIterator.GroupEndEntry);
    assertTrue(iterator.next() instanceof ProtocolIterator.ProtocolEnd);
  }
}
