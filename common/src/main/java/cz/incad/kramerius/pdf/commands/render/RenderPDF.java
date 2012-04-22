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
package cz.incad.kramerius.pdf.commands.render;

import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Level;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ElementListener;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.TextElementArray;
import com.lowagie.text.pdf.HyphenationAuto;
import com.lowagie.text.pdf.draw.LineSeparator;

import cz.incad.kramerius.pdf.commands.AbstractITextCommand.Hyphenation;
import cz.incad.kramerius.pdf.commands.ITextCommand;
import cz.incad.kramerius.pdf.commands.ITextCommandProcessListener;
import cz.incad.kramerius.pdf.commands.ITextCommands;
import cz.incad.kramerius.pdf.commands.Line;
import cz.incad.kramerius.pdf.commands.List;
import cz.incad.kramerius.pdf.commands.ListItem;
import cz.incad.kramerius.pdf.commands.Paragraph;
import cz.incad.kramerius.pdf.commands.Text;
import cz.incad.kramerius.pdf.commands.TextsArray;
import cz.incad.kramerius.pdf.commands.lists.GreekList;
import cz.incad.kramerius.pdf.commands.lists.RomanList;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;

public class RenderPDF   {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(RenderPDF.class.getName());
    

    private FontMap fontMap;
    
    
    public RenderPDF(FontMap fontMap) {
        super();
        this.fontMap = fontMap;
    }



    public Font getFont(String formalName) {
        return this.fontMap.getRegistredFont(formalName);
    }



    public void render(final com.lowagie.text.Document pdfDoc , ITextCommands commands) {
        commands.process(new Processor(pdfDoc));
    }

    public boolean notEmptyString(String fName) {
        return fName != null && (!fName.trim().equals(""));
    }


    class DocumentWrapper implements com.lowagie.text.TextElementArray {

        private Document document;
        
        public DocumentWrapper(Document document) {
            super();
            this.document = document;
        }

        @Override
        public ArrayList getChunks() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isContent() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isNestable() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean process(ElementListener arg0) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public int type() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public boolean add(Object arg0) {
            try {
                return this.document.add((Element) arg0);
            } catch (DocumentException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                return false;
            }
        }
        
    }
    
    class Processor implements ITextCommandProcessListener {
        
        private Document pdfDoc;
        private Stack<Element> createdElm  = new Stack<Element>();

        
        public Processor(Document pdfDoc) {
            super();
            this.pdfDoc = pdfDoc;
            this.createdElm.push(new DocumentWrapper(this.pdfDoc));
        }

        @Override
        public void after(ITextCommand iTextCommand) {
            Element self = this.createdElm.pop();
            if (!this.createdElm.isEmpty()) {
                Element parent = this.createdElm.peek();
                
                if (parent instanceof TextElementArray) {
                    ((TextElementArray) parent).add(self);
                }
            }
        }

        @Override
        public void before(ITextCommand iTextCommand) {
            if (!(iTextCommand instanceof ITextCommands)) {
                this.createdElm.push(this.create(iTextCommand));
            }            
        }
        
        private Element create(ITextCommand cmd) {
            if (cmd instanceof Paragraph) {
                Paragraph cmdPar = (Paragraph) cmd;
                com.lowagie.text.Paragraph par = new com.lowagie.text.Paragraph();
                if (cmdPar.isSpacingAfterDefined()) {
                    par.setSpacingAfter(cmdPar.getSpacingAfter());
                }
                if (cmdPar.isSpacingBeforeDefined()) {
                    int spacingBefore = cmdPar.getSpacingBefore();
                    par.setSpacingBefore(spacingBefore);
                }
        
                Hyphenation hyphenation = cmdPar.getHyphenation();
                if (hyphenation != null) {
                    par.setHyphenation(new HyphenationAuto(hyphenation.getCountry(), hyphenation.getLang(), 2, 2));
                }
                
                return par;
            } else if (cmd instanceof Text) {
                Text txt = (Text) cmd;
                Chunk chunk = new Chunk(txt.getText());
                
                String formalName = txt.getFontFormalName();
                if (notEmptyString(formalName)) {
                    chunk.setFont(getFont(formalName));
                }
                
                Hyphenation hyphenation = txt.getHyphenation();
                if (hyphenation != null) {
                    chunk.setHyphenation(new HyphenationAuto(hyphenation.getCountry(), hyphenation.getLang(), 2, 2));
                }

                return chunk;
            } else if (cmd instanceof TextsArray) {
                TextsArray txtsA = (TextsArray) cmd;
                Phrase phrase = new Phrase();
                Hyphenation hyphenation = txtsA.getHyphenation();
                if (hyphenation != null) {
                    phrase.setHyphenation(new HyphenationAuto(hyphenation.getCountry(), hyphenation.getLang(), 2, 2));
                }
                return phrase;
            } else if (cmd instanceof GreekList) {
                return new com.lowagie.text.GreekList();
            } else if (cmd instanceof RomanList) {
                return new com.lowagie.text.RomanList();
            } else if (cmd instanceof List) {
                List cmdList = (List) cmd;
                Boolean orderingFlag = null;
                Boolean alphabeticalFlag = null;
                if (cmdList.getListType() != null) {
                    if (cmdList.getListType().equals("ORDERED")) {
                        orderingFlag = com.lowagie.text.List.ORDERED;
                    } else {
                        orderingFlag = com.lowagie.text.List.UNORDERED;
                    }
                }
                if (cmdList.getOrderingType() != null) {
                    if (cmdList.getOrderingType().equals("ALPHABETICAL")) {
                        alphabeticalFlag = com.lowagie.text.List.ALPHABETICAL;
                    } else {
                        alphabeticalFlag = com.lowagie.text.List.NUMERICAL;
                    }
                }
                com.lowagie.text.List returningList = null;
                if (orderingFlag != null && alphabeticalFlag != null) {
                    returningList = new com.lowagie.text.List(orderingFlag.booleanValue(), alphabeticalFlag.booleanValue());
                } else if (orderingFlag != null) {
                    returningList = new com.lowagie.text.List(false);
                    //return new com.lowagie.text.List(orderingFlag.booleanValue());
                } else {
                    returningList = new com.lowagie.text.List();
                }
                
                if (cmdList.getAutoIndent() != null) {
                    returningList.setAutoindent(cmdList.getAutoIndent());
                }
                if (cmdList.getSymbolIndent() > -1) {
                    returningList.setSymbolIndent(cmdList.getSymbolIndent());
                }
                return returningList;
            } else if (cmd instanceof ListItem) {
                ListItem cmdItem = (ListItem) cmd;
                com.lowagie.text.ListItem item = new com.lowagie.text.ListItem();
                if (cmdItem.isSpacingAfterDefined()) {
                    item.setSpacingAfter(cmdItem.getSpacingAfter());
                }
                if (cmdItem.isSpacingBeforeDefined()) {
                    item.setSpacingBefore(cmdItem.getSpacingBefore());
                }

                if (cmdItem.getListSymbol() != null) {
                    item.setListSymbol(new Chunk(cmdItem.getListSymbol()));
                }
                return item;
            } else if (cmd instanceof Line ) {
                LineSeparator line = new LineSeparator(
                        1, 100, null, Element.ALIGN_CENTER, -2);
                return line;
                
            } else throw new UnsupportedOperationException("unsupported");
        }
    }

}
