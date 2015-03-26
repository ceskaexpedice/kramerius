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
package cz.incad.kramerius.utils;

import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.utils.XMLUtils.ElementsFilter;

public class ALTOUtils {

    
    public static class AltoImageDimension {
        
        private Dimension dimension;

        public AltoImageDimension(Dimension dimension) {
            super();
            this.dimension = dimension;
        }
        
        public Dimension getDimension() {
            return dimension;
        }

        @Override
        public String toString() {
            return "AltoImageDimension [dimension=" + dimension + "]";
        }
        
        public JSONObject toJSON() {
            JSONObject jsonOBJ = new JSONObject();
            jsonOBJ.put("width", this.dimension.width);
            jsonOBJ.put("height", this.dimension.height);
            return jsonOBJ;
        }
    }

    
    public static class AltoSelectedBox {

        private String selectedKey;
        private Dimension dimension;
        private Point point;
        
        public AltoSelectedBox(String selectedKey, Dimension dimension,
                Point point) {
            super();
            this.selectedKey = selectedKey;
            this.dimension = dimension;
            this.point = point;
        }
        
        
        public String getSelectedKey() {
            return selectedKey;
        }
        
        public Dimension getDimension() {
            return dimension;
        }

        public Point getPoint() {
            return point;
        }


        @Override
        public String toString() {
            return "AltoSelectedBox [selectedKey=" + selectedKey
                    + ", dimension=" + dimension + ", point=" + point + "]";
        }

        public JSONObject toJSON() {
            JSONObject jsonOBJ = new JSONObject();
            jsonOBJ.put("term", this.selectedKey);
            jsonOBJ.put("width", this.dimension.width);
            jsonOBJ.put("height", this.dimension.height);
            jsonOBJ.put("xpos", this.point.x);
            jsonOBJ.put("ypos", this.point.y);
            return jsonOBJ;
        }

        
    }
    
    public static class AltoDisected {
        
        private AltoImageDimension altoImageDimension;
        private List<AltoSelectedBox> boxes;

        public AltoDisected(AltoImageDimension altoImageDimension,
                List<AltoSelectedBox> boxes) {
            super();
            this.altoImageDimension = altoImageDimension;
            this.boxes = boxes;
        }

        public AltoImageDimension getAltoImageDimension() {
            return altoImageDimension;
        }
        
        public List<AltoSelectedBox> getBoxes() {
            return boxes;
        }

        @Override
        public String toString() {
            return "AltoDisected [altoImageDimension=" + altoImageDimension
                    + ", boxes=" + boxes + "]";
        }

        public JSONObject toJSON() {
            JSONObject jsonObject = new JSONObject();
            if (this.altoImageDimension != null) {
                jsonObject.put("image", this.altoImageDimension.toJSON());
            }
            JSONArray jsonArr = new JSONArray();
            for (AltoSelectedBox asBox : this.boxes) {
                jsonArr.add(asBox.toJSON());
            }
            jsonObject.put("boxes", jsonArr);
            return jsonObject;
        }        
        
    }
    
    public static AltoDisected disectAlto(final String parameter, Document dom) throws IOException {

        Element measure = XMLUtils.findElement(dom.getDocumentElement(),
                "MeasurementUnit");
        if (measure != null) {
            String measureContentText = measure.getTextContent();
            if (measureContentText != null) {
                measureContentText = measureContentText.trim();
                if (!measureContentText.equals("pixel"))
                    throw new IOException("cannot process measure '"
                            + measureContentText + "'");
            }
        }
        
        AltoImageDimension altoImageDimension = null;

        Element pageElm = XMLUtils
                .findElement(dom.getDocumentElement(), "Page");
        if (pageElm != null) {
            // <Page ID="Page0" PHYSICAL_IMG_NR="0" HEIGHT="3232" WIDTH="2515">
            String imageHeight = pageElm.hasAttribute("HEIGHT") ? pageElm
                    .getAttribute("HEIGHT") : null;
            String imageWidth = pageElm.hasAttribute("WIDTH") ? pageElm
                    .getAttribute("WIDTH") : null;
            if ((!StringUtils.isAnyString(imageHeight))
                    && (!StringUtils.isAnyString(imageWidth))) {
                pageElm = XMLUtils.findElement(dom.getDocumentElement(),
                        "PrintSpace");
                if (pageElm != null) {
                    imageHeight = pageElm.getAttribute("HEIGHT");
                    imageWidth = pageElm.getAttribute("WIDTH");
                }
            }
            
            if ((imageHeight != null) && (imageWidth != null)) {
                altoImageDimension = new AltoImageDimension(new Dimension(Integer.parseInt(imageWidth), Integer.parseInt(imageHeight)));
            }
        }

        
        List<Element> fElements = new ArrayList<Element>();
        if (StringUtils.isAnyString(parameter)) {
            fElements = XMLUtils.getElementsRecursive(dom.getDocumentElement(),
                    new ElementsFilter() {
                        @Override
                        public boolean acceptElement(Element element) {
                            if (element.getNodeName().equals("String")) {
                                String content = element.getAttribute("CONTENT");
                                if (matchContent(content, parameter)) {
                                    return true;
                                }
                            }
                            return false;
                    }
            });
        }

        List<AltoSelectedBox> boxes = new ArrayList<ALTOUtils.AltoSelectedBox>();
        for (Element sElm : fElements) {
            String contentAttr = sElm.getAttribute("CONTENT");
            String height = sElm.getAttribute("HEIGHT");
            String width = sElm.getAttribute("WIDTH");
            String hpos = sElm.getAttribute("HPOS");
            String vpos = sElm.getAttribute("VPOS");

            AltoSelectedBox altoBox = new AltoSelectedBox(contentAttr, new Dimension(Integer.parseInt(width), Integer.parseInt(height)), new Point(Integer.parseInt(hpos), Integer.parseInt(vpos)));
            boxes.add(altoBox);
            
        }
        return new AltoDisected(altoImageDimension, boxes);
    }
 
    protected static boolean matchContent(String content, String parameter) {
        if (content != null && parameter != null) {
            //TODO: configuration 
            Pattern pattern = Pattern.compile("\\b"+parameter+"\\b");
            Matcher matcher =  pattern.matcher(content);
            return  (matcher.find());
        } else
            return content == parameter;
    }
    
}
