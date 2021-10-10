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
import de.sayayi.lib.protocol.exception.ProtocolException;
import org.junit.Test;

import lombok.val;

import java.util.LinkedHashSet;

import static de.sayayi.lib.protocol.Level.Shared.DEBUG;
import static de.sayayi.lib.protocol.Level.Shared.INFO;
import static de.sayayi.lib.protocol.Level.Shared.LOWEST;
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
    val factory = StringProtocolFactory.createPlainTextFactory();
    val gp = factory.createProtocol().createGroup();

    assertFalse(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).isHeaderVisible(LOWEST, Tag.any()));
    assertFalse(gp.setVisibility(SHOW_HEADER_ALWAYS).isHeaderVisible(LOWEST, Tag.any()));
    assertFalse(gp.setVisibility(SHOW_HEADER_ONLY).isHeaderVisible(LOWEST, Tag.any()));
    assertFalse(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).isHeaderVisible(LOWEST, Tag.any()));
    assertFalse(gp.setVisibility(FLATTEN).isHeaderVisible(LOWEST, Tag.any()));
    assertFalse(gp.setVisibility(HIDDEN).isHeaderVisible(LOWEST, Tag.any()));

    gp.setGroupMessage("Test");

    assertFalse(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).isHeaderVisible(LOWEST, Tag.any()));
    assertTrue(gp.setVisibility(SHOW_HEADER_ALWAYS).isHeaderVisible(LOWEST, Tag.any()));
    assertTrue(gp.setVisibility(SHOW_HEADER_ONLY).isHeaderVisible(LOWEST, Tag.any()));
    assertFalse(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).isHeaderVisible(LOWEST, Tag.any()));
    assertFalse(gp.setVisibility(FLATTEN).isHeaderVisible(LOWEST, Tag.any()));
    assertFalse(gp.setVisibility(HIDDEN).isHeaderVisible(LOWEST, Tag.any()));

    gp.debug().message("Msg #1");

    assertTrue(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).isHeaderVisible(DEBUG, Tag.any()));
    assertTrue(gp.setVisibility(SHOW_HEADER_ALWAYS).isHeaderVisible(DEBUG, Tag.any()));
    assertTrue(gp.setVisibility(SHOW_HEADER_ONLY).isHeaderVisible(DEBUG, Tag.any()));
    assertFalse(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).isHeaderVisible(DEBUG, Tag.any()));
    assertFalse(gp.setVisibility(FLATTEN).isHeaderVisible(DEBUG, Tag.any()));
    assertFalse(gp.setVisibility(HIDDEN).isHeaderVisible(DEBUG, Tag.any()));

    assertFalse(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).isHeaderVisible(INFO, Tag.any()));
    assertTrue(gp.setVisibility(SHOW_HEADER_ALWAYS).isHeaderVisible(INFO, Tag.any()));
    assertTrue(gp.setVisibility(SHOW_HEADER_ONLY).isHeaderVisible(INFO, Tag.any()));
    assertFalse(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).isHeaderVisible(INFO, Tag.any()));
    assertFalse(gp.setVisibility(FLATTEN).isHeaderVisible(INFO, Tag.any()));
    assertFalse(gp.setVisibility(HIDDEN).isHeaderVisible(INFO, Tag.any()));

    gp.info().message("Msg #2");

    assertTrue(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).isHeaderVisible(DEBUG, Tag.any()));
    assertTrue(gp.setVisibility(SHOW_HEADER_ALWAYS).isHeaderVisible(DEBUG, Tag.any()));
    assertTrue(gp.setVisibility(SHOW_HEADER_ONLY).isHeaderVisible(DEBUG, Tag.any()));
    assertTrue(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).isHeaderVisible(DEBUG, Tag.any()));
    assertFalse(gp.setVisibility(FLATTEN).isHeaderVisible(DEBUG, Tag.any()));
    assertFalse(gp.setVisibility(HIDDEN).isHeaderVisible(DEBUG, Tag.any()));

    gp.error().message("Msg #3");

    assertTrue(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).isHeaderVisible(LOWEST, Tag.any()));
    assertTrue(gp.setVisibility(SHOW_HEADER_ALWAYS).isHeaderVisible(LOWEST, Tag.any()));
    assertTrue(gp.setVisibility(SHOW_HEADER_ONLY).isHeaderVisible(LOWEST, Tag.any()));
    assertTrue(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).isHeaderVisible(LOWEST, Tag.any()));
    assertFalse(gp.setVisibility(FLATTEN).isHeaderVisible(LOWEST, Tag.any()));
    assertFalse(gp.setVisibility(HIDDEN).isHeaderVisible(LOWEST, Tag.any()));
  }


  @Test
  public void testHasVisualEntry()
  {
    val factory = StringProtocolFactory.createPlainTextFactory();
    val gp = factory.createProtocol().createGroup();

    assertEquals(0, gp.getVisibleEntryCount(true, Shared.LOWEST, Tag.any()));

    gp.setGroupMessage("Test").with("param1", "Huhu");

    assertEquals(1, gp.setVisibility(SHOW_HEADER_ONLY).getVisibleEntryCount(true, LOWEST, Tag.any()));
    assertEquals(1, gp.setVisibility(SHOW_HEADER_ALWAYS).getVisibleEntryCount(true, LOWEST, Tag.any()));
    assertEquals(0, gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).getVisibleEntryCount(true, LOWEST, Tag.any()));
    assertEquals(0, gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).getVisibleEntryCount(true, LOWEST, Tag.any()));
    assertEquals(0, gp.setVisibility(HIDDEN).getVisibleEntryCount(true, LOWEST, Tag.any()));
    assertEquals(0, gp.setVisibility(FLATTEN).getVisibleEntryCount(true, LOWEST, Tag.any()));

    gp.debug().message("msg #1");

    assertEquals(1, gp.setVisibility(SHOW_HEADER_ONLY).getVisibleEntryCount(true, LOWEST, Tag.any()));
    assertEquals(2, gp.setVisibility(SHOW_HEADER_ALWAYS).getVisibleEntryCount(true, LOWEST, Tag.any()));
    assertEquals(2, gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).getVisibleEntryCount(true, LOWEST, Tag.any()));
    assertEquals(1, gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).getVisibleEntryCount(true, LOWEST, Tag.any()));
    assertEquals(0, gp.setVisibility(HIDDEN).getVisibleEntryCount(true, LOWEST, Tag.any()));
    assertEquals(1, gp.setVisibility(FLATTEN).getVisibleEntryCount(true, LOWEST, Tag.any()));

    gp.debug().message("msg #2");

    assertEquals(1, gp.setVisibility(SHOW_HEADER_ONLY).getVisibleEntryCount(true, LOWEST, Tag.any()));
    assertEquals(3, gp.setVisibility(SHOW_HEADER_ALWAYS).getVisibleEntryCount(true, LOWEST, Tag.any()));
    assertEquals(3, gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).getVisibleEntryCount(true, LOWEST, Tag.any()));
    assertEquals(3, gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).getVisibleEntryCount(true, LOWEST, Tag.any()));
    assertEquals(0, gp.setVisibility(HIDDEN).getVisibleEntryCount(true, LOWEST, Tag.any()));
    assertEquals(2, gp.setVisibility(FLATTEN).getVisibleEntryCount(true, LOWEST, Tag.any()));
  }


  @Test
  public void testEffectiveVisibility()
  {
    val factory = StringProtocolFactory.createPlainTextFactory();
    val gp = factory.createProtocol().createGroup();

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


  @Test
  public void testForGroupWithName()
  {
    val factory = StringProtocolFactory.createPlainTextFactory();
    val protocol = factory.createProtocol();
    protocol.createGroup().setName("group-1");
    val gp2 = protocol.createGroup().setName("group-2");
    protocol.createGroup().setName("group-3");

    gp2.createGroup().setName("group-2-1");
    val gp2_2 = gp2.createGroup().setName("group-2-2");

    //noinspection OptionalGetWithoutIsPresent
    assertEquals(gp2_2, protocol.getGroupByName("group-2-2").get());
  }


  @Test
  public void testFindGroupByRegex()
  {
    val factory = StringProtocolFactory.createPlainTextFactory();
    val protocol = factory.createProtocol();
    protocol.createGroup().setName("group-1");
    val gp2 = protocol.createGroup().setName("group-2");
    protocol.createGroup().setName("group-3");

    gp2.createGroup().setName("group-2-1");
    val gp2_2 = gp2.createGroup().setName("group-2-2");

    val protocolGroups = new LinkedHashSet<ProtocolGroup<String>>();
    protocol.forEachGroupByRegex("group.*-2", protocolGroups::add);

    assertEquals(2, protocolGroups.size());
    assertTrue(protocolGroups.contains(gp2));
    assertTrue(protocolGroups.contains(gp2_2));
  }


  @Test(expected = NullPointerException.class)
  public void testSetVisibiityNull()
  {
    //noinspection ConstantConditions
    StringProtocolFactory.createPlainTextFactory().createProtocol().createGroup().setVisibility(null);
  }


  @Test(expected = NullPointerException.class)
  public void testLevelLimitNull()
  {
    //noinspection ConstantConditions
    StringProtocolFactory.createPlainTextFactory().createProtocol().createGroup().setLevelLimit(null);
  }


  @Test(expected = ProtocolException.class)
  public void testDuplicateGroupName()
  {
    val factory = StringProtocolFactory.createPlainTextFactory();
    val protocol = factory.createProtocol();
    protocol.createGroup().setName("group-1");
    val gp2 = protocol.createGroup().setName("group-2");
    protocol.createGroup().setName("group-3");

    gp2.createGroup().setName("group-2-1");
    gp2.createGroup().setName("group-3");
  }
}
