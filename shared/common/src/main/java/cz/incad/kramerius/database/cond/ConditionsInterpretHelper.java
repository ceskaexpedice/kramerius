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
/**
 * 
 */
package cz.incad.kramerius.database.cond;

import java.util.HashMap;
import java.util.Map;


/**
 * Helper class for interpreting conditions
 * @author pavels
 */
public class ConditionsInterpretHelper {
    
    private static Map<String, Condition> CONDS = new HashMap<String, Condition>();

    private static void registerCondition(Condition condition) {
        CONDS.put(condition.getOperatorString(), condition);
    }

    static {
        registerCondition(new Equals());
        registerCondition(new Less());
        registerCondition(new More());
        registerCondition(new LessEq());
        registerCondition(new MoreEq());
    }
    
    
    /**
     * Interpret one condtion 
     * @param leftOperand Left operand
     * @param operator Operator string 
     * @param rightOperand Right operand
     * @return Condition's result
     */
    public static boolean versionCondition(String leftOperand, String operator, String rightOperand) {
        return CONDS.get(operator).execute(leftOperand, rightOperand);
    }


}
