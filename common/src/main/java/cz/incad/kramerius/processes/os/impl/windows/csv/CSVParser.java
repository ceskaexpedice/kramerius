// $ANTLR 2.7.7 (2006-11-01): "csv.g" -> "CSVParser.java"$

	package cz.incad.kramerius.processes.os.impl.windows.csv;
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

public class CSVParser extends antlr.LLkParser       implements CSVParserTokenTypes
 {

protected CSVParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public CSVParser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected CSVParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public CSVParser(TokenStream lexer) {
  this(lexer,1);
}

public CSVParser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
}

	public final List  file() throws RecognitionException, TokenStreamException {
		List file;
		
		
					file = new ArrayList();
					List l=null; 		
				
		
		try {      // for error handling
			{
			_loop3:
			do {
				if ((LA(1)==NEWLINE)) {
					match(NEWLINE);
				}
				else {
					break _loop3;
				}
				
			} while (true);
			}
			{
			l=line();
			file.add(l);
			{
			_loop7:
			do {
				if ((LA(1)==NEWLINE)) {
					match(NEWLINE);
					{
					switch ( LA(1)) {
					case RECORD:
					{
						l=line();
						file.add(l);
						break;
					}
					case EOF:
					case NEWLINE:
					{
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
					break _loop7;
				}
				
			} while (true);
			}
			match(Token.EOF_TYPE);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
		return file;
	}
	
	public final List  line() throws RecognitionException, TokenStreamException {
		List line;
		
		
					line = new ArrayList(); 
					String rec = null;
				
		
		try {      // for error handling
			{
			{
			int _cnt11=0;
			_loop11:
			do {
				if ((LA(1)==RECORD)) {
					rec=record();
					line.add(rec);
				}
				else {
					if ( _cnt11>=1 ) { break _loop11; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt11++;
			} while (true);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_1);
		}
		return line;
	}
	
	public final String  record() throws RecognitionException, TokenStreamException {
		String rec;
		
		Token  r = null;
		
					rec = null;
				
		
		try {      // for error handling
			{
			{
			r = LT(1);
			match(RECORD);
			
						rec = r.getText();
					
			}
			{
			switch ( LA(1)) {
			case COMMA:
			{
				match(COMMA);
				break;
			}
			case EOF:
			case NEWLINE:
			case RECORD:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return rec;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"NEWLINE",
		"RECORD",
		"COMMA",
		"WS"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 18L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 50L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	
	}
