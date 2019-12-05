/*
 * Copyright (C) 2013 Pavel Stastny
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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Network utitlity class
 * @author pavels
 */
public class NetworkUtils {
    
    public static Logger LOGGER = Logger.getLogger(NetworkUtils.class.getName());
    
    /**
     * Remove all host ip addresses
     * @return
     */
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
                    LOGGER.fine("local ip address "+ip);
                    String ipaddr = ip;
                    if (ip.contains("%"+iface.getDisplayName())) {
                        LOGGER.fine("removing postfix "+"%"+iface.getDisplayName());
                        ipaddr = StringUtils.minus(ip, "%"+iface.getDisplayName());
                    }
                    alist.add(ipaddr);
                }
            }
        } catch (SocketException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return alist.toArray(new String[alist.size()]);
    }

    
}
