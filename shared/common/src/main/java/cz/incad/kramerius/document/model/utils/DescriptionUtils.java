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
package cz.incad.kramerius.document.model.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import cz.incad.kramerius.document.model.DCConent;

/**
 * Utility class for collecting descriptions
 * @author pavels
 */
public class DescriptionUtils {

    /**
     * Collects descriptions for given pids
     * @param resBundle ResourceBundle
     * @param dcs DC contents map
     * @param pids Pids
     * @return returns collected descriptions
     * @throws IOException
     */
    public static String[] getDescriptions(ResourceBundle resBundle,Map<String, List<DCConent>> dcs, List<String> pids) throws IOException {

        List<String> descs = new ArrayList<String>();
        for (String pid : pids) {
            List<DCConent> list = dcs.get(pid);
            DCConent dcConent = DCConent.collectFirstWin(list);
    
            StringBuilder line = new StringBuilder();
    
            // issn
            String[] idents = dcConent.getIdentifiers();
            for (String id : idents) {
                if(id.startsWith("issn:")) {
                    line.append(resBundle.getString("pdf.dc.issn")).append(":");
                    line.append(id.substring("issn:".length()));
                }
            }
            if (line.length() > 0) {
                descs.add(line.toString());
            }
    
            //publishers
            line = new StringBuilder();
            String[] publishers = dcConent.getPublishers();
            if (publishers.length > 0) {
                line.append(resBundle.getString("pdf.dc.publishers")).append(":");
                for (int i = 0; i < publishers.length; i++) {
                    if (i > 0) line.append(",");
                    line.append(publishers[i]);
                }
            }
            if (line.length() > 0) {
                descs.add(line.toString());
            }
    
            //creators
            line = new StringBuilder();
            String[] creators = dcConent.getCreators();
            if (creators.length > 0) {
                line.append(resBundle.getString("pdf.dc.creators")).append(":");
                for (int i = 0; i < creators.length; i++) {
                    if (i > 0) line.append(",");
                    line.append(creators[i]);
                }
            }
    
            if (line.length() > 0) {
                descs.add(line.toString());
            }
    
        }
    
        return (String[]) descs.toArray(new String[descs.size()]);
    }

}
