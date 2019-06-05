package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.Level.Shared;
import org.junit.Test;

import static de.sayayi.lib.protocol.Level.Shared.ALL;
import static de.sayayi.lib.protocol.Level.Shared.DEBUG;
import static de.sayayi.lib.protocol.Level.Shared.INFO;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.FLATTEN;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.FLATTEN_ON_SINGLE_ENTRY;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.HIDDEN;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ALWAYS;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_IF_NOT_EMPTY;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ONLY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * @author Jeroen Gremmen
 */
public class ProtocolGroupTest
{
  @Test
  public void testIsHeaderVisible()
  {
    DefaultProtocolFactory factory = new DefaultProtocolFactory();
    Tag tag = factory.getDefaultTag();
    ProtocolGroup gp = factory.createProtocol().createGroup();

    assertFalse(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).isHeaderVisible(ALL, tag));
    assertFalse(gp.setVisibility(SHOW_HEADER_ALWAYS).isHeaderVisible(ALL, tag));
    assertFalse(gp.setVisibility(SHOW_HEADER_ONLY).isHeaderVisible(ALL, tag));
    assertFalse(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).isHeaderVisible(ALL, tag));
    assertFalse(gp.setVisibility(FLATTEN).isHeaderVisible(ALL, tag));
    assertFalse(gp.setVisibility(HIDDEN).isHeaderVisible(ALL, tag));

    gp.setGroupMessage("Test");

    assertFalse(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).isHeaderVisible(ALL, tag));
    assertTrue(gp.setVisibility(SHOW_HEADER_ALWAYS).isHeaderVisible(ALL, tag));
    assertTrue(gp.setVisibility(SHOW_HEADER_ONLY).isHeaderVisible(ALL, tag));
    assertFalse(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).isHeaderVisible(ALL, tag));
    assertFalse(gp.setVisibility(FLATTEN).isHeaderVisible(ALL, tag));
    assertFalse(gp.setVisibility(HIDDEN).isHeaderVisible(ALL, tag));

    gp.debug().message("Msg #1");

    assertTrue(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).isHeaderVisible(DEBUG, tag));
    assertTrue(gp.setVisibility(SHOW_HEADER_ALWAYS).isHeaderVisible(DEBUG, tag));
    assertTrue(gp.setVisibility(SHOW_HEADER_ONLY).isHeaderVisible(DEBUG, tag));
    assertFalse(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).isHeaderVisible(DEBUG, tag));
    assertFalse(gp.setVisibility(FLATTEN).isHeaderVisible(DEBUG, tag));
    assertFalse(gp.setVisibility(HIDDEN).isHeaderVisible(DEBUG, tag));

    assertFalse(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).isHeaderVisible(INFO, tag));
    assertTrue(gp.setVisibility(SHOW_HEADER_ALWAYS).isHeaderVisible(INFO, tag));
    assertTrue(gp.setVisibility(SHOW_HEADER_ONLY).isHeaderVisible(INFO, tag));
    assertFalse(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).isHeaderVisible(INFO, tag));
    assertFalse(gp.setVisibility(FLATTEN).isHeaderVisible(INFO, tag));
    assertFalse(gp.setVisibility(HIDDEN).isHeaderVisible(INFO, tag));

    gp.info().message("Msg #2");

    assertTrue(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).isHeaderVisible(DEBUG, tag));
    assertTrue(gp.setVisibility(SHOW_HEADER_ALWAYS).isHeaderVisible(DEBUG, tag));
    assertTrue(gp.setVisibility(SHOW_HEADER_ONLY).isHeaderVisible(DEBUG, tag));
    assertTrue(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).isHeaderVisible(DEBUG, tag));
    assertFalse(gp.setVisibility(FLATTEN).isHeaderVisible(DEBUG, tag));
    assertFalse(gp.setVisibility(HIDDEN).isHeaderVisible(DEBUG, tag));

    gp.error().message("Msg #3");

    assertTrue(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).isHeaderVisible(ALL, tag));
    assertTrue(gp.setVisibility(SHOW_HEADER_ALWAYS).isHeaderVisible(ALL, tag));
    assertTrue(gp.setVisibility(SHOW_HEADER_ONLY).isHeaderVisible(ALL, tag));
    assertTrue(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).isHeaderVisible(ALL, tag));
    assertFalse(gp.setVisibility(FLATTEN).isHeaderVisible(ALL, tag));
    assertFalse(gp.setVisibility(HIDDEN).isHeaderVisible(ALL, tag));
  }


  @Test
  public void testHasVisualEntry()
  {
    DefaultProtocolFactory factory = new DefaultProtocolFactory();
    Tag tag = factory.getDefaultTag();
    ProtocolGroup gp = factory.createProtocol().createGroup();

    assertFalse(gp.hasVisibleElement(Shared.ALL, tag));

    gp.setGroupMessage("Test").with("param1", "Huhu");

    assertTrue(gp.setVisibility(SHOW_HEADER_ONLY).hasVisibleElement(ALL, tag));
    assertTrue(gp.setVisibility(SHOW_HEADER_ALWAYS).hasVisibleElement(ALL, tag));
    assertFalse(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).hasVisibleElement(ALL, tag));
    assertFalse(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).hasVisibleElement(ALL, tag));
    assertFalse(gp.setVisibility(HIDDEN).hasVisibleElement(ALL, tag));
    assertFalse(gp.setVisibility(FLATTEN).hasVisibleElement(ALL, tag));

    gp.debug().message("msg #1");

    assertTrue(gp.setVisibility(SHOW_HEADER_ONLY).hasVisibleElement(ALL, tag));
    assertTrue(gp.setVisibility(SHOW_HEADER_ALWAYS).hasVisibleElement(ALL, tag));
    assertTrue(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).hasVisibleElement(ALL, tag));
    assertTrue(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).hasVisibleElement(ALL, tag));
    assertFalse(gp.setVisibility(HIDDEN).hasVisibleElement(ALL, tag));
    assertTrue(gp.setVisibility(FLATTEN).hasVisibleElement(ALL, tag));

    gp.debug().message("msg #2");

    assertTrue(gp.setVisibility(SHOW_HEADER_ONLY).hasVisibleElement(ALL, tag));
    assertTrue(gp.setVisibility(SHOW_HEADER_ALWAYS).hasVisibleElement(ALL, tag));
    assertTrue(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).hasVisibleElement(ALL, tag));
    assertTrue(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).hasVisibleElement(ALL, tag));
    assertFalse(gp.setVisibility(HIDDEN).hasVisibleElement(ALL, tag));
    assertTrue(gp.setVisibility(FLATTEN).hasVisibleElement(ALL, tag));
  }


  @Test
  public void testEffectiveVisibility()
  {
    DefaultProtocolFactory factory = new DefaultProtocolFactory();
    ProtocolGroup gp = factory.createProtocol().createGroup();

    // no group header
    assertEquals(HIDDEN, gp.setVisibility(SHOW_HEADER_ONLY).getEffectiveVisibility());
    assertEquals(FLATTEN, gp.setVisibility(SHOW_HEADER_ALWAYS).getEffectiveVisibility());
    assertEquals(FLATTEN, gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).getEffectiveVisibility());
    assertEquals(FLATTEN_ON_SINGLE_ENTRY, gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).getEffectiveVisibility());
    assertEquals(HIDDEN, gp.setVisibility(HIDDEN).getEffectiveVisibility());
    assertEquals(FLATTEN, gp.setVisibility(FLATTEN).getEffectiveVisibility());

    gp.setGroupMessage("Test");

    // with group header
    assertEquals(SHOW_HEADER_ONLY, gp.setVisibility(SHOW_HEADER_ONLY).getEffectiveVisibility());
    assertEquals(SHOW_HEADER_ALWAYS, gp.setVisibility(SHOW_HEADER_ALWAYS).getEffectiveVisibility());
    assertEquals(SHOW_HEADER_IF_NOT_EMPTY, gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).getEffectiveVisibility());
    assertEquals(FLATTEN_ON_SINGLE_ENTRY, gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).getEffectiveVisibility());
    assertEquals(HIDDEN, gp.setVisibility(HIDDEN).getEffectiveVisibility());
    assertEquals(FLATTEN, gp.setVisibility(FLATTEN).getEffectiveVisibility());
  }
}
