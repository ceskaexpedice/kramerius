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

public class Text extends AbstractITextCommand {

    private String fontFormalName;
    private String text;

    private String hyperLink;

    private String hyphLang;
    private String hyphCountry;
    
    public String getFontFormalName() {
        return fontFormalName;
    }

    public String getText() {
        return text;
    }
    
    @Override
    public void load(Element elm, ITextCommands cmnds) throws InstantiationException, IllegalAccessException {
        if (elm.getNodeName().equals("text")) {

            this.hyphenation = this.hyphenationFromAttributes(elm);

            this.fontFormalName = elm.getAttribute("font-formal-name");
            this.text = elm.getTextContent();
            if ((elm.getAttribute("hyphLang") != null) && 
                (elm.getAttribute("hyphCountry") != null)) {
                
                this.hyphCountry = elm.getAttribute("hyph-country");
                this.hyphLang = elm.getAttribute("hyph-lang");
            }
            if ((elm.getAttribute("hyperlink") != null) &&
                    (elm.getAttribute("hyperlink") != null)) {
                this.hyperLink = elm.getAttribute("hyperlink");
            }
        }
    }


    public String getHyphLang() {
        return hyphLang;
    }

    public void setHyphLang(String hyphLang) {
        this.hyphLang = hyphLang;
    }

    public String getHyphCountry() {
        return hyphCountry;
    }

    public void setHyphLand(String hyphLand) {
        this.hyphCountry = hyphLand;
    }

    public String getHyperLink() {
        return hyperLink;
    }

    public void setHyperLink(String hyperLink) {
        this.hyperLink = hyperLink;
    }

    @Override
    public void process(ITextCommandProcessListener procsListener, ITextCommands xmlDoc) {
        procsListener.before(this, xmlDoc);
        procsListener.after(this, xmlDoc);
    }
}
