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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Level;

import com.lowagie.text.*;
import cz.incad.kramerius.pdf.commands.*;
import cz.incad.kramerius.pdf.commands.Image;
import cz.incad.kramerius.pdf.commands.List;
import cz.incad.kramerius.pdf.commands.ListItem;
import cz.incad.kramerius.pdf.commands.Paragraph;
import cz.incad.kramerius.utils.StringUtils;
import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.AkubraRepository;

import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.HyphenationAuto;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

import cz.incad.kramerius.pdf.commands.AbstractITextCommand.Hyphenation;
import cz.incad.kramerius.pdf.commands.lists.GreekList;
import cz.incad.kramerius.pdf.commands.lists.RomanList;
import cz.incad.kramerius.pdf.impl.AbstractPDFRenderSupport.ScaledImageOptions;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.knav.pdf.PdfTextUnderImage;
import org.ceskaexpedice.akubra.KnownDatastreams;

public class RenderPDF {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(RenderPDF.class.getName());

    private FontMap fontMap;
    private AkubraRepository akubraRepository;

    public RenderPDF(FontMap fontMap, AkubraRepository akubraRepository) {
        super();
        this.fontMap = fontMap;
        this.akubraRepository = akubraRepository;
    }

    public Font getFont(String formalName) {
        return this.fontMap.getRegistredFont(formalName);
    }

