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

import de.sayayi.lib.protocol.Tag;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.selector.TagSelectorLexer.Token;
import de.sayayi.lib.protocol.selector.TagSelectorLexer.TokenType;

import lombok.AllArgsConstructor;
import lombok.val;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static de.sayayi.lib.protocol.selector.TagSelectorLexer.TokenType.*;


/**
 * @author Jeroen Gremmen
 */
public final class TagSelectorParser
{
  private final TagSelectorLexer lexer;
  private final Iterator<Token> tokenIterator;
  private final List<Token> tokens;


  public TagSelectorParser(@NotNull String selector)
  {
    lexer = new TagSelectorLexer(selector);
    tokenIterator = lexer.iterator();
    tokens = new ArrayList<Token>();
  }


  private Token getTokenAt(int idx)
  {
    while(idx >= tokens.size() && tokenIterator.hasNext())
      tokens.add(tokenIterator.next());

    return (idx < tokens.size()) ? tokens.get(idx) : null;
  }


  private TokenType getTypeAt(int idx)
  {
    val token = getTokenAt(idx);
    return (token == null) ? null : token.getType();
  }


  public TagSelector parseSelector()
  {
    val selector = parseSelector(0);
    val token = getTokenAt(selector.tokenLast + 1);

    if (token != null)
      throw new TagSelectorParserException(token.getStart(), token.getEnd(), "unexpected token: " + token.getText());

    return selector.result;
  }


  ParsedRule<TagSelector> parseSelector(int t)
  {
    val t0 = getTokenAt(t);
    if (t0 == null)
    {
      val idx = lexer.getLength() + 1;
      throw new TagSelectorParserException(idx, idx, "missing selector");
    }

    switch(t0.getType())
    {
      case TAG:
        // t0=<tag>
        return new ParsedRule<TagSelector>(t, t, Tag.of(t0.getText()));

      case ALL_OF:
        // t0=allOf ...
        return parseXXXOf(t, ALL_OF);

      case AND:
        // t0=and ...
        return parseAndOr(t, true);

      case ANY:
        // t0=any ...
        return parseAny(t);

      case ANY_OF:
        // t0=anyOf ...
        return parseXXXOf(t, ANY_OF);

      case NONE_OF:
        // t0=noneOf ...
        return parseXXXOf(t, NONE_OF);

      case NOT:
        // t0=not ...
        return parseNot(t);

      case OR:
        // t0=or ...
        return parseAndOr(t, false);

      default:
        throw new TagSelectorParserException(t0.getStart(), t0.getEnd(), "unexpected token '" + t0.getText() + "'");
    }
  }


  private ParsedRule<TagSelector> parseAndOr(int t, boolean and)
  {
    // t0=and t1=<parameters>
    // t0=or t1=<parameters>
    val parameters = parseParameters(t + 1);

    return new ParsedRule<TagSelector>(t, parameters.tokenLast, and
        ? Tag.and(parameters.result.toArray(new TagSelector[0]))
        : Tag.or(parameters.result.toArray(new TagSelector[0])));
  }


  private ParsedRule<TagSelector> parseXXXOf(int t, TokenType type)
  {
    expect(t + 1, L_PAREN);

    val tagList = parseTagList(t + 1);

    TagSelector selector = null;

    switch(type)
    {
      case ANY_OF:
        selector = Tag.anyOf(tagList.result.toArray(new String[0]));
        break;

      case ALL_OF:
        selector = Tag.allOf(tagList.result.toArray(new String[0]));
        break;

      case NONE_OF:
        selector = Tag.noneOf(tagList.result.toArray(new String[0]));
        break;
    }

    return new ParsedRule<TagSelector>(t, tagList.tokenLast, selector);
  }


  private ParsedRule<TagSelector> parseAny(int t)
  {
    // t0=any t1=( t2=)
    assert getTypeAt(t) == ANY;

    expect(t + 1, L_PAREN);
    expect(t + 2, R_PAREN);

    return new ParsedRule<TagSelector>(t, t + 2, Tag.any());
  }


  private ParsedRule<TagSelector> parseNot(int t)
  {
    expect(t + 1, L_PAREN);

    val selector = parseSelector(t + 2);
    val lastIdx = selector.tokenLast + 1;

    expect(lastIdx, R_PAREN);

    return new ParsedRule<TagSelector>(t, lastIdx, Tag.not(selector.result));
  }


  private ParsedRule<List<TagSelector>> parseParameters(int t)
  {
    assert getTypeAt(t) == L_PAREN;

    val parameters = new ArrayList<TagSelector>();
    val tokenStart = t;

    // t0=( t1=selector t2=)
    // t0=( t1=selector t2=COMMA t3=selector t4=)
    // t0=( t1=selector t2=COMMA t3=selector t4=COMMA t5=selector t6=)

    do {
      val t1Selector = parseSelector(++t);

      parameters.add(t1Selector.result);
      t = t1Selector.tokenLast;
    } while(getTypeAt(++t) == COMMA);

    expect(t, R_PAREN);

    return new ParsedRule<List<TagSelector>>(tokenStart, t, parameters);
  }


  private ParsedRule<List<String>> parseTagList(int t)
  {
    assert getTypeAt(t) == L_PAREN;

    val parameters = new ArrayList<String>();
    val tokenStart = t;

    // t0=( t1=tag t2=)
    // t0=( t1=tag t2=COMMA t3=tag t4=)
    // t0=( t1=tag t2=COMMA t3=tag t4=COMMA t5=tag t6=)

    do {
      parameters.add(expect(++t, TAG).getText());
    } while(getTypeAt(++t) == COMMA);

    expect(t, R_PAREN);

    return new ParsedRule<List<String>>(tokenStart, t, parameters);
  }


  private Token expect(int t, TokenType type)
  {
    val token = getTokenAt(t);

    if (token == null)
    {
      val lastIdx = lexer.getLength() + 1;
      throw new TagSelectorParserException(lastIdx, lastIdx, "unexpected end; missing " + type.getText());
    }

    if (token.getType() != type)
      throw new TagSelectorParserException(token.getStart(), token.getStart(), "missing " + type.getText());

    return token;
  }




  @AllArgsConstructor
  private static final class ParsedRule<T>
  {
    final int tokenFirst;
    final int tokenLast;
    final T result;
  }
}
