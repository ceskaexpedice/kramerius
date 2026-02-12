package cz.incad.kramerius.pdf.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import cz.incad.kramerius.Constants;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.model.AbstractPage;
import cz.incad.kramerius.document.model.AkubraDocument;
import cz.incad.kramerius.pdf.FirstPagePDFService;
import cz.incad.kramerius.pdf.commands.ITextCommands;
import cz.incad.kramerius.pdf.commands.render.RenderPDF;
import cz.incad.kramerius.pdf.utils.pdf.DocumentUtils;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.security.SecuredAkubraRepository;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.mods.*;
import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.pid.LexerException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;

public class FirstPageForUserProcess extends AbstractPDFRenderSupport implements FirstPagePDFService {

    public static final String DEFAUTL_LICENSE = "special-needs";
    public static final String PROCESS_PDFS_FOLDER="process-pdfs-settings";

    //public static final String SPECIAL_NEEDS_DIR = "special-needs";
    public static final String FIRST_PAGE_XML = "firstpage.xml";

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(FirstPagePDFServiceImpl.class.getName());

    @Inject
    SecuredAkubraRepository akubraRepository;

    @Inject
    TextsService textsService;

    @Inject
    ResourceBundleService resourceBundleService;

    @Inject
    @Named("new-index")
    SolrAccess solrAccess;

    @Inject
    Provider<Locale> localesProvider;

    Map<String, Map<String, List<String>>> processMods(String... pids) throws IOException, JAXBException, XPathExpressionException, LexerException {
        Map<String, Map<String, List<String>>> maps = new HashMap<String, Map<String, List<String>>>();
        for (String pid : pids) {
            ObjectPidsPath selectedPath = selectOnePath(pid);
            Map<String, Map<String, List<String>>> nmaps = processModsFromPath(selectedPath, null);
            maps.putAll(nmaps);
        }
        return maps;
    }

