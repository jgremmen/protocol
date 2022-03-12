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
parser grammar MessageMatcherParser;


options {
    language = Java;
    tokenVocab = MessageMatcherLexer;
}


@header {
import de.sayayi.lib.protocol.matcher.MessageMatcher;
import de.sayayi.lib.protocol.Level;
}


parseMatcher returns [MessageMatcher.Junction matcher]
        : parExpression EOF
        ;

parExpression returns [MessageMatcher.Junction matcher]
        : parExpression AND parExpression
        | parExpression OR parExpression
        | L_PAREN parExpression R_PAREN
        | expression
        ;

expression returns [MessageMatcher.Junction matcher]
        : AND parExpressionList                          #andExpression
        | OR parExpressionList                           #orExpression
        | NOT L_PAREN parExpression R_PAREN              #notExpression
        | ANY                                            #booleanExpression
        | NONE                                           #booleanExpression
        | THROWABLE ( L_PAREN QUALIFIED_NAME R_PAREN )?  #throwableExpression
        | TAG L_PAREN tagName R_PAREN                    #tagsExpression
        | ANY_OF parTagNames                             #tagsExpression
        | ALL_OF parTagNames                             #tagsExpression
        | NONE_OF parTagNames                            #tagsExpression
        | HAS_PARAM parString                            #paramExpression
        | HAS_PARAM_VALUE parString                      #paramExpression
        | DEBUG                                          #levelExpression
        | INFO                                           #levelExpression
        | WARN                                           #levelExpression
        | ERROR                                          #levelExpression
        | LEVEL L_PAREN level R_PAREN                    #levelExpression
        | MESSAGE parString                              #messageExpression
        | IN_GROUP parString                             #inGroupExpression
        | IN_GROUP_REGEX parString                       #inGroupExpression
        | IN_GROUP                                       #depthExpression
        | IN_ROOT                                        #depthExpression
        ;

parExpressionList returns [List<MessageMatcher.Junction> matchers]
        : L_PAREN parExpression ( COMMA parExpression )* R_PAREN
        ;

parTagNames returns [List<String> tags]
        : L_PAREN tagName ( COMMA tagName )* R_PAREN
        ;

parString returns [String str]
        : L_PAREN string R_PAREN
        ;

tagName returns [String tag]
        : string
        | IDENTIFIER
        ;

level returns [Level lvl]
        : string
        | IDENTIFIER
        ;

string returns [String str]
        : STRING
        ;