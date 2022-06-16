/*
 * Copyright 2020 Jeroen Gremmen
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
package de.sayayi.lib.protocol.spi;

import org.junit.jupiter.api.Test;

import lombok.val;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Jeroen Gremmen
 */
public class ParameterMapTest
{
  @Test
  public void testPutNoParent()
  {
    val map = new ParameterMap();

    map.put("c", "c");
    map.put("b", "b");
    map.put("e", "e");
    map.put("a", "a");

    assertEquals(4, map.size());
  }


  @Test
  public void testGetNoParent()
  {
    val map = new ParameterMap();

    map.put("c", "c1");
    map.put("e", "e1");
    map.put("a", "a1");

    assertEquals("a1", map.get("a"));
    assertEquals("c1", map.get("c"));
    assertEquals("e1", map.get("e"));
  }


  @Test
  public void testReplaceNoParent()
  {
    val map = new ParameterMap();

    map.put("c", "c1");
    map.put("e", "e1");
    map.put("a", "a1");

    map.put("e", "e");

    assertEquals(3, map.size());
    assertEquals("e", map.get("e"));
  }


  @Test
  public void testHasNoParent()
  {
    val map = new ParameterMap();

    map.put("c", "c");
    map.put("e", "e");
    map.put("a", "a");

    assertTrue(map.has("c"));
    assertTrue(map.has("e"));
    assertTrue(map.has("a"));

    assertFalse(map.has("d"));
  }


  @Test
  public void testPutWithParent()
  {
    val map0 = new ParameterMap();

    val map1 = new ParameterMap(map0);

    map1.put("a", "a1");
    map1.put("d", "d1");
    map1.put("g", "g1");

    val map2 = new ParameterMap(map1);

    map2.put("b", "b2");
    map2.put("d", "d2");
    map2.put("f", "f2");
    map2.put("h", "h2");

    val iterator = map2.iterator();

    assertEquals("a1", iterator.next().getValue());
    assertEquals("b2", iterator.next().getValue());
    assertEquals("d2", iterator.next().getValue());
    assertEquals("f2", iterator.next().getValue());
    assertEquals("g1", iterator.next().getValue());
    assertEquals("h2", iterator.next().getValue());

    assertTrue(map0.isEmpty());
    assertFalse(map1.isEmpty());
    assertFalse(map2.isEmpty());
  }


  @Test
  public void testUnmodifyableMap()
  {
    val map1 = new ParameterMap();

    map1.put("a", "a1");
    map1.put("d", "d1");
    map1.put("g", "g1");

    val map2 = new ParameterMap(map1);

    map2.put("b", "b2");
    map2.put("d", "d2");
    map2.put("f", "f2");
    map2.put("h", "h2");

    val um = map2.unmodifyableMap();

    assertFalse(um.isEmpty());
    assertEquals(6, um.size());
    assertEquals("a1", um.get("a"));
    assertEquals("d2", um.get("d"));
    assertTrue(um.containsKey("h"));
    assertTrue(um.containsValue("f2"));
    assertEquals(new HashSet<>(Arrays.asList("a", "b", "d", "f", "g", "h")), um.keySet());
    assertArrayEquals(new Object[] { "a", "b", "d", "f", "g", "h" }, um.keySet().toArray());
    assertArrayEquals(new String[] { "a", "b", "d", "f", "g", "h" }, um.keySet().toArray(new String[0]));
  }
}