    Map<String, Map<String, List<String>>> processModsFromPath(ObjectPidsPath selectedPath, BuilderFilter filter) throws IOException, XPathExpressionException, LexerException {
        Map<String, Map<String, List<String>>> maps = new HashMap<String, Map<String, List<String>>>();
        if (selectedPath != null) {
            String[] pathFromLeaf = selectedPath.getPathFromLeafToRoot();
            for (int i = 0; i < pathFromLeaf.length; i++) {
                String pidFromPath = pathFromLeaf[i];
                if (!maps.containsKey(pidFromPath)) {
                    org.w3c.dom.Document modsCol = akubraRepository.getDatastreamContent(pidFromPath, KnownDatastreams.BIBLIO_MODS).asDom(true);
                    String modelName = akubraRepository.re().getModel(pidFromPath);
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
        ObjectPidsPath[] paths = this.solrAccess.getPidPaths(pid);
        ObjectPidsPath selectedPath = paths.length > 0 ? paths[0] : null;
        return selectedPath;
    }

    @Override
    public void selection(AkubraDocument rdoc, OutputStream os, String[] pids, FontMap fontMap, String providedbyLicense) {
        try {

            Document doc = DocumentUtils.createDocument(rdoc);
            PdfWriter writer = PdfWriter.getInstance(doc, os);
            doc.open();
            String itextCommands = templateSelection(rdoc, pids);
            renderFromTemplate(rdoc, doc, writer, fontMap, new StringReader(itextCommands));

            doc.close();
            os.flush();
        } catch (IOException | InstantiationException | ParserConfigurationException | IllegalAccessException |
                 DocumentException | SAXException | XPathExpressionException | LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    void renderFromTemplate(AkubraDocument rdoc, Document doc, PdfWriter pdfWriter, FontMap fontMap, StringReader reader) throws IOException, InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException {
        ITextCommands cmnds = new ITextCommands();
        cmnds.load(XMLUtils.parseDocument(reader).getDocumentElement(), cmnds);

        RenderPDF render = new RenderPDF(fontMap, akubraRepository);
        render.render(doc, pdfWriter, cmnds);
    }

    String templateSelection(AkubraDocument rdoc, String ... pids) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException, LexerException {
        ResourceBundle resourceBundle = resourceBundleService.getResourceBundle("base", localesProvider.get());

        org.antlr.stringtemplate.StringTemplate template = new org.antlr.stringtemplate.StringTemplate(IOUtils.readAsString(this.getClass().getResourceAsStream("templates/_first_page.st"), Charset.forName("UTF-8"), true));
        FirstPagePDFServiceImpl.FirstPageViewObject fpvo = prepareViewObject(resourceBundle);

        // tistena polozka
        FirstPagePDFServiceImpl.GeneratedItem itm = new FirstPagePDFServiceImpl.GeneratedItem();


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
        List<FirstPagePDFServiceImpl.DetailItem> details = new ArrayList<FirstPagePDFServiceImpl.DetailItem>();
        LinkedHashSet<String> maintitles = detailItemValues.get(TitleBuilder.MODS_TITLE);
        String key =  maintitles != null && maintitles.size() > 1 ? resourceBundle.getString("pdf.fp.titles") :  resourceBundle.getString("pdf.fp.title");
        if (maintitles != null && (!maintitles.isEmpty())) {
            details.add(new FirstPagePDFServiceImpl.DetailItem(key, vals(maintitles).toString()));
        }
        for(String prop: renderedProperties(roots.size() == 1)) {
            LinkedHashSet<String> vals = detailItemValues.get(prop);
            key = vals != null && vals.size() > 1 ? resourceBundle.getString(i18nKey(prop)+"s") :  resourceBundle.getString(i18nKey(prop));
            if (vals != null && (!vals.isEmpty())) {
                details.add(new FirstPagePDFServiceImpl.DetailItem(key, vals(vals).toString()));
            }
        }



        // stranky v pdfku
        //pagesInSelectiontPdf(rdoc, resourceBundle, details);

        itm.setDetailItems((FirstPagePDFServiceImpl.DetailItem[]) details.toArray(new FirstPagePDFServiceImpl.DetailItem[details.size()]));
        fpvo.setGeneratedItems(new FirstPagePDFServiceImpl.GeneratedItem[] {itm});

        template.setAttribute("title", fpvo);

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

    String templateParent(AkubraDocument rdoc, ObjectPidsPath path, String providedByLicense) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, JAXBException, LexerException {



        ResourceBundle resourceBundle = resourceBundleService.getResourceBundle("base", localesProvider.get());

        String templateContent = findTemplate(localesProvider.get(), providedByLicense);
        org.antlr.stringtemplate.StringTemplate template = new org.antlr.stringtemplate.StringTemplate(templateContent);
        FirstPagePDFServiceImpl.FirstPageViewObject fpvo = prepareViewObject(resourceBundle);

        // tistena polozka
        FirstPagePDFServiceImpl.GeneratedItem itm = new FirstPagePDFServiceImpl.GeneratedItem();

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
        List<FirstPagePDFServiceImpl.DetailItem> details = new ArrayList<FirstPagePDFServiceImpl.DetailItem>();
        LinkedHashSet<String> maintitles = detailItemValues.get(TitleBuilder.MODS_TITLE);
        String key =  maintitles != null && maintitles.size() > 1 ? resourceBundle.getString("pdf.fp.titles") :  resourceBundle.getString("pdf.fp.title");
        if (maintitles != null && (!maintitles.isEmpty())) {
            details.add(new FirstPagePDFServiceImpl.DetailItem(key, vals(maintitles).toString()));
        }

        String[] props = renderedProperties(true);
        for(String prop: props) {
            LinkedHashSet<String> vals = detailItemValues.get(prop);
            key = vals != null && vals.size() > 1 ? resourceBundle.getString(i18nKey(prop)+"s") :  resourceBundle.getString(i18nKey(prop));
            if (vals != null && (!vals.isEmpty())) {
                details.add(new FirstPagePDFServiceImpl.DetailItem(key, vals(vals).toString()));
            }
        }

        Map<String,String> user = new HashMap<>();
        String uid = System.getProperty("user.uid");
        if (StringUtils.isNotBlank(uid)) {
            user.put("uid", uid);
        }
        if (StringUtils.isNotBlank(providedByLicense)) {
            user.put("license", providedByLicense);
        }
        String roles = System.getProperty("user.roles");
        if (StringUtils.isNotBlank(roles)) {
            user.put("roles", roles);
        }
        String date = System.getProperty("date");

        itm.setDetailItems((FirstPagePDFServiceImpl.DetailItem[]) details.toArray(new FirstPagePDFServiceImpl.DetailItem[details.size()]));

        fpvo.setGeneratedItems( new FirstPagePDFServiceImpl.GeneratedItem[] { itm });
        template.setAttribute("title", fpvo);
        template.setAttribute("user", user);
        if (StringUtils.isNotBlank(date)) {
            template.setAttribute("date", date);
        }
        String templateText = template.toString();
        return templateText;
    }

    private String findTemplate(Locale locale, String providedByLicense) throws IOException {
        File processPdfsFolder = new File(Constants.WORKING_DIR, PROCESS_PDFS_FOLDER);
        String licenseFolderName = StringUtils.isNotBlank(providedByLicense) ? providedByLicense : DEFAUTL_LICENSE;

        if (!processPdfsFolder.exists()) {
            File licensesFolder = new File(processPdfsFolder, licenseFolderName);

            if (locale != null) {
                File localeTemplate = new File(new File(licensesFolder, locale.getLanguage()), FIRST_PAGE_XML);
                LOGGER.info("Finding template  "+localeTemplate.getAbsolutePath());
                if (localeTemplate.exists() && localeTemplate.isFile()) {
                    return readFromFile(localeTemplate);
                } else {
                    LOGGER.info("Template not found");
                }
            }

            //
            File customTemplate = new File(licensesFolder, FIRST_PAGE_XML);
            LOGGER.info("Finding template  "+customTemplate.getAbsolutePath());
            if (customTemplate.exists() && customTemplate.isFile()) {
                return readFromFile(customTemplate);
            } else {
                LOGGER.info("Template not found");
            }
        }

        //
        String licenceStreamPath =  String.format("templates/licenses/%s", licenseFolderName);
        LOGGER.info("Finding template  in stream path  "+licenceStreamPath);
        InputStream is = this.getClass().getResourceAsStream(licenceStreamPath);
        if (is == null) {
            LOGGER.info("Template not found");

            LOGGER.info("Finding template  in stream path  "+String.format("templates/licenses/%s", DEFAUTL_LICENSE));
            is = this.getClass().getResourceAsStream(String.format("templates/licenses/%s", DEFAUTL_LICENSE));
        }
        if (is != null) {
            return IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
        } else {
            throw new FileNotFoundException("Default template not found: " + DEFAUTL_LICENSE);
        }
    }

    private String readFromFile(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
        }
    }

    void pagesInParentPdf(AkubraDocument rdoc, ResourceBundle resourceBundle, List<FirstPagePDFServiceImpl.DetailItem> details) {
        // tistene stranky
        List<AbstractPage> pages = rdoc.getPages();
        if (pages.size() == 1) {
            details.add(new FirstPagePDFServiceImpl.DetailItem(resourceBundle.getString("pdf.fp.page"),pages.get(0).getPageNumber()));
        } else if (pages.size() > 1) {
            details.add(new FirstPagePDFServiceImpl.DetailItem(resourceBundle.getString("pdf.fp.pages"),""+pages.get(0).getPageNumber() +" - "+pages.get(pages.size() - 1).getPageNumber() ));
        }
    }


    void pagesInSelectiontPdf(AkubraDocument rdoc, ResourceBundle resourceBundle, List<FirstPagePDFServiceImpl.DetailItem> details) {
        // tistene stranky
        List<AbstractPage> pages = rdoc.getPages();
        if (pages.size() == 1) {
            details.add(new FirstPagePDFServiceImpl.DetailItem(resourceBundle.getString("pdf.fp.page"),pages.get(0).getPageNumber()));
        } else if (pages.size() > 1) {
            org.antlr.stringtemplate.StringTemplate template = new org.antlr.stringtemplate.StringTemplate("$data:{page|$page.pageNumber$};separator=\", \"$");
            template.setAttribute("data", pages);
            details.add(new FirstPagePDFServiceImpl.DetailItem(resourceBundle.getString("pdf.fp.pages"),
                    template.toString()));
        }
    }


    org.antlr.stringtemplate.StringTemplate vals(Collection<String> list) {
        org.antlr.stringtemplate.StringTemplate dataTemplate = new StringTemplate("$data;separator=\", \"$");
        dataTemplate.setAttribute("data", list);
        return dataTemplate;
    }

    FirstPagePDFServiceImpl.FirstPageViewObject prepareViewObject(ResourceBundle resourceBundle) throws IOException, ParserConfigurationException, SAXException, UnsupportedEncodingException {
        FirstPagePDFServiceImpl.FirstPageViewObject fpvo = new FirstPagePDFServiceImpl.FirstPageViewObject();

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

    /* TODO
    public String localizedModel(ResourceBundle resourceBundle, String pid) throws IOException {
        String modelName = this.fedoraAccess.getKrameriusModelName(pid);
        String localizedModelName = resourceBundle.getString("fedora.model." + modelName);
        return localizedModelName;
    }

     */

    @Override
    public void parent(AkubraDocument rdoc, OutputStream os, ObjectPidsPath path, FontMap fontMap, String providedByLicense) {
        try {

            Document doc = DocumentUtils.createDocument(rdoc);

            PdfWriter writer = PdfWriter.getInstance(doc, os);
            doc.open();

            String itextCommands = templateParent(rdoc, path, providedByLicense);

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
        } catch (SAXException | LexerException e) {
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

        private FirstPagePDFServiceImpl.GeneratedItem[] generatedItems = new FirstPagePDFServiceImpl.GeneratedItem[0];


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

        public FirstPagePDFServiceImpl.GeneratedItem[] getGeneratedItems() {
            return generatedItems;
        }

        public void setGeneratedItems(FirstPagePDFServiceImpl.GeneratedItem[] generatedItems) {
            this.generatedItems = generatedItems;
        }

        public boolean isMoreGeneratedItems() {
            return this.generatedItems != null && this.generatedItems.length > 0;
        }

    }

    static class GeneratedItem {

        private FirstPagePDFServiceImpl.DetailItem[] detailItems = new FirstPagePDFServiceImpl.DetailItem[0];

        public GeneratedItem() {
        }

        public FirstPagePDFServiceImpl.DetailItem[] getDetailItems() {
            return detailItems;
        }

        public void setDetailItems(FirstPagePDFServiceImpl.DetailItem[] dCItems) {
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
