/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.kramerius.pdf.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.model.AbstractRenderedDocument;
import cz.incad.kramerius.pdf.FirstPagePDFService;
import cz.incad.kramerius.pdf.ModelRender;
import cz.incad.kramerius.pdf.PDFContext;
import cz.incad.kramerius.pdf.PDFFontConfigBean;
import cz.incad.kramerius.pdf.utils.BiblioMods;
import cz.incad.kramerius.pdf.utils.BiblioModsIdentifier;
import cz.incad.kramerius.pdf.utils.BiblioModsPart;
import cz.incad.kramerius.pdf.utils.BiblioModsTitleInfo;
import cz.incad.kramerius.pdf.utils.ModsUtils;
import cz.incad.kramerius.pdf.utils.pdf.DocumentUtils;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;

public class FirstPagePDFServiceImpl implements FirstPagePDFService {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(FirstPagePDFServiceImpl.class.getName());

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    TextsService textsService;

    @Inject
    ResourceBundleService resourceBundleService;

    @Inject
    SolrAccess solrAccess;

    @Inject
    Provider<Locale> localesProvider;

    public void logo(Document pdfDoc, ResourceBundle resBundle, Font bigFont) throws DocumentException {
        bigFont.setSize(48f);
        // TODO: Change in text
        pdfDoc.add(new Paragraph(getLogoString(resBundle), bigFont));
    }

    public String getLogoString(ResourceBundle resBundle) {
        return resBundle.getString("pdf.firstpage.title");
    }

    public void digitalLibrary(Document pdfDoc, ResourceBundle resBundle, Font smallerFont) throws DocumentException {
        // TODO: Change in text
        pdfDoc.add(new Paragraph(getDigitalLibraryString(resBundle), smallerFont));
        pdfDoc.add(new Paragraph(" \n"));
        pdfDoc.add(new LineSeparator());
        pdfDoc.add(new Paragraph(" \n"));
    }

    public String getDigitalLibraryString(ResourceBundle resBundle) {
        return resBundle.getString("pdf.firstpage.library");
    }

