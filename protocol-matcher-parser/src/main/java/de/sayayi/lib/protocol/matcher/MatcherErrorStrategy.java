package de.sayayi.lib.protocol.matcher;

import org.antlr.v4.runtime.ANTLRErrorStrategy;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.IntervalSet;

import lombok.NoArgsConstructor;
import lombok.val;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.stream.Collectors.toCollection;
import static lombok.AccessLevel.PACKAGE;
import static org.antlr.v4.runtime.Token.EOF;


@NoArgsConstructor(access = PACKAGE)
class MatcherErrorStrategy extends DefaultErrorStrategy
{
  static final ANTLRErrorStrategy INSTANCE = new MatcherErrorStrategy();

  private static final String EOF_DISPLAY_NAME = "end of matcher";


  @Override
  protected void reportInputMismatch(Parser recognizer, InputMismatchException ex)
  {
    val expectedTokens = ex.getExpectedTokens();
    val offendingToken = ex.getOffendingToken();

    if (isEOFToken(offendingToken))
    {
      // don't confuse user with a long list of tokens
      if (expectedTokens.size() <= 4)
      {
        recognizer.notifyErrorListeners(offendingToken,
            "incomplete matcher; expecting " +
            tokenList(expectedTokens, recognizer.getVocabulary()) + " at end", ex);
      }
      else
        recognizer.notifyErrorListeners(offendingToken, "incomplete matcher", ex);
    }
    else
    {
      val offendingTokenText = getTokenErrorDisplay(offendingToken);

      if (expectedTokens.size() <= 4)
      {
        recognizer.notifyErrorListeners(offendingToken,
            "mismatched input " + offendingTokenText + "; expecting " +
            tokenList(expectedTokens, recognizer.getVocabulary()), ex);
      }
      else
      {
        recognizer.notifyErrorListeners(offendingToken,
            "mismatched input " + offendingTokenText, ex);
      }
    }
  }


  @Contract(pure = true)
  protected String getTokenErrorDisplay(Token t) {
    return isEOFToken(t) ? EOF_DISPLAY_NAME : super.getTokenErrorDisplay(t);
  }


  @Contract(pure = true)
  private boolean isEOFToken(Token t) {
    return t != null && t.getType() == EOF;
  }


  @Contract(pure = true)
  private @NotNull String tokenList(@NotNull IntervalSet tokens,
                                    @NotNull Vocabulary vocabulary)
  {
    val list = new StringBuilder();

    for(Iterator<String> tokenNameIterator = getTokenDisplayNames(tokens, vocabulary).iterator();
        tokenNameIterator.hasNext();)
    {
      val tokenName = tokenNameIterator.next();

      if (list.length() > 0)
        list.append(tokenNameIterator.hasNext() ? ", " : " or ");

      list.append(tokenName);
    }

    return list.toString();
  }


  @Contract(pure = true)
  private @NotNull Set<String> getTokenDisplayNames(@NotNull IntervalSet tokens,
                                                    @NotNull Vocabulary vocabulary)
  {
    return tokens.toSet()
        .stream()
        .map(vocabulary::getDisplayName)
        .filter(s -> s != null && !s.isEmpty())
        .collect(toCollection(LinkedHashSet::new));
  }
}