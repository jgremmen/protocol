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
import de.sayayi.lib.protocol.ProtocolGroup;
import de.sayayi.lib.protocol.exception.MessageMatcherParserException;
import org.junit.jupiter.api.Test;

import lombok.val;
import lombok.var;

import java.util.HashMap;

import static de.sayayi.lib.protocol.Level.Shared.DEBUG;
import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;
import static de.sayayi.lib.protocol.Level.Shared.INFO;
import static de.sayayi.lib.protocol.Level.Shared.LOWEST;
import static de.sayayi.lib.protocol.Level.Shared.WARN;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.any;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.none;
import static java.util.Collections.unmodifiableMap;
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
class MessageMatcherParserTest extends AbstractMatcherParserTest
{
  private static final MessageMatcherParser PARSER = MessageMatcherParser.INSTANCE;
  private static final Level TRACE = () -> 50;


  @Test
  @SuppressWarnings("unchecked")
  void testBooleanAtom()
  {
    var matcher = PARSER.parseMessageMatcher("any");

    assertSame(any(), matcher);
    assertTrue(matcher.isTagSelector());
    assertTrue(matcher.matches(HIGHEST, mock(Message.class)));

    matcher = PARSER.parseMessageMatcher("none");

    assertSame(none(), matcher);
    assertTrue(matcher.isTagSelector());
    assertFalse(matcher.matches(HIGHEST, mock(Message.class)));
  }


  @Test
  @SuppressWarnings("unchecked")
  void testTagAtom()
  {
    // tag(default) = any()
    var matcher = PARSER.parseMessageMatcher("tag('default')");

    assertSame(any(), matcher);
    assertTrue(matcher.isTagSelector());

    // tag(system)
    matcher = PARSER.parseMessageMatcher("tag(system)");

    assertTrue(matcher.isTagSelector());

    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);
    when(message.getTagNames()).thenReturn(asTagNameSet("system", "ticket"));
    assertTrue(matcher.matches(HIGHEST, message));

    // tag('') = none()
    matcher = PARSER.parseMessageMatcher("tag('')");

