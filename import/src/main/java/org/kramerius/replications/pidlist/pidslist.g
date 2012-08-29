header {
    package cz.incad.kramerius.replications.pidslist;
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
    
    public PidsListCollect getPidsListCollect() {
        return this.pidListCollect;
    }
    
    public void setPidsListCollect(PidsListCollect col) {
        this.pidListCollect = col;
    }
    
    public void ident(String i) throws RecognitionException {
        if ((i.startsWith("'"))  || (i.startsWith("\""))) {
            i = i.substring(1,i.length()-1);
        }
        
        if (!i.trim().toLowerCase().equals("pids")) {
            throw new RecognitionException("expecting pids");
        }
    }    

    public void first(String f) {
        if (this.pidListCollect != null)
            this.pidListCollect.pidEmitted(f);
    } 
    
    public void rest(String r) {
        if (this.pidListCollect != null)
            this.pidListCollect.pidEmitted(r);
    }   
}


pids: CURLYL_BRACKET pidsKey DOUBLEDOT  ARRAYL_BRACKET (pidsArray)?  ARRAYR_BRACKET CURLYR_BRACKET;

pidsKey : s:STRING_LITERAL {ident(s.getText());} | i:IDENT {ident(i.getText());};

pidsArray : f:STRING_LITERAL {first(f.getText());} (COMMA r:STRING_LITERAL {rest(r.getText());})*;

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