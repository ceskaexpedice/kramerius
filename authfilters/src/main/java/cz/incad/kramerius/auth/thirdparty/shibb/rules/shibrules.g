/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Shib rules grammar 
 */
header {
    package cz.incad.kramerius.security.impl.http.shibrules;
    
    import java.util.*;
    import cz.incad.kramerius.security.impl.http.shibrules.*;
    import cz.incad.kramerius.security.impl.http.shibrules.shibs.*;
}

class ShibRuleParser extends Parser;
{
    String getStringVal(String val) {
        if ( val.startsWith("\"") && (val.endsWith("\"")) ) {
            return val.substring(1,val.length()-1);
        } else return val;
    }
}

    shibRules returns[ShibRules rules] {rules = new ShibRules();  MatchRule rule;}: (rule=matchRule {rules.addRule(rule);})+;

    
    matchRule returns[MatchRule rule]
    { rule = null;  ExpressionsBody b;}:
     MATCH_KWD rule=condition b=body {rule.setBody(b);};

    condition returns [MatchRule rule]
    {rule=new MatchRule(); Value l,r;}:
     L_BRACKET l=value { rule.setLeftOperand(l); } COMMA  (r=value {rule.setRightOperand(r);} | re:REGEXP_LITERAL {rule.setRightOperand(new ExpressionValue(re.getText()));})  R_BRACKET;

    body returns [ExpressionsBody body]
    {body = new ExpressionsBody(); Expr c,ms; }:
    CURLYL_BRACKET (c=command {body.addExpression(c);} | ms=matchRule {body.addExpression(ms);})* CURLYR_BRACKET;

    command returns [Expr expr]
    {expr=null;} : expr=userassoc | expr=roleassoc;

    userassoc returns [Expr u]  
    {u=null; Value v;} :
    USER_KWD L_BRACKET  s:STRING_LITERAL COMMA v=value R_BRACKET {u=new UserExpr( getStringVal(s.getText()) ,v);};
    
    roleassoc returns [Expr r] 
    {r=null; Value v;}: 
    ROLE_KWD L_BRACKET  v=value R_BRACKET {r=new RoleExpr(v);};

    value returns [Value value] 
    {value=null;}: s:STRING_LITERAL {value = new StringValue( getStringVal(s.getText())); } | value=funcvalue;
    
    
    funcvalue returns [Value value] {value=null;}: (HEADER_KWD L_BRACKET s:STRING_LITERAL R_BRACKET) {value = new HeaderValue(  getStringVal(s.getText()) );} 
                | 
                (ATTRIBUTE_KWD L_BRACKET s1:STRING_LITERAL R_BRACKET) {value = new AttributeValue( getStringVal(s1.getText()) );}
                |
                (PRINCIPAL_KWD L_BRACKET R_BRACKET) {value = new PrincipalValue();};




class ShibRuleLexer extends Lexer;
options{
        charVocabulary='\u0003'..'\u7FFE';
}
tokens {
    MATCH_KWD="match";
    USER_KWD="user";
    ROLE_KWD="role";
    HEADER_KWD="header";
    ATTRIBUTE_KWD="attribute";
    PRINCIPAL_KWD="principal";
}

L_BRACKET: '(';
R_BRACKET: ')';

CURLYL_BRACKET:'{';
CURLYR_BRACKET:'}';

COMMA: ',';

EQUAL    : '=';

IDENT    : (~('('|'='|')'|','|'\r'|'\n'|'\t'|' '|'{'|'}'|'"'|'/'))+;

STRING_LITERAL
    :   '"' (~('"'|'\\'|'\n'|'\r'))* '"'
    ;
    
protected REGEXP_LITERAL: '/' (~('/'|'\\'|'\n'|'\r'))* '/';
        

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


REXP_OR_COMMENT: 
("/*") => ML_COMMENT {$setType(Token.SKIP);} | ("//") => SL_COMMENT {$setType(Token.SKIP);newline();}
| 
REGEXP_LITERAL {$setType(REGEXP_LITERAL);};



NEWLINE : ('\r''\n')=> '\r''\n' //DOS
        | '\r'                  //MAC
        | '\n'                  //UNIX
        { newline();  $setType(Token.SKIP);}
        ;
WS      : (' '|'\t') { $setType(Token.SKIP); } ;