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
package cz.incad.kramerius.auth.thirdparty.shibb.rules.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents expression body block
 * @author pavels
 */
public class ExpressionsBody implements Expr {

    // expressions inside block
    private List<Expr> expressions = new ArrayList<Expr>();

    /**
     * Add new expression into block
     * @param expr
     */
    public void addExpression(Expr expr) {
        this.expressions.add(expr);
    }
    
    /**
     * Remove any old expression from block
     * @param expr
     */
    public void removeExpression(Expr expr) {
        this.expressions.remove(expr);
    }
    
    /**
     * Returns all expressions inside block
     * @return
     */
    public List<Expr> getExpressions() {
        return expressions;
    }
    
    /**
     * Evaluate expressions inside block
     */
    public void evaluate( ShibbolethContext ctx) {
        for (Expr expr : this.expressions) {
            expr.evaluate(ctx);
        }
    }

}
