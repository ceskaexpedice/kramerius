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
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.pdf.FirstPageRenderer;
import cz.incad.kramerius.pdf.utils.BiblioMods;
import cz.incad.kramerius.pdf.utils.ModsUtils;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;

public class FirstPageSelectionRenderer extends AbstractFirstPageRenderer {

    java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(FirstPageSelectionRenderer.class.getName());
    
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
    
    private final Font bigFont;
    private final Font smallerFont;
    
    public FirstPageSelectionRenderer(final Font bigFont, final Font smallerFont) {
        this.bigFont = bigFont;
        this.smallerFont = smallerFont;
    }
        
    
    @Override
    public void firstPage(Document doc, PdfWriter writer) {
        try {

            ResourceBundle resBundle = resourceBundleService.getResourceBundle("base", localesProvider.get());

            logo(doc, resBundle, bigFont);
            digitalLibrary(doc, resBundle, smallerFont);


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


    public FedoraAccess getFedoraAccess() {
        return fedoraAccess;
    }


    public void setFedoraAccess(FedoraAccess fedoraAccess) {
        this.fedoraAccess = fedoraAccess;
    }


    public TextsService getTextsService() {
        return textsService;
    }


    public void setTextsService(TextsService textsService) {
        this.textsService = textsService;
    }


    public ResourceBundleService getResourceBundleService() {
        return resourceBundleService;
    }


    public void setResourceBundleService(ResourceBundleService resourceBundleService) {
        this.resourceBundleService = resourceBundleService;
    }


    public SolrAccess getSolrAccess() {
        return solrAccess;
    }


    public void setSolrAccess(SolrAccess solrAccess) {
        this.solrAccess = solrAccess;
    }


    public Provider<Locale> getLocalesProvider() {
        return localesProvider;
    }


    public void setLocalesProvider(Provider<Locale> localesProvider) {
        this.localesProvider = localesProvider;
    }


    
}