    assertSame(none(), matcher);
    assertTrue(matcher.isTagSelector());
  }


  @Test
  @SuppressWarnings("unchecked")
  void testAnyOfAtom()
  {
    // any-of(default,default) = any()
    var matcher = PARSER.parseMessageMatcher("any-of('default', default)");

    assertSame(any(), matcher);
    assertTrue(matcher.isTagSelector());

    // any-of(system,ticket)
    matcher = PARSER.parseMessageMatcher("any-of ( system, ticket ) ");

    assertTrue(matcher.isTagSelector());

    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);
    when(message.getTagNames()).thenReturn(asTagNameSet("something"));
    assertFalse(matcher.matches(HIGHEST, message));

    when(message.getTagNames()).thenReturn(asTagNameSet("ticket"));
    assertTrue(matcher.matches(HIGHEST, message));

    // any-of('','') = none()
    matcher = PARSER.parseMessageMatcher("any-of('','')");

    assertSame(none(), matcher);
    assertTrue(matcher.isTagSelector());
  }


  @Test
  @SuppressWarnings("unchecked")
  void testAllOfAtom()
  {
    // all-of(default,default) = any()
    var matcher = PARSER.parseMessageMatcher("all-of('default', default)");

    assertSame(any(), matcher);
    assertTrue(matcher.isTagSelector());

    // all-of(system,ticket)
    matcher = PARSER.parseMessageMatcher("all-of ( system, ticket ) ");

    assertTrue(matcher.isTagSelector());

    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);
    when(message.getTagNames()).thenReturn(asTagNameSet("something", "ticket", "system"));
    assertTrue(matcher.matches(HIGHEST, message));

    when(message.getTagNames()).thenReturn(asTagNameSet("ticket"));
    assertFalse(matcher.matches(HIGHEST, message));

    // all-of('','') = none()
    matcher = PARSER.parseMessageMatcher("all-of('','')");

    assertSame(none(), matcher);
    assertTrue(matcher.isTagSelector());
  }


  @Test
  @SuppressWarnings("unchecked")
  void testNoneOfAtom()
  {
    // none-of(default,default) = none()
    var matcher = PARSER.parseMessageMatcher("none-of('default', default)");

    assertSame(none(), matcher);
    assertTrue(matcher.isTagSelector());

    // none-of(system,ticket)
    matcher = PARSER.parseMessageMatcher("none-of ( system, ticket ) ");

    assertTrue(matcher.isTagSelector());

    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);
    when(message.getTagNames()).thenReturn(asTagNameSet("something", "different"));
    assertTrue(matcher.matches(HIGHEST, message));

    when(message.getTagNames()).thenReturn(asTagNameSet("ticket"));
    assertFalse(matcher.matches(HIGHEST, message));

    // none-of('','') = any()
    matcher = PARSER.parseMessageMatcher("none-of('','')");

    assertSame(any(), matcher);
    assertTrue(matcher.isTagSelector());
  }


  @Test
  @SuppressWarnings("unchecked")
  void testInRootAtom()
  {
    var matcher = PARSER.parseMessageMatcher("in-root");

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
    val matcher = PARSER.parseMessageMatcher("in-group");

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
    val matcher = PARSER.parseMessageMatcher("in-group('mygroup')");

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
    val matcher = PARSER.parseMessageMatcher("in-group-regex('my\\x2e*')");

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
    assertSame(none(), PARSER.parseMessageMatcher("message('')"));

    val matcher = PARSER.parseMessageMatcher("message('MSG-001')");

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
    val parser = new MessageMatcherParser(null, l -> "trace".equalsIgnoreCase(l) ? TRACE : null);

    var matcher = parser.parseMessageMatcher("level(TrAcE)");

    assertFalse(matcher.isTagSelector());

    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);

    when(message.getLevel()).thenReturn(Level.Shared.DEBUG);
    assertTrue(matcher.matches(HIGHEST, message));
    assertTrue(matcher.matches(TRACE, message));
    assertFalse(matcher.matches(LOWEST, message));

    matcher = parser.parseMessageMatcher("level(info)");

    when(message.getLevel()).thenReturn(Level.Shared.INFO);
    assertTrue(matcher.matches(HIGHEST, message));
    assertFalse(matcher.matches(TRACE, message));

    matcher = parser.parseMessageMatcher("level(WARN)");

    when(message.getLevel()).thenReturn(Level.Shared.ERROR);
    assertTrue(matcher.matches(HIGHEST, message));
    assertTrue(matcher.matches(WARN, message));
    assertFalse(matcher.matches(INFO, message));

    matcher = parser.parseMessageMatcher("error");

    when(message.getLevel()).thenReturn(Level.Shared.ERROR);
    assertTrue(matcher.matches(HIGHEST, message));
    assertFalse(matcher.matches(WARN, message));

    assertThrowsExactly(MessageMatcherParserException.class,
        () -> parser.parseMessageMatcher("level(tracer)"));
  }


  @Test
  @SuppressWarnings({ "unchecked", "ResultOfMethodCallIgnored" })
  void testThrowableAtom()
  {
    var matcher = PARSER.parseMessageMatcher("throwable");

    assertFalse(matcher.isTagSelector());

    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);

    // no throwable
    when(message.getThrowable()).thenReturn(null);
    assertFalse(matcher.matches(HIGHEST, message));

    // NPE
    when(message.getThrowable()).thenReturn(new NullPointerException());
    assertTrue(matcher.matches(HIGHEST, message));

    matcher = PARSER.parseMessageMatcher("throwable(java.lang.Exception)");

    when(message.getThrowable()).thenReturn(new NullPointerException());
    assertTrue(matcher.matches(HIGHEST, message));

    when(message.getThrowable()).thenReturn(new OutOfMemoryError());
    assertFalse(matcher.matches(HIGHEST, message));

    assertThrowsExactly(MessageMatcherParserException.class,
        () -> PARSER.parseMessageMatcher("throwable(java.lang.String)"));

    assertThrowsExactly(MessageMatcherParserException.class,
        () -> PARSER.parseMessageMatcher("throwable(aa.bb.cc.dd.ee.Class)"));
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

    assertTrue(PARSER.parseMessageMatcher("has-param('msg-null')").matches(HIGHEST, message));
    assertTrue(PARSER.parseMessageMatcher("has-param('msg')").matches(HIGHEST, message));
    assertFalse(PARSER.parseMessageMatcher("has-param('text')").matches(HIGHEST, message));
    assertSame(none(), PARSER.parseMessageMatcher("has-param('')"));
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

    assertFalse(PARSER.parseMessageMatcher("has-param-value('msg-null')").matches(HIGHEST, message));
    assertTrue(PARSER.parseMessageMatcher("has-param-value('msg')").matches(HIGHEST, message));
    assertFalse(PARSER.parseMessageMatcher("has-param-value('text')").matches(HIGHEST, message));
    assertSame(none(), PARSER.parseMessageMatcher("has-param-value('')"));
  }


  @Test
  @SuppressWarnings("unchecked")
  void testCompoundAnd()
  {
    var matcher = PARSER.parseMessageMatcher("throwable and error and message('ID')");

    val message = (Message<Object>)mock(Message.class);

    // throwable, error, message id
    when(message.getThrowable()).thenReturn(new NullPointerException());
    when(message.getLevel()).thenReturn(Level.Shared.ERROR);
    when(message.getMessageId()).thenReturn("ID");
    assertTrue(matcher.matches(HIGHEST, message));
    assertFalse(matcher.matches(DEBUG, message));

    matcher = PARSER.parseMessageMatcher("and(throwable, message('ID'))");
    assertTrue(matcher.matches(HIGHEST, message));

    when(message.getMessageId()).thenReturn("??");
    assertFalse(matcher.matches(HIGHEST, message));
  }


  @Test
  @SuppressWarnings("unchecked")
  void testCompoundOr()
  {
    var matcher = PARSER.parseMessageMatcher("throwable or error or message('ID')");

    val message = (Message<Object>)mock(Message.class);

    // throwable, error, message id
    when(message.getThrowable()).thenReturn(new NullPointerException());
    when(message.getLevel()).thenReturn(Level.Shared.ERROR);
    when(message.getMessageId()).thenReturn("ID");
    assertTrue(matcher.matches(HIGHEST, message));

    when(message.getThrowable()).thenReturn(null);
    assertTrue(matcher.matches(HIGHEST, message));
    assertTrue(matcher.matches(DEBUG, message));

    matcher = PARSER.parseMessageMatcher("or(error, message('ID'))");
    assertTrue(matcher.matches(HIGHEST, message));
    assertTrue(matcher.matches(DEBUG, message));

    when(message.getMessageId()).thenReturn("??");
    assertTrue(matcher.matches(HIGHEST, message));
    assertFalse(matcher.matches(DEBUG, message));
  }


  @Test
  @SuppressWarnings("unchecked")
  void testCompoundNot()
  {
    var matcher = PARSER.parseMessageMatcher("not(throwable)");

    val message = (Message<Object>)mock(Message.class);

    when(message.getThrowable()).thenReturn(new NullPointerException());
    assertFalse(matcher.matches(HIGHEST, message));

    when(message.getThrowable()).thenReturn(null);
    assertTrue(matcher.matches(HIGHEST, message));

    matcher = PARSER.parseMessageMatcher("(info)");

    when(message.getLevel()).thenReturn(WARN);
    assertTrue(matcher.matches(HIGHEST, message));
    assertTrue(matcher.matches(INFO, message));
    assertFalse(matcher.matches(DEBUG, message));
  }


  @Test
  @SuppressWarnings("ResultOfMethodCallIgnored")
  void testLexerError()
  {
    assertTrue(assertThrowsExactly(MessageMatcherParserException.class,
        () -> PARSER.parseMessageMatcher("all-of%("))
        .getMessage().startsWith("unexpected input"));
  }


  @Test
  @SuppressWarnings("ResultOfMethodCallIgnored")
  void testParserError()
  {
    assertThrowsExactly(MessageMatcherParserException.class, () -> PARSER.parseMessageMatcher(""));

    assertTrue(assertThrowsExactly(MessageMatcherParserException.class,
        () -> PARSER.parseMessageMatcher("all-of("))
        .getMessage().startsWith("incomplete matcher"));

    assertTrue(assertThrowsExactly(MessageMatcherParserException.class,
        () -> PARSER.parseMessageMatcher("all-of(all-of)"))
        .getMessage().startsWith("mismatched input"));
  }
}