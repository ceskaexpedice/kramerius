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
package cz.incad.kramerius.auth.thirdparty.shibb.rules;

import java.awt.BorderLayout;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;

import javax.swing.JFrame;

import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.Expr;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.MatchRule;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.ShibRules;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.Value;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.pid.Token.TokenType;


import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenStreamException;
import antlr.collections.AST;
import antlr.debug.misc.JTreeASTModel;
import antlr.debug.misc.JTreeASTPanel;

public class TestShib {
    
    
    public static void main(String[] args) throws RecognitionException, TokenStreamException {
        ShibRuleLexer lexer = new ShibRuleLexer(TestShib.class.getResourceAsStream("test2.rules"));
        ShibRuleParser parser = new ShibRuleParser(lexer);
        ShibRules shibRules = parser.shibRules();
        List<MatchRule> rules = shibRules.getRules();
        for (MatchRule matchRule : rules) {
            System.out.println(matchRule);
            Value leftOperand = matchRule.getLeftOperand();
            Value rightOperand = matchRule.getRightOperand();
            System.out.println("\t"+leftOperand); System.out.println("\t"+rightOperand);
            List<Expr> exps = matchRule.getBody().getExpressions();
            for (Expr expr : exps) {
                System.out.println("\t\t"+expr);
            }
        }
    }
}
