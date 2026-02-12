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
package cz.incad.kramerius.pdf.commands;

import org.w3c.dom.Element;

public abstract class AbstractITextCommand implements ITextCommand {

    protected ITextCommand parentCommand;
    protected Hyphenation hyphenation;
    protected ScaledMeasurements scaledMeasurements;

    @Override
    public ITextCommands getRoot() {
        ITextCommand parent = this.getParent();
        while(parent.getParent() != null) {
            parent = parent.getParent();
        }
        return (ITextCommands) parent;
    }


    @Override
    public ITextCommand getParent() {
        return this.parentCommand;
    }

    @Override
    public void setParent(ITextCommand parent) {
        this.parentCommand = parent;
    }

    public Hyphenation getHyphenation() {
        return hyphenation;
    }

    public ScaledMeasurements getScaledMeasurements() {
        return scaledMeasurements;
    }

    /**
     * Helper method. Returns true if given element contains attribute with any value 
     * @param elm XML element
     * @param name Attribute name
     * @return True if given element contains attributre with any value
     */
    public boolean notEmptyAttribute(Element elm, String name) {
        String attrVal = elm.getAttribute(name);
        return (attrVal != null && (!attrVal.trim().equals("")));
    }
    
    /**
     * Construct Hyphenation object from given element
     * @param elm XML element
     * @return Hyphenation object
     * @see Hyphenation
     */
    public Hyphenation hyphenationFromAttributes(Element elm) {
        if (notEmptyAttribute(elm, "hyphenation-lang") && notEmptyAttribute(elm, "hyphenation-country")) {
            String country = elm.getAttribute("hyphenation-lang");
            String lang = elm.getAttribute("hyphenation-country");
            return new Hyphenation(country, lang);
        } else return null;
    }

    /*
    {
          "extraQualities": [
            "color",
            "gray",
            "bitonal"
          ],
          "profile": "level2",
          "type": "ImageService3",
          "extraFeatures": [
            "regionByPct",
            "sizeByForcedWh",
            "sizeByWh",
            "sizeAboveFull",
            "sizeUpscaling",
            "rotationBy90s",
            "mirroring"
          ],
          "@context": "http://iiif.io/api/image/3/context.json",
          "tiles": [
            {
              "scaleFactors": [1, 2, 4, 8, 16, 32],
              "width": 256,
              "height": 256
            }
          ],
          "protocol": "http://iiif.io/api/image",
          "sizes": [
            {
              "width": 93,
              "height": 135
            },
            {
              "width": 186,
              "height": 271
            },
            {
              "width": 372,
              "height": 542
            },
            {
              "width": 744,
              "height": 1085
            },
            {
              "width": 1488,
              "height": 2170
            }
          ],
          "maxHeight": 12000,
          "service": [
            {
              "physicalScale": 0.00635001,
              "profile": "http://iiif.io/api/annex/services/physdim",
              "physicalUnits": "cm",
              "@context": "http://iiif.io/api/annex/services/physdim/1/context.json"
            }
          ],
          "width": 2977,
          "extraFormats": [
            "webp"
          ],
          "id": "http://imageserver.mzk.cz/NDK/2026/01/b3a01ed3-d731-11f0-b3b2-5acb2ee39cf4/uc_b3a01ed3-d731-11f0-b3b2-5acb2ee39cf4_0001",
          "@id": "http://api.kramerius.mzk.cz/search/iiif/uuid:aba8ce74-f859-11f0-9901-ce52219f3ff6",
          "height": 4340,
          "maxWidth": 12000
        }
     */
    public ScaledMeasurements scaledMeasurementsFromAttributes(Element elm) {
        if (notEmptyAttribute(elm, "scaledmeasurements-unit") && notEmptyAttribute(elm, "scaledmeasurements-physicalScale")) {
            String unit = elm.getAttribute("scaledmeasurements-unit");
            String physicalScale = elm.getAttribute("scaledmeasurements-physicalScale");
            String width = elm.getAttribute("scaledmeasurements-width");
            String height = elm.getAttribute("scaledmeasurements-height");


            ScaledMeasurements scaledMeasurements =
                    new ScaledMeasurements(unit,
                            Double.parseDouble(width),
                            Double.parseDouble(height),
                            Double.parseDouble(physicalScale)
                    );
            return scaledMeasurements;
        }
        return null;
    }



    /**
     * Represents Hyphenation used in PDF generation
     * @author pavels
     */
    public static class Hyphenation {
        private String country;
        private String lang;
        
        public Hyphenation(String country, String lang) {
            super();
            this.country = country;
            this.lang = lang;
        }
        
        /**
         * Returns country code 
         * @return country code 
         */
        public String getCountry() {
            return country;
        }

        /**
         * Returns language code
         * @return language code
         */
        public String getLang() {
            return lang;
        }
    }


    public static class ScaledMeasurements {

        private String unit;
        private double width;
        private double height;
        private double scale;

        public ScaledMeasurements(String unit, double width, double height, double scale) {
            this.unit = unit;
            this.width = width;
            this.height = height;
            this.scale = scale;
        }

        public String getUnit() {
            return unit;
        }

        public double getHeight() {
            return height;
        }

        public double getScale() {
            return scale;
        }

        public double getWidth() {
            return width;
        }
    }
}
