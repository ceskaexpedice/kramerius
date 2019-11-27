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

import static cz.incad.kramerius.utils.imgs.KrameriusImageSupport.writeImageToStream;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;

import com.lowagie.text.pdf.*;
import org.xml.sax.SAXException;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ElementListener;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.TextElementArray;
import com.lowagie.text.pdf.HyphenationAuto;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.pdf.commands.AbstractITextCommand.Hyphenation;
import cz.incad.kramerius.pdf.commands.ITextCommand;
import cz.incad.kramerius.pdf.commands.ITextCommandProcessListener;
import cz.incad.kramerius.pdf.commands.ITextCommands;
import cz.incad.kramerius.pdf.commands.Image;
import cz.incad.kramerius.pdf.commands.Line;
import cz.incad.kramerius.pdf.commands.List;
import cz.incad.kramerius.pdf.commands.ListItem;
import cz.incad.kramerius.pdf.commands.PageBreak;
import cz.incad.kramerius.pdf.commands.Paragraph;
import cz.incad.kramerius.pdf.commands.Text;
import cz.incad.kramerius.pdf.commands.TextsArray;
import cz.incad.kramerius.pdf.commands.lists.GreekList;
import cz.incad.kramerius.pdf.commands.lists.RomanList;
import cz.incad.kramerius.pdf.impl.AbstractPDFRenderSupport.ScaledImageOptions;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.knav.pdf.PdfTextUnderImage;

public class RenderPDF   {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(RenderPDF.class.getName());

    private FontMap fontMap;
    private FedoraAccess fedoraAccess;
    
    public RenderPDF(FontMap fontMap, FedoraAccess fedoraAccess) {
        super();
        this.fontMap = fontMap;
        this.fedoraAccess = fedoraAccess;
    }

    public Font getFont(String formalName) {
        return this.fontMap.getRegistredFont(formalName);
    }

    public void render(final com.lowagie.text.Document pdfDoc, PdfWriter pdfWriter , ITextCommands commands) {
        commands.process(new Processor(pdfDoc, pdfWriter, this.fedoraAccess,commands.getFooter(), commands.getHeader()));
    }

    public boolean notEmptyString(String fName) {
        return fName != null && (!fName.trim().equals(""));
    }

    class FooterAndHeader extends PdfPageEventHelper {

        private String footer;
        private String header;

