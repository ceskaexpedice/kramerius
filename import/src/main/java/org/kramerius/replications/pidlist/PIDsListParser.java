// $ANTLR 2.7.7 (2006-11-01): "pidslist.g" -> "PIDsListParser.java"$

//    package cz.incad.kramerius.utils.params;
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

public class PIDsListParser extends antlr.LLkParser       implements PIDsListParserTokenTypes
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
		pidsKey();
		match(DOUBLEDOT);
		match(ARRAYL_BRACKET);
		{
		switch ( LA(1)) {
		case STRING_LITERAL:
		{
			pidsArray();
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
		match(CURLYR_BRACKET);
	}
	
	public final void pidsKey() throws RecognitionException, TokenStreamException {
		
		Token  s = null;
		Token  i = null;
		
		switch ( LA(1)) {
		case STRING_LITERAL:
		{
			s = LT(1);
			match(STRING_LITERAL);
			ident(s.getText());
			break;
		}
		case IDENT:
		{
			i = LT(1);
			match(IDENT);
			ident(i.getText());
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
	}
	
	public final void pidsArray() throws RecognitionException, TokenStreamException {
		
		Token  f = null;
		Token  r = null;
		
		f = LT(1);
		match(STRING_LITERAL);
		first(f.getText());
		{
		_loop6:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				r = LT(1);
				match(STRING_LITERAL);
				rest(r.getText());
			}
			else {
				break _loop6;
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
		"DOUBLEDOT",
		"ARRAYL_BRACKET",
		"ARRAYR_BRACKET",
		"CURLYR_BRACKET",
		"STRING_LITERAL",
		"IDENT",
		"COMMA",
		"L_BRACKET",
		"R_BRACKET",
		"EQUAL",
		"SL_COMMENT",
		"ML_COMMENT",
		"NEWLINE",
		"WS"
	};
	
	
	}
