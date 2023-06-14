/*
 * Copyright (C) Jun 8, 2023 Pavel Stastny
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
package cz.incad.kramerius.security.licenses.impl.embedded.sk;

import java.util.Arrays;
import java.util.List;

import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.LicensesManager;
import cz.incad.kramerius.security.licenses.impl.LicenseImpl;

/** Licenses in SNK */
// TODO: Move to different  
public class SlovakEmbeddedLicenses {


    /** paying users **/
    public static License PAYING_USERS_LICENSE = new LicenseImpl("paying_users", " Available online - Out-of-Commerce Works", LicensesManager.GLOBAL_GROUP_NAME_EMBEDDED);
            
    /** not accessible **/
    public static License NOT_ACESSIBLE = new LicenseImpl("not_accessible", "Not accessible", LicensesManager.GLOBAL_GROUP_NAME_EMBEDDED);

    /** only in library - on site equivalent **/
    public static License ONLY_IN_LIBRARY = new LicenseImpl("only_in_library", "Accessible only in the library", LicensesManager.GLOBAL_GROUP_NAME_EMBEDDED);


    public static List<License> LICENSES = Arrays.asList(
            PAYING_USERS_LICENSE,
            NOT_ACESSIBLE,
            ONLY_IN_LIBRARY
    );
    
}
