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

import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import de.sayayi.lib.protocol.exception.ProtocolException;
import de.sayayi.lib.protocol.message.formatter.ToStringMessageFormatter;
import de.sayayi.lib.protocol.spi.GenericMessageWithId;
import de.sayayi.lib.protocol.spi.GenericProtocolFactory;
import org.junit.Test;

import lombok.val;

import static de.sayayi.lib.protocol.Level.Shared.DEBUG;
import static de.sayayi.lib.protocol.Level.Shared.ERROR;
import static de.sayayi.lib.protocol.Level.Shared.INFO;
import static de.sayayi.lib.protocol.Level.Shared.LOWEST;
import static de.sayayi.lib.protocol.Level.Shared.WARN;
import static de.sayayi.lib.protocol.ProtocolFactory.DEFAULT_TAG_NAME;
import static de.sayayi.lib.protocol.TagDef.MatchCondition.AT_LEAST;
import static de.sayayi.lib.protocol.TagDef.MatchCondition.EQUAL;
import static de.sayayi.lib.protocol.TagDef.MatchCondition.NOT_EQUAL;
import static de.sayayi.lib.protocol.TagDef.MatchCondition.UNTIL;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.isLowest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * @author Jeroen Gremmen
 */
@SuppressWarnings({ "ConstantConditions", "ResultOfMethodCallIgnored" })
public class ProtocolFactoryTest
{
  @Test(expected = ProtocolException.class)
  public void testCreateEmptyTag() {
    StringProtocolFactory.createPlainTextFactory().createTag("");
  }


  @Test(expected = ProtocolException.class)
  public void testCreateDuplicateTag()
  {
    val factory = StringProtocolFactory.createPlainTextFactory();
    factory.createTag("tag");
    factory.createTag("tag");
  }


  @Test(expected = ProtocolException.class)
  public void testModifyEmptyTag() {
    StringProtocolFactory.createPlainTextFactory().modifyTag("");
  }


  @Test(expected = ProtocolException.class)
  public void testModifyUnknownTag() {
    StringProtocolFactory.createPlainTextFactory().modifyTag("unknown");
  }


  @Test
  public void testModifyKnownTag()
  {
    val factory = new StringProtocolFactory(ToStringMessageFormatter.IDENTITY);
    factory.createTag("tag");

    assertNotNull(factory.modifyTag("tag"));
  }


  @Test
  public void testGetTags()
  {
    val factory = new StringProtocolFactory(ToStringMessageFormatter.IDENTITY);

    val tagDef1 = factory.createTag("tag1").getTagDef();
    val tagDefUI = factory.createTag("UI").getTagDef();
    val tagDefSummary = factory.createTag("summary").getTagDef();

    val tagDefs = factory.getTagDefs();

    assertEquals(4, tagDefs.size());

    assertTrue(tagDefs.contains(factory.getDefaultTag()));
    assertTrue(tagDefs.contains(tagDef1));
    assertTrue(tagDefs.contains(tagDefUI));
    assertTrue(tagDefs.contains(tagDefSummary));

    assertTrue(factory.hasTag(DEFAULT_TAG_NAME));
    assertTrue(factory.hasTag("tag1"));
    assertTrue(factory.hasTag("UI"));
    assertTrue(factory.hasTag("summary"));
    assertFalse(factory.hasTag(null));
    assertFalse(factory.hasTag(""));
    assertFalse(factory.hasTag("(.,"));
  }


  @Test
  public void testTagByName()
  {
    val factory = new StringProtocolFactory(ToStringMessageFormatter.IDENTITY);

    val tagDef1 = factory.createTag("tag1").getTagDef();

    assertEquals("tag1", tagDef1.getName());
    assertEquals(tagDef1, factory.getTagByName("tag1"));
    assertEquals(factory.getDefaultTag(), factory.getTagByName(DEFAULT_TAG_NAME));
    assertNotNull(factory.getTagByName("xyz"));

    try {
      //noinspection ResultOfMethodCallIgnored
      factory.getTagByName(null);
      fail();
    } catch(Exception ignore) {
    }

    try {
      // noinspection ResultOfMethodCallIgnored
      factory.getTagByName("");
      fail();
    } catch(Exception ignore) {
    }
  }


  @Test
  public void testTagMatch()
  {
    val factory = new StringProtocolFactory(ToStringMessageFormatter.IDENTITY);

    val tagDefAtLeastInfo = factory.createTag("tag1").match(AT_LEAST, INFO).getTagDef();

    assertEquals(INFO, tagDefAtLeastInfo.getMatchLevel());
    assertEquals(AT_LEAST, tagDefAtLeastInfo.getMatchCondition());

    assertTrue(tagDefAtLeastInfo.matches(INFO));
    assertTrue(tagDefAtLeastInfo.matches(ERROR));
    assertFalse(tagDefAtLeastInfo.matches(DEBUG));

    val tagDefEqualDebug = factory.createTag("tag2").match(EQUAL, DEBUG).getTagDef();

    assertEquals(DEBUG, tagDefEqualDebug.getMatchLevel());
    assertEquals(EQUAL, tagDefEqualDebug.getMatchCondition());

    assertTrue(tagDefEqualDebug.matches(DEBUG));
    assertFalse(tagDefEqualDebug.matches(ERROR));
    assertFalse(tagDefEqualDebug.matches(LOWEST));

    val tagDefNotEqualError = factory.createTag("tag3").match(NOT_EQUAL, ERROR).getTagDef();

    assertEquals(ERROR, tagDefNotEqualError.getMatchLevel());
    assertEquals(NOT_EQUAL, tagDefNotEqualError.getMatchCondition());

    assertTrue(tagDefNotEqualError.matches(DEBUG));
    assertFalse(tagDefNotEqualError.matches(ERROR));
    assertTrue(tagDefNotEqualError.matches(LOWEST));

    val tagDefUntilWarn = factory.createTag("tag4").match(UNTIL, WARN).getTagDef();

    assertEquals(WARN, tagDefUntilWarn.getMatchLevel());
    assertEquals(UNTIL, tagDefUntilWarn.getMatchCondition());

    assertTrue(tagDefUntilWarn.matches(DEBUG));
    assertTrue(tagDefUntilWarn.matches(WARN));
    assertFalse(tagDefUntilWarn.matches(ERROR));
    assertTrue(tagDefUntilWarn.matches(LOWEST));
  }


  @Test
  public void testProcessMessage()
  {
    val factory = new GenericProtocolFactory<>(message -> new GenericMessageWithId<>(message + "(ok)"),
        ToStringMessageFormatter.IDENTITY);
    val protocol = factory.createProtocol().debug().message("msg");
    val iterator = protocol.iterator(isLowest());

    iterator.next();  // protocol start

    assertEquals("msg(ok)", ((MessageEntry<String>)iterator.next()).getMessage());
  }


  @Test
  public void testToString()
  {
    val factory = new StringProtocolFactory(ToStringMessageFormatter.IDENTITY);
    factory.createTag("XYZ1");
    factory.createTag("XYZ2");

    val s = factory.toString();

    assertTrue(s.contains("XYZ1"));
    assertTrue(s.contains("XYZ2"));
  }
}
