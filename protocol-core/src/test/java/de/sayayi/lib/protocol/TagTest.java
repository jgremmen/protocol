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
package de.sayayi.lib.protocol;


/**
 * @author Jeroen Gremmen
 */
public class TagTest
{
/*
  @Test
  public void testOf()
  {
    val selector = Tag.of("system");

    assertTrue(selector.match(singletonList("system")));
    assertTrue(selector.match(asList("default", "system", "test")));
    assertFalse(selector.match(singletonList("default")));
    assertFalse(selector.match(emptyList()));
  }


  @Test
  public void testAllOf()
  {
    val selector = Tag.allOf("system", "default", "system");

    assertFalse(selector.match(singletonList("system")));
    assertTrue(selector.match(asList("default", "system", "test")));
    assertFalse(selector.match(singletonList("default")));
  }


  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  public void testAllOfFail() {
    assertThrows(ProtocolException.class, Tag::allOf);
  }


  @Test
  public void testAny()
  {
    val selector = Tag.any();

    assertTrue(selector.match(singletonList("system")));
    assertTrue(selector.match(asList("default", "system", "test")));
    assertFalse(selector.match(emptyList()));
  }


  @Test
  public void testAnyOf1()
  {
    val selector = Tag.anyOf("system");

    assertTrue(selector.match(singletonList("system")));
    assertTrue(selector.match(asList("system", "test")));
    assertFalse(selector.match(singletonList("default")));
  }


  @Test
  public void testAnyOf2()
  {
    val selector = Tag.anyOf("test", "default");

    assertFalse(selector.match(singletonList("system")));
    assertTrue(selector.match(asList("system", "test")));
    assertTrue(selector.match(singletonList("default")));
  }


  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  public void testAnyOfFail() {
    assertThrows(IllegalArgumentException.class, Tag::anyOf);
  }


  @Test
  public void testNot1()
  {
    val selector = Tag.not("system");

    assertFalse(selector.match(singletonList("system")));
    assertFalse(selector.match(asList("default", "system", "test")));
    assertTrue(selector.match(singletonList("default")));
    assertTrue(selector.match(emptyList()));
  }


  @Test
  public void testNoneOf()
  {
    val selector = Tag.noneOf("system", "test");

    assertFalse(selector.match(singletonList("system")));
    assertFalse(selector.match(asList("default", "system", "test")));
    assertTrue(selector.match(singletonList("default")));
    assertTrue(selector.match(emptyList()));
  }


  @Test
  public void testOrFlatten()
  {
    val selector = Tag.of("system").or("default")
        .or(Tag.anyOf("console", "system"));

    assertTrue(selector.match(singletonList("system")));
    assertTrue(selector.match(asList("default", "system", "test")));
    assertTrue(selector.match(singletonList("console")));
    assertFalse(selector.match(emptyList()));
  }


  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  public void testOrFail() {
    assertThrows(ProtocolException.class, Tag::or);
  }


  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  public void testAndFail() {
    assertThrows(ProtocolException.class, Tag::and);
  }


  @Test
  public void testComplex1()
  {
    val selector = Tag.of("system").and("test").or("console");

    assertFalse(selector.match(singletonList("system")));
    assertTrue(selector.match(asList("default", "system", "test")));
    assertTrue(selector.match(singletonList("console")));
    assertFalse(selector.match(emptyList()));
  }


  @Test
  public void testComplex2()
  {
    val selector = Tag.of("system").and(Tag.not("test"));

    assertTrue(selector.match(singletonList("system")));
    assertFalse(selector.match(asList("default", "system", "test")));
    assertFalse(selector.match(singletonList("console")));
    assertFalse(selector.match(emptyList()));
  }


  @Test
  public void testParse()
  {
    val selector = Tag.parse("and(system,noneOf(info,test,warning),or(fatal,not(debug)))");

    assertTrue(selector.match(singletonList("system")));
    assertTrue(selector.match(asList("system", "fatal")));
    assertFalse(selector.match(asList("system", "debug")));
    assertFalse(selector.match(asList("system", "info")));
    assertFalse(selector.match(asList("system", "test")));
    assertFalse(selector.match(asList("system", "warning")));
    assertFalse(selector.match(asList("system", "warning", "fatal", "info")));
  }
*/
}