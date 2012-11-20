// $ANTLR 2.7.7 (2006-11-01): "nparams.g" -> "ParamsParser.java"$

    package cz.incad.kramerius.utils.params;
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

public class ParamsParser extends antlr.LLkParser       implements ParamsParserTokenTypes
 {


protected ParamsParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public ParamsParser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected ParamsParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public ParamsParser(TokenStream lexer) {
  this(lexer,1);
}

public ParamsParser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
}

	public final List  params() throws RecognitionException, TokenStreamException {
		List prms;
		
		prms=new java.util.ArrayList(); 
		String pr1 = null; 
		List lpr1 = null;
		String pr2=null;
		List lpr2=null;
		
		
		try {      // for error handling
			match(LPAREN);
			{
			switch ( LA(1)) {
			case ANY:
			{
				pr1=param();
				prms.add(pr1);
				break;
			}
			case LPAREN:
			{
				lpr1=params();
				prms.add(lpr1);
				break;
			}
			case SEMI:
			case RPAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			_loop5:
			do {
				if ((LA(1)==SEMI)) {
					match(SEMI);
					{
					switch ( LA(1)) {
					case ANY:
					{
						pr2=param();
						prms.add(pr2);
						break;
					}
					case LPAREN:
					{
						lpr2=params();
						prms.add(lpr2);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
				}
				else {
					break _loop5;
				}
				
			} while (true);
			}
			match(RPAREN);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
		return prms;
	}
	
	public final String  param() throws RecognitionException, TokenStreamException {
		String pk1;
		
		pk1=null;
		
		try {      // for error handling
			pk1=paramkey();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
		return pk1;
	}
	
	public final String  paramkey() throws RecognitionException, TokenStreamException {
		String pk;
		
		Token  a = null;
		pk = null;
		
		try {      // for error handling
			{
			int _cnt9=0;
			_loop9:
			do {
				if ((LA(1)==ANY)) {
					a = LT(1);
					match(ANY);
					pk = a.getText();
				}
				else {
					if ( _cnt9>=1 ) { break _loop9; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt9++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
		return pk;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"LPAREN",
		"SEMI",
		"RPAREN",
		"ANY",
		"DOUBLEDOT",
		"ESC"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 96L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	
	}
