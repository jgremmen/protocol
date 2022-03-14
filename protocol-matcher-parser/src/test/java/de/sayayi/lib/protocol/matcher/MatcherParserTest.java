/*
 * Copyright 2022 Jeroen Gremmen
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
package de.sayayi.lib.protocol.matcher;

import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolEntry.Message;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolGroup;
import org.junit.jupiter.api.Test;

import lombok.val;
import lombok.var;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashSet;
import java.util.Set;

import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.any;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.none;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author Jeroen Gremmen
 * @since 1.2.0
 */
class MatcherParserTest
{
  private static final MatcherParser PARSER = MatcherParser.INSTANCE;


  @Test
  @SuppressWarnings("unchecked")
  void testTagAtom()
  {
    // tag(default) = any()
    var matcher = PARSER.parse("tag('default')");

    assertSame(any(), matcher);
    assertTrue(matcher.isTagSelector());

    // tag(system)
    matcher = PARSER.parse("tag(system)");

    assertTrue(matcher.isTagSelector());

    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);
    when(message.getTagNames()).thenReturn(asTagNameSet("system", "ticket"));
    assertTrue(matcher.matches(HIGHEST, message));

    // tag('') = none()
    matcher = PARSER.parse("tag('')");

    assertSame(none(), matcher);
    assertTrue(matcher.isTagSelector());
  }


  @Test
  @SuppressWarnings("unchecked")
  void testAnyOfAtom()
  {
    // any-of(default,default) = any()
    var matcher = PARSER.parse("any-of('default', default)");

    assertSame(any(), matcher);
    assertTrue(matcher.isTagSelector());

    // any-of(system,ticket)
    matcher = PARSER.parse("any-of ( system, ticket ) ");

    assertTrue(matcher.isTagSelector());

    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);
    when(message.getTagNames()).thenReturn(asTagNameSet("something"));
    assertFalse(matcher.matches(HIGHEST, message));

    when(message.getTagNames()).thenReturn(asTagNameSet("ticket"));
    assertTrue(matcher.matches(HIGHEST, message));

    // any-of('','') = none()
    matcher = PARSER.parse("any-of('','')");

    assertSame(none(), matcher);
    assertTrue(matcher.isTagSelector());
  }


  @Test
  @SuppressWarnings("unchecked")
  void testAllOfAtom()
  {
    // all-of(default,default) = any()
    var matcher = PARSER.parse("all-of('default', default)");

    assertSame(any(), matcher);
    assertTrue(matcher.isTagSelector());

    // all-of(system,ticket)
    matcher = PARSER.parse("all-of ( system, ticket ) ");

    assertTrue(matcher.isTagSelector());

    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);
    when(message.getTagNames()).thenReturn(asTagNameSet("something", "ticket", "system"));
    assertTrue(matcher.matches(HIGHEST, message));

    when(message.getTagNames()).thenReturn(asTagNameSet("ticket"));
    assertFalse(matcher.matches(HIGHEST, message));

    // all-of('','') = none()
    matcher = PARSER.parse("all-of('','')");

    assertSame(none(), matcher);
    assertTrue(matcher.isTagSelector());
  }


  @Test
  @SuppressWarnings("unchecked")
  void testNoneOfAtom()
  {
    // none-of(default,default) = none()
    var matcher = PARSER.parse("none-of('default', default)");

    assertSame(none(), matcher);
    assertTrue(matcher.isTagSelector());

    // none-of(system,ticket)
    matcher = PARSER.parse("none-of ( system, ticket ) ");

    assertTrue(matcher.isTagSelector());

    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);
    when(message.getTagNames()).thenReturn(asTagNameSet("something", "different"));
    assertTrue(matcher.matches(HIGHEST, message));

    when(message.getTagNames()).thenReturn(asTagNameSet("ticket"));
    assertFalse(matcher.matches(HIGHEST, message));

    // none-of('','') = any()
    matcher = PARSER.parse("none-of('','')");

    assertSame(any(), matcher);
    assertTrue(matcher.isTagSelector());
  }


  @Test
  @SuppressWarnings("unchecked")
  void testInRootAtom()
  {
    var matcher = PARSER.parse("in-root");

    assertFalse(matcher.isTagSelector());

    val protocol = mock(Protocol.class);
    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);
    when(message.getProtocol()).thenReturn(protocol);

    // no parent (= in root)
    when(protocol.getParent()).thenReturn(null);
    assertTrue(matcher.matches(HIGHEST, message));

    // with parent (= not in root)
    when(protocol.getParent()).thenReturn(mock(Protocol.class));
    assertFalse(matcher.matches(HIGHEST, message));
  }


  @Test
  @SuppressWarnings("unchecked")
  void testInGroupAtom()
  {
    val matcher = PARSER.parse("in-group");

    assertFalse(matcher.isTagSelector());

    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);

    // no group
    when(message.getProtocol()).thenReturn(mock(Protocol.class, CALLS_REAL_METHODS));
    assertFalse(matcher.matches(HIGHEST, message));

    // group
    when(message.getProtocol()).thenReturn(mock(ProtocolGroup.class, CALLS_REAL_METHODS));
    assertTrue(matcher.matches(HIGHEST, message));
  }


  @Test
  @SuppressWarnings("unchecked")
  void testInGroupNameAtom()
  {
    val matcher = PARSER.parse("in-group('mygroup')");

    assertFalse(matcher.isTagSelector());

    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);

    // no group
    when(message.getProtocol()).thenReturn(mock(Protocol.class, CALLS_REAL_METHODS));
    assertFalse(matcher.matches(HIGHEST, message));

    // group
    val protocolGroup = mock(ProtocolGroup.class, CALLS_REAL_METHODS);
    when(message.getProtocol()).thenReturn(protocolGroup);

    when(protocolGroup.getName()).thenReturn("mygroup");
    assertTrue(matcher.matches(HIGHEST, message));

    when(protocolGroup.getName()).thenReturn("test");
    assertFalse(matcher.matches(HIGHEST, message));
  }


  @Test
  @SuppressWarnings("unchecked")
  void testInGroupRegexAtom()
  {
    val matcher = PARSER.parse("in-group-regex('my\\x2e*')");

    assertFalse(matcher.isTagSelector());

    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);

    // no group
    when(message.getProtocol()).thenReturn(mock(Protocol.class, CALLS_REAL_METHODS));
    assertFalse(matcher.matches(HIGHEST, message));

    val protocolGroup = mock(ProtocolGroup.class, CALLS_REAL_METHODS);
    when(message.getProtocol()).thenReturn(protocolGroup);

    // group 'mygroup' = success
    when(protocolGroup.getName()).thenReturn("mygroup");
    assertTrue(matcher.matches(HIGHEST, message));

    // group 'test' = fail
    when(protocolGroup.getName()).thenReturn("test");
    assertFalse(matcher.matches(HIGHEST, message));
  }


  @Unmodifiable
  private static @NotNull Set<String> asTagNameSet(@NotNull String ... s)
  {
    val set = new HashSet<>(asList(s));
    set.add(ProtocolFactory.DEFAULT_TAG_NAME);

    return unmodifiableSet(set);
  }
}