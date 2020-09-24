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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Iterator;


/**
 * @author Jeroen Gremmen
 */
public final class TagSelectorLexer implements Iterable<TagSelectorLexer.Token>
{
  @Getter private final String message;
  @Getter private final int length;


  TagSelectorLexer(String message) {
    length = (this.message = message).length();
  }


  private Token nextToken(TokenIterator data)
  {
    while(data.pos < length && message.charAt(data.pos) == ' ')
      data.pos++;

    if (data.pos == length)
      return null;

    final int start = data.pos;

    switch(message.charAt(start))
    {
      case '(':
        data.pos++;
        return new Token(start, start, TokenType.L_PAREN, "(");

      case ')':
        data.pos++;
        return new Token(start, start, TokenType.R_PAREN, ")");

      case ',':
        data.pos++;
        return new Token(start, start, TokenType.COMMA, ",");
    }

    final StringBuilder tagBuilder = new StringBuilder();

    for(; data.pos < length; data.pos++)
    {
      final char c = message.charAt(data.pos);
      if (c == '(' || c == ')' || c == ',' || c == ' ')
        break;

      tagBuilder.append(c);
    }

    final String tag = tagBuilder.toString();

    if ("allOf".equals(tag))
      return new Token(start, start + 4, TokenType.ALL_OF, tag);
    if ("and".equals(tag))
      return new Token(start, start + 2, TokenType.AND, tag);
    if ("any".equals(tag))
      return new Token(start, start + 2, TokenType.ANY, tag);
    if ("anyOf".equals(tag))
      return new Token(start, start + 4, TokenType.ANY_OF, tag);
    if ("noneOf".equals(tag))
      return new Token(start, start + 5, TokenType.NONE_OF, tag);
    if ("not".equals(tag))
      return new Token(start, start + 2, TokenType.NOT, tag);
    if ("or".equals(tag))
      return new Token(start, start + 1, TokenType.OR, tag);

    return new Token(start, data.pos - 1, TokenType.TAG, tag);
  }


  @Override
  public Iterator<Token> iterator() {
    return new TokenIterator();
  }




  private final class TokenIterator implements Iterator<Token>
  {
    private int pos;
    private Token token;


    private TokenIterator()
    {
      pos = 0;
      token = nextToken(this);
    }


    @Override
    public boolean hasNext() {
      return token != null;
    }


    @Override
    public Token next()
    {
      if (!hasNext())
        throw new IllegalStateException("no more tokens available");

      final Token returnToken = token;
      token = nextToken(this);

      return returnToken;
    }


    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }


  @ToString
  @EqualsAndHashCode(doNotUseGetters=true)
  @AllArgsConstructor(access = AccessLevel.PACKAGE)
  static final class Token
  {
    @Getter int start;
    @Getter int end;
    @Getter TokenType type;
    @Getter String text;
  }


  enum TokenType
  {
    TAG("tag name"), ANY("'any'"), ANY_OF("'anyOf'"), ALL_OF("'allOf'"),
    NONE_OF("'noneOf'"), AND("'and'"), OR("'or'"), NOT("'not'"),
    L_PAREN("'('"), R_PAREN("')'"), COMMA("','");


    @Getter private final String text;

    TokenType(String text) {
      this.text = text;
    }
  }
}
