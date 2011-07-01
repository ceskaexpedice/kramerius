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
package cz.incad.kramerius.security.impl.http.shibrules.shibs;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public class MatchRule implements Expr {

    private Value leftOperand;
    private Value rightOperand;
    
    private ExpressionsBody body;
    
    public MatchRule() {
        super();
    }

    
    
    public Value getLeftOperand() {
        return leftOperand;
    }



    public void setLeftOperand(Value leftOperand) {
        this.leftOperand = leftOperand;
    }



    public Value getRightOperand() {
        return rightOperand;
    }



    public void setRightOperand(Value rightOperand) {
        this.rightOperand = rightOperand;
    }

    
    public ExpressionsBody getBody() {
        return body;
    }



    public void setBody(ExpressionsBody body) {
        this.body = body;
    }



    public void evaluate( ShibContext ctx) {
        if (this.leftOperand.match(this.rightOperand, ctx.getHttpServletRequest())) {
            if (body != null) {
                this.body.evaluate(ctx);
            }
        }
    }
}
