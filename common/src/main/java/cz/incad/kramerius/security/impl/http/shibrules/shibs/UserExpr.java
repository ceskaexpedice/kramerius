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

import javax.servlet.http.HttpServletRequest;

import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.UserImpl;

public class UserExpr implements Expr {

    public static final String FIRSTNAME = "firstname";
    public static final String SURNAME ="surname";

    private String userField;
    private Value value;
    
    public UserExpr(String userField, Value value) {
        super();
        this.userField = userField;
        this.value = value;
    }


    @Override
    public void evaluate(ShibContext ctx) {
        User user = ctx.getUser();
        if (userField.equals(FIRSTNAME)) {
            ((UserImpl) user).setFirstName(this.value.getValue(ctx.getHttpServletRequest()));
        } else if (userField.equals(SURNAME)) {
            ((UserImpl) user).setSurname(this.value.getValue(ctx.getHttpServletRequest()));
        } else throw new IllegalStateException("illegal key '"+userField);
    }
}
