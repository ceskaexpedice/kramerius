package cz.incad.kramerius.pdf.impl;

import org.apache.commons.configuration.Configuration;

import cz.incad.kramerius.pdf.OutOfRangeException;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ConfigurationUtils {

    
    public static int checkNumber(String number) throws NumberFormatException, OutOfRangeException {
        Configuration config = KConfiguration.getInstance().getConfiguration();
        if (StringUtils.isAnyString(number)) {
            return checkNumber(Integer.parseInt(number), config);
        } else {
            return checkNumber(Integer.MAX_VALUE, config);
        }
    }

    public static int checkNumber(int val, Configuration config) throws OutOfRangeException {
        String maxPage = config.getString("generatePdfMaxRange");

        boolean turnOff = config
                .getBoolean("turnOffPdfCheck");
        if (turnOff) {
            return val;
        }

        if (val > Integer.parseInt(maxPage)) {
            throw new OutOfRangeException("too much pages");
        } else {
            return val;
        }
    }

    public static int checkNumber(int n) throws OutOfRangeException {
        if (n < 0) {
            n = Integer.MAX_VALUE;
        }
        Configuration config = KConfiguration.getInstance().getConfiguration();
        return checkNumber(n, config);
    }

    public static void checkNumber(String[] pids) throws OutOfRangeException {
        checkNumber(pids.length, KConfiguration.getInstance().getConfiguration());
    }

}
