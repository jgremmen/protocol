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
package de.sayayi.lib.protocol.matcher.antlr;

import de.sayayi.lib.protocol.exception.MessageMatcherParserException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.IterativeParseTreeWalker;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import lombok.NoArgsConstructor;
import lombok.val;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static java.util.Arrays.fill;
import static lombok.AccessLevel.PROTECTED;
import static org.antlr.v4.runtime.Token.EOF;


/**
 * @author Jeroen Gremmen
 * @since 1.2.0
 */
@NoArgsConstructor(access = PROTECTED)
public abstract class AbstractAntlr4Compiler
{
  @NotNull
  protected <L extends Lexer & CompilerInputSupplier,P extends Parser,C extends ParserRuleContext>
      C compile(@NotNull L lexer, @NotNull Function<L,P> parserSupplier,
                @NotNull Function<P,C> executeRule)
  {
    val compilerInput = lexer.getCompilerInput();
    val errorListener = new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?,?> recognizer, Object offendingSymbol, int line,
                              int charPositionInLine, String msg, RecognitionException ex) {
        AbstractAntlr4Compiler.this.syntaxError(compilerInput, (Token)offendingSymbol, msg, ex);
      }
    };

    lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
    lexer.addErrorListener(errorListener);

    val parser = parserSupplier.apply(lexer);

    parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
    parser.addErrorListener(errorListener);

    return executeRule.apply(parser);
  }


  protected <L extends Lexer & CompilerInputSupplier,P extends Parser,C extends ParserRuleContext,R>
      R compile(@NotNull L lexer, @NotNull Function<L,P> parserSupplier,
                @NotNull Function<P,C> executeRule, @NotNull ParseTreeListener listener,
                @NotNull Function<C,R> resultExtractor)
  {
    val parserRuleContext = compile(lexer, parserSupplier, executeRule);

    walk(listener, parserRuleContext);

    return resultExtractor.apply(parserRuleContext);
  }


  @Contract(mutates = "param2")
  private void walk(@NotNull ParseTreeListener listener, @NotNull ParseTree parseTree)
  {
    if (listener instanceof WalkerSupplier)
      switch(((WalkerSupplier)listener).getWalker())
      {
        case WALK_EXIT_RULES_ONLY:
          walkExitsOnly(listener, parseTree);
          return;

        case WALK_ENTRY_EXIT_RULES_ONLY:
          walkEntryExitsOnly(listener, parseTree);
          return;

        case WALK_FULL_HEAP:
          new IterativeParseTreeWalker().walk(listener, parseTree);
          return;
      }

    // default walker
    ParseTreeWalker.DEFAULT.walk(listener, parseTree);
  }


  @Contract(mutates = "param2")
  private void walkEntryExitsOnly(@NotNull ParseTreeListener listener, @NotNull ParseTree parseTree)
  {
    if (parseTree instanceof ParserRuleContext)
    {
      ((ParserRuleContext)parseTree).enterRule(listener);

      val children = ((ParserRuleContext)parseTree).children;
      if (children != null)
        for(val parseTreeChild: children)
          walkEntryExitsOnly(listener, parseTreeChild);

      ((ParserRuleContext)parseTree).exitRule(listener);
    }
  }


  @Contract(mutates = "param2")
  private void walkExitsOnly(@NotNull ParseTreeListener listener, @NotNull ParseTree parseTree)
  {
    if (parseTree instanceof ParserRuleContext)
    {
      val children = ((ParserRuleContext)parseTree).children;
      if (children != null)
        for(val parseTreeChild: children)
          walkExitsOnly(listener, parseTreeChild);

      ((ParserRuleContext)parseTree).exitRule(listener);
    }
  }


  @Contract("_, _, _ -> fail")
  protected void syntaxError(@NotNull String compilerInput, @NotNull ParserRuleContext ctx,
                             @NotNull String errorMsg) {
    syntaxError(compilerInput, ctx.getStart(), errorMsg, null);
  }


  @Contract("_, _, _, _ -> fail")
  protected void syntaxError(@NotNull String compilerInput, @NotNull Token token,
                             @NotNull String errorMsg, RecognitionException ex)
  {
    val lines = compilerInput.split("\r?\n");

    if (lines.length == 1)
      syntaxErrorSingleLine(compilerInput, token, errorMsg, ex);

    throw createException(errorMsg, compilerInput, token.getStartIndex(), token.getStopIndex(),
        "", ex);
  }


  @Contract("_, _, _, _ -> fail")
  private void syntaxErrorSingleLine(@NotNull String compilerInput, @NotNull Token token,
                                     @NotNull String errorMsg, RecognitionException ex)
  {
    val text = new StringBuilder(errorMsg).append(":\n")
        .append(compilerInput).append('\n');

    val startIndex = token.getStartIndex();
    val stopIndex = token.getType() == EOF ? startIndex : token.getStopIndex();
    val marker = new char[stopIndex + 1];

    fill(marker, 0, startIndex, ' ');  // leading spaces
    fill(marker, startIndex, stopIndex + 1, '^');  // marker

    throw createException(errorMsg, compilerInput, startIndex, stopIndex,
        text.append(marker).toString(), ex);
  }


  @SuppressWarnings("unused")
  protected @NotNull RuntimeException createException(
      @NotNull String errorMsg, @NotNull String compilerInput, int startIndex, int stopIndex,
      @NotNull String visibleMarker, RecognitionException ex)
  {
    return new MessageMatcherParserException(compilerInput, startIndex, stopIndex,
        visibleMarker, ex);
  }




  public interface CompilerInputSupplier
  {
    @Contract(pure = true)
    @NotNull String getCompilerInput();
  }




  public interface WalkerSupplier
  {
    @Contract(pure = true)
    @NotNull Walker getWalker();
  }




  public enum Walker
  {
    WALK_FULL_RECURSIVE,
    WALK_FULL_HEAP,
    WALK_EXIT_RULES_ONLY,
    WALK_ENTRY_EXIT_RULES_ONLY
  }
}