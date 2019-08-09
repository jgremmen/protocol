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

import de.sayayi.lib.protocol.Protocol.MessageWithLevel;
import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static de.sayayi.lib.protocol.Level.Shared.DEBUG;
import static de.sayayi.lib.protocol.Level.Shared.ERROR;
import static de.sayayi.lib.protocol.Level.Shared.INFO;
import static de.sayayi.lib.protocol.Level.Shared.LOWEST;
import static de.sayayi.lib.protocol.Level.Shared.WARN;
import static de.sayayi.lib.protocol.ProtocolFactory.Constants.TICKET_PARAMETER_NAME;
import static de.sayayi.lib.protocol.Tag.MatchCondition.AT_LEAST;
import static de.sayayi.lib.protocol.Tag.MatchCondition.EQUAL;
import static de.sayayi.lib.protocol.Tag.MatchCondition.NOT_EQUAL;
import static de.sayayi.lib.protocol.Tag.MatchCondition.UNTIL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * @author Jeroen Gremmen
 */
@SuppressWarnings({ "ConstantConditions" })
public class ProtocolFactoryTest
{
  @Test
  public void testIsRegisteredTag()
  {
    GenericProtocolFactory factory = new GenericProtocolFactory();
    Tag tag = factory.createTag("tag").getTag();

    assertTrue(factory.isRegisteredTag(tag));

    Tag tagSame = new Tag() {
      @NotNull @Override public String getName() { return "tag"; }
      @NotNull @Override public Tag.MatchCondition getMatchCondition() { return AT_LEAST; }
      @NotNull @Override public Level getMatchLevel() { return LOWEST; }
      @Override public boolean matches(@NotNull Level level) { return true; }
      @NotNull @Override public Set<Tag> getImpliedTags() { return Collections.<Tag>singleton(this); }
    };

    assertFalse(factory.isRegisteredTag(tagSame));

    ProtocolFactory factory2 = new GenericProtocolFactory();
    Tag tag2 = factory2.createTag("tag").getTag();

    assertTrue(factory2.isRegisteredTag(tag2));
    assertFalse(factory.isRegisteredTag(tag2));
    assertFalse(factory2.isRegisteredTag(tag));

    assertFalse(factory2.isRegisteredTag(tagSame));

    try {
      //noinspection ResultOfMethodCallIgnored
      factory.isRegisteredTag(null);
      fail();
    } catch(NullPointerException ignore) {
    }
  }


  @Test
  public void testGetTags()
  {
    GenericProtocolFactory factory = new GenericProtocolFactory();

    Tag tag1 = factory.createTag("tag1").getTag();
    Tag tagUI = factory.createTag("UI").getTag();
    Tag tagSummary = factory.createTag("summary").getTag();

    Set<Tag> tags = factory.getTags();

    assertEquals(4, tags.size());

    assertTrue(tags.contains(factory.getDefaultTag()));
    assertTrue(tags.contains(tag1));
    assertTrue(tags.contains(tagUI));
    assertTrue(tags.contains(tagSummary));

    assertTrue(factory.hasTag(ProtocolFactory.Constants.DEFAULT_TAG_NAME));
    assertTrue(factory.hasTag("tag1"));
    assertTrue(factory.hasTag("UI"));
    assertTrue(factory.hasTag("summary"));

    try {
      //noinspection ResultOfMethodCallIgnored
      factory.hasTag(null);
      fail();
    } catch(Exception ignore) {
    }

    try {
      //noinspection ResultOfMethodCallIgnored,PatternValidation
      factory.hasTag("");
      fail();
    } catch(Exception ignore) {
    }
  }


  @Test
  public void testTagByName()
  {
    GenericProtocolFactory factory = new GenericProtocolFactory();

    Tag tag1 = factory.createTag("tag1").getTag();

    assertEquals("tag1", tag1.getName());
    assertEquals(tag1, factory.getTagByName("tag1"));
    assertEquals(factory.getDefaultTag(), factory.getTagByName(ProtocolFactory.Constants.DEFAULT_TAG_NAME));
    assertNull(factory.getTagByName("xyz"));

    try {
      //noinspection ResultOfMethodCallIgnored
      factory.getTagByName(null);
      fail();
    } catch(Exception ignore) {
    }

    try {
      //noinspection ResultOfMethodCallIgnored,PatternValidation
      factory.getTagByName("");
      fail();
    } catch(Exception ignore) {
    }
  }


  @Test
  public void testTagMatch()
  {
    GenericProtocolFactory factory = new GenericProtocolFactory();

    Tag tagAtLeastInfo = factory.createTag("tag1").match(AT_LEAST, INFO).getTag();

    assertEquals(INFO, tagAtLeastInfo.getMatchLevel());
    assertEquals(AT_LEAST, tagAtLeastInfo.getMatchCondition());

    assertTrue(tagAtLeastInfo.matches(INFO));
    assertTrue(tagAtLeastInfo.matches(ERROR));
    assertFalse(tagAtLeastInfo.matches(DEBUG));

    Tag tagEqualDebug = factory.createTag("tag2").match(EQUAL, DEBUG).getTag();

    assertEquals(DEBUG, tagEqualDebug.getMatchLevel());
    assertEquals(EQUAL, tagEqualDebug.getMatchCondition());

    assertTrue(tagEqualDebug.matches(DEBUG));
    assertFalse(tagEqualDebug.matches(ERROR));
    assertFalse(tagEqualDebug.matches(LOWEST));

    Tag tagNotEqualError = factory.createTag("tag3").match(NOT_EQUAL, ERROR).getTag();

    assertEquals(ERROR, tagNotEqualError.getMatchLevel());
    assertEquals(NOT_EQUAL, tagNotEqualError.getMatchCondition());

    assertTrue(tagNotEqualError.matches(DEBUG));
    assertFalse(tagNotEqualError.matches(ERROR));
    assertTrue(tagNotEqualError.matches(LOWEST));

    Tag tagUntilWarn = factory.createTag("tag4").match(UNTIL, WARN).getTag();

    assertEquals(WARN, tagUntilWarn.getMatchLevel());
    assertEquals(UNTIL, tagUntilWarn.getMatchCondition());

    assertTrue(tagUntilWarn.matches(DEBUG));
    assertTrue(tagUntilWarn.matches(WARN));
    assertFalse(tagUntilWarn.matches(ERROR));
    assertTrue(tagUntilWarn.matches(LOWEST));
  }


  @Test
  public void testProcessMessage()
  {
    GenericProtocolFactory factory = new GenericProtocolFactory() {
      @Override public String processMessage(@NotNull String message) { return message + "(ok)"; }
    };

    Protocol<String> protocol = factory.createProtocol().debug().message("msg");
    ProtocolIterator<String> iterator = protocol.iterator(LOWEST, factory.getDefaultTag());

    iterator.next();  // protocol start

    assertEquals("msg(ok)", ((MessageEntry<String>)iterator.next()).getMessage());
  }


  @Test
  public void testCreateTicketFor()
  {
    GenericProtocolFactory factory = new GenericProtocolFactory() {
      @Override public @NotNull String createTicketFor(@NotNull MessageWithLevel<String> message) { return "TICKET"; }
    };

    Protocol<String> protocol = factory.createProtocol().debug().message("msg").withTicket();
    ProtocolIterator<String> iterator = protocol.iterator(LOWEST, factory.getDefaultTag());

    iterator.next();  // protocol start

    MessageEntry<String> entry = (MessageEntry<String>)iterator.next();

    assertEquals("TICKET", entry.getParameterValues().get(TICKET_PARAMETER_NAME));
  }
}
