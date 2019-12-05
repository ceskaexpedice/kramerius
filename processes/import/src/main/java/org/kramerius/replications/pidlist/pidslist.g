header {
    package org.kramerius.replications.pidlist;
    import java.util.*;
}  

/**
* JSON replication pids parser which can handle big data
*/
class PIDsListParser extends Parser;
options{
    defaultErrorHandler=false;
}

{
    
    private PidsListCollect pidListCollect;

    private boolean pidsCollecting = false;
    private boolean pathsCollecting = false;

    public PidsListCollect getPidsListCollect() {
        return this.pidListCollect;
    }
    
    public void setPidsListCollect(PidsListCollect col) {
        this.pidListCollect = col;
    }
    
    public void key(String k) {
        if ( (k.startsWith("'")) || (k.startsWith("\"")) ) {
            k = k.substring(1,k.length()-1);
        }
        if (k.equals("pids")) { pidsCollecting = true; pathsCollecting = false; }
        else if (k.equals("paths")) { pidsCollecting = false; pathsCollecting = true; }
    }

    public void startOfArray() {}

    public void endOfArray() {
        if (pidsCollecting) pidsCollecting = false;
        if (pathsCollecting) pathsCollecting = false;
    }

    public void value(String i) {
        /*
        if (pathsCollecting) {
            if (this.pidListCollect != null)
                this.pidListCollect.pathEmitted(i);
        } else if (
        */
    }

    public void first(String f) {
        if (pidsCollecting) {
            if (this.pidListCollect != null)
                this.pidListCollect.pidEmitted(f);
        } else if (pathsCollecting) {
            if (this.pidListCollect != null)
                this.pidListCollect.pathEmitted(f);
        }
    } 

    public void rest(String r) {
        if (pidsCollecting) {
            if (this.pidListCollect != null)
                this.pidListCollect.pidEmitted(r);
        } else if (pathsCollecting) {
            if (this.pidListCollect != null)
                this.pidListCollect.pathEmitted(r);
        }
    }   
}

pids: CURLYL_BRACKET itemDef (COMMA itemDef)*  CURLYR_BRACKET EOF;

itemDef : keyDef ((ARRAYL_BRACKET) => arrayDef | valueDef);

keyDef:  (s:STRING_LITERAL { key(s.getText()); } | i:IDENT { key(i.getText());} ) DOUBLEDOT;

arrayDef: ARRAYL_BRACKET { startOfArray(); } (arrayVals)? ARRAYR_BRACKET { endOfArray(); } ;

arrayVals : f:STRING_LITERAL {first(f.getText());} (COMMA r:STRING_LITERAL {rest(r.getText());})*;

valueDef: s:STRING_LITERAL { value(s.getText()); };


class PIDsListLexer extends Lexer;
options{
        charVocabulary='\u0003'..'\u7FFE';
}

L_BRACKET: '(';
R_BRACKET: ')';

CURLYL_BRACKET:'{';
CURLYR_BRACKET:'}';

ARRAYL_BRACKET:'[';
ARRAYR_BRACKET:']';

COMMA: ',';


EQUAL    : '=';

DOUBLEDOT : ':'
;


IDENT    : (~('('|'='|')'|','|'\r'|'\n'|'\t'|' '|'{'|'}'|'"'|'\''|'/'|':'|'['|']'))+;

STRING_LITERAL
    :   '"' (~('"'|'\\'|'\n'|'\r'|'\''))* '"' | 
       '\'' (~('"'|'\\'|'\n'|'\r'|'\''))* '\''  
    ;

protected SL_COMMENT
    :   "//"
        (~('\n'|'\r'))* ('\n'|'\r'('\n')?)?
    ;


protected ML_COMMENT
    :   "/*"
        (   
         options {
                generateAmbigWarnings=false;
            }
        :
            { LA(2)!='/' }? '*'
        |   '\r' '\n'       {newline();}
        |   '\r'            {newline();}
        |   '\n'            {newline();}
        |   ~('*'|'\n'|'\r')
        )*
        "*/"
    ;




NEWLINE : ('\r''\n')=> '\r''\n' //DOS
        | '\r'                  //MAC
        | '\n'                  //UNIX
        { newline();  $setType(Token.SKIP);}
        ;
WS      : (' '|'\t') { $setType(Token.SKIP); } ;