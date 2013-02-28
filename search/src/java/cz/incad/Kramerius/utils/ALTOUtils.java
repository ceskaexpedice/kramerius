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
/**
 * 
 */
package cz.incad.Kramerius.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.XMLUtils.ElementsFilter;

/**
 * @author pavels
 *
 */
public class ALTOUtils {

    /**
     * @param parameter
     * @throws IOException 
     */
    public static  Map<String, Map<String, Double>> disectAlto(final String parameter, Document dom) throws IOException {
        Map<String, Map<String, Double>> map = new HashMap<String, Map<String,Double>>(); 

        Element measure = XMLUtils.findElement(dom.getDocumentElement(), "MeasurementUnit");
        if (measure != null) {
            String measureContentText = measure.getTextContent();
            if (measureContentText != null) {
                measureContentText = measureContentText.trim();
                if (!measureContentText.equals("pixel")) throw new IOException("cannot process measure '"+measureContentText+"'");
            }
        }

        Element pageElm = XMLUtils.findElement(dom.getDocumentElement(), "PrintSpace");
        if (pageElm != null) {
            //<Page ID="Page0" PHYSICAL_IMG_NR="0" HEIGHT="3232" WIDTH="2515">
            String imageHeight = pageElm.getAttribute("HEIGHT");
            String imageWidth = pageElm.getAttribute("WIDTH");
            Map<String, Double> image = new HashMap<String, Double>();
            image.put("HEIGHT", Double.parseDouble(imageHeight));
            image.put("WIDTH", Double.parseDouble(imageWidth));
            map.put("image", image);
        }
        
        Element foundElement = XMLUtils.findElement(dom.getDocumentElement(), new ElementsFilter() {
            
            @Override
            public boolean acceptElement(Element element) {
                if (element.getNodeName().equals("String")) {
                    String content = element.getAttribute("CONTENT");
                    if ((content != null) && (content.equals(parameter))) {
                        return true;
                    }
                }
                return false;
            }
        });
        if (foundElement != null) {
            Map<String, Double> box = new HashMap<String, Double>();
            
            String height = foundElement.getAttribute("HEIGHT");
            String width = foundElement.getAttribute("WIDTH");
            String hpos = foundElement.getAttribute("HPOS");
            String vpos = foundElement.getAttribute("VPOS");
            
            box.put("HEIGHT", Double.parseDouble(height));
            box.put("WIDTH", Double.parseDouble(width));
            box.put("HPOS", Double.parseDouble(hpos));
            box.put("VPOS", Double.parseDouble(vpos));
            
            map.put("box", box);
            
        } 
        
        return map;
    }

}
