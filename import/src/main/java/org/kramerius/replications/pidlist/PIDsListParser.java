// $ANTLR 2.7.7 (2006-11-01): "pidslist.g" -> "PIDsListParser.java"$

    package org.kramerius.replications.pidlist;
    import java.util.*;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;

/**
* JSON replication pids parser which can handle big data
*/
public class PIDsListParser extends antlr.LLkParser       implements PIDsListParserTokenTypes
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

protected PIDsListParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public PIDsListParser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected PIDsListParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public PIDsListParser(TokenStream lexer) {
  this(lexer,1);
}

public PIDsListParser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
}

    public final void pids() throws RecognitionException, TokenStreamException {
        
        
        match(CURLYL_BRACKET);
        itemDef();
        {
        _loop3:
        do {
            if ((LA(1)==COMMA)) {
                match(COMMA);
                itemDef();
            }
            else {
                break _loop3;
            }
            
        } while (true);
        }
        match(CURLYR_BRACKET);
        match(Token.EOF_TYPE);
    }
    
    public final void itemDef() throws RecognitionException, TokenStreamException {
        
        
        keyDef();
        {
        switch ( LA(1)) {
        case ARRAYL_BRACKET:
        {
            arrayDef();
            break;
        }
        case STRING_LITERAL:
        {
            valueDef();
            break;
        }
        default:
        {
            throw new NoViableAltException(LT(1), getFilename());
        }
        }
        }
    }
    
    public final void keyDef() throws RecognitionException, TokenStreamException {
        
        Token  s = null;
        Token  i = null;
        
        {
        switch ( LA(1)) {
        case STRING_LITERAL:
        {
            s = LT(1);
            match(STRING_LITERAL);
            if ( inputState.guessing==0 ) {
                key(s.getText());
            }
            break;
        }
        case IDENT:
        {
            i = LT(1);
            match(IDENT);
            if ( inputState.guessing==0 ) {
                key(i.getText());
            }
            break;
        }
        default:
        {
            throw new NoViableAltException(LT(1), getFilename());
        }
        }
        }
        match(DOUBLEDOT);
    }
    
    public final void arrayDef() throws RecognitionException, TokenStreamException {
        
        
        match(ARRAYL_BRACKET);
        if ( inputState.guessing==0 ) {
            startOfArray();
        }
        {
        switch ( LA(1)) {
        case STRING_LITERAL:
        {
            arrayVals();
            break;
        }
        case ARRAYR_BRACKET:
        {
            break;
        }
        default:
        {
            throw new NoViableAltException(LT(1), getFilename());
        }
        }
        }
        match(ARRAYR_BRACKET);
        if ( inputState.guessing==0 ) {
            endOfArray();
        }
    }
    
    public final void valueDef() throws RecognitionException, TokenStreamException {
        
        Token  s = null;
        
        s = LT(1);
        match(STRING_LITERAL);
        if ( inputState.guessing==0 ) {
            value(s.getText());
        }
    }
    
    public final void arrayVals() throws RecognitionException, TokenStreamException {
        
        Token  f = null;
        Token  r = null;
        
        f = LT(1);
        match(STRING_LITERAL);
        if ( inputState.guessing==0 ) {
            first(f.getText());
        }
        {
        _loop14:
        do {
            if ((LA(1)==COMMA)) {
                match(COMMA);
                r = LT(1);
                match(STRING_LITERAL);
                if ( inputState.guessing==0 ) {
                    rest(r.getText());
                }
            }
            else {
                break _loop14;
            }
            
        } while (true);
        }
    }
    
    
    public static final String[] _tokenNames = {
        "<0>",
        "EOF",
        "<2>",
        "NULL_TREE_LOOKAHEAD",
        "CURLYL_BRACKET",
        "COMMA",
        "CURLYR_BRACKET",
        "ARRAYL_BRACKET",
        "STRING_LITERAL",
        "IDENT",
        "DOUBLEDOT",
        "ARRAYR_BRACKET",
        "L_BRACKET",
        "R_BRACKET",
        "EQUAL",
        "SL_COMMENT",
        "ML_COMMENT",
        "NEWLINE",
        "WS"
    };
    
    
    }
