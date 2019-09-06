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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.model.AbstractPage;
import cz.incad.kramerius.document.model.PreparedDocument;
import cz.incad.kramerius.pdf.FirstPagePDFService;
import cz.incad.kramerius.pdf.commands.ITextCommands;
import cz.incad.kramerius.pdf.commands.render.RenderPDF;
import cz.incad.kramerius.pdf.utils.pdf.DocumentUtils;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.mods.ArticleTitleBuilder;
import cz.incad.kramerius.utils.mods.AuthorBuilder;
import cz.incad.kramerius.utils.mods.BuilderFilter;
import cz.incad.kramerius.utils.mods.IdentifiersBuilder;
import cz.incad.kramerius.utils.mods.ModsBuildersDirector;
import cz.incad.kramerius.utils.mods.PeriodicalIssueNumberBuilder;
import cz.incad.kramerius.utils.mods.PeriodicalVolumeNumberBuilder;
import cz.incad.kramerius.utils.mods.PublisherBuilder;
import cz.incad.kramerius.utils.mods.TitleBuilder;

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

    Map<String, Map<String, List<String>>> processMods(String... pids) throws IOException, JAXBException, XPathExpressionException {
        Map<String, Map<String, List<String>>> maps = new HashMap<String, Map<String, List<String>>>();
        for (String pid : pids) {
            ObjectPidsPath selectedPath = selectOnePath(pid);
            Map<String, Map<String, List<String>>> nmaps = processModsFromPath(selectedPath, null);
            maps.putAll(nmaps);
        }
        return maps;
    }

    Map<String, Map<String, List<String>>> processModsFromPath(ObjectPidsPath selectedPath, BuilderFilter filter) throws IOException, XPathExpressionException {
        Map<String, Map<String, List<String>>> maps = new HashMap<String, Map<String, List<String>>>();
        if (selectedPath != null) {
            String[] pathFromLeaf = selectedPath.getPathFromLeafToRoot();
            for (int i = 0; i < pathFromLeaf.length; i++) {
                String pidFromPath = pathFromLeaf[i];
                if (!maps.containsKey(pidFromPath)) {
                    org.w3c.dom.Document modsCol = this.fedoraAccess.getBiblioMods(pidFromPath);
                    String modelName = this.fedoraAccess.getKrameriusModelName(pidFromPath);
                    ModsBuildersDirector director = new ModsBuildersDirector();
                    director.setBuilderFilter(filter);
                    Map<String, List<String>> map = new HashMap<String, List<String>>();
                    director.build(modsCol, map, modelName);
                    maps.put(pidFromPath, map);
                }
            }
        }
        return maps;
    }

    ObjectPidsPath selectOnePath(String pid) throws IOException {
        ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
        ObjectPidsPath selectedPath = paths.length > 0 ? paths[0] : null;
        return selectedPath;
    }

    @Override
    public void selection(PreparedDocument rdoc, OutputStream os, String[] pids,  FontMap fontMap) {
        try {

            Document doc = DocumentUtils.createDocument(rdoc);
            PdfWriter writer = PdfWriter.getInstance(doc, os);
            doc.open();
            String itextCommands = templateSelection(rdoc, pids);
            renderFromTemplate(rdoc, doc, writer, fontMap, new StringReader(itextCommands));

            doc.close();
            os.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (DocumentException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    void renderFromTemplate(PreparedDocument rdoc,Document doc, PdfWriter pdfWriter, FontMap fontMap, StringReader reader) throws IOException, InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException {
        ITextCommands cmnds = new ITextCommands();
        cmnds.load(XMLUtils.parseDocument(reader).getDocumentElement(), cmnds);

        
        RenderPDF render = new RenderPDF(fontMap, this.fedoraAccess);
        render.render(doc, pdfWriter, cmnds);
    }

    String templateSelection(PreparedDocument rdoc, String ... pids) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
        ResourceBundle resourceBundle = resourceBundleService.getResourceBundle("base", localesProvider.get());

        StringTemplate template = new StringTemplate(IOUtils.readAsString(this.getClass().getResourceAsStream("templates/_first_page.st"), Charset.forName("UTF-8"), true));
        FirstPageViewObject fpvo = prepareViewObject(resourceBundle);

        // tistena polozka
        GeneratedItem itm = new GeneratedItem();
        
  
        Map<String, LinkedHashSet<String>> detailItemValues = new HashMap<String, LinkedHashSet<String>>();
        Map<String, ObjectPidsPath> pathsMapping = new HashMap<String, ObjectPidsPath>();
        LinkedHashSet<String> roots = new LinkedHashSet<String>();
        for (String pid : pids) {
            ObjectPidsPath sPath = selectOnePath(pid);
            pathsMapping.put(pid, sPath);
            roots.add(sPath.getRoot());
        }
        
        for (String pid : pids) {
            ObjectPidsPath path = pathsMapping.get(pid);
            Map<String, Map<String, List<String>>> mods = processModsFromPath(path, null);
            String rootPid = path.getRoot();
            if (mods.get(rootPid).containsKey(TitleBuilder.MODS_TITLE)) {
                List<String> list = mods.get(rootPid).get(TitleBuilder.MODS_TITLE);
                if (!list.isEmpty()) {
                    String key = TitleBuilder.MODS_TITLE;
                    itemVals(detailItemValues, list, key);
                }
            }

            
            String[] rProps = renderedProperties(roots.size() == 1);
            String[] fromRootToLeaf = path.getPathFromRootToLeaf();
            for (int i = 0; i < fromRootToLeaf.length; i++) {
                String pidPath = fromRootToLeaf[i];
                for (String prop : rProps) {

                    if (mods.get(pidPath).containsKey(prop)) {
                        List<String> list = mods.get(pidPath).get(prop);
                        itemVals(detailItemValues, list, prop);
                    }
                }
            }
        }
        

        // hlavni nazev
        List<DetailItem> details = new ArrayList<FirstPagePDFServiceImpl.DetailItem>();
        LinkedHashSet<String> maintitles = detailItemValues.get(TitleBuilder.MODS_TITLE);
        String key =  maintitles != null && maintitles.size() > 1 ? resourceBundle.getString("pdf.fp.titles") :  resourceBundle.getString("pdf.fp.title");
        if (maintitles != null && (!maintitles.isEmpty())) {
            details.add(new DetailItem(key, vals(maintitles).toString()));
        }
        for(String prop: renderedProperties(roots.size() == 1)) {
            LinkedHashSet<String> vals = detailItemValues.get(prop);
            key = vals != null && vals.size() > 1 ? resourceBundle.getString(i18nKey(prop)+"s") :  resourceBundle.getString(i18nKey(prop));
            if (vals != null && (!vals.isEmpty())) {
                details.add(new DetailItem(key, vals(vals).toString()));
            }
        }

        // stranky v pdfku
        pagesInSelectiontPdf(rdoc, resourceBundle, details);
        
        itm.setDetailItems((DetailItem[]) details.toArray(new DetailItem[details.size()]));
        fpvo.setGeneratedItems(new GeneratedItem[] {itm});

        template.setAttribute("viewinfo", fpvo);
        
        String templateText = template.toString();
        
        return templateText;
    }

    public String[] renderedProperties(boolean oneRoot) {
        String[] rProps;
        // v pripade, ze je jeden rodic, ma smysl mit vice informaci
        if (oneRoot) {
            rProps = new String[] {
                    AuthorBuilder.MODS_AUTHOR,
                    ArticleTitleBuilder.MODS_TITLE,
                    PublisherBuilder.MODS_PUBLISHER, 
                    PublisherBuilder.MODS_DATE, 
                    PeriodicalVolumeNumberBuilder.MODS_VOLUMENUMBER, 
                    PeriodicalIssueNumberBuilder.MODS_ISSUESNUMBER, 
                    PeriodicalIssueNumberBuilder.MODS_DATE,
                    IdentifiersBuilder.MODS_ISBN,
                    IdentifiersBuilder.MODS_ISSN,
                    IdentifiersBuilder.MODS_SICI,
                    IdentifiersBuilder.MODS_CODEN

            };
        } else {
            rProps = new String[] {
                    TitleBuilder.MODS_TITLE,
                    ArticleTitleBuilder.MODS_TITLE,
                    PeriodicalVolumeNumberBuilder.MODS_VOLUMENUMBER, 
                    PeriodicalIssueNumberBuilder.MODS_ISSUESNUMBER, 
                    IdentifiersBuilder.MODS_ISBN,
                    IdentifiersBuilder.MODS_ISSN,
                    IdentifiersBuilder.MODS_SICI,
                    IdentifiersBuilder.MODS_CODEN

            };
        }
        return rProps;
    }

    void itemVals(Map<String, LinkedHashSet<String>> detailItemValues, List<String> list, String key) {
        LinkedHashSet<String> vals = detailItemValues.get(key);
        if (vals == null) { 
            vals = new LinkedHashSet<String>();
            detailItemValues.put(key, vals);
        }
        vals.addAll(list);
    }

    String templateParent(PreparedDocument rdoc, ObjectPidsPath path) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, JAXBException {
        ResourceBundle resourceBundle = resourceBundleService.getResourceBundle("base", localesProvider.get());

        StringTemplate template = new StringTemplate(IOUtils.readAsString(this.getClass().getResourceAsStream("templates/_first_page.st"), Charset.forName("UTF-8"), true));
        FirstPageViewObject fpvo = prepareViewObject(resourceBundle);

        // tistena polozka
        GeneratedItem itm = new GeneratedItem();
        
        // detaily
        Map<String, LinkedHashSet<String>> detailItemValues = new HashMap<String, LinkedHashSet<String>>();
        Map<String, Map<String, List<String>>> mods = processModsFromPath(path, null);

        // Hlavni nazev
        String rootPid = path.getRoot();
        if (mods.get(rootPid).containsKey(TitleBuilder.MODS_TITLE)) {
            List<String> list = mods.get(rootPid).get(TitleBuilder.MODS_TITLE);
            if (!list.isEmpty()) {
                String key = TitleBuilder.MODS_TITLE;
                itemVals(detailItemValues, list, key);
            }
        }

        // pouze jeden root
        String[] rProps = renderedProperties(true);
        String[] fromRootToLeaf = path.getPathFromRootToLeaf();
        for (int i = 0; i < fromRootToLeaf.length; i++) {
            String pidPath = fromRootToLeaf[i];
            for (String prop : rProps) {
                if (mods.get(pidPath).containsKey(prop)) {
                    List<String> list = mods.get(pidPath).get(prop);
                    itemVals(detailItemValues, list, prop);
                }
            }
        }

        // hlavni nazev
        List<DetailItem> details = new ArrayList<FirstPagePDFServiceImpl.DetailItem>();
        LinkedHashSet<String> maintitles = detailItemValues.get(TitleBuilder.MODS_TITLE);
        String key =  maintitles != null && maintitles.size() > 1 ? resourceBundle.getString("pdf.fp.titles") :  resourceBundle.getString("pdf.fp.title");
        if (maintitles != null && (!maintitles.isEmpty())) {
            details.add(new DetailItem(key, vals(maintitles).toString()));
        }

        String[] props = renderedProperties(true);
        for(String prop: props) {
            LinkedHashSet<String> vals = detailItemValues.get(prop);
            key = vals != null && vals.size() > 1 ? resourceBundle.getString(i18nKey(prop)+"s") :  resourceBundle.getString(i18nKey(prop));
            if (vals != null && (!vals.isEmpty())) {
                details.add(new DetailItem(key, vals(vals).toString()));
            }
        }

        
        pagesInParentPdf(rdoc, resourceBundle, details);

        itm.setDetailItems((DetailItem[]) details.toArray(new DetailItem[details.size()]));
        

        fpvo.setGeneratedItems( new GeneratedItem[] { itm });
        template.setAttribute("viewinfo", fpvo);
        String templateText = template.toString();
        return templateText;
    }

    void pagesInParentPdf(PreparedDocument rdoc, ResourceBundle resourceBundle, List<DetailItem> details) {
        // tistene stranky
        List<AbstractPage> pages = rdoc.getPages();
        if (pages.size() == 1) {
            details.add(new DetailItem(resourceBundle.getString("pdf.fp.page"),pages.get(0).getPageNumber()));
        } else if (pages.size() > 1) {
            details.add(new DetailItem(resourceBundle.getString("pdf.fp.pages"),""+pages.get(0).getPageNumber() +" - "+pages.get(pages.size() - 1).getPageNumber() ));
        }
    }
    
    
    void pagesInSelectiontPdf(PreparedDocument rdoc, ResourceBundle resourceBundle, List<DetailItem> details) {
        // tistene stranky
        List<AbstractPage> pages = rdoc.getPages();
        if (pages.size() == 1) {
            details.add(new DetailItem(resourceBundle.getString("pdf.fp.page"),pages.get(0).getPageNumber()));
        } else if (pages.size() > 1) {
            StringTemplate template = new StringTemplate("$data:{page|$page.pageNumber$};separator=\", \"$");
            template.setAttribute("data", pages);
            details.add(new DetailItem(resourceBundle.getString("pdf.fp.pages"),
                    template.toString()));
        }
    }


    StringTemplate vals(Collection<String> list) {
        StringTemplate dataTemplate = new StringTemplate("$data;separator=\", \"$");
        dataTemplate.setAttribute("data", list);
        return dataTemplate;
    }

    FirstPageViewObject prepareViewObject(ResourceBundle resourceBundle) throws IOException, ParserConfigurationException, SAXException, UnsupportedEncodingException {
        FirstPageViewObject fpvo = new FirstPageViewObject();

        String xml = this.textsService.getText("first_page_nolines_xml", this.localesProvider.get());
        org.w3c.dom.Document doc = XMLUtils.parseDocument(new ByteArrayInputStream(xml.getBytes("UTF-8")), false);
        Element head = XMLUtils.findElement(doc.getDocumentElement(), "head");
        Element desc = XMLUtils.findElement(doc.getDocumentElement(), "desc");

        fpvo.setConditionUsage(head.getTextContent());
        fpvo.setConditionUsageText(desc.getTextContent());

        fpvo.setDitigalLibrary(resourceBundle.getString("pdf.digitallibrary"));
        fpvo.setHyphLang(this.localesProvider.get().getLanguage());
        fpvo.setHyphCountry(this.localesProvider.get().getCountry());
        fpvo.setPdfContainsTitle(resourceBundle.getString("pdf.pdfcontainstitle"));
        return fpvo;
    }

    
    public String i18nValue(ResourceBundle bundle, String modsKey) {
        String key = i18nKey(modsKey);
        if (key != null) {
            if (bundle.containsKey(key)) {
                return bundle.getString(key);
            } else {
                LOGGER.log(Level.WARNING, "cannot find key '"+ key+"'");
                return modsKey;
            }
        } else
            return modsKey;
    }

    public String i18nKey(String modsKey) {
        String[] splitted = modsKey.split(":");
        if (splitted.length == 2 && splitted[0].equals("mods")) {
            return "pdf.fp." + splitted[1];
        } else return modsKey;
    }

    public String localizedModel(ResourceBundle resourceBundle, String pid) throws IOException {
        String modelName = this.fedoraAccess.getKrameriusModelName(pid);
        String localizedModelName = resourceBundle.getString("fedora.model." + modelName);
        return localizedModelName;
    }

    @Override
    public void parent(PreparedDocument rdoc, OutputStream os, ObjectPidsPath path,   FontMap fontMap) {
        try {

            Document doc = DocumentUtils.createDocument(rdoc);

            PdfWriter writer = PdfWriter.getInstance(doc, os);
            doc.open();

            String itextCommands = templateParent(rdoc, path);

            renderFromTemplate(rdoc, doc, writer, fontMap, new StringReader(itextCommands));

            doc.close();
            os.flush();
        } catch (DocumentException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (JAXBException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

    }

    // Reprezentuje objekt do sablony pro zobrazeni
    static class FirstPageViewObject {

        private String ditigalLibrary;
        private String conditionUsage;
        private String conditionUsageText;
        private String pdfContainsTitle;

        private String hyphCountry;
        private String hyphLang;

        private GeneratedItem[] generatedItems = new GeneratedItem[0];

        
        public String getDitigalLibrary() {
            return StringEscapeUtils.escapeXml(this.ditigalLibrary);
        }

        public void setDitigalLibrary(String ditigalLibrary) {
            this.ditigalLibrary = ditigalLibrary;
        }

        public String getConditionUsage() {
            return StringEscapeUtils.escapeXml(conditionUsage);
        }

        public void setConditionUsage(String conditionUsage) {
            this.conditionUsage = conditionUsage;
        }

        public String getConditionUsageText() {
            return StringEscapeUtils.escapeXml(conditionUsageText);
        }

        public void setConditionUsageText(String conditionUsageText) {
            this.conditionUsageText = conditionUsageText;
        }

        public String getPdfContainsTitle() {
            return StringEscapeUtils.escapeXml(pdfContainsTitle);
        }

        public void setPdfContainsTitle(String pdfContainsTitle) {
            this.pdfContainsTitle = pdfContainsTitle;
        }

        public String getHyphCountry() {
            return hyphCountry;
        }

        public void setHyphCountry(String hyphCountry) {
            this.hyphCountry = hyphCountry;
        }

        public String getHyphLang() {
            return hyphLang;
        }

        public void setHyphLang(String hyphLang) {
            this.hyphLang = hyphLang;
        }

        public GeneratedItem[] getGeneratedItems() {
            return generatedItems;
        }

        public void setGeneratedItems(GeneratedItem[] generatedItems) {
            this.generatedItems = generatedItems;
        }

        public boolean isMoreGeneratedItems() {
            return this.generatedItems != null && this.generatedItems.length > 0;
        }

    }

    // reprezentuje generovanou polozku
    static class GeneratedItem {

        private DetailItem[] detailItems = new DetailItem[0];

        public GeneratedItem() {
        }

        public DetailItem[] getDetailItems() {
            return detailItems;
        }

        public void setDetailItems(DetailItem[] dCItems) {
            this.detailItems = dCItems;
        }

        public boolean isDetailItemsDefined() {
            return this.detailItems != null && this.detailItems.length > 0;
        }

    }


    static class DetailItem {

        private String key;
        private String value;

        public DetailItem(String key, String value) {
            super();
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return StringEscapeUtils.escapeXml(key);
        }

        public String getValue() {
            return StringEscapeUtils.escapeXml(value);
        }
    }
}
