package cz.incad.kramerius.security.impl.criteria.utils;

import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.impl.criteria.AbstractIPAddressFilter;

import java.util.logging.Logger;

public class CriteriaIPAddrUtils {

    public static final Logger LOGGER = Logger.getLogger(CriteriaIPAddrUtils.class.getName());

    public static boolean matchIPAddresses(RightCriteriumContext ctx, Object[] objs) {
        String remoteAddr = ctx.getRemoteAddr();
        return matchIPAddresses(objs, remoteAddr);
    }

    public static boolean matchIPAddresses(Object[] objs, String remoteAddr) {
        for (Object pattern : objs) {
            boolean negativePattern = false;
            String patternStr = pattern.toString();
            if (patternStr.startsWith("!")) {
                patternStr = patternStr.substring(1);
                negativePattern = true;
            }

            boolean matched = remoteAddr.matches(patternStr);
            if ((matched) && (!negativePattern)) {
                LOGGER.fine("\t regexpattern '"+patternStr+"' trying to match with address  '"+remoteAddr+"' - ACCEPTING");
                return true;
            } else if ((!matched) && (negativePattern)) {
                LOGGER.fine("\t regexpattern '"+patternStr+"' trying to match with address  '"+remoteAddr+"' - (negative pattern) ACCEPTING");
                return true;
            }

            // only debug
            if ((!matched) && (!negativePattern)) {
                LOGGER.fine("\t regexpattern '"+patternStr+"' trying to match with address  '"+remoteAddr+"' - NOT ACCEPTING");
            } else if ((matched) && (negativePattern)) {
                LOGGER.fine("\t regexpattern '"+patternStr+"' trying to match with address  '"+remoteAddr+"' -(negative pattern) NOT ACCEPTING");
            }
        }
        return false;
    }
}
