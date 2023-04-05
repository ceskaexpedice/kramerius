package cz.incad.kramerius.security.impl.criteria;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;

public class LicenseGEIPDatabase {
    
    @Test
    public void testGeoIP() throws IOException {
        try {
            String ipAddres ="89.102.62.118";
            
            InetAddress byName = InetAddress.getByName(ipAddres);
            
            DatabaseReader reader = new DatabaseReader.Builder(new File("c:\\Users\\happy\\.kramerius4\\geoip\\GeoLite2-Country.mmdb")).build();
            CountryResponse response = reader.country(byName);
            Country country = response.getCountry();
            System.out.println(country.getIsoCode());
            System.out.println(country.getName());
            System.out.println(country.getGeoNameId());
            System.out.println(country.getNames());
            
        } catch (GeoIp2Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
