package cz.incad.kramerius.security.impl.criteria.utils;

import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.impl.criteria.AbstractIPAddressFilter;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;

public class CriteriaIPAddrUtils {

    public static final Logger LOGGER = Logger.getLogger(CriteriaIPAddrUtils.class.getName());

    
    
    public static boolean matchGeolocationByIP(DatabaseReader reader, RightCriteriumContext ctx, Object[] objs) throws IOException, GeoIp2Exception {
        String remoteAddr = ctx.getRemoteAddr();
        return matchGeolocationByIP(reader, objs, remoteAddr);
    }
    
    public static boolean matchGeolocationByIP(DatabaseReader reader,  Object[] objs, String remoteAddr) throws IOException, GeoIp2Exception {
        
        try {
            InetAddress ipAddress = InetAddress.getByName(remoteAddr);
            CountryResponse response = reader.country(ipAddress);
            Country country = response.getCountry();
            for (int i = 0; i < objs.length; i++) {
                String obj = objs[i].toString();
                if (obj.equals(country.getIsoCode())) {
                    return true;
                }
            }
        } catch (AddressNotFoundException e) {
            // ok
        }
        
        return false;
    }
    
    
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

            try {
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
            } catch (Exception e) {
                LOGGER.warning(e.getMessage());
            }
        }
        return false;
    }
}
