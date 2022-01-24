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

import de.sayayi.lib.protocol.exception.ProtocolException;
import de.sayayi.lib.protocol.matcher.MessageMatcher;
import org.junit.jupiter.api.Test;

import lombok.val;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;

import static de.sayayi.lib.protocol.Level.Shared.LOWEST;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.FLATTEN;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.FLATTEN_ON_SINGLE_ENTRY;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.HIDDEN;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ALWAYS;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_IF_NOT_EMPTY;
import static de.sayayi.lib.protocol.ProtocolGroup.Visibility.SHOW_HEADER_ONLY;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.any;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.is;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.isDebug;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.isInfo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


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

    assertFalse(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).isHeaderVisible(is(LOWEST)));
    assertFalse(gp.setVisibility(SHOW_HEADER_ALWAYS).isHeaderVisible(is(LOWEST)));
    assertFalse(gp.setVisibility(SHOW_HEADER_ONLY).isHeaderVisible(is(LOWEST)));
    assertFalse(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).isHeaderVisible(is(LOWEST)));
    assertFalse(gp.setVisibility(FLATTEN).isHeaderVisible(is(LOWEST)));
    assertFalse(gp.setVisibility(HIDDEN).isHeaderVisible(is(LOWEST)));

    gp.setGroupMessage("Test");

    assertFalse(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).isHeaderVisible(is(LOWEST)));
    assertTrue(gp.setVisibility(SHOW_HEADER_ALWAYS).isHeaderVisible(is(LOWEST)));
    assertTrue(gp.setVisibility(SHOW_HEADER_ONLY).isHeaderVisible(is(LOWEST)));
    assertFalse(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).isHeaderVisible(is(LOWEST)));
    assertFalse(gp.setVisibility(FLATTEN).isHeaderVisible(is(LOWEST)));
    assertFalse(gp.setVisibility(HIDDEN).isHeaderVisible(is(LOWEST)));

    gp.debug().message("Msg #1");

    assertTrue(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).isHeaderVisible(isDebug()));
    assertTrue(gp.setVisibility(SHOW_HEADER_ALWAYS).isHeaderVisible(isDebug()));
    assertTrue(gp.setVisibility(SHOW_HEADER_ONLY).isHeaderVisible(isDebug()));
    assertFalse(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).isHeaderVisible(isDebug()));
    assertFalse(gp.setVisibility(FLATTEN).isHeaderVisible(isDebug()));
    assertFalse(gp.setVisibility(HIDDEN).isHeaderVisible(isDebug()));

    assertFalse(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).isHeaderVisible(isInfo()));
    assertTrue(gp.setVisibility(SHOW_HEADER_ALWAYS).isHeaderVisible(isInfo()));
    assertTrue(gp.setVisibility(SHOW_HEADER_ONLY).isHeaderVisible(isInfo()));
    assertFalse(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).isHeaderVisible(isInfo()));
    assertFalse(gp.setVisibility(FLATTEN).isHeaderVisible(isInfo()));
    assertFalse(gp.setVisibility(HIDDEN).isHeaderVisible(isInfo()));

    gp.info().message("Msg #2");

    assertTrue(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).isHeaderVisible(isDebug()));
    assertTrue(gp.setVisibility(SHOW_HEADER_ALWAYS).isHeaderVisible(isDebug()));
    assertTrue(gp.setVisibility(SHOW_HEADER_ONLY).isHeaderVisible(isDebug()));
    assertTrue(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).isHeaderVisible(isDebug()));
    assertFalse(gp.setVisibility(FLATTEN).isHeaderVisible(isDebug()));
    assertFalse(gp.setVisibility(HIDDEN).isHeaderVisible(isDebug()));

    gp.error().message("Msg #3");

    assertTrue(gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).isHeaderVisible(is(LOWEST)));
    assertTrue(gp.setVisibility(SHOW_HEADER_ALWAYS).isHeaderVisible(is(LOWEST)));
    assertTrue(gp.setVisibility(SHOW_HEADER_ONLY).isHeaderVisible(is(LOWEST)));
    assertTrue(gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).isHeaderVisible(is(LOWEST)));
    assertFalse(gp.setVisibility(FLATTEN).isHeaderVisible(is(LOWEST)));
    assertFalse(gp.setVisibility(HIDDEN).isHeaderVisible(is(LOWEST)));
  }


  @Test
  public void testHasVisualEntry()
  {
    val factory = StringProtocolFactory.createPlainTextFactory();
    val gp = factory.createProtocol().createGroup();

    assertEquals(0, gp.getVisibleEntryCount(true, is(LOWEST)));

    gp.setGroupMessage("Test").with("param1", "Huhu");

    assertEquals(1, gp.setVisibility(SHOW_HEADER_ONLY).getVisibleEntryCount(true, is(LOWEST)));
    assertEquals(1, gp.setVisibility(SHOW_HEADER_ALWAYS).getVisibleEntryCount(true, is(LOWEST)));
    assertEquals(0, gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).getVisibleEntryCount(true, is(LOWEST)));
    assertEquals(0, gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).getVisibleEntryCount(true, is(LOWEST)));
    assertEquals(0, gp.setVisibility(HIDDEN).getVisibleEntryCount(true, is(LOWEST)));
    assertEquals(0, gp.setVisibility(FLATTEN).getVisibleEntryCount(true, is(LOWEST)));

    gp.debug().message("msg #1");

    assertEquals(1, gp.setVisibility(SHOW_HEADER_ONLY).getVisibleEntryCount(true, is(LOWEST)));
    assertEquals(2, gp.setVisibility(SHOW_HEADER_ALWAYS).getVisibleEntryCount(true, is(LOWEST)));
    assertEquals(2, gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).getVisibleEntryCount(true, is(LOWEST)));
    assertEquals(1, gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).getVisibleEntryCount(true, is(LOWEST)));
    assertEquals(0, gp.setVisibility(HIDDEN).getVisibleEntryCount(true, is(LOWEST)));
    assertEquals(1, gp.setVisibility(FLATTEN).getVisibleEntryCount(true, is(LOWEST)));

    gp.debug().message("msg #2");

    assertEquals(1, gp.setVisibility(SHOW_HEADER_ONLY).getVisibleEntryCount(true, is(LOWEST)));
    assertEquals(3, gp.setVisibility(SHOW_HEADER_ALWAYS).getVisibleEntryCount(true, is(LOWEST)));
    assertEquals(3, gp.setVisibility(SHOW_HEADER_IF_NOT_EMPTY).getVisibleEntryCount(true, is(LOWEST)));
    assertEquals(3, gp.setVisibility(FLATTEN_ON_SINGLE_ENTRY).getVisibleEntryCount(true, is(LOWEST)));
    assertEquals(0, gp.setVisibility(HIDDEN).getVisibleEntryCount(true, is(LOWEST)));
    assertEquals(2, gp.setVisibility(FLATTEN).getVisibleEntryCount(true, is(LOWEST)));
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


  @Test
  public void testSetVisibiityNull()
  {
    assertThrows(NullPointerException.class, () -> {
      //noinspection ConstantConditions
      StringProtocolFactory.createPlainTextFactory().createProtocol().createGroup().setVisibility(null);
    });
  }


  @Test
  public void testLevelLimitNull()
  {
    assertThrows(NullPointerException.class, () -> {
      //noinspection ConstantConditions
      StringProtocolFactory.createPlainTextFactory().createProtocol().createGroup().setLevelLimit(null);
    });
  }


  @Test
  public void testDuplicateGroupName()
  {
    val factory = StringProtocolFactory.createPlainTextFactory();
    val protocol = factory.createProtocol();
    protocol.createGroup().setName("group-1");
    val gp2 = protocol.createGroup().setName("group-2");
    protocol.createGroup().setName("group-3");

    assertThrows(ProtocolException.class, () -> {
      gp2.createGroup().setName("group-2-1");
      gp2.createGroup().setName("group-3");
    });
  }


  @Test
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void testProtocolGroupFormatter()
  {
    val factory = StringProtocolFactory.createPlainTextFactory();
    val protocol = factory.createProtocol();
    val groupProtocol = protocol.createGroup()
        .setVisibility(SHOW_HEADER_ALWAYS)
        .setGroupMessage("Group message");

    groupProtocol.warn().message("Warning");

    protocol.format(new ProtocolFormatter<String,Object>()
    {
      @Override
      public void init(@NotNull ProtocolFactory<String> factory, @NotNull MessageMatcher matcher, int estimatedGroupDepth) {
        assertEquals(1, estimatedGroupDepth);
      }

      @Override public void message(ProtocolIterator.@NotNull MessageEntry<String> message) {}
      @Override public Object getResult() { return null; }
    }, any());

    groupProtocol.format(new ProtocolFormatter<String,Object>()
    {
      @Override
      public void init(@NotNull ProtocolFactory<String> factory, @NotNull MessageMatcher matcher, int estimatedGroupDepth) {
        assertEquals(1, estimatedGroupDepth);
      }

      @Override public void message(ProtocolIterator.@NotNull MessageEntry<String> message) {}
      @Override public Object getResult() { return null; }
    }, any());

    protocol.toStringTree();
    groupProtocol.toStringTree();
  }
}