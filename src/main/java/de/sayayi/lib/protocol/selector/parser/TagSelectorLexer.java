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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.val;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;


/**
 * @author Jeroen Gremmen
 */
public final class TagSelectorLexer implements Iterable<TagSelectorLexer.Token>
{
  private static final Map<TokenType,String> TOKEN_NAME_MAP =
      new EnumMap<TokenType,String>(TokenType.class);

  @Getter private final String message;
  @Getter private final int length;


  static
  {
    TOKEN_NAME_MAP.put(TokenType.ALL_OF, "allOf");
    TOKEN_NAME_MAP.put(TokenType.AND, "and");
    TOKEN_NAME_MAP.put(TokenType.ANY, "any");
    TOKEN_NAME_MAP.put(TokenType.ANY_OF, "anyOf");
    TOKEN_NAME_MAP.put(TokenType.FALSE, "false");
    TOKEN_NAME_MAP.put(TokenType.NONE_OF, "noneOf");
    TOKEN_NAME_MAP.put(TokenType.NOT, "not");
    TOKEN_NAME_MAP.put(TokenType.OR, "or");
    TOKEN_NAME_MAP.put(TokenType.TRUE, "true");
  }


  TagSelectorLexer(String message) {
    length = (this.message = message).length();
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


    private Token nextToken(TokenIterator data)
    {
      while(data.pos < length && message.charAt(data.pos) == ' ')
        data.pos++;

      if (data.pos == length)
        return null;

      val start = data.pos;

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

      val tagBuilder = new StringBuilder();

      for(; data.pos < length; data.pos++)
      {
        final char c = message.charAt(data.pos);
        if (c == '(' || c == ')' || c == ',' || c == ' ')
          break;

        tagBuilder.append(c);
      }

      val tag = tagBuilder.toString();

      for(val tokenNameEntry: TOKEN_NAME_MAP.entrySet())
      {
        val name = tokenNameEntry.getValue();

        if (name.equals(tag))
          return new Token(start, start + name.length() - 1, tokenNameEntry.getKey(), tag);
      }

      return new Token(start, data.pos - 1, TokenType.TAG, tag);
    }


    @Override
    public boolean hasNext() {
      return token != null;
    }


    @Override
    public Token next()
    {
      if (!hasNext())
        throw new NoSuchElementException("no more tokens available");

      val returnToken = token;
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
    TAG("tag name"),
    ANY("'any'"),
    ANY_OF("'anyOf'"),
    ALL_OF("'allOf'"),
    NONE_OF("'noneOf'"),
    AND("'and'"),
    OR("'or'"),
    NOT("'not'"),
    TRUE("'true'"),
    FALSE("'false"),
    L_PAREN("'('"),
    R_PAREN("')'"),
    COMMA("','");


    @Getter private final String text;

    TokenType(String text) {
      this.text = text;
    }
  }
}
