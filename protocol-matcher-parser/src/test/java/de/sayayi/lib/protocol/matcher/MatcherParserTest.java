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

import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.Protocol;
import de.sayayi.lib.protocol.ProtocolEntry.Message;
import de.sayayi.lib.protocol.ProtocolFactory;
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.exception.MatcherParserException;
import org.junit.jupiter.api.Test;

import lombok.val;
import lombok.var;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static de.sayayi.lib.protocol.Level.Shared.DEBUG;
import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;
import static de.sayayi.lib.protocol.Level.Shared.INFO;
import static de.sayayi.lib.protocol.Level.Shared.LOWEST;
import static de.sayayi.lib.protocol.Level.Shared.WARN;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.any;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.none;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
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
  private static final Level TRACE = () -> 50;


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


  @Test
  @SuppressWarnings("unchecked")
  void testMessageAtom()
  {
    assertSame(none(), PARSER.parse("message('')"));

    val matcher = PARSER.parse("message('MSG-001')");

    assertFalse(matcher.isTagSelector());

    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);

    when(message.getMessageId()).thenReturn("MSG-0010");
    assertFalse(matcher.matches(HIGHEST, message));

    when(message.getMessageId()).thenReturn("MSG-001");
    assertTrue(matcher.matches(HIGHEST, message));
  }


  @Test
  @SuppressWarnings({ "unchecked", "ResultOfMethodCallIgnored" })
  void testLevelAtom()
  {
    val parser = new MatcherParser(null, l -> "trace".equalsIgnoreCase(l) ? TRACE : null);

    var matcher = parser.parse("level(TrAcE)");

    assertFalse(matcher.isTagSelector());

    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);

    when(message.getLevel()).thenReturn(Level.Shared.DEBUG);
    assertTrue(matcher.matches(HIGHEST, message));
    assertTrue(matcher.matches(TRACE, message));
    assertFalse(matcher.matches(LOWEST, message));

    matcher = parser.parse("level(info)");

    when(message.getLevel()).thenReturn(Level.Shared.INFO);
    assertTrue(matcher.matches(HIGHEST, message));
    assertFalse(matcher.matches(TRACE, message));

    matcher = parser.parse("level(WARN)");

    when(message.getLevel()).thenReturn(Level.Shared.ERROR);
    assertTrue(matcher.matches(HIGHEST, message));
    assertTrue(matcher.matches(WARN, message));
    assertFalse(matcher.matches(INFO, message));

    matcher = parser.parse("error");

    when(message.getLevel()).thenReturn(Level.Shared.ERROR);
    assertTrue(matcher.matches(HIGHEST, message));
    assertFalse(matcher.matches(WARN, message));

    assertThrowsExactly(MatcherParserException.class,
        () -> parser.parse("level(tracer)"));
  }


  @Test
  @SuppressWarnings({ "unchecked", "ResultOfMethodCallIgnored" })
  void testThrowableAtom()
  {
    var matcher = PARSER.parse("throwable");

    assertFalse(matcher.isTagSelector());

    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);

    // no throwable
    when(message.getThrowable()).thenReturn(null);
    assertFalse(matcher.matches(HIGHEST, message));

    // NPE
    when(message.getThrowable()).thenReturn(new NullPointerException());
    assertTrue(matcher.matches(HIGHEST, message));

    matcher = PARSER.parse("throwable(java.lang.Exception)");

    when(message.getThrowable()).thenReturn(new NullPointerException());
    assertTrue(matcher.matches(HIGHEST, message));

    when(message.getThrowable()).thenReturn(new OutOfMemoryError());
    assertFalse(matcher.matches(HIGHEST, message));

    assertThrowsExactly(MatcherParserException.class,
        () -> PARSER.parse("throwable(java.lang.String)"));

    assertThrowsExactly(MatcherParserException.class,
        () -> PARSER.parse("throwable(aa.bb.cc.dd.ee.Class)"));
  }


  @Test
  @SuppressWarnings("unchecked")
  void testHasParamAtom()
  {
    val message = (Message<Object>)mock(Message.class);
    when(message.getParameterValues()).thenReturn(unmodifiableMap(new HashMap<String,Object>() {{
      put("visible", true);
      put("msg-null", null);
      put("msg", "message");
    }}));

    assertTrue(PARSER.parse("has-param('msg-null')").matches(HIGHEST, message));
    assertTrue(PARSER.parse("has-param('msg')").matches(HIGHEST, message));
    assertFalse(PARSER.parse("has-param('text')").matches(HIGHEST, message));
    assertSame(none(), PARSER.parse("has-param('')"));
  }


  @Test
  @SuppressWarnings("unchecked")
  void testHasParamValueAtom()
  {
    val message = (Message<Object>)mock(Message.class);
    when(message.getParameterValues()).thenReturn(unmodifiableMap(new HashMap<String,Object>() {{
      put("visible", true);
      put("msg-null", null);
      put("msg", "message");
    }}));

    assertFalse(PARSER.parse("has-param-value('msg-null')").matches(HIGHEST, message));
    assertTrue(PARSER.parse("has-param-value('msg')").matches(HIGHEST, message));
    assertFalse(PARSER.parse("has-param-value('text')").matches(HIGHEST, message));
    assertSame(none(), PARSER.parse("has-param-value('')"));
  }


  @Test
  @SuppressWarnings("unchecked")
  void testCompoundAnd()
  {
    var matcher = PARSER.parse("throwable and error and message('ID')");

    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);

    // throwable, error, message id
    when(message.getThrowable()).thenReturn(new NullPointerException());
    when(message.getLevel()).thenReturn(Level.Shared.ERROR);
    when(message.getMessageId()).thenReturn("ID");
    assertTrue(matcher.matches(HIGHEST, message));
    assertFalse(matcher.matches(DEBUG, message));

    matcher = PARSER.parse("and(throwable, message('ID'))");
    assertTrue(matcher.matches(HIGHEST, message));

    when(message.getMessageId()).thenReturn("??");
    assertFalse(matcher.matches(HIGHEST, message));
  }


  @Test
  @SuppressWarnings("unchecked")
  void testCompoundOr()
  {
    var matcher = PARSER.parse("throwable or error or message('ID')");

    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);

    // throwable, error, message id
    when(message.getThrowable()).thenReturn(new NullPointerException());
    when(message.getLevel()).thenReturn(Level.Shared.ERROR);
    when(message.getMessageId()).thenReturn("ID");
    assertTrue(matcher.matches(HIGHEST, message));

    when(message.getThrowable()).thenReturn(null);
    assertTrue(matcher.matches(HIGHEST, message));
    assertTrue(matcher.matches(DEBUG, message));

    matcher = PARSER.parse("or(error, message('ID'))");
    assertTrue(matcher.matches(HIGHEST, message));
    assertTrue(matcher.matches(DEBUG, message));

    when(message.getMessageId()).thenReturn("??");
    assertTrue(matcher.matches(HIGHEST, message));
    assertFalse(matcher.matches(DEBUG, message));
  }


  @Unmodifiable
  private static @NotNull Set<String> asTagNameSet(@NotNull String ... s)
  {
    val set = new HashSet<>(asList(s));
    set.add(ProtocolFactory.DEFAULT_TAG_NAME);

    return unmodifiableSet(set);
  }
}