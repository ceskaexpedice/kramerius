package cz.incad.kramerius.utils.pid;

public class Token {

	private TokenType type;
	private String value;
	
	
	public Token(TokenType type, String value) {
		super();
		this.type = type;
		this.value = value;
	}


	
	
	public TokenType getType() {
		return type;
	}




	public String getValue() {
		return value;
	}




	public enum TokenType {
		ALPHA, DIGIT, HEXDIGIT, PERCENT, DOT, DOUBLEDOT, MINUS, TILDA, EOI, UNDERSCOPE,LPAREN,RPAREN, SPACE, TAB, NEWLINE, AT, DIV
	}
}
