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
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.exception.MatcherParserException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static de.sayayi.lib.protocol.matcher.MessageMatcherParser.*;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.inGroup;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.inGroupRegex;
import static java.util.Arrays.fill;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;


/**
 * @author Jeroen Gremmen
 * @since 1.2.0
 */
@AllArgsConstructor
public class MatcherParser
{
  public static final MatcherParser INSTANCE = new MatcherParser(null, null);

  protected final ClassLoader classLoader;
  protected final Function<String,Level> levelResolver;


  @Contract(pure = true)
  public @NotNull MessageMatcher parse(@NotNull String matcherText)
  {
    val matcherParseTree =
        createParser(matcherText).parseMatcher();

    walk(new ParserListener(matcherText), matcherParseTree);

    return matcherParseTree.matcher;
  }


  @Contract(pure = true)
  public @NotNull TagSelector parseTagSelector(@NotNull String matcherText)
  {
    val matcherParseTree =
        createParser(matcherText).parseTagSelector();

    walk(new ParserListener(matcherText), matcherParseTree);

    return matcherParseTree.selector;
  }


  private @NotNull Parser createParser(@NotNull String matcherText)
  {
    val parser = new Parser(matcherText);
    val errorListener = new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?,?> recognizer, Object offendingSymbol, int line,
                              int charPositionInLine, String msg, RecognitionException ex) {
        MatcherParser.this.syntaxError(matcherText, (Token)offendingSymbol, msg, ex);
      }
    };

    parser.removeErrorListeners();
    parser.addErrorListener(errorListener);
    parser.setErrorHandler(ERROR_HANDLER);

    return parser;
  }


  private void walk(@NotNull ParseTreeListener listener, @NotNull ParseTree parseTree)
  {
    if (parseTree instanceof ParserRuleContext)
    {
      val children = ((ParserRuleContext)parseTree).children;
      if (children != null)
        for(val tc: children)
          walk(listener, tc);

      ((ParserRuleContext)parseTree).exitRule(listener);
    }
  }


  @Contract("_, _, _, _ -> fail")
  private void syntaxError(@NotNull String matcherText, @NotNull Token token,
                           @NotNull String errorMsg, RecognitionException ex)
  {
    val text = new StringBuilder(errorMsg).append(":\n").append(matcherText).append('\n');
    val startIndex = token.getStartIndex();
    val stopIndex = token.getType() == Token.EOF ? startIndex : token.getStopIndex();
    val marker = new char[stopIndex + 1];

    fill(marker, 0, startIndex, ' ');  // leading spaces
    fill(marker, startIndex, stopIndex + 1, '^');  // marker

    throw new MatcherParserException(matcherText, startIndex, stopIndex,
        text.append(marker).toString(), ex);
  }




  private static final class Lexer extends MessageMatcherLexer
  {
    @SuppressWarnings("deprecation")
    public Lexer(@NotNull String matcherText) {
      super(new ANTLRInputStream(matcherText));
    }


    @Override
    public Vocabulary getVocabulary() {
      return MatcherVocabulary.INSTANCE;
    }
  }




  private static final class Parser extends MessageMatcherParser
  {
    private Parser(@NotNull String matcherText) {
      super(new BufferedTokenStream(new Lexer(matcherText)));
    }


    @Override
    public void exitRule()
    {
      // fix ANTLR bug
      if (!getErrorHandler().inErrorRecoveryMode(this))
        super.exitRule();
    }


    @Override
    public Vocabulary getVocabulary() {
      return MatcherVocabulary.INSTANCE;
    }
  }




  @RequiredArgsConstructor(access = PRIVATE)
  private final class ParserListener extends MessageMatcherParserBaseListener
  {
    private final String matcherText;


    @Override
    public void exitParseMatcher(ParseMatcherContext ctx) {
      ctx.matcher = ctx.compoundMatcher().matcher;
    }


    @Override
    public void exitParseTagSelector(ParseTagSelectorContext ctx) {
      ctx.selector = ctx.compoundTagSelector().matcher.asTagSelector();
    }


    @Override
    public void exitAndMatcher(AndMatcherContext ctx)
    {
      ctx.matcher = Conjunction.of(
          ctx.compoundMatcher()
              .stream()
              .map(ec -> ec.matcher)
              .toArray(MessageMatcher[]::new));
    }


    @Override
    public void exitOrMatcher(OrMatcherContext ctx)
    {
      ctx.matcher = Disjunction.of(
          ctx.compoundMatcher()
              .stream()
              .map(ec -> ec.matcher)
              .toArray(MessageMatcher[]::new));
    }


    @Override
    public void exitNotMatcher(NotMatcherContext ctx)
    {
      val expr = ctx.compoundMatcher().matcher;
      ctx.matcher = ctx.NOT() != null ? Negation.of(expr) : expr;
    }


    @Override
    public void exitToMatcher(ToMatcherContext ctx) {
      ctx.matcher = ctx.matcherAtom().matcher.asJunction();
    }


    @Override
    public void exitBooleanMatcher(BooleanMatcherContext ctx) {
      ctx.matcher = ctx.ANY() != null ? BooleanMatcher.ANY : BooleanMatcher.NONE;
    }


    @Override
    public void exitThrowableMatcher(ThrowableMatcherContext ctx)
    {
      final TerminalNode qualifiedName = ctx.QUALIFIED_NAME();

      if (qualifiedName == null)
        ctx.matcher = MessageMatchers.hasThrowable();
      else
      {
        Class<?> clazz = null;

        try {
          clazz = classLoader == null
              ? Class.forName(qualifiedName.getText())
              : Class.forName(qualifiedName.getText(), false, classLoader);
        } catch(ClassNotFoundException ignored) {
        }

        if (clazz == null || !Throwable.class.isAssignableFrom(clazz))
          reportError("class not found or not of type Throwable", qualifiedName.getSymbol());

        //noinspection unchecked
        ctx.matcher = MessageMatchers.hasThrowable((Class<? extends Throwable>)clazz);
      }
    }


    @Override
    public void exitTagMatcher(TagMatcherContext ctx) {
      ctx.matcher = ctx.tagMatcherAtom().matcher;
    }


    @Override
    public void exitParamMatcher(ParamMatcherContext ctx)
    {
      val paramName = ctx.string().str;

      ctx.matcher = ctx.HAS_PARAM() != null
          ? MessageMatchers.hasParam(paramName)
          : MessageMatchers.hasParamValue(paramName);
    }


    @Override
    public void exitLevelMatcher(LevelMatcherContext ctx)
    {
      switch(((TerminalNode)ctx.getChild(0)).getSymbol().getType())
      {
        case DEBUG:
          ctx.matcher = LevelMatcher.DEBUG;
          break;

        case INFO:
          ctx.matcher = LevelMatcher.INFO;
          break;

        case WARN:
          ctx.matcher = LevelMatcher.WARN;
          break;

        case ERROR:
          ctx.matcher = LevelMatcher.ERROR;
          break;

        case LEVEL:
          ctx.matcher = LevelMatcher.of(ctx.level().lvl);
          break;
      }
    }


    @Override
    public void exitMessageMatcher(MessageMatcherContext ctx) {
      ctx.matcher = MessageMatchers.hasMessage(ctx.string().str);
    }


    @Override
    public void exitInGroupMatcher(InGroupMatcherContext ctx)
    {
      val groupName = ctx.string().str;
      ctx.matcher = ctx.IN_GROUP() != null ? inGroup(groupName) : inGroupRegex(groupName);
    }


    @Override
    public void exitDepthMatcher(DepthMatcherContext ctx) {
      ctx.matcher = ctx.IN_GROUP() != null ? inGroup() : MessageMatchers.inRoot();
    }


    @Override
    public void exitTagSelectorAtom(TagSelectorAtomContext ctx)
    {
      val tagExpression = ctx.tagMatcherAtom();

      ctx.matcher = tagExpression != null ? tagExpression.matcher
          : ctx.ANY() != null ? BooleanMatcher.ANY : BooleanMatcher.NONE;
    }


    @Override
    public void exitTagMatcherAtom(TagMatcherAtomContext ctx)
    {
      switch(((TerminalNode)ctx.getChild(0)).getSymbol().getType())
      {
        case TAG:
          ctx.matcher = MessageMatchers.hasTag(ctx.tagName().tag);
          break;

        case ANY_OF:
          ctx.matcher = MessageMatchers.hasAnyOf(ctx.tagNameList().tags);
          break;

        case ALL_OF:
          ctx.matcher = MessageMatchers.hasAllOf(ctx.tagNameList().tags);
          break;

        case NONE_OF:
          ctx.matcher = MessageMatchers.hasNoneOf(ctx.tagNameList().tags);
          break;
      }
    }


    @Override
    public void exitTagNameList(TagNameListContext ctx) {
      ctx.tags = ctx.tagName().stream().map(tnc -> tnc.tag).collect(toList());
    }


    @Override
    public void exitTagName(TagNameContext ctx)
    {
      val stringContext = ctx.string();
      ctx.tag = stringContext != null ? stringContext.str : ctx.getChild(0).getText();
    }


    @Override
    public void exitLevel(LevelContext ctx)
    {
      val stringContext = ctx.string();
      val name = stringContext != null ? stringContext.str : ctx.getChild(0).getText();

      try {
        ctx.lvl = levelResolver == null ? null : levelResolver.apply(name);
      } catch(Exception ignored) {
      }

      if (ctx.lvl == null)
      {
        for(val level: Level.Shared.values())
          if (level.name().equals(name)) {
            ctx.lvl = level;
            return;
          }

        reportError("unknown level '" + name + "'", ctx);
      }
    }


    @Override
    public void exitString(StringContext ctx)
    {
      val s = ctx.STRING().getText();
      ctx.str = s.substring(1, s.length() - 1);
    }


    @Contract(value = "_, _ -> fail")
    private void reportError(@NotNull String msg, @NotNull ParserRuleContext ctx) {
      syntaxError(matcherText, ctx.getStart(), msg, null);
    }


    @Contract("_, _ -> fail")
    @SuppressWarnings("SameParameterValue")
    private void reportError(@NotNull String msg, @NotNull Token offendingToken) {
      syntaxError(matcherText, offendingToken, msg, null);
    }
  }




  private static final ANTLRErrorStrategy ERROR_HANDLER = new DefaultErrorStrategy() {
    @Override
    protected String getTokenErrorDisplay(Token t)
    {
      return t != null && t.getType() == Token.EOF
          ? "end of message matcher"
          : super.getTokenErrorDisplay(t);
    }
  };
}