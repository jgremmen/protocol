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

import de.sayayi.lib.protocol.selector.parser.TagSelectorLexer.Token;
import de.sayayi.lib.protocol.selector.parser.TagSelectorLexer.TokenType;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static de.sayayi.lib.protocol.selector.parser.TagSelectorLexer.TokenType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


/**
 * @author Jeroen Gremmen
 */
public class TagSelectorLexerTest
{
  @Test
  public void testAllTokens()
  {
    final Iterator<Token> iterator =
        new TagSelectorLexer(" ( ,  )any anyOf allOf noneOf not  info-checker and or ").iterator();
    Token token;

    // (
    token = iterator.next();
    assertEquals(L_PAREN, token.getType());
    assertEquals(1, token.getStart());
    assertEquals(1, token.getEnd());

    // ,
    token = iterator.next();
    assertEquals(COMMA, token.getType());
    assertEquals(3, token.getStart());
    assertEquals(3, token.getEnd());

    // )
    token = iterator.next();
    assertEquals(R_PAREN, token.getType());
    assertEquals(6, token.getStart());
    assertEquals(6, token.getEnd());

    // any
    token = iterator.next();
    assertEquals(ANY, token.getType());
    assertEquals(7, token.getStart());
    assertEquals(9, token.getEnd());

    // anyOf
    token = iterator.next();
    assertEquals(ANY_OF, token.getType());
    assertEquals(11, token.getStart());
    assertEquals(15, token.getEnd());

    // allOf
    token = iterator.next();
    assertEquals(ALL_OF, token.getType());
    assertEquals(17, token.getStart());
    assertEquals(21, token.getEnd());

    // noneOf
    token = iterator.next();
    assertEquals(NONE_OF, token.getType());
    assertEquals(23, token.getStart());
    assertEquals(28, token.getEnd());

    // not
    token = iterator.next();
    assertEquals(TokenType.NOT, token.getType());
    assertEquals(30, token.getStart());
    assertEquals(32, token.getEnd());

    // tag
    token = iterator.next();
    assertEquals(TAG, token.getType());
    assertEquals(35, token.getStart());
    assertEquals(46, token.getEnd());
    assertEquals("info-checker", token.getText());

    // and
    token = iterator.next();
    assertEquals(AND, token.getType());
    assertEquals(48, token.getStart());
    assertEquals(50, token.getEnd());

    // or
    token = iterator.next();
    assertEquals(OR, token.getType());
    assertEquals(52, token.getStart());
    assertEquals(53, token.getEnd());

    assertFalse(iterator.hasNext());
  }


  @Test
  public void testRealExample()
  {
    final Iterator<Token> iterator = new TagSelectorLexer("and(system,or(error,warning),console)").iterator();

    assertEquals(AND, iterator.next().getType());
    assertEquals(L_PAREN, iterator.next().getType());
    assertEquals(TAG, iterator.next().getType());
    assertEquals(COMMA, iterator.next().getType());
    assertEquals(OR, iterator.next().getType());
    assertEquals(L_PAREN, iterator.next().getType());
    assertEquals(TAG, iterator.next().getType());
    assertEquals(COMMA, iterator.next().getType());
    assertEquals(TAG, iterator.next().getType());
    assertEquals(R_PAREN, iterator.next().getType());
    assertEquals(COMMA, iterator.next().getType());
    assertEquals(TAG, iterator.next().getType());
    assertEquals(R_PAREN, iterator.next().getType());
    assertFalse(iterator.hasNext());
  }
}
