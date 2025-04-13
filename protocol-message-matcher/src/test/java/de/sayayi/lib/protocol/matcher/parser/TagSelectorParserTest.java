/*
 * Copyright 2022 Jeroen Gremmen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.sayayi.lib.protocol.matcher.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Jeroen Gremmen
 * @since 1.2.0
 */
@DisplayName("Tag selector")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class TagSelectorParserTest extends AbstractMatcherParserTest
{
  private static final MessageMatcherParser PARSER = MessageMatcherParser.INSTANCE;


  @Test
  @DisplayName("any / none")
  void testBooleanAtom()
  {
    assertTrue(PARSER
        .parseTagSelector("any")
        .match(asTagNameSet("test")));
    assertFalse(PARSER
        .parseTagSelector("none")
        .match(asTagNameSet("test")));
  }


  @Test
  @DisplayName("tag(...)")
  void testTagAtom()
  {
    assertTrue(PARSER
        .parseTagSelector("default")
        .match(asTagNameSet("test")));

    assertTrue(PARSER
        .parseTagSelector("tag(system)")
        .match(asTagNameSet("system", "ticket")));

    assertFalse(PARSER
        .parseTagSelector("tag('')")
        .match(asTagNameSet("ticket")));
  }


  @Test
  @DisplayName("any-of(...)")
  void testAnyOfAtom()
  {
    assertTrue(PARSER
        .parseTagSelector("any-of('default', default)")
        .match(asTagNameSet("mytag")));

    assertTrue(PARSER
        .parseTagSelector("any-of ( system, ticket ) ")
        .match(asTagNameSet("ticket")));

    assertFalse(PARSER
        .parseTagSelector("any-of('','')")
        .match(asTagNameSet("ticket")));
  }


  @Test
  @DisplayName("all-of(...)")
  void testAllOfAtom()
  {
    assertTrue(PARSER
        .parseTagSelector("all-of('default', default)")
        .match(asTagNameSet("mytag")));

    assertTrue(PARSER
        .parseTagSelector("all-of ( system, ticket ) ")
        .match(asTagNameSet("mytag", "ticket", "system", "develop")));

    assertFalse(PARSER
        .parseTagSelector("all-of ( system, ticket2 ) ")
        .match(asTagNameSet("mytag", "ticket", "system", "develop")));

    assertFalse(PARSER
        .parseTagSelector("all-of('','')")
        .match(asTagNameSet("ticket")));
  }


  @Test
  @DisplayName("none-of(...)")
  void testNoneOfAtom()
  {
    assertFalse(PARSER
        .parseTagSelector("none-of('default', default)")
        .match(asTagNameSet("mytag")));

    assertTrue(PARSER
        .parseTagSelector("none-of ( system, ticket ) ")
        .match(asTagNameSet("mytag")));

    assertFalse(PARSER
        .parseTagSelector("none-of ( 'syst\\u0065m', ticket ) ")
        .match(asTagNameSet("mytag", "system")));

    assertTrue(PARSER
        .parseTagSelector("none-of('','')")
        .match(asTagNameSet("ticket")));
  }


  @Test
  @DisplayName("... and ...")
  void testCompoundAnd()
  {
    var matcher = PARSER.parseTagSelector("system and not(ticket)");

    assertTrue(matcher.match(asTagNameSet("mytag", "system")));
    assertFalse(matcher.match(asTagNameSet("ticket", "system")));
    assertFalse(matcher.match(asTagNameSet("ticket")));
  }


  @Test
  @DisplayName("... or ...")
  void testCompoundOr()
  {
    var matcher = PARSER.parseTagSelector("system or not(test-ticket)");

    assertTrue(matcher.match(asTagNameSet("mytag", "system")));
    assertFalse(matcher.match(asTagNameSet("test-ticket")));
    assertTrue(matcher.match(asTagNameSet("mytag")));
  }
}