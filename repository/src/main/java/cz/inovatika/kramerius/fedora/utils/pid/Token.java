package cz.inovatika.kramerius.fedora.utils.pid;

class Token {

	private TokenType type;
	private String value;

	Token(TokenType type, String value) {
		super();
		this.type = type;
		this.value = value;
	}

	TokenType getType() {
		return type;
	}

	String getValue() {
		return value;
	}

	enum TokenType {
		ALPHA, DIGIT, HEXDIGIT, PERCENT, DOT, DOUBLEDOT, MINUS, TILDA, EOI, UNDERSCOPE,LPAREN,RPAREN, SPACE, TAB, NEWLINE, AT, DIV
	}
}
