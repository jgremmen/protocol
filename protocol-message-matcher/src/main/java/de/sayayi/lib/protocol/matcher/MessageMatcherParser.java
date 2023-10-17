/*
 * Copyright 2022 Jeroen Gremmen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.sayayi.lib.protocol.matcher;

import de.sayayi.lib.antlr4.AbstractAntlr4Parser;
import de.sayayi.lib.antlr4.AbstractVocabulary;
import de.sayayi.lib.antlr4.syntax.GenericSyntaxErrorFormatter;
import de.sayayi.lib.antlr4.syntax.SyntaxErrorFormatter;
import de.sayayi.lib.antlr4.walker.Walker;
import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.ProtocolMessageMatcher;
import de.sayayi.lib.protocol.TagSelector;
import de.sayayi.lib.protocol.exception.MessageMatcherParserException;
import de.sayayi.lib.protocol.matcher.antlr.MessageMatcherBaseListener;
import de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.TerminalNode;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

import static de.sayayi.lib.antlr4.walker.Walker.WALK_EXIT_RULES_HEAP;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.inGroup;
import static de.sayayi.lib.protocol.matcher.MessageMatchers.inGroupRegex;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.ALL_OF;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.AND;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.ANY;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.ANY_OF;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.COMMA;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.DEBUG;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.ERROR;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.HAS_PARAM;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.HAS_PARAM_VALUE;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.IDENTIFIER;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.INFO;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.IN_GROUP;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.IN_GROUP_REGEX;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.IN_ROOT;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.LEVEL;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.L_PAREN;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.MESSAGE;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.NONE;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.NONE_OF;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.NOT;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.OR;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.QUALIFIED_CLASS_NAME;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.R_PAREN;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.STRING;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.TAG;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.THROWABLE;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.WARN;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherLexer.WS;
import static de.sayayi.lib.protocol.matcher.antlr.MessageMatcherParser.*;
import static java.lang.Character.digit;
import static java.util.Locale.ROOT;
import static java.util.stream.Collectors.toList;


/**
 * @author Jeroen Gremmen
 * @since 1.2.0
 */
public class MessageMatcherParser extends AbstractAntlr4Parser
{
  private static final SyntaxErrorFormatter SYNTAX_ERROR_FORMATTER =
      new GenericSyntaxErrorFormatter(1, 0, 0, "> ");

  public static final MessageMatcherParser INSTANCE =
      new MessageMatcherParser(null, null);

  protected final ClassLoader classLoader;
  protected final Function<String,Level> levelResolver;


  public MessageMatcherParser(ClassLoader classLoader, Function<String,Level> levelResolver)
  {
    super(SYNTAX_ERROR_FORMATTER);

    this.classLoader = classLoader;
    this.levelResolver = levelResolver;
  }


  @Contract(pure = true)
  public @NotNull MessageMatcher parseMessageMatcher(@NotNull String messageMatcherText)
  {
    return parse(new Lexer(messageMatcherText), Parser::new, Parser::parseMatcher,
        new Listener(), ctx -> ctx.matcher);
  }


  @Contract(pure = true)
  public @NotNull TagSelector parseTagSelector(@NotNull String tagSelectorText)
  {
    return parse(new Lexer(tagSelectorText), Parser::new, Parser::parseTagSelector,
        new Listener(), ctx -> ctx.selector);
  }


  @Override
  protected @NotNull RuntimeException createException(
      @NotNull Token startToken, @NotNull Token stopToken, @NotNull String formattedMessage,
      @NotNull String errorMsg, Exception cause) {
    return new MessageMatcherParserException(errorMsg, formattedMessage, cause);
  }




  private static final class Lexer extends MessageMatcherLexer
  {
    private Lexer(@NotNull String matcherText) {
      super(CharStreams.fromString(matcherText));
    }


    @Override
    public Vocabulary getVocabulary() {
      return MessageMatcherParser.VOCABULARY;
    }


    @Override
    public void notifyListeners(LexerNoViableAltException ex)
    {
      final Token token = getTokenFactory().create(_tokenFactorySourcePair, SKIP, null,
          _channel, _tokenStartCharIndex, _input.index(), _tokenStartLine,
          _tokenStartCharPositionInLine);

      getErrorListenerDispatch().syntaxError(this, token, _tokenStartLine,
          _tokenStartCharPositionInLine,
          "unexpected input at: " + getErrorDisplay(token.getText()), ex);
    }
  }




  private static final class Parser
      extends de.sayayi.lib.protocol.matcher.antlr.MessageMatcherParser
  {
    private Parser(@NotNull Lexer lexer)
    {
      super(new BufferedTokenStream(lexer));

      setErrorHandler(MessageMatcherErrorStrategy.INSTANCE);
    }


    @Override
    public Vocabulary getVocabulary() {
      return MessageMatcherParser.VOCABULARY;
    }
  }




