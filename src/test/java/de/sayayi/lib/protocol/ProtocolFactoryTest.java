package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.ProtocolIterator.MessageEntry;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static de.sayayi.lib.protocol.Level.Shared.ALL;
import static de.sayayi.lib.protocol.Level.Shared.DEBUG;
import static de.sayayi.lib.protocol.Level.Shared.ERROR;
import static de.sayayi.lib.protocol.Level.Shared.INFO;
import static de.sayayi.lib.protocol.Level.Shared.WARN;
import static de.sayayi.lib.protocol.Tag.LevelMatch.AT_LEAST;
import static de.sayayi.lib.protocol.Tag.LevelMatch.EQUAL;
import static de.sayayi.lib.protocol.Tag.LevelMatch.NOT_EQUAL;
import static de.sayayi.lib.protocol.Tag.LevelMatch.UNTIL;
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
    DefaultProtocolFactory factory = new DefaultProtocolFactory();
    Tag tag = factory.createTag("tag").getTag();

    assertTrue(factory.isRegisteredTag(tag));

    Tag tagSame = new Tag() {
      @NotNull @Override public String getName() { return "tag"; }
      @NotNull @Override public LevelMatch getLevelMatch() { return AT_LEAST; }
      @NotNull @Override public Level getLevel() { return ALL; }
      @Override public boolean isMatch(@NotNull Level level) { return true; }
      @NotNull @Override public Set<Tag> getImpliedTags() { return Collections.<Tag>singleton(this); }
    };

    assertFalse(factory.isRegisteredTag(tagSame));

    ProtocolFactory factory2 = new DefaultProtocolFactory();
    Tag tag2 = factory2.createTag("tag").getTag();

    assertTrue(factory2.isRegisteredTag(tag2));
    assertFalse(factory.isRegisteredTag(tag2));
    assertFalse(factory2.isRegisteredTag(tag));

    assertFalse(factory2.isRegisteredTag(tagSame));

    try {
      factory.isRegisteredTag(null);
      fail();
    } catch(NullPointerException ignore) {
    }
  }


  @Test
  public void testGetTags()
  {
    DefaultProtocolFactory factory = new DefaultProtocolFactory();

    Tag tag1 = factory.createTag("tag1").getTag();
    Tag tagUI = factory.createTag("UI").getTag();
    Tag tagSummary = factory.createTag("summary").getTag();

    Set<Tag> tags = factory.getTags();

    assertEquals(4, tags.size());

    assertTrue(tags.contains(factory.getDefaultTag()));
    assertTrue(tags.contains(tag1));
    assertTrue(tags.contains(tagUI));
    assertTrue(tags.contains(tagSummary));

    assertTrue(factory.hasTag(ProtocolFactory.DEFAULT_TAG_NAME));
    assertTrue(factory.hasTag("tag1"));
    assertTrue(factory.hasTag("UI"));
    assertTrue(factory.hasTag("summary"));

    try {
      factory.hasTag(null);
      fail();
    } catch(Exception ignore) {
    }

    try {
      factory.hasTag("");
      fail();
    } catch(Exception ignore) {
    }
  }


  @Test
  public void testTagByName()
  {
    DefaultProtocolFactory factory = new DefaultProtocolFactory();

    Tag tag1 = factory.createTag("tag1").getTag();

    assertEquals("tag1", tag1.getName());
    assertEquals(tag1, factory.getTagByName("tag1"));
    assertEquals(factory.getDefaultTag(), factory.getTagByName(ProtocolFactory.DEFAULT_TAG_NAME));
    assertNull(factory.getTagByName("xyz"));

    try {
      factory.getTagByName(null);
      fail();
    } catch(Exception ignore) {
    }

    try {
      factory.getTagByName("");
      fail();
    } catch(Exception ignore) {
    }
  }


  @Test
  public void testTagMatch()
  {
    DefaultProtocolFactory factory = new DefaultProtocolFactory();

    Tag tagAtLeastInfo = factory.createTag("tag1").match(AT_LEAST, INFO).getTag();

    assertEquals(INFO, tagAtLeastInfo.getLevel());
    assertEquals(AT_LEAST, tagAtLeastInfo.getLevelMatch());

    assertTrue(tagAtLeastInfo.isMatch(INFO));
    assertTrue(tagAtLeastInfo.isMatch(ERROR));
    assertFalse(tagAtLeastInfo.isMatch(DEBUG));

    Tag tagEqualDebug = factory.createTag("tag2").match(EQUAL, DEBUG).getTag();

    assertEquals(DEBUG, tagEqualDebug.getLevel());
    assertEquals(EQUAL, tagEqualDebug.getLevelMatch());

    assertTrue(tagEqualDebug.isMatch(DEBUG));
    assertFalse(tagEqualDebug.isMatch(ERROR));
    assertFalse(tagEqualDebug.isMatch(ALL));

    Tag tagNotEqualError = factory.createTag("tag3").match(NOT_EQUAL, ERROR).getTag();

    assertEquals(ERROR, tagNotEqualError.getLevel());
    assertEquals(NOT_EQUAL, tagNotEqualError.getLevelMatch());

    assertTrue(tagNotEqualError.isMatch(DEBUG));
    assertFalse(tagNotEqualError.isMatch(ERROR));
    assertTrue(tagNotEqualError.isMatch(ALL));

    Tag tagUntilWarn = factory.createTag("tag4").match(UNTIL, WARN).getTag();

    assertEquals(WARN, tagUntilWarn.getLevel());
    assertEquals(UNTIL, tagUntilWarn.getLevelMatch());

    assertTrue(tagUntilWarn.isMatch(DEBUG));
    assertTrue(tagUntilWarn.isMatch(WARN));
    assertFalse(tagUntilWarn.isMatch(ERROR));
    assertTrue(tagUntilWarn.isMatch(ALL));
  }


  @Test
  public void testProcessMessage()
  {
    DefaultProtocolFactory factory = new DefaultProtocolFactory() {
      @Override public String processMessage(@NotNull String message) { return message + "(ok)"; }
    };

    Protocol<String> protocol = factory.createProtocol().debug().message("msg");
    ProtocolIterator<String> iterator = protocol.iterator(ALL, factory.getDefaultTag());

    assertEquals("msg(ok)", ((MessageEntry<String>)iterator.next()).getMessage());
  }
}
