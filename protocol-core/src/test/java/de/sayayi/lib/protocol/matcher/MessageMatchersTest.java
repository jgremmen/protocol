/*
 * Copyright 2021 Jeroen Gremmen
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

import de.sayayi.lib.protocol.ProtocolEntry.Message;
import de.sayayi.lib.protocol.StringProtocolFactory;
import org.junit.jupiter.api.Test;

import lombok.val;

import java.util.Collections;
import java.util.TreeMap;
import java.util.TreeSet;

import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;
import static de.sayayi.lib.protocol.ProtocolFactory.DEFAULT_TAG_NAME;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.hasAllOf;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.hasAnyOf;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.hasMessage;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.hasNoneOf;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.hasParam;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.hasParamValue;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.hasTag;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author Jeroen Gremmen
 */
public class MessageMatchersTest
{
  @Test
  public void testHasTag1()
  {
    //noinspection unchecked
    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);
    when(message.getTagNames()).thenReturn(new TreeSet<>(asList(DEFAULT_TAG_NAME, "gui")));

    assertTrue(hasTag("gui").matches(HIGHEST, message));
    assertTrue(hasTag(DEFAULT_TAG_NAME).matches(HIGHEST, message));
    assertFalse(hasTag("support").matches(HIGHEST, message));

    assertFalse(hasTag("").matches(HIGHEST, message));
  }


  @Test
  public void testHasTag2()
  {
    val factory = StringProtocolFactory.createPlainTextFactory();
    val protocol = factory.createProtocol();

    protocol.debug().forTag("gui").message("Test");

    assertTrue(protocol.matches(hasTag("gui")));
    assertTrue(protocol.matches(hasTag(DEFAULT_TAG_NAME)));
    assertFalse(protocol.matches(hasTag("support")));
  }


  @Test
  public void testHasAnyOf()
  {
    //noinspection unchecked
    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);
    when(message.getTagNames()).thenReturn(new TreeSet<>(asList(DEFAULT_TAG_NAME, "gui")));

    assertTrue(hasAnyOf("support", "gui", "xyz").matches(HIGHEST, message));
    assertTrue(hasAnyOf(DEFAULT_TAG_NAME).matches(HIGHEST, message));
    assertFalse(hasAnyOf("support", "xyz").matches(HIGHEST, message));
  }


  @Test
  public void testHasAllOf()
  {
    //noinspection unchecked
    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);
    when(message.getTagNames()).thenReturn(new TreeSet<>(asList(DEFAULT_TAG_NAME, "gui")));

    assertTrue(hasAllOf(DEFAULT_TAG_NAME, "gui").matches(HIGHEST, message));
    assertTrue(hasAllOf(DEFAULT_TAG_NAME).matches(HIGHEST, message));
    assertFalse(hasAllOf("support", "gui").matches(HIGHEST, message));
  }


  @Test
  public void testHasNoneOf()
  {
    //noinspection unchecked
    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);
    when(message.getTagNames()).thenReturn(new TreeSet<>(asList(DEFAULT_TAG_NAME, "gui")));

    assertTrue(hasNoneOf("support", "xzy").matches(HIGHEST, message));
    assertFalse(hasNoneOf("support", "gui").matches(HIGHEST, message));
    assertFalse(hasNoneOf(DEFAULT_TAG_NAME, "gui").matches(HIGHEST, message));
  }


  @Test
  public void testHasParam()
  {
    //noinspection unchecked
    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);
    when(message.getParameterValues()).thenReturn(singletonMap("id", 34));

    assertTrue(hasParam("id").matches(HIGHEST, message));
    assertFalse(hasParam("").matches(HIGHEST, message));
    assertFalse(hasParam("test").matches(HIGHEST, message));
  }


  @Test
  public void testHasParamValue1()
  {
    //noinspection unchecked
    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);
    val params = new TreeMap<String,Object>();

    params.put("null", null);
    params.put("not-null", true);

    when(message.getParameterValues()).thenReturn(Collections.unmodifiableMap(params));

    assertFalse(hasParamValue("null").matches(HIGHEST, message));
    assertTrue(hasParamValue("not-null").matches(HIGHEST, message));
  }


  @Test
  public void testHasParamValue2()
  {
    //noinspection unchecked
    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);
    val params = new TreeMap<String,Object>();

    params.put("null", null);
    params.put("not-null", true);

    when(message.getParameterValues()).thenReturn(Collections.unmodifiableMap(params));

    assertTrue(hasParamValue("null", null).matches(HIGHEST, message));
    assertTrue(hasParamValue("not-null", true).matches(HIGHEST, message));
    assertFalse(hasParamValue("not-null", "true").matches(HIGHEST, message));
  }


  @Test
  public void testHasMessage()
  {
    //noinspection unchecked
    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);
    when(message.getMessageId()).thenReturn("MSG-0341");

    assertTrue(hasMessage("MSG-0341").matches(HIGHEST, message));
    assertFalse(hasMessage("MSG-0001").matches(HIGHEST, message));
    assertFalse(hasMessage("").matches(HIGHEST, message));
  }

/*
  @Test
  public void testIsTagSelector()
  {
    //noinspection unchecked
    val message = (Message<Object>)mock(Message.class, CALLS_REAL_METHODS);
    when(message.getTagNames()).thenReturn(new TreeSet<>(asList(DEFAULT_TAG_NAME, "gui")));

    assertTrue(is(Tag.parse("gui")).matches(HIGHEST, message));
    assertTrue(is(Tag.parse(DEFAULT_TAG_NAME)).matches(HIGHEST, message));
    assertTrue(is(Tag.parse("or(gui,test)")).matches(HIGHEST, message));
    assertFalse(is(Tag.parse("and(gui,test)")).matches(HIGHEST, message));
  }
*/
}