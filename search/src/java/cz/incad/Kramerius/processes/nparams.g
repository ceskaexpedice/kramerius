header {
//    package cz.incad.kramerius.utils.params;
    package cz.incad.Kramerius.processes;
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
LPAREN  ( pr1=param {prms.add(pr1); } | lpr1 = params {prms.add(lpr1);} )

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
        )
    ;
    