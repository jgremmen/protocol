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

import de.sayayi.lib.protocol.ProtocolEntry.Message;
import de.sayayi.lib.protocol.ProtocolFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import lombok.val;
import lombok.var;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashSet;
import java.util.Set;

import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    assertSame(MessageMatchers.any(), matcher);
    assertTrue(matcher.isTagSelector());

    // tag(system)
    matcher = PARSER.parse("tag(system)");

    assertTrue(matcher.isTagSelector());

    val message = (Message<Object>)mock(Message.class, Answers.CALLS_REAL_METHODS);
    when(message.getTagNames()).thenReturn(asTagNameSet("system", "ticket"));
    assertTrue(matcher.matches(HIGHEST, message));

    // tag('')
    matcher = PARSER.parse("tag('')");

    assertSame(MessageMatchers.none(), matcher);
    assertTrue(matcher.isTagSelector());
  }


  @Unmodifiable
  private static @NotNull Set<String> asTagNameSet(@NotNull String ... s)
  {
    val set = new HashSet<>(asList(s));
    set.add(ProtocolFactory.DEFAULT_TAG_NAME);

    return unmodifiableSet(set);
  }
}