        public FooterAndHeader(String footer, String header) {
            this.footer = footer;
            this.header = header;
        }

        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            if (this.footer != null) {
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer(),
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        document.bottom() - 10, 0);
            }
            if (this.header != null) {
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, header(),
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        document.top() + 10, 0);
            }
        }
        private Phrase header() {
            Phrase p = new Phrase(this.header,getFont("normal"));
            return p;
        }
        private Phrase footer() {
            Phrase p = new Phrase(this.footer,getFont("normal"));
            return p;
        }
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
    
    
    class NullElement implements Element {

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
    }
    
    
    class Processor implements ITextCommandProcessListener {
        
        private Document pdfDoc;
        private PdfWriter pdfWriter;
        private FedoraAccess fedoraAccess;
        
        private Stack<Element> createdElm  = new Stack<>();

        private final String footer;
        private final String header;

        public Processor(Document pdfDoc, PdfWriter pdfWriter, FedoraAccess fedoraAccess,String footer, String header) {
            super();
            this.pdfDoc = pdfDoc;
            this.pdfWriter = pdfWriter;
            this.fedoraAccess = fedoraAccess;
            this.createdElm.push(new DocumentWrapper(this.pdfDoc));
            this.footer = footer;
            this.header = header;
            if (this.footer != null || this.header != null) {
                pdfWriter.setPageEvent(new FooterAndHeader(footer, header));
            }
        }

        @Override
        public void after(ITextCommand iTextCommand) {
            Element self = this.createdElm.pop();
            if (self instanceof NullElement) return ;
            
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
                Element created = this.create(iTextCommand);
                this.createdElm.push(created);
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
                if (cmdPar.isAlignmentDefined()) {
                    par.setAlignment(cmdPar.getAlignment());
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
                
            } else if (cmd instanceof PageBreak) {
                pdfDoc.newPage();
                return new NullElement();
            } else if (cmd instanceof Image) {

            	Image cmdImage = (Image) cmd;
        		String pid = cmdImage.getPid();

                boolean altoStream = false; 
            	try {
            		altoStream = this.fedoraAccess.isStreamAvailable(pid,
                        FedoraUtils.ALTO_STREAM);
				} catch (MalformedURLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
				} catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
				} 

                boolean useAlto = KConfiguration.getInstance()
                        .getConfiguration()
                        .getBoolean("pdfQueue.useAlto", false);

                if (useAlto && altoStream) {
                	try {


                		org.w3c.dom.Document alto = XMLUtils
                                .parseDocument(this.fedoraAccess
                                        .getDataStream(pid,
                                                FedoraUtils.ALTO_STREAM));

                		String file = cmdImage.getFile();
						com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(file);
             
                        ITextCommands root = cmdImage.getRoot();
                        float percentage = (root.getFooter() != null || root.getHeader() != null) ? 0.9f : 1.0f;
                        Float ratio = ratio(pdfDoc, percentage, img);

						int fitToPageWidth = (int) (img.getWidth() * ratio);
						int fitToPageHeight = (int) (img.getHeight() * ratio);

						int offsetX = ((int) pdfDoc.getPageSize().getWidth() - fitToPageWidth) / 2;
						int offsetY = ((int) pdfDoc.getPageSize().getHeight() - fitToPageHeight) / 2;

						img.scaleAbsoluteHeight(ratio * img.getHeight());

						img.scaleAbsoluteWidth(ratio * img.getWidth());
						img.setAbsolutePosition((offsetX), pdfDoc.getPageSize().getHeight()
						        - offsetY - (ratio * img.getHeight()));

						
						ScaledImageOptions options = new ScaledImageOptions();
						options.setXdpi(img.getDpiX());
						options.setYdpi(img.getDpiY());

						options.setXoffset(offsetX);
						options.setYoffset(offsetY);

						options.setWidth(fitToPageWidth);
						options.setHeight(fitToPageHeight);
						options.setScaleFactor(ratio);
					
		                PdfTextUnderImage textUnderImage = new PdfTextUnderImage();
		                textUnderImage.imageWithAlto(pdfDoc, pdfWriter, alto, options);
                        return img;

                    } catch (BadElementException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
					} catch (MalformedURLException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
					} catch (IOException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
					} catch (ParserConfigurationException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
					} catch (SAXException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
					}
                	return new NullElement();
                } else {
                    try {
 
                        String file = cmdImage.getFile();
                        com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(file);
                     
                        ITextCommands root = cmdImage.getRoot();
                        float percentage = (root.getFooter() != null || root.getHeader() != null) ? 0.9f : 1.0f;
                        Float ratio = ratio(pdfDoc, percentage, img);

                        int fitToPageWidth = (int) (img.getWidth() * ratio);
                        int fitToPageHeight = (int) (img.getHeight() * ratio);

                        int offsetX = ((int) pdfDoc.getPageSize().getWidth() - fitToPageWidth) / 2;
                        int offsetY = ((int) pdfDoc.getPageSize().getHeight() - fitToPageHeight) / 2;

                        img.scaleAbsoluteHeight(ratio * img.getHeight());

                        img.scaleAbsoluteWidth(ratio * img.getWidth());
                        img.setAbsolutePosition((offsetX), pdfDoc.getPageSize().getHeight()
                                - offsetY - (ratio * img.getHeight()));

                        
                        ScaledImageOptions options = new ScaledImageOptions();
                        options.setXdpi(img.getDpiX());
                        options.setYdpi(img.getDpiY());

                        options.setXoffset(offsetX);
                        options.setYoffset(offsetY);

                        options.setWidth(fitToPageWidth);
                        options.setHeight(fitToPageHeight);
                        options.setScaleFactor(ratio);

                        return img;
                    } catch (BadElementException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    } catch (MalformedURLException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                    return new NullElement();
                }
                

            } else throw new UnsupportedOperationException("unsupported");
        }
    }

    public static Float ratio(Document document, float percentage,
            com.lowagie.text.Image img) {
        Float wratio = document.getPageSize().getWidth()
                / img.getWidth();
        Float hratio = document.getPageSize().getHeight()
                / img.getHeight();
        Float ratio = Math.min(wratio, hratio);
        if (percentage != 1.0) {
            ratio = ratio * percentage;
        }
        return ratio;
    }

}