    @Override
    public void generateFirstPageForSelection(AbstractRenderedDocument rdoc, OutputStream os, String imgServlet, String i18nServlet, PDFFontConfigBean fontConfigBean) {
        try {
            PDFContext pdfContext = new PDFContext(FontMap.createFontMap(fontConfigBean), imgServlet, i18nServlet);

            Font bigFont = pdfContext.getFontMap().getRegistredFont(FontMap.BIG_FONT);
            Font smallerFont = pdfContext.getFontMap().getRegistredFont(FontMap.NORMAL_FONT);


            Document doc = DocumentUtils.createDocument(rdoc);

            PdfWriter writer = PdfWriter.getInstance(doc, os);
            doc.open();

            new FirstPage(bigFont, smallerFont).render(doc, writer, pdfContext);

            doc.close();
            os.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (DocumentException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public void generateFirstPageForParent(AbstractRenderedDocument rdoc, OutputStream os, ObjectPidsPath path,  String imgServlet, String i18nServlet, PDFFontConfigBean configBean) {
        try {
            PDFContext pdfContext = new PDFContext(FontMap.createFontMap(configBean), imgServlet, i18nServlet);
    
            Font bigFont = pdfContext.getFontMap().getRegistredFont(FontMap.BIG_FONT);
            Font smallerFont = pdfContext.getFontMap().getRegistredFont(FontMap.NORMAL_FONT);

            Document doc = DocumentUtils.createDocument(rdoc);

            PdfWriter writer = PdfWriter.getInstance(doc, os);
            doc.open();

            new FirsPageFromParent(bigFont, smallerFont, path).render(doc, writer, pdfContext);

            doc.close();
            os.flush();
        } catch (DocumentException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

    }

    private class FirstPage {
        private final Font bigFont;
        private final Font smallerFont;

        public FirstPage(final Font bigFont, final Font smallerFont) {
            this.bigFont = bigFont;
            this.smallerFont = smallerFont;
        }

        public void render(Document doc, PdfWriter writer, PDFContext pdfContext) {
            try {
                ResourceBundle resBundle = resourceBundleService.getResourceBundle("base", localesProvider.get());
                logo(doc, resBundle, bigFont);
                digitalLibrary(doc, resBundle, smallerFont);

                Paragraph paragraph = new Paragraph("\n");
                paragraph.setSpacingAfter(10);
                doc.add(paragraph);

                Paragraph parDesc = new Paragraph(textsService.getText("first_page", localesProvider.get()), smallerFont);
                doc.add(parDesc);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            } catch (DocumentException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private class FirsPageFromParent {

        final Map<String, ModelRender> filledMap = new HashMap<String, ModelRender>();

        private final Font bigFont;
        private final Font smallerFont;

        private ObjectPidsPath objectPidsPath;

        public FirsPageFromParent(final Font bigFont, final Font smallerFont, ObjectPidsPath path) {

            this.bigFont = bigFont;
            this.smallerFont = smallerFont;
            this.objectPidsPath = path;

            filledMap.put("periodical", new ModelRender() {

                @Override
                public void render(BiblioMods bmods, com.lowagie.text.List list) {
                    BiblioModsTitleInfo title = bmods.findTitle();
                    if (title != null) {
                        list.setListSymbol(new Chunk("Hlavni nazev: ", smallerFont));
                        com.lowagie.text.ListItem item = new com.lowagie.text.ListItem(new Chunk(title.getTitle(), smallerFont));
                        list.add(item);
                    }

                    BiblioModsIdentifier issn = bmods.findIdent("issn");
                    if (issn != null) {
                        list.setListSymbol(new Chunk("ISSN: ", smallerFont));
                        com.lowagie.text.ListItem item = new com.lowagie.text.ListItem(new Chunk(issn.getIdent(), smallerFont));
                        list.add(item);
                    }
                }
            });

            filledMap.put("monograph", new ModelRender() {

                @Override
                public void render(BiblioMods bmods, com.lowagie.text.List list) {
                    BiblioModsTitleInfo title = bmods.findTitle();
                    if (title != null) {
                        list.setListSymbol(new Chunk("Hlavni nazev: ", smallerFont));
                        com.lowagie.text.ListItem item = new com.lowagie.text.ListItem(new Chunk(title.getTitle(), smallerFont));
                        list.add(item);
                    }

                    BiblioModsIdentifier isbn = bmods.findIdent("isbn");
                    if (isbn != null) {
                        list.setListSymbol(new Chunk("ISBN: ", smallerFont));
                        com.lowagie.text.ListItem item = new com.lowagie.text.ListItem(new Chunk(isbn.getIdent(), smallerFont));
                        list.add(item);
                    }
                }
            });

            filledMap.put("periodicalvolume", new ModelRender() {

                @Override
                public void render(BiblioMods bmods, com.lowagie.text.List list) {
                    BiblioModsPart part = bmods.findDetail("volume");
                    if (part != null) {
                        String detail = part.findDetail("volume").getNumber();

                        list.setListSymbol(new Chunk("Cislo rocniku:", smallerFont));
                        com.lowagie.text.ListItem item = new com.lowagie.text.ListItem(new Chunk(detail, smallerFont));
                        list.add(item);

                        String date = part.getDate();
                        if (date != null) {
                            list.setListSymbol(new Chunk("Rok:", smallerFont));
                            com.lowagie.text.ListItem itemY = new com.lowagie.text.ListItem(new Chunk(date, smallerFont));
                            list.add(itemY);
                        }
                    }
                }
            });

            filledMap.put("periodicalitem", new ModelRender() {

                @Override
                public void render(BiblioMods bmods, com.lowagie.text.List list) {

                    BiblioModsPart part = bmods.findDetail("issue");
                    if (part != null) {
                        String detail = part.findDetail("issue").getNumber();

                        list.setListSymbol(new Chunk("Cislo :", smallerFont));
                        com.lowagie.text.ListItem item = new com.lowagie.text.ListItem(new Chunk(detail, smallerFont));
                        list.add(item);

                        String date = part.getDate();
                        if (date != null) {
                            list.setListSymbol(new Chunk("Rok:", smallerFont));
                            com.lowagie.text.ListItem itemY = new com.lowagie.text.ListItem(new Chunk(date, smallerFont));
                            list.add(itemY);
                        }
                    }
                }
            });

            filledMap.put("internalpart", new ModelRender() {

                @Override
                public void render(BiblioMods bmods, com.lowagie.text.List list) {
                    BiblioModsTitleInfo title = bmods.findTitle();
                    if (title != null) {
                        list.setListSymbol(new Chunk("Nazev: ", smallerFont));
                        com.lowagie.text.ListItem item = new com.lowagie.text.ListItem(new Chunk(title.getTitle(), smallerFont));
                        list.add(item);
                    }
                }
            });

            filledMap.put("default", new ModelRender() {

                @Override
                public void render(BiblioMods bmods, com.lowagie.text.List list) {
                    BiblioModsTitleInfo title = bmods.findTitle();
                    if (title != null) {
                        list.setListSymbol(new Chunk("Nazev: ", smallerFont));
                        com.lowagie.text.ListItem item = new com.lowagie.text.ListItem(new Chunk(title.getTitle(), smallerFont));
                        list.add(item);
                    }
                }
            });

        }

        public void render(Document doc, PdfWriter writer, PDFContext pdfContext) {
            try {

                ResourceBundle resBundle = resourceBundleService.getResourceBundle("base", localesProvider.get());

                logo(doc, resBundle, bigFont);
                digitalLibrary(doc, resBundle, smallerFont);

                com.lowagie.text.List metadatalist = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
                BiblioMods[] mods = new BiblioMods[this.objectPidsPath.getLength()];
                for (int i = 0; i < mods.length; i++) {
                    mods[i] = ModsUtils.biblioMods(this.objectPidsPath.getPathFromLeafToRoot()[i], fedoraAccess);
                }

                for (int j = 0; j < mods.length; j++) {
                    if (filledMap.get(mods[j].getModelName()) != null) {
                        filledMap.get(mods[j].getModelName()).render(mods[j], metadatalist);

                    } else {
                        filledMap.get("default").render(mods[j], metadatalist);
                    }
                }

                doc.add(metadatalist);

                Paragraph paragraph = new Paragraph("\n");
                paragraph.setSpacingAfter(10);
                doc.add(paragraph);

                Paragraph parDesc = new Paragraph(textsService.getText("first_page", localesProvider.get()), smallerFont);
                doc.add(parDesc);
                doc.newPage();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            } catch (DocumentException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }

    }
}
