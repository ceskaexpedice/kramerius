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
package cz.incad.kramerius.security.database;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import cz.incad.kramerius.users.database.LoggedUserDbHelper;

public class SecurityDatabaseUtils {

    static Logger LOGGER = Logger.getLogger(SecurityDatabaseUtils.class.getName());

    static StringTemplateGroup DATABASE_GROUP = loadStGroup();
    static StringTemplateGroup RIGHT_GROUP = loadUpdateRightGroup();
    static StringTemplateGroup stGroup;

    static {
        InputStream is = LoggedUserDbHelper.class.getResourceAsStream("res/database.stg");
        stGroup = new StringTemplateGroup(new InputStreamReader(is), DefaultTemplateLexer.class);
    }

    public static StringTemplateGroup stGroup() {
        return DATABASE_GROUP;
    }

    private static StringTemplateGroup loadStGroup() {
        InputStream is = SecurityDatabaseUtils.class.getResourceAsStream("udatabase.stg");
        StringTemplateGroup grp = new StringTemplateGroup(new InputStreamReader(is), DefaultTemplateLexer.class);
        return grp;
    }

    public static StringTemplateGroup stUdateRightGroup() {
        return RIGHT_GROUP;
    }

    private static StringTemplateGroup loadUpdateRightGroup() {
        InputStream is = SecurityDatabaseUtils.class.getResourceAsStream("updateright.stg");
        StringTemplateGroup grp = new StringTemplateGroup(new InputStreamReader(is), DefaultTemplateLexer.class);
        return grp;
    }

}
