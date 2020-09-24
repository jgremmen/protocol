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
package de.sayayi.lib.protocol.selector;

import de.sayayi.lib.protocol.TagSelector;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * @author Jeroen Gremmen
 */
public class TagSelectorParserTest
{
  @Test
  public void testTag()
  {
    final TagSelector selector = new TagSelectorParser("system").parseSelector();

    assertTrue(selector.match(Arrays.asList("system", "default")));
    assertFalse(selector.match(Collections.singletonList("default")));
  }


  @Test
  public void testAny()
  {
    final TagSelector selector = new TagSelectorParser("any( ) ").parseSelector();

    assertTrue(selector.match(Arrays.asList("system", "default")));
    assertTrue(selector.match(Collections.singletonList("default")));
    assertFalse(selector.match(Collections.<String>emptyList()));
  }


  @Test
  public void testAnyOf()
  {
    final TagSelector selector = new TagSelectorParser("anyOf(system, test, hello)").parseSelector();

    assertTrue(selector.match(Arrays.asList("system", "default")));
    assertTrue(selector.match(Arrays.asList("default", "hello")));
    assertTrue(selector.match(Collections.singletonList("test")));
    assertFalse(selector.match(Collections.singletonList("default")));
    assertFalse(selector.match(Collections.<String>emptyList()));
  }


  @Test
  public void testOr()
  {
    final TagSelector selector = new TagSelectorParser("or(system, test, hello)").parseSelector();

    assertTrue(selector.match(Arrays.asList("system", "default")));
    assertTrue(selector.match(Arrays.asList("default", "hello")));
    assertTrue(selector.match(Collections.singletonList("test")));
    assertFalse(selector.match(Collections.singletonList("default")));
    assertFalse(selector.match(Collections.<String>emptyList()));
  }


  @Test
  public void testNoneOf()
  {
    final TagSelector selector = new TagSelectorParser("noneOf(system, test, hello)").parseSelector();

    assertFalse(selector.match(Arrays.asList("system", "default")));
    assertFalse(selector.match(Arrays.asList("default", "hello")));
    assertFalse(selector.match(Collections.singletonList("test")));
    assertTrue(selector.match(Collections.singletonList("default")));
    assertTrue(selector.match(Collections.<String>emptyList()));
  }


  @Test(expected = TagSelectorParserException.class)
  public void testFailAdditional() {
    new TagSelectorParser("any( ) test").parseSelector();
  }


  @Test(expected = TagSelectorParserException.class)
  public void testFailNoSelection() {
    new TagSelectorParser(" ").parseSelector();
  }


  @Test(expected = TagSelectorParserException.class)
  public void testFailIncomplete() {
    new TagSelectorParser("allOf(").parseSelector();
  }


  @Test(expected = TagSelectorParserException.class)
  public void testFailWrongToken() {
    new TagSelectorParser("allOf)").parseSelector();
  }
}
