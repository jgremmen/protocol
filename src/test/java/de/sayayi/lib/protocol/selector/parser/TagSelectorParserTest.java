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
package de.sayayi.lib.protocol.selector.parser;

import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.exception.TagSelectorParserException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


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
    assertFalse(selector.match(singletonList("default")));
  }


  @Test
  public void testAny()
  {
    final TagSelector selector = new TagSelectorParser("any( ) ").parseSelector();

    assertTrue(selector.match(Arrays.asList("system", "default")));
    assertTrue(selector.match(singletonList("default")));
    assertFalse(selector.match(emptyList()));
  }


  @Test
  public void testAnyOf()
  {
    final TagSelector selector = new TagSelectorParser("anyOf(system, test, hello)").parseSelector();

    assertTrue(selector.match(Arrays.asList("system", "default")));
    assertTrue(selector.match(Arrays.asList("default", "hello")));
    assertTrue(selector.match(singletonList("test")));
    assertFalse(selector.match(singletonList("default")));
    assertFalse(selector.match(emptyList()));
  }


  @Test
  public void testOr()
  {
    final TagSelector selector = new TagSelectorParser("or(system, test, hello)").parseSelector();

    assertTrue(selector.match(Arrays.asList("system", "default")));
    assertTrue(selector.match(Arrays.asList("default", "hello")));
    assertTrue(selector.match(singletonList("test")));
    assertFalse(selector.match(singletonList("default")));
    assertFalse(selector.match(emptyList()));
  }


  @Test
  public void testNoneOf()
  {
    final TagSelector selector = new TagSelectorParser("noneOf(system, test, hello)").parseSelector();

    assertFalse(selector.match(Arrays.asList("system", "default")));
    assertFalse(selector.match(Arrays.asList("default", "hello")));
    assertFalse(selector.match(singletonList("test")));
    assertTrue(selector.match(singletonList("default")));
    assertTrue(selector.match(emptyList()));
  }


  @Test
  public void testFailAdditional()
  {
    assertThrows(TagSelectorParserException.class,
        () -> new TagSelectorParser("any( ) test").parseSelector());
  }


  @Test
  public void testFailNoSelection()
  {
    assertThrows(TagSelectorParserException.class,
        () -> new TagSelectorParser(" ").parseSelector());
  }


  @Test
  public void testFailIncomplete()
  {
    assertThrows(TagSelectorParserException.class,
        () -> new TagSelectorParser("allOf(").parseSelector());
  }


  @Test
  public void testFailWrongToken()
  {
    assertThrows(TagSelectorParserException.class,
        () -> new TagSelectorParser("allOf)").parseSelector());
  }
}
