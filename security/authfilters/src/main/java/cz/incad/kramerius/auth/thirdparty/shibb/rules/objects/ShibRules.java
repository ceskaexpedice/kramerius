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
 * Represents all shibboleth rules
 * @author pavels
 *
 */
public class ShibRules implements Expr{

    private List<MatchRule> rules = new ArrayList<MatchRule>();

    /**
     * Adds new rule
     * @param mRule
     */
    public void addRule(MatchRule mRule) {
        this.rules.add(mRule);
    }

    /**
     * Remove old rule
     * @param mRule
     */
    public void removeRule(MatchRule mRule) {
        this.rules.remove(mRule);
    }
    
    /**
     * Returns all rules
     * @return
     */
    public List<MatchRule> getRules() {
        return rules;
    }
    
    @Override
    public void evaluate(ShibbolethContext ctx) {
        for (MatchRule mrule : this.rules) {
            mrule.evaluate(ctx);
        }
    }

    
}