    public void render(final com.lowagie.text.Document pdfDoc, PdfWriter pdfWriter, ITextCommands commands) {
        commands.process(
                new Processor(pdfDoc, pdfWriter, akubraRepository, commands.getFooter(), commands.getHeader()), commands);
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
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
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
                        (document.right() - document.left()) / 2 + document.leftMargin(), document.bottom() - 10, 0);
            }
            if (this.header != null) {
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, header(),
                        (document.right() - document.left()) / 2 + document.leftMargin(), document.top() + 10, 0);
            }
        }

        private Phrase header() {
            Phrase p = new Phrase(this.header, getFont("normal"));
            return p;
        }

        private Phrase footer() {
            Phrase p = new Phrase(this.footer, getFont("normal"));
            return p;
        }
    }

    class Processor implements ITextCommandProcessListener {

        private Document pdfDoc;
        private PdfWriter pdfWriter;
        private AkubraRepository akubraRepository;

        private Stack<Element> createdElm = new Stack<>();

        private final String footer;
        private final String header;

        public Processor(Document pdfDoc, PdfWriter pdfWriter, AkubraRepository akubraRepository, String footer,
                String header) {
            super();
            this.pdfDoc = pdfDoc;
            this.pdfWriter = pdfWriter;
            this.akubraRepository = akubraRepository;
            this.createdElm.push(new DocumentWrapper(this.pdfDoc));
            this.footer = footer;
            this.header = header;
            if (this.footer != null || this.header != null) {
                pdfWriter.setPageEvent(new FooterAndHeader(footer, header));
            }
        }

        @Override
        public void after(ITextCommand iTextCommand, ITextCommands cmds) {
            Element self = this.createdElm.pop();
            if (self instanceof NullElement)
                return;
            if (!this.createdElm.isEmpty()) {
                Element parent = this.createdElm.peek();
                if (parent instanceof TextElementArray) {
                    ((TextElementArray) parent).add(self);
                }
            }
        }

        @Override
        public void before(ITextCommand iTextCommand, ITextCommands cmds) {
            if (!(iTextCommand instanceof ITextCommands)) {
                Element created = this.create(iTextCommand, cmds);
                this.createdElm.push(created);
            }
        }

        private Element create(ITextCommand cmd, ITextCommands xmlDoc) {
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

                if (cmdPar.isAlignmentDefined()) {
                    par.setAlignment(cmdPar.getAlignment());
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
                    returningList = new com.lowagie.text.List(orderingFlag.booleanValue(),
                            alphabeticalFlag.booleanValue());
                } else if (orderingFlag != null) {
                    returningList = new com.lowagie.text.List(false);
                    // return new com.lowagie.text.List(orderingFlag.booleanValue());
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
            } else if (cmd instanceof Line) {
                LineSeparator line = new LineSeparator(1, 100, null, Element.ALIGN_CENTER, -2);
                return line;

            } else if (cmd instanceof PageBreak) {
                Float width = ((PageBreak) cmd).getWidth();
                Float height = ((PageBreak) cmd).getHeight();
                if (width != null && height != null) {
                    Rectangle imageRect = new Rectangle(width, height);
                    pdfDoc.setPageSize(imageRect);
                } else {
                    Float docwidth =xmlDoc.getWidth();
                    Float docheight =xmlDoc.getHeight();
                    Rectangle docRect = new Rectangle(docwidth, docheight);
                    pdfDoc.setPageSize(docRect);
                }
                pdfDoc.newPage();
                return new NullElement();
            } else if (cmd instanceof Image) {

                Image cmdImage = (Image) cmd;
                String pid = cmdImage.getPid();

                if (pid != null) {
                    boolean altoStream  = akubraRepository.datastreamExists(pid, KnownDatastreams.OCR_ALTO);
                    boolean useAlto = KConfiguration.getInstance().getConfiguration().getBoolean("pdfQueue.useAlto", false);
                    if (useAlto && altoStream) {
                        try {
                            InputStream inputStream = akubraRepository.getDatastreamContent(pid, KnownDatastreams.OCR_ALTO).asInputStream();
                            IOUtils.copy(inputStream, System.out);

                            org.w3c.dom.Document alto = akubraRepository.getDatastreamContent(pid, KnownDatastreams.OCR_ALTO).asDom(false);

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
                            img.setAbsolutePosition((offsetX),
                                    pdfDoc.getPageSize().getHeight() - offsetY - (ratio * img.getHeight()));

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
                        }
                        return new NullElement();
                    } else {
                        try {

                            String file = cmdImage.getFile();
                            com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(file);

                            ITextCommands root = cmdImage.getRoot();
                            img.scaleToFit(pdfDoc.getPageSize().getWidth(), pdfDoc.getPageSize().getHeight());
                            img.setAbsolutePosition(0,0);

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

                } else {
                    try {

                        com.lowagie.text.Image img = null;
                        if (cmdImage.getFile() != null) {
                            img = com.lowagie.text.Image.getInstance(cmdImage.getFile());
                        } else if (cmdImage.getUrl() != null)  {
                            img = com.lowagie.text.Image.getInstance(new URL(cmdImage.getUrl()));
                        }
                        if (StringUtils.isAnyString(cmdImage.getX()) &&  StringUtils.isAnyString(cmdImage.getY())) {
                            img.setAbsolutePosition(Float.parseFloat(cmdImage.getX()),Float.parseFloat(cmdImage.getY()));
                        }
                        if (StringUtils.isAnyString(cmdImage.getHeight()) &&  StringUtils.isAnyString(cmdImage.getWidth())) {
                            img.scaleAbsoluteHeight(Float.parseFloat(cmdImage.getHeight()));
                            img.scaleAbsoluteWidth(Float.parseFloat(cmdImage.getWidth()));
                        }

                        return img;
                    } catch (BadElementException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                    return new NullElement();
                }

            } else
                throw new UnsupportedOperationException("unsupported");
        }
    }

    public static Float ratio(Document document, float percentage, com.lowagie.text.Image img) {
        img.scaleToFit(document.getPageSize().getWidth(), document.getPageSize().getHeight());
        Float wratio = document.getPageSize().getWidth() / img.getWidth();
        Float hratio = document.getPageSize().getHeight() / img.getHeight();
        Float ratio = Math.min(wratio, hratio);
        if (percentage != 1.0) {
            ratio = ratio * percentage;
        }
        return ratio;
    }
//    public static Float ratioFromImage(Image image, float percentage, com.lowagie.text.Image img) {
//        Float wratio = document.getPageSize().getWidth() / img.getWidth();
//        Float hratio = document.getPageSize().getHeight() / img.getHeight();
//        Float ratio = Math.min(wratio, hratio);
//        if (percentage != 1.0) {
//            ratio = ratio * percentage;
//        }
//        return ratio;
//    }

}
