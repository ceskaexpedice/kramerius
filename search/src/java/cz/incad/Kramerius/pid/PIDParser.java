package cz.incad.Kramerius.pid;

import java.util.Arrays;
import java.util.List;

import cz.incad.Kramerius.pid.Token.TokenType;


/**
 * Simple parser for PID 
 * @see EBNF http://www.fedora-commons.org/confluence/display/FCR30/Fedora+Identifiers
 * @author pavels
 *
 */
public class PIDParser {

	private Lexer lexer;
	public String getObjectId() {
		return objectId;
	}


	public String getNamespaceId() {
		return namespaceId;
	}



	private Token token;

	private String objectId;
	private String namespaceId;
		
	public PIDParser(String sform) throws LexerException {
		super();
		this.lexer = new Lexer(sform);
	}
	

	public void objectPid() throws LexerException {
		this.consume();
		String namespaceId = namespaceId();
		matchDoubleDot();
		String objectId = objectId();
		
		this.namespaceId = namespaceId;
		this.objectId = objectId;
	}

	public void disseminationURI() throws LexerException {
		this.lexer.matchString("info:fedora/");
		this.objectPid();
	}

	
	public void matchDoubleDot() throws LexerException {
		if (this.token.getType() == TokenType.DOUBLEDOT) {
			matchToken(TokenType.DOUBLEDOT);
		} else if (this.token.getType() == TokenType.PERCENT) {
			this.consume();
			if (this.token.getType() == TokenType.HEXDIGIT) {
				String value = this.token.getValue();
				if (value.equals("3A")) {
					this.consume();
				} else throw new LexerException("Expecting 3A");
			} else throw new LexerException("Expecting %"); 
		} else throw new LexerException("%3A");
	}

	private void matchToken(TokenType doubledot) throws LexerException {
		if (this.token.getType() == doubledot) {
			this.consume();
		} else throw new LexerException("Expecting ':'");
	}


	private String namespaceId() throws LexerException {
		List<TokenType> types = Arrays.asList(new TokenType[] {
				TokenType.ALPHA,
				TokenType.DIGIT,
				TokenType.MINUS,
				TokenType.DOT
		});
		StringBuffer buffer = new StringBuffer();
		if (!types.contains(this.token.getType())) throw new LexerException("expecting ALPHA, DIGIT, MINUS or DOT");
		while(types.contains(this.token.getType())) {
			buffer.append(token.getValue());
			this.consume();
			
		}
		return buffer.toString();
	}

	public void consume() throws LexerException {
		this.token = this.lexer.readToken();
	}
	

	private String objectId() throws LexerException {
		
		List<TokenType> types = Arrays.asList(new TokenType[] {
				TokenType.ALPHA,
				TokenType.DIGIT,
				TokenType.MINUS,
				TokenType.DOT,
				TokenType.TILDA,
				TokenType.UNDERSCOPE,
				TokenType.PERCENT,
				TokenType.HEXDIGIT
		});
		StringBuffer buffer = new StringBuffer();
		if (!types.contains(this.token.getType())) throw new LexerException("expecting ALPHA, DIGIT, MINUS or DOT");
		while(types.contains(this.token.getType())) {
			buffer.append(token.getValue());
			if (token.getType() == TokenType.PERCENT) {
				this.consume();
				while(token.getType().equals(TokenType.HEXDIGIT)) {
					buffer.append(token.getValue());
					this.consume();
				}
			} else {
				this.consume();
			}
		}
		return buffer.toString();
	}
	
}
