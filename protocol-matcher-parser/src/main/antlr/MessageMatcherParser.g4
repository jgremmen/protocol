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
  import de.sayayi.lib.protocol.TagSelector;
}


parseMatcher returns [MessageMatcher.Junction matcher]
        : compoundMatcher EOF
        ;

parseTagSelector returns [TagSelector selector]
        : compoundTagSelector EOF
        ;

compoundMatcher returns [MessageMatcher.Junction matcher]
        : compoundMatcher AND compoundMatcher                             #andMatcher
        | AND L_PAREN compoundMatcher ( COMMA compoundMatcher )+ R_PAREN  #andMatcher
        | compoundMatcher OR compoundMatcher                              #orMatcher
        | OR L_PAREN compoundMatcher ( COMMA compoundMatcher )+ R_PAREN   #orMatcher
        | NOT? L_PAREN compoundMatcher R_PAREN                            #notMatcher
        | matcherAtom                                                     #toMatcher
        ;

matcherAtom returns [MessageMatcher matcher]
        : ANY                                                             #booleanMatcher
        | NONE                                                            #booleanMatcher
        | THROWABLE ( L_PAREN QUALIFIED_NAME R_PAREN )?                   #throwableMatcher
        | tagMatcherAtom                                                  #tagsMatcher
        | HAS_PARAM L_PAREN string R_PAREN                                #paramMatcher
        | HAS_PARAM_VALUE L_PAREN string R_PAREN                          #paramMatcher
        | DEBUG                                                           #levelMatcher
        | INFO                                                            #levelMatcher
        | WARN                                                            #levelMatcher
        | ERROR                                                           #levelMatcher
        | LEVEL L_PAREN level R_PAREN                                     #levelMatcher
        | MESSAGE L_PAREN string R_PAREN                                  #messageMatcher
        | IN_GROUP L_PAREN string R_PAREN                                 #inGroupMatcher
        | IN_GROUP_REGEX L_PAREN string R_PAREN                           #inGroupMatcher
        | IN_GROUP                                                        #depthMatcher
        | IN_ROOT                                                         #depthMatcher
        ;

compoundTagSelector returns [MessageMatcher.Junction matcher]
        : compoundTagSelector AND compoundTagSelector                             #andTagSelector
        | AND L_PAREN compoundTagSelector ( COMMA compoundTagSelector )+ R_PAREN  #andTagSelector
        | compoundTagSelector OR compoundTagSelector                              #orTagSelector
        | OR L_PAREN compoundTagSelector ( COMMA compoundTagSelector )+ R_PAREN   #orTagSelector
        | NOT? L_PAREN compoundTagSelector R_PAREN                                #notTagSelector
        | tagSelectorAtom                                                         #toTagSelector
        ;

tagSelectorAtom returns [MessageMatcher matcher]
        : ANY
        | NONE
        | tagMatcherAtom
        ;

tagMatcherAtom returns [MessageMatcher matcher]
        : TAG L_PAREN tagName R_PAREN
        | ANY_OF L_PAREN tagNameList R_PAREN
        | ALL_OF L_PAREN tagNameList R_PAREN
        | NONE_OF L_PAREN tagNameList R_PAREN
        ;

tagNameList returns [List<String> tags]
        : tagName ( COMMA tagName )*
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