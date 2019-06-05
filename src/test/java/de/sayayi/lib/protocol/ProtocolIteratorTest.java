package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.ProtocolIterator.DepthEntry;
import de.sayayi.lib.protocol.ProtocolIterator.GroupEntry;
import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import org.junit.Test;

import static de.sayayi.lib.protocol.Level.Shared.ALL;
import static de.sayayi.lib.protocol.Level.Shared.DEBUG;
import static de.sayayi.lib.protocol.Level.Shared.ERROR;
import static de.sayayi.lib.protocol.Level.Shared.WARN;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.FLATTEN_ON_SINGLE_ENTRY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class ProtocolIteratorTest
{
  @Test
  public void testGroupGroup()
  {
    DefaultProtocolFactory factory = new DefaultProtocolFactory();
    Tag tag = factory.getDefaultTag();
    Protocol<String> protocol = factory.createProtocol();

    protocol.createGroup().setGroupMessage("grp #1, header")
                          .debug().message("grp #1, msg #1");
    protocol.createGroup().setGroupMessage("grp #2, header").setVisibility(FLATTEN_ON_SINGLE_ENTRY)
                          .debug().message("grp #2, msg #1");

    ProtocolIterator<String> iterator = protocol.iterator(ALL, tag);
    GroupEntry<String> grpEntry;
    MessageEntry<String> msgEntry;

    grpEntry = (GroupEntry<String>)iterator.next();
    assertTrue(grpEntry.isFirst());
    assertFalse(grpEntry.isLast());
    assertTrue(grpEntry.hasEntryAfterGroup());
    assertEquals(0, grpEntry.getDepth());
    assertEquals("grp #1, header", grpEntry.getGroupMessage().getMessage());

    msgEntry = (MessageEntry<String>)iterator.next();
    assertTrue(msgEntry.isFirst());
    assertTrue(msgEntry.isLast());
    assertEquals(1, msgEntry.getDepth());
    assertEquals("grp #1, msg #1", msgEntry.getMessage());

    msgEntry = (MessageEntry<String>)iterator.next();
    assertFalse(msgEntry.isFirst());
    assertTrue(msgEntry.isLast());
    assertEquals(0, msgEntry.getDepth());
    assertEquals("grp #2, msg #1", msgEntry.getMessage());
  }


  @Test
  public void testNoMessages()
  {
    DefaultProtocolFactory factory = new DefaultProtocolFactory();
    Tag tag = factory.getDefaultTag();
    Protocol<String> protocol = factory.createProtocol().debug().message("msg");
    ProtocolIterator<String> iterator = protocol.iterator(ERROR, tag);

    assertEquals(tag, iterator.getTag());
    assertEquals(ERROR, iterator.getLevel());
    assertFalse(iterator.hasNext());
  }


  @SuppressWarnings("unchecked")
  @Test
  public void testSingleMessage()
  {
    DefaultProtocolFactory factory = new DefaultProtocolFactory();
    Tag tag = factory.getDefaultTag();
    Protocol<String> protocol = factory.createProtocol().debug().message("msg #1");
    ProtocolIterator<String> iterator = protocol.iterator(ALL, tag);

    assertEquals(tag, iterator.getTag());
    assertEquals(ALL, iterator.getLevel());

    DepthEntry entry;
    MessageEntry<String> message;

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

    assertFalse(iterator.hasNext());
  }


  @SuppressWarnings("unchecked")
  @Test
  public void testMessagesOnly()
  {
    DefaultProtocolFactory factory = new DefaultProtocolFactory();
    Tag tag = factory.getDefaultTag();
    Protocol<String> protocol = factory.createProtocol();

    protocol.debug().message("msg #1")
            .warn().message("msg #2")
            .error().message("msg #3");

    ProtocolIterator<String> iterator = protocol.iterator(ALL, tag);

    assertEquals(tag, iterator.getTag());
    assertEquals(ALL, iterator.getLevel());

    DepthEntry entry;
    MessageEntry<String> message;

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

    assertFalse(iterator.hasNext());
  }
}
