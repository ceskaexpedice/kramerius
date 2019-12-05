package cz.incad.kramerius.utils.pid;

import cz.incad.kramerius.utils.pid.Token.TokenType;


/**
 * @author pavels
 *
 
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Lexer {

	public static final int LOOK_AHEAD_DEPTH = 5;
	
	// char buffer with lookahead support
	private CharBuffer buffer = null;
	
	public Lexer(String inputString) throws LexerException {
		this.buffer = new CharBuffer(inputString, LOOK_AHEAD_DEPTH);
	}
	
	public Lexer(String inputString, int lookAhead) throws LexerException {
		this.buffer = new CharBuffer(inputString, lookAhead);
	}
	
	
	/**
	 * Returns char from given position
	 * @param pos position of char
	 * @return char 
	 * @throws SQLLexerException throws when the s
	 */	
	protected char charLookAhead(int charPosition) throws LexerException {
		char ch = (char)this.buffer.la(charPosition);
		return ch;
	}
	/**
	 * Returns real stream position of char
	 * @param position
	 * @return int
	 * @throws SQLLexerException
	 */
	protected int charPosition(int charPosition) throws LexerException {
		return this.buffer.position(charPosition);
	}
	
	/**
	 * Consume char and read new char into buffer
	 * @throws SQLLexerException
	 */
	protected void consumeChar() throws LexerException {
		this.buffer.consume();
	}
	
	/**
	 * Match char
	 * @param expectingChar
	 * @throws SQLLexerException
	 */
	protected void matchChar(char expectingChar) throws LexerException {
		if (charLookAhead(1) == expectingChar) {
			this.consumeChar();
		} else throw new LexerException("i am expecting '"+expectingChar+"' but got '"+charLookAhead(1)+"'");
	}
	
	

	protected Token matchALPHA() throws LexerException {
		int ch = this.buffer.la(1);
        this.consumeChar();
        return new Token(TokenType.ALPHA, ""+(char)ch);
	}
	
	
	
	public boolean hexDigitPostfix(char ch) {
		switch(ch) {
			case 'A':
			case 'B': 
			case 'C': 
			case 'D':
			case 'E': 
			case 'F':
			case 'a':
			case 'b': 
			case 'c': 
			case 'd':
			case 'e': 
			case 'f':
				return true;
			default:
				return false;
		}			
	}
	
	public Token matchHexDigit() throws LexerException {
		StringBuffer buffer = new StringBuffer();
		char ch = charLookAhead(1);
		if (!Character.isDigit(ch)) throw new LexerException("Expecting Digit !");
		buffer.append(ch);
		this.consumeChar();
		ch = charLookAhead(1);
		if (!hexDigitPostfix(ch)) throw new LexerException("Expecting 'A','B','C','D','E' or 'F' !");
		buffer.append(Character.toUpperCase(ch));
		this.consumeChar();
		return new Token(TokenType.HEXDIGIT, buffer.toString());
	}

	public Token matchDigit() throws LexerException {
		StringBuffer buffer = new StringBuffer();
		char ch = charLookAhead(1);
		if (!Character.isDigit(ch)) throw new LexerException("Expecting Digit !");
		buffer.append(ch);
		this.consumeChar();
		return new Token(TokenType.DIGIT, buffer.toString());
	}
	
	public void matchString(String str) throws LexerException {
		char[] chrs = str.toCharArray();
		for (int i = 0; i < chrs.length; i++) {
			matchChar(chrs[i]);
		}
	}
	
	public Token readToken() throws LexerException {
		char ch = charLookAhead(1);
		if (ch == 65535) return new Token(TokenType.EOI, "eoi");
		switch(ch) {
			case ':': {
				this.matchChar(':');
				return new Token(TokenType.DOUBLEDOT,":");
			}
			case '-': {
				this.matchChar('-');
				return new Token(TokenType.MINUS,"-");
			}
			case '~': {
				this.matchChar('~');
				return new Token(TokenType.TILDA,"~");
			}
			case '.': {
				this.matchChar('.');
				return new Token(TokenType.DOT,".");
			}
            case ' ': {
                this.matchChar(' ');
                return new Token(TokenType.SPACE," ");
            }
            case '\t': {
                this.matchChar('\t');
                return new Token(TokenType.TAB,"\t");
            }
            case '\n': {
                this.matchChar('\n');
                return new Token(TokenType.NEWLINE,"\n");
            }
            case '@': {
                this.matchChar('@');
                return new Token(TokenType.AT,"@");
            }
            case '/': {
                this.matchChar('/');
                return new Token(TokenType.DIV,"/");
            }
			case '%': {
				this.matchChar('%');
				if (Character.isDigit(charLookAhead(2)) && hexDigitPostfix(charLookAhead(3))) {
					return matchHexDigit();
				} else 
				return new Token(TokenType.PERCENT,"%");
			}
			case '_': {
				this.matchChar('_');
				return new Token(TokenType.UNDERSCOPE,"_");
			}
			case '(': {
                this.matchChar('(');
			    return new Token(TokenType.LPAREN,"(");
			}
			case (')') :{
                this.matchChar(')');
			    return new Token(TokenType.RPAREN,")");
			}
			default: {
				if (Character.isDigit(ch)) {
					return matchDigit();
				}
				return matchALPHA();
			}
			
		}
	}
	

	
	
	
	
}