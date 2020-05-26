package de.sayayi.lib.protocol;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;


/**
 * @author Jeroen Gremmen
 */
public class TranslateTagTest
{
  @Test
  public void testEffectiveTag()
  {
    ProtocolFactory<String> factory = new GenericProtocolFactory();
    Tag tagUi = factory.createTag("ui").getTag();
    Tag tagValidate = factory.createTag("validate").getTag();
    Tag tagTest = factory.createTag("test").getTag();

    Protocol<String> pr = factory.createProtocol()
        .translateTag("validate", "ui");

    assertSame(tagUi, pr.getEffectiveTag(tagUi));
    assertSame(tagUi, pr.getEffectiveTag(tagValidate));
    assertSame(tagTest, pr.getEffectiveTag(tagTest));

    ProtocolGroup<String> pg1 = pr.createGroup()
        .translateTag("test", "validate");

    assertSame(tagUi, pg1.getEffectiveTag(tagUi));
    assertSame(tagUi, pg1.getEffectiveTag(tagValidate));
    assertSame(tagUi, pg1.getEffectiveTag(tagTest));
  }


  @Test
  public void testTranslateTag()
  {
    ProtocolFactory<String> factory = new GenericProtocolFactory();
    Tag tagUi = factory.createTag("ui").getTag();
    Tag tagValidate = factory.createTag("validate").getTag();

    Protocol<String> pr = factory.createProtocol()
        .translateTag("validate", "ui");

    ProtocolGroup<String> pg1 = pr.createGroup().error().forTag("validate").message("Validation error");
    ProtocolIterator<String> iterator = pr.iterator(Level.Shared.LOWEST, tagUi);

    assertTrue(iterator.hasNext());

    ProtocolIterator.DepthEntry<String> entry = iterator.next();
    assertTrue(entry instanceof ProtocolIterator.ProtocolStart);

    entry = iterator.next();
    assertTrue(entry instanceof ProtocolIterator.MessageEntry);

    ProtocolIterator.MessageEntry<String> msg = (ProtocolIterator.MessageEntry<String>)entry;
    assertEquals("Validation error", msg.getMessage());
  }
}
