package de.sayayi.lib.protocol.matcher;

import org.antlr.v4.runtime.Vocabulary;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.SortedMap;
import java.util.TreeMap;

import static de.sayayi.lib.protocol.matcher.MessageMatcherLexer.*;
import static lombok.AccessLevel.PRIVATE;


/**
 * @author Jeroen Gremmen
 * @since 1.2.0
 */
@NoArgsConstructor(access = PRIVATE)
enum MatcherVocabulary implements Vocabulary
{
  INSTANCE;


  private static final SortedMap<Integer,Name> VOCABULARY = new TreeMap<>();


  static
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
    add(QUALIFIED_NAME, "<qualified name>", "QUALIFIED_NAME");
    add(IDENTIFIER, "<identifier>", "IDENTIFIER");
    add(WS, "' '", "WS");
  }


  @Override
  public int getMaxTokenType() {
    return VOCABULARY.lastKey();
  }


  @Override
  public String getLiteralName(int tokenType) {
    return VOCABULARY.containsKey(tokenType) ? VOCABULARY.get(tokenType).literal : null;
  }


  @Override
  public String getSymbolicName(int tokenType) {
    return VOCABULARY.containsKey(tokenType) ? VOCABULARY.get(tokenType).symbol : null;
  }


  @Override
  public String getDisplayName(int tokenType) {
    return !VOCABULARY.containsKey(tokenType) ? Integer.toString(tokenType) : VOCABULARY.get(tokenType).literal;
  }


  private static void add(int tokenType, String literal, String symbolic) {
    VOCABULARY.put(tokenType, new Name(literal, symbolic));
  }




  @AllArgsConstructor(access = PRIVATE)
  private static final class Name
  {
    final String literal;
    final String symbol;
  }
}