// $ANTLR 2.7.7 (2006-11-01): "shibrules.g" -> "ShibRuleParser.java"$

    package cz.incad.kramerius.auth.thirdparty.shibb.rules;
    
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
import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.AttributeValue;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.Expr;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.ExpressionValue;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.ExpressionsBody;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.HeaderValue;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.MatchRule;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.PrincipalValue;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.RoleExpr;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.ShibRules;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.StringValue;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.UserExpr;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.Value;

public class ShibRuleParser extends antlr.LLkParser       implements ShibRuleParserTokenTypes
 {

    String getStringVal(String val) {
        if ( val.startsWith("\"") && (val.endsWith("\"")) ) {
            return val.substring(1,val.length()-1);
        } else return val;
    }

protected ShibRuleParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public ShibRuleParser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected ShibRuleParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public ShibRuleParser(TokenStream lexer) {
  this(lexer,1);
}

public ShibRuleParser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
}

	public final ShibRules  shibRules() throws RecognitionException, TokenStreamException {
		ShibRules rules;
		
		rules = new ShibRules(); 
		MatchRule rule;
		
		try {      // for error handling
			{
			int _cnt3=0;
			_loop3:
			do {
				if ((LA(1)==MATCH_KWD)) {
					rule=matchRule();
					rules.addRule(rule);
				}
				else {
					if ( _cnt3>=1 ) { break _loop3; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt3++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
		return rules;
	}
	
	public final MatchRule  matchRule() throws RecognitionException, TokenStreamException {
		MatchRule rule;
		
		rule = null;  ExpressionsBody b;
		
		try {      // for error handling
			match(MATCH_KWD);
			rule=condition();
			b=body();
			rule.setBody(b);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_1);
		}
		return rule;
	}
	
	public final MatchRule  condition() throws RecognitionException, TokenStreamException {
		MatchRule rule;
		
		Token  re = null;
		rule=new MatchRule(); Value l,r;
		
		try {      // for error handling
			match(L_BRACKET);
			l=value();
			rule.setLeftOperand(l);
			match(COMMA);
			{
			switch ( LA(1)) {
			case STRING_LITERAL:
			case HEADER_KWD:
			case ATTRIBUTE_KWD:
			case PRINCIPAL_KWD:
			{
				r=value();
				rule.setRightOperand(r);
				break;
			}
			case REGEXP_LITERAL:
			{
				re = LT(1);
				match(REGEXP_LITERAL);
				rule.setRightOperand(new ExpressionValue(re.getText()));
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(R_BRACKET);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return rule;
	}
	
	public final ExpressionsBody  body() throws RecognitionException, TokenStreamException {
		ExpressionsBody body;
		
		body = new ExpressionsBody(); Expr c,ms;
		
		try {      // for error handling
			match(CURLYL_BRACKET);
			{
			_loop9:
			do {
				switch ( LA(1)) {
				case USER_KWD:
				case ROLE_KWD:
				{
					c=command();
					body.addExpression(c);
					break;
				}
				case MATCH_KWD:
				{
					ms=matchRule();
					body.addExpression(ms);
					break;
				}
				default:
				{
					break _loop9;
				}
				}
			} while (true);
			}
			match(CURLYR_BRACKET);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_1);
		}
		return body;
	}
	
	public final Value  value() throws RecognitionException, TokenStreamException {
		Value value;
		
		Token  s = null;
		value=null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case STRING_LITERAL:
			{
				s = LT(1);
				match(STRING_LITERAL);
				value = new StringValue( getStringVal(s.getText()));
				break;
			}
			case HEADER_KWD:
			case ATTRIBUTE_KWD:
			case PRINCIPAL_KWD:
			{
				value=funcvalue();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_3);
		}
		return value;
	}
	
	public final Expr  command() throws RecognitionException, TokenStreamException {
		Expr expr;
		
		expr=null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case USER_KWD:
			{
				expr=userassoc();
				break;
			}
			case ROLE_KWD:
			{
				expr=roleassoc();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_4);
		}
		return expr;
	}
	
	public final Expr  userassoc() throws RecognitionException, TokenStreamException {
		Expr u;
		
		Token  s = null;
		u=null; Value v;
		
		try {      // for error handling
			match(USER_KWD);
			match(L_BRACKET);
			s = LT(1);
			match(STRING_LITERAL);
			match(COMMA);
			v=value();
			match(R_BRACKET);
			u=new UserExpr( getStringVal(s.getText()) ,v);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_4);
		}
		return u;
	}
	
	public final Expr  roleassoc() throws RecognitionException, TokenStreamException {
		Expr r;
		
		r=null; Value v;
		
		try {      // for error handling
			match(ROLE_KWD);
			match(L_BRACKET);
			v=value();
			match(R_BRACKET);
			r=new RoleExpr(v);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_4);
		}
		return r;
	}
	
	public final Value  funcvalue() throws RecognitionException, TokenStreamException {
		Value value;
		
		Token  s = null;
		Token  s1 = null;
		value=null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case HEADER_KWD:
			{
				{
				match(HEADER_KWD);
				match(L_BRACKET);
				s = LT(1);
				match(STRING_LITERAL);
				match(R_BRACKET);
				}
				value = new HeaderValue(  getStringVal(s.getText()) );
				break;
			}
			case ATTRIBUTE_KWD:
			{
				{
				match(ATTRIBUTE_KWD);
				match(L_BRACKET);
				s1 = LT(1);
				match(STRING_LITERAL);
				match(R_BRACKET);
				}
				value = new AttributeValue( getStringVal(s1.getText()) );
				break;
			}
			case PRINCIPAL_KWD:
			{
				{
				match(PRINCIPAL_KWD);
				match(L_BRACKET);
				match(R_BRACKET);
				}
				value = new PrincipalValue();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_3);
		}
		return value;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"match\"",
		"L_BRACKET",
		"COMMA",
		"REGEXP_LITERAL",
		"R_BRACKET",
		"CURLYL_BRACKET",
		"CURLYR_BRACKET",
		"\"user\"",
		"STRING_LITERAL",
		"\"role\"",
		"\"header\"",
		"\"attribute\"",
		"\"principal\"",
		"EQUAL",
		"IDENT",
		"SL_COMMENT",
		"ML_COMMENT",
		"REXP_OR_COMMENT",
		"NEWLINE",
		"WS"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 11282L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 512L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 320L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 11280L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	
	}
