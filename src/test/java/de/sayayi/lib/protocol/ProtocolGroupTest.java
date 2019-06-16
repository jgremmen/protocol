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
    GenericProtocolFactory factory = new GenericProtocolFactory();
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
    GenericProtocolFactory factory = new GenericProtocolFactory();
    Tag tag = factory.getDefaultTag();
    ProtocolGroup gp = factory.createProtocol().createGroup();

    assertEquals(0, gp.getVisibleEntryCount(Shared.ALL, tag));

    gp.setGroupMessage("Test").with("param1", "Huhu");

    assertEquals(1, gp.setVisibility(SHOW_HEADER_ONLY).getVisibleEntryCount(ALL, tag));
    assertEquals(1, gp.setVisibility(SHOW_HEADER_ALWAYS).getVisibleEntryCount(ALL, tag));
    assertEquals(0, gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).getVisibleEntryCount(ALL, tag));
    assertEquals(0, gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).getVisibleEntryCount(ALL, tag));
    assertEquals(0, gp.setVisibility(HIDDEN).getVisibleEntryCount(ALL, tag));
    assertEquals(0, gp.setVisibility(FLATTEN).getVisibleEntryCount(ALL, tag));

    gp.debug().message("msg #1");

    assertEquals(1, gp.setVisibility(SHOW_HEADER_ONLY).getVisibleEntryCount(ALL, tag));
    assertEquals(1, gp.setVisibility(SHOW_HEADER_ALWAYS).getVisibleEntryCount(ALL, tag));
    assertEquals(1, gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).getVisibleEntryCount(ALL, tag));
    assertEquals(1, gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).getVisibleEntryCount(ALL, tag));
    assertEquals(0, gp.setVisibility(HIDDEN).getVisibleEntryCount(ALL, tag));
    assertEquals(1, gp.setVisibility(FLATTEN).getVisibleEntryCount(ALL, tag));

    gp.debug().message("msg #2");

    assertEquals(1, gp.setVisibility(SHOW_HEADER_ONLY).getVisibleEntryCount(ALL, tag));
    assertEquals(1, gp.setVisibility(SHOW_HEADER_ALWAYS).getVisibleEntryCount(ALL, tag));
    assertEquals(1, gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).getVisibleEntryCount(ALL, tag));
    assertEquals(1, gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).getVisibleEntryCount(ALL, tag));
    assertEquals(0, gp.setVisibility(HIDDEN).getVisibleEntryCount(ALL, tag));
    assertEquals(2, gp.setVisibility(FLATTEN).getVisibleEntryCount(ALL, tag));
  }


  @Test
  public void testEffectiveVisibility()
  {
    GenericProtocolFactory factory = new GenericProtocolFactory();
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
