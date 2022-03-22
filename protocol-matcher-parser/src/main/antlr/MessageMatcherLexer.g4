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
lexer grammar MessageMatcherLexer;


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
        : IDENTIFIER ('.' IDENTIFIER)+
        ;

IDENTIFIER
        : Letter LetterOrDigit*
        ;

WS
        : ' '+ -> skip
        ;

fragment LetterOrDigit
        : Letter
        | [0-9]
        ;

fragment Letter
        : [a-zA-Z$_]                      // these are the "identifier letters" below 0x7F
        | ~[\u0000-\u007F\uD800-\uDBFF]   // covers all characters above 0x7F which are not a surrogate
        | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        ;

fragment EscapeSequence
        : '\\' ["'\\]
        | '\\x' HexDigit HexDigit
        | '\\u' HexDigit HexDigit HexDigit HexDigit
        ;

fragment HexDigit
        : [0-9a-fA-F]
        ;