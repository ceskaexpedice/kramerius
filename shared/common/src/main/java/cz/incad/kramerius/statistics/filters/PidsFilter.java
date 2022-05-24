/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.statistics.filters;

/**
 *
 * @author Gabriela Melingerová
 */
public class PidsFilter implements StatisticsFilter {
    
    private String pids = "";
    
    public String getPids() {
        return this.pids;
    }
    
    public void setPids(String pids) {
        this.pids = pids;
    }
    
    
}
