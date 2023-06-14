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
package cz.incad.kramerius.security.licenses.impl.embedded.cz;

import java.util.Arrays;
import java.util.List;

import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.LicensesManager;
import cz.incad.kramerius.security.licenses.impl.LicenseImpl;

/**
 * The class contains global lisense definitions
 * @author happy
 */
public class CzechEmbeddedLicenses {


    /** dnnto licenses **/
    public static License DNNTO_LICENSE = new LicenseImpl("dnnto", "DNNT online license", LicensesManager.GLOBAL_GROUP_NAME_EMBEDDED, 1);

    /** dnntt licenses **/
    public static License DNNTT_LICENSE = new LicenseImpl("dnntt", "DNNT terminal license", LicensesManager.GLOBAL_GROUP_NAME_EMBEDDED, 2);

    /** Generic licenses - on site **/
    public static License ONSITE_LICENSE = new LicenseImpl("onsite", "Accessible only in library", LicensesManager.GLOBAL_GROUP_NAME_EMBEDDED, 3);

    /** Generic licenses - public **/
    public static License PUBLIC_LICENSE = new LicenseImpl("public", "Public license", LicensesManager.GLOBAL_GROUP_NAME_EMBEDDED, 4);

    /** Generic licenses - public muo **/
    public static License PUBLIC_MUO_LICENSE = new LicenseImpl("public-muo", "Public license - sheetmusic Kroměříž ", LicensesManager.GLOBAL_GROUP_NAME_EMBEDDED, 5);
    
    /** Public licenses - sheetmusic **/
    public static License SHHETMUSIC_ONSITE_LICENSE = new LicenseImpl("onsite-sheetmusic", "On site sheet music", LicensesManager.GLOBAL_GROUP_NAME_EMBEDDED, 6);
    
    
    public static List<License> LICENSES = Arrays.asList(
            DNNTT_LICENSE, 
            DNNTO_LICENSE, 
            ONSITE_LICENSE, 
            PUBLIC_LICENSE,
            PUBLIC_MUO_LICENSE,
            SHHETMUSIC_ONSITE_LICENSE
    );
    
}
