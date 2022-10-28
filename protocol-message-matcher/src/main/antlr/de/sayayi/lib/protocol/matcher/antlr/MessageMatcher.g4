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
grammar MessageMatcher;


options {
  language = Java;
}


@header {
package de.sayayi.lib.protocol.matcher.antlr;

import de.sayayi.lib.protocol.matcher.MessageMatcher;
import de.sayayi.lib.protocol.Level;
import de.sayayi.lib.protocol.TagSelector;
}


// parser

parseMatcher returns [MessageMatcher matcher]
        : compoundMatcher EOF
        ;

parseTagSelector returns [TagSelector selector]
        : compoundTagSelector EOF
        ;

compoundMatcher returns [MessageMatcher.Junction matcher]
        : compoundMatcher AND compoundMatcher                                     #andMatcher
        | AND L_PAREN compoundMatcher ( COMMA compoundMatcher )+ R_PAREN          #andMatcher
        | compoundMatcher OR compoundMatcher                                      #orMatcher
        | OR L_PAREN compoundMatcher ( COMMA compoundMatcher )+ R_PAREN           #orMatcher
        | NOT? L_PAREN compoundMatcher R_PAREN                                    #notMatcher
        | matcherAtom                                                             #toMatcher
        ;

matcherAtom returns [MessageMatcher matcher]
        : ANY                                                                     #booleanMatcher
        | NONE                                                                    #booleanMatcher
        | THROWABLE ( L_PAREN QUALIFIED_CLASS_NAME R_PAREN )?                     #throwableMatcher
        | tagMatcherAtom                                                          #tagMatcher
        | HAS_PARAM L_PAREN string R_PAREN                                        #paramMatcher
        | HAS_PARAM_VALUE L_PAREN string R_PAREN                                  #paramMatcher
        | levelShared                                                             #levelMatcher
        | LEVEL L_PAREN level R_PAREN                                             #levelMatcher
        | BETWEEN L_PAREN level COMMA level R_PAREN                               #betweenMatcher
        | MESSAGE L_PAREN string R_PAREN                                          #messageMatcher
        | IN_GROUP ( L_PAREN string R_PAREN )?                                    #inGroupMatcher
        | IN_GROUP_REGEX L_PAREN string R_PAREN                                   #inGroupMatcher
        | IN_ROOT                                                                 #inRootMatcher
        ;

compoundTagSelector returns [MessageMatcher.Junction selector]
        : compoundTagSelector AND compoundTagSelector                             #andTagSelector
        | AND L_PAREN compoundTagSelector ( COMMA compoundTagSelector )+ R_PAREN  #andTagSelector
        | compoundTagSelector OR compoundTagSelector                              #orTagSelector
        | OR L_PAREN compoundTagSelector ( COMMA compoundTagSelector )+ R_PAREN   #orTagSelector
        | NOT? L_PAREN compoundTagSelector R_PAREN                                #notTagSelector
        | tagSelectorAtom                                                         #toTagSelector
        ;

tagSelectorAtom returns [MessageMatcher selector]
        : ANY
        | NONE
        | tagMatcherAtom
        ;

tagMatcherAtom returns [MessageMatcher matcher]
        : TAG L_PAREN tagName R_PAREN
        | tagName
        | ANY_OF L_PAREN tagNameList R_PAREN
        | ALL_OF L_PAREN tagNameList R_PAREN
        | NONE_OF L_PAREN tagNameList R_PAREN
        ;

tagNameList returns [List<String> tags]
        : tagName ( COMMA tagName )+
        ;

tagName returns [String tag]
        : string
        | IDENTIFIER
        ;

level returns [Level lvl]
        : levelShared
        | string
        | IDENTIFIER
        ;

levelShared returns [Level.Shared lvl]
        : DEBUG
        | INFO
        | WARN
        | ERROR
        ;

string returns [String str]
        : STRING
        ;


// lexer

ANY
        : 'any'
        ;
NONE
        : 'none'
        ;
NOT
        : 'not'
        ;
THROWABLE
        : 'throwable'
        ;
TAG
        : 'tag'
        ;
ANY_OF
        : 'any-of'
        ;
ALL_OF
        : 'all-of'
        ;
NONE_OF
        : 'none-of'
        ;
HAS_PARAM
        : 'has-param'
        ;
HAS_PARAM_VALUE
        : 'has-param-value'
        ;
DEBUG
        : 'debug'
        ;
INFO
        : 'info'
        ;
WARN
        : 'warn'
        ;
ERROR
        : 'error'
        ;
LEVEL
        : 'level'
        ;
BETWEEN
        : 'between'
        ;
MESSAGE
        : 'message'
        ;
IN_GROUP
        : 'in-group'
        ;
IN_GROUP_REGEX
        : 'in-group-regex'
        ;
IN_ROOT
        : 'in-root'
        ;
AND
        : 'and'
        ;
OR
        : 'or'
        ;
L_PAREN
        : '('
        ;
R_PAREN
        : ')'
        ;
COMMA
        : ','
        ;
STRING
        : '\'' (~['\\] | EscapeSequence)* '\''
        | '"' (~["\\] | EscapeSequence)* '"'
        ;
QUALIFIED_CLASS_NAME
        : PackageOrClassName ('.' PackageOrClassName)+
        ;
IDENTIFIER
        : Letter ('-' | LetterOrDigit+)*
        ;
WS
        : ' '+ -> skip
        ;

fragment PackageOrClassName
        : Letter LetterOrDigit*
        ;

fragment LetterOrDigit
        : Letter
        | [0-9]
        ;

fragment Letter
        : [a-zA-Z$_]                      // these are the "identifier letters" below 0x7F
        | ~[\u0000-\u007F\uD800-\uDBFF]   // covers all characters above 0x7F which are not a surrogate
        ;

fragment EscapeSequence
        : '\\' ["'\\]
        | '\\x' Hex Hex
        | '\\u' Hex Hex Hex Hex
        ;

fragment Hex
        : [0-9a-fA-F]
        ;