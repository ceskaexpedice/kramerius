package cz.incad.kramerius.security.labels;

import cz.incad.kramerius.security.labels.impl.LabelImpl;
import junit.framework.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LabelsTest {

    @Test
    public void testLabelsRegExps() {

        Assert.assertTrue(Label.ACCEPTABLE_LABEL_NAME_REGEXP.matcher("covid_:/").matches());
        Assert.assertTrue(Label.ACCEPTABLE_LABEL_NAME_REGEXP.matcher("CoViD_:/9210").matches());
        Assert.assertTrue(Label.ACCEPTABLE_LABEL_NAME_REGEXP.matcher("knav/CoViD_:/9210").matches());

        Assert.assertFalse(Label.ACCEPTABLE_LABEL_NAME_REGEXP.matcher("C o ViD_:/9210").matches());
        Assert.assertFalse(Label.ACCEPTABLE_LABEL_NAME_REGEXP.matcher("CoViD.").matches());
        Assert.assertFalse(Label.ACCEPTABLE_LABEL_NAME_REGEXP.matcher("CoViD*").matches());
        Assert.assertFalse(Label.ACCEPTABLE_LABEL_NAME_REGEXP.matcher("CoViD+").matches());
        Assert.assertFalse(Label.ACCEPTABLE_LABEL_NAME_REGEXP.matcher("CoViD?").matches());

    }

    @Test
    public void testLabels() {
        new LabelImpl(-1, "kNaV/CoViD0_", "desscription","grp",3);
        new LabelImpl(-1, "CoViD", "desscription","grp",3);
        new LabelImpl(-1, "kNaV/CoViD", "desscription","grp",3);
        new LabelImpl(-1, "kNaV/CoViD_", "desscription","grp",3);
        new LabelImpl(-1, "kNaV/CoViD0_", "desscription","grp",3);
        new LabelImpl(-1, "kNaV/CoViD0-", "desscription","grp",3);

        try {
            new LabelImpl(-1, "kNaV/CoViD0.", "desscription","grp",3);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // ok
        }

        try {
            new LabelImpl(-1, "kNaV/CoViD0?", "desscription","grp",3);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            new LabelImpl(-1, "coViDo9+", "desscription","grp",3);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // ok
        }
    }
}
