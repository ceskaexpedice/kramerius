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
package cz.incad.kramerius.rest.api.utils.dbfilter;

import cz.incad.kramerius.utils.database.SQLFilter.Op;
import cz.incad.kramerius.utils.database.SQLFilter.Tripple;

public class FilterCondition {

    protected Operand leftOperand;
    protected Operand rightOperand;
    protected Op op;
    
    public Operand getLeftOperand() {
        return leftOperand;
    }
    
    public void setLeftOperand(Operand leftOperand) {
        this.leftOperand = leftOperand;
    }
    
    public Operand getRightOperand() {
        return rightOperand;
    }
    
    public void setRightOperand(Operand rightOperand) {
        this.rightOperand = rightOperand;
    }

    public Op getOp() {
        return op;
    }

    public void setOp(Op op) {
        this.op = op;
    }
    
    
    public Tripple getFilterValue() {
        if (this.getLeftOperand().getConvert() != null) {
            Convert convert = this.leftOperand.getConvert();
            return new Tripple(this.getLeftOperand().getValue(), convert.convert(this.getRightOperand().getValue()), op.name());
        } else if (this.getRightOperand().getConvert() != null){
            Convert convert = this.rightOperand.getConvert();
            return new Tripple(this.getRightOperand().getValue(),convert.convert(this.getLeftOperand().getValue()),  op.name());
        } else  return new Tripple(this.getLeftOperand().getValue(), this.rightOperand.getValue(), op.name());
    }
    
}
