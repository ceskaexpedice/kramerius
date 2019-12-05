/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Grammar for params parsing 
 */
header {
    package cz.incad.kramerius.utils.params;
    import java.util.*;
}

  

class ParamsParser extends Parser;
{
}

params returns [List prms]
{prms=new java.util.ArrayList(); 
    String pr1 = null; 
    List lpr1 = null;
    String pr2=null;
    List lpr2=null;
}
: 
LPAREN  ( pr1=param {prms.add(pr1); } | lpr1 = params {prms.add(lpr1);} ) ?

(SEMI 
  (pr2=param {prms.add(pr2); } | lpr2 = params {prms.add(lpr2);})
)* 
 RPAREN
;

param returns [String pk1]
{ pk1=null;}
: pk1=paramkey  
;

paramkey returns[String pk]
{pk = null;}
: (a:ANY { pk = a.getText();})+
;


class ParamsLexer extends Lexer;

options {
    charVocabulary='\u0000'..'\uFFFE';
}

SEMI:   ';'
    ;

LPAREN:'{'
;

RPAREN:'}'
;

DOUBLEDOT:':'
;

ANY
    :   (ESC|~(';'|'\\'|'{'|'}'|':'))+ 
    ;

    
protected
ESC
    :   '\\'
        (   ';' {$setText(";");}
        |   '{' {$setText("{");}
        |   '}' {$setText("}");}
        |   ':' {$setText(":");}
        |   '\\' {$setText("\\");}
        )
    ;
    