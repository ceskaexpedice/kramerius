/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.statistics.filters;

/**
 *
 * @author gabriela melingerova
 */
public class UniqueIPAddressesFilter implements StatisticsFilter {
    
    private Boolean uniqueIpAddresses;

    public Boolean getUniqueIPAddresses() {
        return uniqueIpAddresses;
    }

    public void setUniqueIPAddressesl(Boolean uniqueIpAddresses) {
        this.uniqueIpAddresses = uniqueIpAddresses;
    }
    
}
