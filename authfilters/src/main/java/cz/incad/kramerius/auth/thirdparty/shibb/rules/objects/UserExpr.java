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

import javax.servlet.http.HttpServletRequest;

import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.UserImpl;

/**
 * Perform set attributes to actual logged user (firstname and surname)
 * @see Expr
 * @author pavels
 */
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
    public void evaluate(ShibbolethContext ctx) {
        //User user = ctx.getUser();
        if (userField.equals(FIRSTNAME)) {
            String firstName = this.value.getValue(ctx.getHttpServletRequest());
            ctx.associateFirstName(firstName);
        } else if (userField.equals(SURNAME)) {
            String sName = this.value.getValue(ctx.getHttpServletRequest());
            ctx.associateLastName(sName);
        } else throw new IllegalStateException("illegal key '"+userField);
    }
}
