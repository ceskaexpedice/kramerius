/*
 * Copyright (C) 2012 Pavel Stastny
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
package cz.incad.kramerius.security.impl.criteria;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;

import javax.print.attribute.ResolutionSyntax;

import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.SecuredActions;

/**
 * 
 * @author pavels
 * TODO: Support IPV6 !
 */
public abstract class AbstractDomainFilter extends AbstractCriterium implements RightCriterium {

    public static final String IPV4_REGEX = "\\A(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z";

    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AbstractDomainFilter.class.getName());
    
    protected boolean matchDomain(Object[] objs) throws UnknownHostException {
        String remoteAddr = this.getEvaluateContext().getRemoteAddr();
        String remoteDomain = this.getEvaluateContext().getRemoteHost();
        return matchDomain(objs, remoteAddr, remoteDomain);
    }

    
    
    protected boolean matchDomain(Object[] objs, String remoteAddr, String remoteHostFromReq) throws UnknownHostException {
        String resolvedHost = remoteHostFromReq;
        if ((resolvedHost == null) || (resolvedHost.equals(remoteAddr))) {
            resolvedHost = resolveDNS(remoteAddr);
        }
        for (Object pattern : objs) {
            boolean matched = resolvedHost.matches(pattern.toString());
            if (matched) return true;
            
        }
        
        return false;
    }



    private static String resolveDNS(String remoteAddr) throws UnknownHostException {
        if (remoteAddr.matches(IPV4_REGEX)) {
            String[] split = remoteAddr.split("\\.");
            byte[] bytes = new byte[4];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) Integer.parseInt(split[i]);
            }
            InetAddress byAddress = Inet4Address.getByAddress(bytes);
            return byAddress.getHostName();
        } else {
            LOGGER.log(Level.SEVERE,"IPv6 protocol is not supported !");
            return "";
        }
    }


    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        return RightCriteriumPriorityHint.MAX;
    }



    @Override
    public SecuredActions[] getApplicableActions() {
        return new SecuredActions[] {SecuredActions.READ};
    }



    @Override
    public boolean isParamsNecessary() {
        return true;
    }

}
