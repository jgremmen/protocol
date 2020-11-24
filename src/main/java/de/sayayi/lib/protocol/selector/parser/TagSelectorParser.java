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

import de.sayayi.lib.protocol.Tag;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.exception.TagSelectorParserException;
import de.sayayi.lib.protocol.selector.match.MatchAny;
import de.sayayi.lib.protocol.selector.match.MatchFixResult;
import de.sayayi.lib.protocol.selector.parser.TagSelectorLexer.Token;
import de.sayayi.lib.protocol.selector.parser.TagSelectorLexer.TokenType;

import lombok.AllArgsConstructor;
import lombok.val;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static de.sayayi.lib.protocol.selector.parser.TagSelectorLexer.TokenType.AND;
import static de.sayayi.lib.protocol.selector.parser.TagSelectorLexer.TokenType.COMMA;
import static de.sayayi.lib.protocol.selector.parser.TagSelectorLexer.TokenType.L_PAREN;
import static de.sayayi.lib.protocol.selector.parser.TagSelectorLexer.TokenType.R_PAREN;
import static de.sayayi.lib.protocol.selector.parser.TagSelectorLexer.TokenType.TAG;
import static de.sayayi.lib.protocol.selector.parser.TagSelectorLexer.TokenType.TRUE;


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

    return idx < tokens.size() ? tokens.get(idx) : null;
  }


  private TokenType getTypeAt(int idx)
  {
    val token = getTokenAt(idx);
    return token == null ? null : token.getType();
  }


  public TagSelector parseSelector()
  {
    val selector = parseSelector(0);
    val token = getTokenAt(selector.tokenLast + 1);

    if (token != null)
      throw new TagSelectorParserException(token.getStart(), token.getEnd(), "unexpected token: " + token.getText());

    return selector.result;
  }


  @NotNull ParsedRule<TagSelector> parseSelector(int t)
  {
    val t0 = getTokenAt(t);
    if (t0 == null)
    {
      val idx = lexer.getLength() + 1;
      throw new TagSelectorParserException(idx, idx, "missing selector");
    }

    val type = t0.getType();

    switch(type)
    {
      case TAG:
        return new ParsedRule<TagSelector>(t, t, Tag.of(t0.getText()));

      case ALL_OF:
      case ANY_OF:
      case NONE_OF:
        return parseXXXOf(t, type);

      case AND:
      case OR:
        return parseAndOr(t, type == AND);

      case ANY:
      case TRUE:
      case FALSE:
        return parseNoArgsFunction(t, type);

      case NOT:
        return parseNot(t);

      default:
        throw new TagSelectorParserException(t0.getStart(), t0.getEnd(), "unexpected token '" + t0.getText() + "'");
    }
  }


  private @NotNull ParsedRule<TagSelector> parseAndOr(int t, boolean and)
  {
    // t0=and t1=<parameters>
    // t0=or t1=<parameters>
    val parameters = parseParameters(t + 1);

    return new ParsedRule<TagSelector>(t, parameters.tokenLast, and
        ? Tag.and(parameters.result.toArray(new TagSelector[0]))
        : Tag.or(parameters.result.toArray(new TagSelector[0])));
  }


  private @NotNull ParsedRule<TagSelector> parseXXXOf(int t, @NotNull TokenType type)
  {
    expect(t + 1, L_PAREN);

    val tagList = parseTagList(t + 1);

    TagSelector selector;

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

      default:
        throw new IllegalStateException("Unexpected value: " + type);
    }

    return new ParsedRule<TagSelector>(t, tagList.tokenLast, selector);
  }


  private @NotNull ParsedRule<TagSelector> parseNoArgsFunction(int t, @NotNull TokenType type)
  {
    assert getTypeAt(t) == type;

    expect(t + 1, L_PAREN);
    expect(t + 2, R_PAREN);

    switch(type)
    {
      case ANY:
        return new ParsedRule<TagSelector>(t, t + 2, new MatchAny());

      case TRUE:
      case FALSE:
        return new ParsedRule<TagSelector>(t, t + 2, MatchFixResult.valueOf(type == TRUE));

      default:
        throw new IllegalStateException("Unexpected value: " + type);
    }
  }


  private @NotNull ParsedRule<TagSelector> parseNot(int t)
  {
    expect(t + 1, L_PAREN);

    val selector = parseSelector(t + 2);
    val lastIdx = selector.tokenLast + 1;

    expect(lastIdx, R_PAREN);

    return new ParsedRule<TagSelector>(t, lastIdx, Tag.not(selector.result));
  }


  private @NotNull ParsedRule<List<TagSelector>> parseParameters(int t)
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


  private @NotNull ParsedRule<List<String>> parseTagList(int t)
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


  private @NotNull Token expect(int t, @NotNull TokenType type)
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
