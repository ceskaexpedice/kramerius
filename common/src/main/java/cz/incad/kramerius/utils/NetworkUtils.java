package cz.incad.kramerius.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class NetworkUtils {
    
    public static Logger LOGGER = Logger.getLogger(NetworkUtils.class.getName());
    //private static String[] _LOCALHOST={"127.0.0.1","localhost","0:0:0:0:0:0:0:1","::1"}; 
    
    public static String[] getLocalhostsAddress() {
        List<String> alist = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (!iface.isUp())
                    continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    String ip = addr.getHostAddress();
                    String ipaddr = StringUtils.minus(ip, "%"+iface.getDisplayName());
                    alist.add(ipaddr);
                }
            }
        } catch (SocketException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return alist.toArray(new String[alist.size()]);
    }

 
    public static void main(String[] args) {
        getLocalhostsAddress();
    }
}