  private final class Listener extends MessageMatcherBaseListener implements WalkerSupplier
  {
    @Override
    public @NotNull Walker getWalker() {
      return WALK_EXIT_RULES_HEAP;
    }


    @Override
    public void exitParseMatcher(ParseMatcherContext ctx) {
      ctx.matcher = ctx.compoundMatcher().matcher;
    }


    @Override
    public void exitParseTagSelector(ParseTagSelectorContext ctx) {
      ctx.selector = ctx.compoundTagSelector().selector.asTagSelector();
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
      final MessageMatcher.Junction expr = ctx.compoundMatcher().matcher;
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
      final TerminalNode qualifiedName = ctx.QUALIFIED_CLASS_NAME();
      if (qualifiedName == null)
        ctx.matcher = MessageMatchers.hasThrowable();
      else
      {
        final String qualifiedNameText = qualifiedName.getText();
        Class<?> clazz = null;

        try {
          clazz = classLoader == null
              ? Class.forName(qualifiedNameText)
              : Class.forName(qualifiedNameText, false, classLoader);
        } catch(ClassNotFoundException ignored) {
        }

        if (clazz == null || !Throwable.class.isAssignableFrom(clazz))
          syntaxError(qualifiedName, "class not found or not of type Throwable");

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
      final String paramName = ctx.string().str;

      ctx.matcher = ctx.HAS_PARAM() != null
          ? MessageMatchers.hasParam(paramName)
          : MessageMatchers.hasParamValue(paramName);
    }


    @Override
    public void exitLevelMatcher(LevelMatcherContext ctx)
    {
      final LevelSharedContext levelShared = ctx.levelShared();
      ctx.matcher = LevelMatcher.of(levelShared != null ? levelShared.lvl : ctx.level().lvl);
    }


    @Override
    public void exitBetweenMatcher(BetweenMatcherContext ctx)
    {
      final List<LevelContext> levels = ctx.level();
      ctx.matcher = MessageMatchers.between(levels.get(0).lvl, levels.get(1).lvl);
    }


    @Override
    public void exitMessageMatcher(MessageMatcherContext ctx) {
      ctx.matcher = MessageMatchers.hasMessage(ctx.string().str);
    }


    @Override
    public void exitInGroupMatcher(InGroupMatcherContext ctx)
    {
      final StringContext groupName = ctx.string();

      ctx.matcher = groupName == null
          ? MessageMatchers.inGroup()
          : ctx.IN_GROUP() != null ? inGroup(groupName.str) : inGroupRegex(groupName.str);
    }


    @Override
    public void exitInRootMatcher(InRootMatcherContext ctx) {
      ctx.matcher = MessageMatchers.inRoot();
    }


    @Override
    public void exitAndTagSelector(AndTagSelectorContext ctx)
    {
      ctx.selector = Conjunction.of(
          ctx.compoundTagSelector()
              .stream()
              .map(ec -> ec.selector)
              .toArray(MessageMatcher[]::new));
    }


    @Override
    public void exitOrTagSelector(OrTagSelectorContext ctx)
    {
      ctx.selector = Disjunction.of(
          ctx.compoundTagSelector()
              .stream()
              .map(ec -> ec.selector)
              .toArray(MessageMatcher[]::new));
    }


    @Override
    public void exitNotTagSelector(NotTagSelectorContext ctx)
    {
      final MessageMatcher.Junction expr = ctx.compoundTagSelector().selector;
      ctx.selector = ctx.NOT() != null ? Negation.of(expr) : expr;
    }


    @Override
    public void exitToTagSelector(ToTagSelectorContext ctx) {
      ctx.selector = ctx.tagSelectorAtom().selector.asJunction();
    }


    @Override
    public void exitTagSelectorAtom(TagSelectorAtomContext ctx)
    {
      final TagMatcherAtomContext tagExpression = ctx.tagMatcherAtom();

      ctx.selector = tagExpression != null ? tagExpression.matcher
          : ctx.ANY() != null ? BooleanMatcher.ANY : BooleanMatcher.NONE;
    }


    @Override
    public void exitTagMatcherAtom(TagMatcherAtomContext ctx)
    {
      final TagNameContext tagName = ctx.tagName();
      if (tagName != null)
        ctx.matcher = MessageMatchers.hasTag(tagName.tag);
      else
      {
        final List<String> tagNameList = ctx.tagNameList().tags;

        switch(((TerminalNode)ctx.getChild(0)).getSymbol().getType())
        {
          case ANY_OF:
            ctx.matcher = MessageMatchers.hasAnyOf(tagNameList);
            break;

          case ALL_OF:
            ctx.matcher = MessageMatchers.hasAllOf(tagNameList);
            break;

          case NONE_OF:
            ctx.matcher = MessageMatchers.hasNoneOf(tagNameList);
            break;
        }
      }
    }


    @Override
    public void exitTagNameList(TagNameListContext ctx) {
      ctx.tags = ctx.tagName().stream().map(tnc -> tnc.tag).collect(toList());
    }


    @Override
    public void exitTagName(TagNameContext ctx)
    {
      final StringContext stringContext = ctx.string();
      ctx.tag = stringContext != null ? stringContext.str : ctx.getChild(0).getText();
    }


    @Override
    public void exitLevel(LevelContext ctx)
    {
      final LevelSharedContext levelShared = ctx.levelShared();
      if (levelShared != null)
        ctx.lvl = levelShared.lvl;
      else
      {
        final StringContext stringContext = ctx.string();
        final String name = stringContext != null ? stringContext.str : ctx.getChild(0).getText();

        try {
          ctx.lvl = levelResolver == null ? null : levelResolver.apply(name);
        } catch(Exception ignored) {
        }

        if (ctx.lvl == null)
        {
          for(final Level.Shared level: Level.Shared.values())
            if (level.name().equalsIgnoreCase(name))
            {
              ctx.lvl = level;
              return;
            }

          syntaxError(ctx, "unknown level '" + name + "'");
        }
      }
    }


    @Override
    public void exitLevelShared(LevelSharedContext ctx) {
      ctx.lvl = Level.Shared.valueOf(ctx.getChild(0).getText().toUpperCase(ROOT));
    }


    @Override
    public void exitString(StringContext ctx)
    {
      final char[] str = ctx.STRING().getText().toCharArray();
      final StringBuilder s = new StringBuilder();
      char c;

      for(int i = 1, n = str.length - 1; i < n; i++)
      {
        if ((c = str[i]) == '\\')
          switch(c = str[++i])
          {
            case 'x':
              c = (char)(digit(str[i + 1], 16) * 16 + digit(str[i + 2], 16));
              i += 2;
              break;

            case 'u':
              c = (char)(digit(str[i + 1], 16) * 4096 + digit(str[i + 2], 16) * 256 +
                         digit(str[i + 3], 16) * 16 + digit(str[i + 4], 16));
              i += 4;
              break;
          }

        s.append(c);
      }

      ctx.str = s.toString();
    }
  }




