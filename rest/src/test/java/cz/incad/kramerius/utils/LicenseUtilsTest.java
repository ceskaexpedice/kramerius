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

import cz.incad.kramerius.security.licenses.RuntimeLicenseType;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.impl.LicenseImpl;
import cz.incad.kramerius.security.licenses.utils.LicenseTOJSONSupport;

public class LicenseUtilsTest {
    
    @Test
    public void testLicenseToJSON() {
        // Plain license
        JSONObject mobj =new JSONObject("{\"name\":\"inovatika_test\",\"description\":\"desc\",\"id\":-1,\"priority\":1,\"group\":\"group\"}");
        License plainLicense = new LicenseImpl("inovatika_test", "desc", "group");
        Assert.assertTrue(mobj.toString().equals(LicenseTOJSONSupport.licenseToJSON(plainLicense).toString()));

        // Exclusive lock license
        JSONObject exclusivemobj = new JSONObject("{\"name\":\"inovatika_exclusive_test\",\"description\":\"desc\",\"exclusive\":true,\"maxreaders\":1,\"id\":-1,\"refreshinterval\":100,\"priority\":1,\"maxinterval\":1000,\"group\":\"group\", \"type\":\"INSTANCE\"}");
        License exclusiveLicense = new LicenseImpl("inovatika_exclusive_test", "desc", "group");
        exclusiveLicense.initExclusiveLock(100, 1000, 1, null);
        String exclusiveLic = LicenseTOJSONSupport.licenseToJSON(exclusiveLicense).toString();
        Assert.assertEquals(exclusivemobj.toString(), new JSONObject(exclusiveLic).toString());

        License runtimeLicense = new LicenseImpl("inovatika_runtime_test", "desc", "group");
        runtimeLicense.initRuntime(RuntimeLicenseType.ALL_DOCUMENTS);


    }
}
