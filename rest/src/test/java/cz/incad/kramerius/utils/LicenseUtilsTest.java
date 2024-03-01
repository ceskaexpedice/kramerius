/*
 * Copyright (C) Feb 27, 2024 Pavel Stastny
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
package cz.incad.kramerius.utils;

import org.json.JSONObject;
import org.junit.Test;

import cz.incad.kramerius.rest.api.k5.admin.utils.LicenseUtils;
import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.impl.LicenseImpl;
import io.smallrye.common.constraint.Assert;

public class LicenseUtilsTest {
    
    @Test
    public void testLicenseToJSON() {
        JSONObject mobj =new JSONObject("{\"name\":\"inovatika_test\",\"description\":\"desc\",\"id\":-1,\"priority\":1,\"group\":\"group\"}");
        License plainLicense = new LicenseImpl("inovatika_test", "desc", "group");
        Assert.assertTrue(mobj.toString().equals(LicenseUtils.licenseToJSON(plainLicense).toString()));
        
        JSONObject exclusivemobj = new JSONObject("{\"name\":\"inovatika_exclusive_test\",\"description\":\"desc\",\"exclusive\":false,\"maxreaders\":1,\"id\":-1,\"refreshinterval\":100,\"priority\":1,\"maxinterval\":1000,\"group\":\"group\"}");
        License exclusiveLicense = new LicenseImpl("inovatika_exclusive_test", "desc", "group");
        exclusiveLicense.initExclusiveLock(100, 1000, 1);
        System.out.println(exclusivemobj.toString().equals(LicenseUtils.licenseToJSON(exclusiveLicense).toString()));
    }
}
