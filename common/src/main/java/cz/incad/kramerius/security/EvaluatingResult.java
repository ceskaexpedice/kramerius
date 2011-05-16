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
package cz.incad.kramerius.security;

/**
 * This represents answer the question whether current requesting user has access 
 * to requesting resource. 
 */
public enum EvaluatingResult {

	/** operation is permited - Yes, user has access to resource. */
	TRUE(1),
    
	/** operation is denided  - No, user hasn't access to resource. */
    FALSE(0),
    
    /** it cannot be resolved by this criterium - I don't know, must resolve any other right (or right+criteirum) defined on requesting object. */
    NOT_APPLICABLE(3);

    
    private EvaluatingResult(int rawVal) {
        this.rawVal = rawVal;
    }

    
    public int getRawVal() {
        return rawVal;
    }



    public static EvaluatingResult valueOf(int rawVal) {
        EvaluatingResult[] values = values();
        for (EvaluatingResult evalREs : values) {
            if (evalREs.getRawVal() == rawVal) return evalREs;
        }
        return null;
    }
    
    
    private int rawVal;
}