  private static final Vocabulary VOCABULARY = new AbstractVocabulary() {
    @Override
    protected void addTokens()
    {
      add(ANY, "'any'", "ANY");
      add(NONE, "'none'", "NONE");
      add(NOT, "'not'", "NOT");
      add(THROWABLE, "'throwable'", "THROWABLE");
      add(TAG, "'tag'", "TAG");
      add(ANY_OF, "'any-of'", "ANY_OF");
      add(ALL_OF, "'all-of'", "ALL_OF");
      add(NONE_OF, "'none-of'", "NONE_OF");
      add(HAS_PARAM, "'has-param'", "HAS_PARAM");
      add(HAS_PARAM_VALUE, "'has-param-value'", "HAS_PARAM_VALUE");
      add(DEBUG, "'debug'", "DEBUG");
      add(INFO, "'info'", "INFO");
      add(WARN, "'warn'", "WARN");
      add(ERROR, "'error'", "ERROR");
      add(LEVEL, "'level'", "LEVEL");
      add(MESSAGE, "'message'", "MESSAGE");
      add(IN_GROUP, "'in-group'", "IN_GROUP");
      add(IN_GROUP_REGEX, "'in-group-regex'", "IN_GROUP_REGEX");
      add(IN_ROOT, "'in-root'", "IN_ROOT");
      add(AND, "'and'", "AND");
      add(OR, "'or'", "OR");
      add(L_PAREN, "'('", "L_PAREN");
      add(R_PAREN, "')'", "R_PAREN");
      add(COMMA, "','", "COMMA");
      add(STRING, "<string>", "STRING");
      add(QUALIFIED_CLASS_NAME, "<qualified class name>", "QUALIFIED_CLASS_NAME");
      add(IDENTIFIER, "<identifier>", "IDENTIFIER");
      add(WS, "' '", "WS");
    }
  };




  public static final class Service implements ProtocolMessageMatcher
  {
    @Override
    public @NotNull MessageMatcher parseMessageMatcher(@NotNull String messageMatcherExpression) {
      return INSTANCE.parseMessageMatcher(messageMatcherExpression);
    }


    @Override
    public @NotNull TagSelector parseTagSelector(@NotNull String tagSelectorExpression) {
      return INSTANCE.parseTagSelector(tagSelectorExpression);
    }
  }
}
