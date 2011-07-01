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

public class ShibRules implements Expr{

    private List<MatchRule> rules = new ArrayList<MatchRule>();

    
    public void addRule(MatchRule mRule) {
        this.rules.add(mRule);
    }

    public void removeRule(MatchRule mRule) {
        this.rules.remove(mRule);
    }
    
    public List<MatchRule> getRules() {
        return rules;
    }
    
    @Override
    public void evaluate(ShibContext ctx) {
        for (MatchRule mrule : this.rules) {
            mrule.evaluate(ctx);
        }
    }

    
}
