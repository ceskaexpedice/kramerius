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

    @Override
    public ITextCommands getRoot() {
        ITextCommand parent = this.getParent();
        while(parent.getParent() != null) {
            parent = parent.getParent();
        }
        return (ITextCommands) parent;
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
    public Hyphenation hyphenationFromAttibutes(Element elm) {
        if (notEmptyAttribute(elm, "hyphenation-lang") && notEmptyAttribute(elm, "hyphenation-country")) {
            String country = elm.getAttribute("hyphenation-lang");
            String lang = elm.getAttribute("hyphenation-country");
            return new Hyphenation(country, lang);
        } else return null;
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
}
