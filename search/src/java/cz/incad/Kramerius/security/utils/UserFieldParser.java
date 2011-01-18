/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.Kramerius.security.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.incad.kramerius.utils.pid.Lexer;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.Token;
import cz.incad.kramerius.utils.pid.Token.TokenType;

public class UserFieldParser {

    private Lexer lexer;
    private Token token;
    
    private String userValue = "";
    private List<String> restGroups = new ArrayList<String>();
    
        
    public UserFieldParser(String user) throws LexerException {
        super();
        
        this.lexer = new Lexer(user);
    }

    public void parseUser() throws LexerException {
        this.consume();
        this.userValue = user();
        while(token.getType() == TokenType.LPAREN) {
            restGroups.add(group());
        }
    }
    
    public String user() throws LexerException {
        this.skipWhiteSpace();
        List<TokenType> types = Arrays.asList(new TokenType[] {
                TokenType.LPAREN,
                TokenType.RPAREN,
                TokenType.EOI
        });
        StringBuffer buffer = new StringBuffer();
        while(!types.contains(this.token.getType())) {
            buffer.append(token.getValue());
            this.consume();
        }
        return buffer.toString();
    }

    public String group() throws LexerException {
        this.skipWhiteSpace();
        this.matchToken(TokenType.LPAREN);
        List<TokenType> types = Arrays.asList(new TokenType[] {
                TokenType.LPAREN,
                TokenType.RPAREN,
                TokenType.EOI
        });
        StringBuffer buffer = new StringBuffer();
        while(!types.contains(this.token.getType())) {
            buffer.append(token.getValue());
            this.consume();
            
        }
        this.matchToken(TokenType.RPAREN);
        this.skipWhiteSpace();
        return buffer.toString();
    }
    

    private void skipWhiteSpace() throws LexerException {
        List<TokenType> ws = Arrays.asList(new TokenType[] {
                TokenType.SPACE,
                TokenType.TAB,
                TokenType.NEWLINE,
                TokenType.TAB
        });
        while(ws.contains(this.token.getType())) {
            this.consume();
        }
    }

    private void matchToken(TokenType expected) throws LexerException {
        if (this.token.getType() == expected) {
            this.consume();
        } else throw new LexerException("Expecting "+expected.name());
    }

    public void consume() throws LexerException {
        this.token = this.lexer.readToken();
    }

    public List<String> getRestGroups() {
        return restGroups;
    }
    
    public String getUserValue() {
        return userValue;
    }
    
    
    public static void main(String[] args) {
        try {
            //String user = "alberto@incad.cz (Alberto Hernandez) (Uzivatel)";
            String user = "pavels@incad.cz (Pavel Šťastný) (Uživatel)";
            UserFieldParser ufp = new UserFieldParser(user);
            ufp.parseUser();
            System.out.println(ufp.getUserValue());
            
        } catch (LexerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
