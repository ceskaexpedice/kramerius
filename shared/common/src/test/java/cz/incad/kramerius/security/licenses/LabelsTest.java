package cz.incad.kramerius.security.licenses;

import cz.incad.kramerius.security.licenses.impl.LicenseImpl;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class LabelsTest {

    @Test
    public void testLabelsRegExps() {

        Assert.assertTrue(License.ACCEPTABLE_LABEL_NAME_REGEXP.matcher("covid_:/").matches());
        Assert.assertTrue(License.ACCEPTABLE_LABEL_NAME_REGEXP.matcher("CoViD_:/9210").matches());
        Assert.assertTrue(License.ACCEPTABLE_LABEL_NAME_REGEXP.matcher("knav/CoViD_:/9210").matches());

        Assert.assertFalse(License.ACCEPTABLE_LABEL_NAME_REGEXP.matcher("C o ViD_:/9210").matches());
        Assert.assertFalse(License.ACCEPTABLE_LABEL_NAME_REGEXP.matcher("CoViD.").matches());
        Assert.assertFalse(License.ACCEPTABLE_LABEL_NAME_REGEXP.matcher("CoViD*").matches());
        Assert.assertFalse(License.ACCEPTABLE_LABEL_NAME_REGEXP.matcher("CoViD+").matches());
        Assert.assertFalse(License.ACCEPTABLE_LABEL_NAME_REGEXP.matcher("CoViD?").matches());

    }

    @Test
    @Ignore
    public void testLabels() {
        new LicenseImpl(-1, "kNaV/CoViD0_", "desscription","grp",3);
        new LicenseImpl(-1, "CoViD", "desscription","grp",3);
        new LicenseImpl(-1, "kNaV/CoViD", "desscription","grp",3);
        new LicenseImpl(-1, "kNaV/CoViD_", "desscription","grp",3);
        new LicenseImpl(-1, "kNaV/CoViD0_", "desscription","grp",3);
        new LicenseImpl(-1, "kNaV/CoViD0-", "desscription","grp",3);

        try {
            new LicenseImpl(-1, "kNaV/CoViD0.", "desscription","grp",3);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // ok
        }

        try {
            new LicenseImpl(-1, "kNaV/CoViD0?", "desscription","grp",3);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            new LicenseImpl(-1, "coViDo9+", "desscription","grp",3);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // ok
        }
    }
}
