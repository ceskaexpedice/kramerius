package org.kramerius.importmets.convertor;

import com.lizardtech.djvu.DjVuOptions;
import com.qbizm.kramerius.imp.jaxb.*;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import cz.incad.kramerius.service.XSLService;
import cz.incad.kramerius.service.impl.XSLServiceImpl;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.PathEncoder;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.kramerius.alto.Alto;
import org.kramerius.dc.ElementType;
import org.kramerius.dc.OaiDcType;
import org.kramerius.importmets.MetsConvertor;
import org.kramerius.importmets.MetsConvertor.NamespacePrefixMapperImpl;
import org.kramerius.importmets.MetsConvertor.NamespacePrefixMapperInternalImpl;
import org.kramerius.importmets.utils.UUIDManager;
import org.kramerius.importmets.utils.XSLTransformer;
import org.kramerius.importmets.valueobj.*;
import org.kramerius.importmets.valueobj.FileDescriptor;
import org.kramerius.mods.*;
import org.kramerius.srwdc.DcCollectionType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.xml.bind.*;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public abstract class BaseConvertor {
    static{
        //disable djvu convertor verbose logging
        DjVuOptions.out = new java.io.PrintStream ( new java.io.OutputStream() { public void write(int b){} });
    }

    protected final Logger log = Logger.getLogger(BaseConvertor.class);

    /**
     * Chybove hlasky
     */
    protected static final String WARN_FILE_DOESNT_EXIST = "Referenced file not found";

    protected static final String WARN_PAGE_INDEX = "Page index missing.";

    protected static final String WARN_MUR_EMPTY_UID = "MonographUnitRepresentation - empty UID";

    /**
     * Validacni pattern na PID FOXML objektu - prevzato z fedory
     */
    protected static final String PID_PATTERN = "([A-Za-z0-9]|-|\\.)+:(([A-Za-z0-9])|-|\\.|~|_|(%[0-9A-F]{2}))+";

    /**
     * Prefix pid
     */
    protected static final String PID_PREFIX = "uuid:";

    private  static final String FILE_SCHEME_PREFIX = "file://";

    /**
     * Nazvy modelu
     */
    protected static final String MODEL_MONOGRAPH = "model:monograph";

    protected static final String MODEL_MONOGRAPH_UNIT = "model:monographunit";

    protected static final String MODEL_PERIODICAL = "model:periodical";

    protected static final String MODEL_PERIODICAL_VOLUME = "model:periodicalvolume";

    protected static final String MODEL_PERIODICAL_ITEM = "model:periodicalitem";

    protected static final String MODEL_INTERNAL_PART = "model:internalpart";

    protected static final String MODEL_PAGE = "model:page";

    protected static final String MODEL_ARTICLE = "model:article";

    protected static final String MODEL_SUPPLEMENT = "model:supplement";

    protected static final String MODEL_PICTURE = "model:picture";

    protected static final String MODEL_MAP = "model:map";

    protected static final String MODEL_SHEETMUSIC = "model:sheetmusic";

    protected static final String MODEL_ARCHIVE = "model:archive";

    private static final String CUSTOM_MODEL_PREFIX = "kramerius";

    /**
     * Nazvy a konstanty datastreamu
     */
    private static final String STREAM_ID_TXT = "TEXT_OCR";

    //private static final String STREAM_ID_IMG = "IMG_FULL";

    //private static final String STREAM_ID_THUMB = "IMG_THUMB";

    private static final String STREAM_ID_POLICY = "POLICY";
    private static final String STREAM_ID_POLICY_DEF = "POLICYDEF";

    private static final String STREAM_ID_MODS = "BIBLIO_MODS";

    private static final String STREAM_ID_RELS_EXT = "RELS-EXT";

    private static final String STREAM_VERSION_SUFFIX = ".0";

    private static final String SUFFIX_TXT = "txt";

    protected static final String XSL_PATH = "";

    protected static final String DONATOR_ID = "***Donator NF***";

    protected static final String DONATOR_PID = "donator:norway";

    protected static final String POLICY_PUBLIC = "policy:public";

    protected static final String POLICY_PRIVATE = "policy:private";

    // Kramerius 3 visibility constants

    public static final int PFLAG_PUBLIC = 1;

    public static final int PFLAG_PRIVATE = 2;

    public static final int PFLAG_INHERIT = 3;


    private static final String NS_DC = "http://purl.org/dc/elements/1.1/";
    /**
     * Atributy
     */
    protected final ConvertorConfig config;

    protected final DocumentBuilder docBuilder;

    private final Map<String, String> mimeMap = new TreeMap<String, String>();

    private SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private int objectCounter;

    private XSLService xslService = new XSLServiceImpl(null);

    /**
     * mapa mods elementů načtená z METS
     */
    protected Map<String, ModsDefinition> modsMap = new HashMap<String,ModsDefinition>();
    /**
     * mapa DC elementů načtená z METS
     */
    protected Map<String, OaiDcType> dcMap = new HashMap<String,OaiDcType>();
    /**
     * mapa fileId -> fileName;
     */
    protected Map<String, FileDescriptor> fileMap = new HashMap<String, FileDescriptor>();

    /*
     * mapa logicalDivId -> FOXML reprezentace
     */
    protected Map<String, Foxml> objects = new HashMap<String, Foxml>();
    /**
     * globální viditelnost foxml objektů (publi/private), nastavuje se podle konfigurace
     */
    protected String policyID = POLICY_PUBLIC;

    protected Unmarshaller unmarshallerMODS;
    protected Unmarshaller unmarshallerDC;
    protected Unmarshaller unmarshallerSRWDC;
    protected Unmarshaller unmarshallerALTO;
    protected Marshaller marshallerMODS;
    protected Marshaller marshallerDC;
    protected Marshaller marshallerALTO;
    protected org.kramerius.dc.ObjectFactory dcObjectFactory = new org.kramerius.dc.ObjectFactory();
    protected org.kramerius.mods.ObjectFactory modsObjectFactory = new org.kramerius.mods.ObjectFactory();


    public BaseConvertor(ConvertorConfig config,Unmarshaller unmarshaller) throws ServiceException {
        this.config = config;
        //this.unmarshaller = unmarshaller;
        initMODSJAXB();
        initDCJAXB();
        initSRWDCJAXB();
        initALTOJAXB();

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            this.docBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ServiceException(e);
        }

        mimeMap.put("txt", "text/plain");
        mimeMap.put("djvu", "image/vnd.djvu");
        mimeMap.put("jpg", "image/jpeg");
        mimeMap.put("jpeg", "image/jpeg");
        mimeMap.put("jp2", "image/jp2");
        mimeMap.put("jpx", "image/jpx");
        mimeMap.put("pdf", "application/pdf");
    }

    private void initMODSJAXB(){
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance( ModsDefinition.class );
            marshallerMODS = jaxbContext.createMarshaller();
            marshallerMODS.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
            marshallerMODS.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            try{
                marshallerMODS.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", new NamespacePrefixMapperInternalImpl());
            } catch (PropertyException ex){
                marshallerMODS.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl());
            }
            //marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd");

            unmarshallerMODS = jaxbContext.createUnmarshaller();


        } catch (Exception e) {
            log.error("Cannot init mods JAXB", e);
            throw new RuntimeException(e);
        }
    }

    private void initDCJAXB(){
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance( OaiDcType.class );
            marshallerDC = jaxbContext.createMarshaller();
            marshallerDC.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
            marshallerDC.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            try{
                marshallerDC.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", new NamespacePrefixMapperInternalImpl());
            } catch (PropertyException ex){
                marshallerDC.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl());
            }
            //marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd");

            unmarshallerDC = jaxbContext.createUnmarshaller();


        } catch (Exception e) {
            log.error("Cannot init DC JAXB", e);
            throw new RuntimeException(e);
        }
    }
    
    private void initSRWDCJAXB(){
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance( DcCollectionType.class );
            /*marshallerDC = jaxbContext.createMarshaller();
            marshallerDC.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
            marshallerDC.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            try{
                marshallerDC.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", new NamespacePrefixMapperInternalImpl());
            } catch (PropertyException ex){
                marshallerDC.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl());
            }
            //marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd");
            */
            unmarshallerSRWDC = jaxbContext.createUnmarshaller();


        } catch (Exception e) {
            log.error("Cannot init SRWDC JAXB", e);
            throw new RuntimeException(e);
        }
    }


    private void initALTOJAXB(){
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance( Alto.class );
            marshallerALTO = jaxbContext.createMarshaller();
            marshallerALTO.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
            marshallerALTO.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            try{
                marshallerALTO.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", new NamespacePrefixMapperInternalImpl());
            } catch (PropertyException ex){
                marshallerALTO.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl());
            }
            //marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd");

            unmarshallerALTO = jaxbContext.createUnmarshaller();


        } catch (Exception e) {
            log.error("Cannot init ALTO JAXB", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Prida property digitalnimu objektu
     *
     * @param digitalObject
     * @param name
     * @param value
     */
    private void setProperty(DigitalObject digitalObject, String name, String value) {
        PropertyType pt = new PropertyType();
        pt.setNAME(name);
        pt.setVALUE(value);
        digitalObject.getObjectProperties().getProperty().add(pt);
    }

    /**
     * Nastavi atributy spolecne pro vsechny digitalni objekty
     *
     * @param digitalObject
     */
    protected void setCommonProperties(DigitalObject digitalObject, String pid, String title) {
        digitalObject.setPID(pid);
        digitalObject.setVERSION("1.1");

        if (digitalObject.getObjectProperties() == null) {
            digitalObject.setObjectProperties(new ObjectPropertiesType());
        }

        setProperty(digitalObject, "info:fedora/fedora-system:def/model#label", title!=null?title.substring(0,Math.min(255, title.length())):"null");
        setProperty(digitalObject, "info:fedora/fedora-system:def/model#state", "Active");
        setProperty(digitalObject, "info:fedora/fedora-system:def/model#ownerId", "fedoraAdmin");

        String timestamp = this.timestampFormat.format(new Date());
        setProperty(digitalObject, "info:fedora/fedora-system:def/model#createdDate", timestamp);
        setProperty(digitalObject, "info:fedora/fedora-system:def/view#lastModifiedDate", timestamp);
    }

    /**
     * Vytvori streamy pro foxml objekt
     *
     * @param foxmlObject
     * @param dc
     * @param mods
     * @param policyID
     * @param files
     * @throws ServiceException
     */
    protected final void setCommonStreams(DigitalObject foxmlObject,  Element dc,  Element mods, String policyID, FileDescriptor[] files, Foxml foxmlModel)
            throws ServiceException {
        // /== DUBLIN CORE
        DatastreamType dcStream = this.createDublinCoreStream(dc);
        addCheckedDataStream(foxmlObject, dcStream);
        // \== DUBLIN CORE

        // /== BASE64 stream
        this.addBase64Streams(foxmlObject, files,  foxmlModel);
        // \== BASE64 stream

        // /== BIBLIO_MODS stream
        DatastreamType biblioModsStream = this.createBiblioModsStream(mods);
        addCheckedDataStream(foxmlObject, biblioModsStream);
        // \== BIBLIO_MODS stream


        // /== POLICY stream
        if (policyID != null) {
            DatastreamType policyStream = this.createPolicyStream(policyID);
            addCheckedDataStream(foxmlObject, policyStream);
        }
        // \== POLICY stream
    }

    /**
     * Pripoji k XML elementu XML potomka
     *
     * @param d
     * @param parent
     * @param name
     * @param value
     * @return
     */
    private Element appendChild(Document d, Node parent, String name, String value) {
        Element e = d.createElement(name);
        e.setTextContent(value);
        parent.appendChild(e);
        return e;
    }

    private Element appendChildNS(Document d, Node parent,String prefix, String name, String value) {
        Element e = d.createElementNS(prefix,name);
        e.setTextContent(value);
        parent.appendChild(e);
        return e;
    }

    /**
     * Ulozeni digitalniho objektu
     *
     * @param foxmlObject
     * @throws ServiceException
     */
    protected void marshalDigitalObject(DigitalObject foxmlObject) throws ServiceException {
        String fileName = foxmlObject.getPID().substring(foxmlObject.getPID().lastIndexOf(':') + 1) + ".xml";
        String targetFolder = getConfig().getExportFolder();
        if (MetsConvertor.useContractSubfolders()){//Issue 73
            targetFolder = targetFolder + System.getProperty("file.separator")+"xml";
        }
        this.marshalDigitalObject(foxmlObject, targetFolder, fileName);
    }

    /**
     * Ulozeni digitalniho objektu
     *
     * @param foxmlObject
     * @throws ServiceException
     */
    private void marshalDigitalObject(DigitalObject foxmlObject, String directory, String file) throws ServiceException {
        if (!Pattern.matches(PID_PATTERN, foxmlObject.getPID())) {
            throw new ServiceException("Invalid PID format: " + foxmlObject.getPID());
        }
        try {
            long start = System.currentTimeMillis();
            String path = directory + System.getProperty("file.separator") + file;
            //getConfig().getMarshaller().setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd");
            getConfig().getMarshaller().marshal(foxmlObject, new File(path));
            long end = System.currentTimeMillis();
            if (log.isDebugEnabled()) {
                log.debug("Marshal: time=" + ((end - start)) + "ms ; file=" + file);
            }
            objectCounter++;
        } catch (JAXBException jaxbe) {
            throw new ServiceException(jaxbe);
        }
    }


    public Element createDublinCoreElement(DublinCore dc){
        Document document = docBuilder.newDocument();

        Element root = document.createElementNS("http://www.openarchives.org/OAI/2.0/oai_dc/", "oai_dc:dc");
        // root.setAttributeNS(
        // "http://www.w3.org/2001/XMLSchema-instance",
        // "xsi:schemaLocation",
        // "http://www.openarchives.org/OAI/2.0/oai_dc/ "
        // + "http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
        root.setAttribute("xmlns:dc", NS_DC);

        this.appendChildNS(document, root,NS_DC , "dc:title", dc.getTitle());

        if (dc.getCreator() != null) {
            for (String creator : dc.getCreator()) {
                this.appendChildNS(document, root, NS_DC, "dc:creator", creator);
            }
        }
        if (dc.getPublisher() != null) {
            for (String publisher : dc.getPublisher()) {
                this.appendChildNS(document, root,NS_DC, "dc:publisher", publisher);
            }
        }
        if (dc.getContributor() != null) {
            for (String contributor : dc.getContributor()) {
                this.appendChildNS(document, root,NS_DC, "dc:contributor", contributor);
            }
        }
        if (dc.getIdentifier() != null) {
            for (String identifier : dc.getIdentifier()) {
                this.appendChildNS(document, root,NS_DC, "dc:identifier", identifier);
            }
        }
        if (dc.getSubject() != null) {
            for (String subject : dc.getSubject()) {
                this.appendChildNS(document, root,NS_DC, "dc:subject", subject);
            }
        }
        if (dc.getDate() != null) {
            this.appendChildNS(document, root,NS_DC, "dc:date", dc.getDate());
        }
        if (dc.getLanguage() != null) {
            this.appendChildNS(document, root,NS_DC, "dc:language", dc.getLanguage());
        }
        if (dc.getDescription() != null) {
            this.appendChildNS(document, root, NS_DC, "dc:description", dc.getDescription());
        }
        if (dc.getFormat() != null) {
            this.appendChildNS(document, root,NS_DC, "dc:format", dc.getFormat());
        }
        if (dc.getType() != null) {
            this.appendChildNS(document, root, NS_DC,"dc:type", dc.getType());
        }
        if (dc.getRights() != null) {
            this.appendChildNS(document, root, NS_DC, "dc:rights", dc.getRights());
        }
        return root;
    }


    public Element createDublinCoreElement(OaiDcType dc) throws ServiceException{
        if (dc == null){
            throw new ServiceException("DC is null");
        }
        Document document = docBuilder.newDocument();

        //Element root = document.createElementNS("http://www.openarchives.org/OAI/2.0/oai_dc/", "oai_dc:dc");



        try {
            marshallerDC.marshal(dcObjectFactory.createDc(dc), document);
        } catch (JAXBException e) {
            throw new ServiceException(e);
        }
        return document.getDocumentElement();
    }
    /**
     * Vytvori DC datastream
     *
     * @param dc
     * @throws ServiceException
     */
    private DatastreamType createDublinCoreStream(Element dc) throws ServiceException {
        DatastreamType stream = new DatastreamType();
        stream.setID("DC");
        stream.setSTATE(StateType.A);
        stream.setVERSIONABLE(false);
        stream.setCONTROLGROUP("X");

        DatastreamVersionType version = new DatastreamVersionType();
        version.setCREATED(this.getCurrentXMLGregorianCalendar());
        version.setFORMATURI("http://www.openarchives.org/OAI/2.0/oai_dc/");
        version.setID("DC" + STREAM_VERSION_SUFFIX);
        version.setLABEL("Dublin Core Record for this object");
        version.setMIMETYPE("text/xml");
        stream.getDatastreamVersion().add(version);

        XmlContentType xmlContent = new XmlContentType();
        version.setXmlContent(xmlContent);


        xmlContent.getAny().add(dc);

        return stream;
    }


    public Element createBiblioModsElement(Object page, String xslFile) throws ServiceException{
        if (StringUtils.isEmpty(xslFile)) {
            return null;
        }

        try {
            ByteArrayOutputStream sourceOut = new ByteArrayOutputStream();

            InputStream stylesheet = null;
            File userStylesheet = xslService.xslFile(xslFile);
            if(userStylesheet != null && (userStylesheet.exists()) && (userStylesheet.canRead())){
                try {
                    stylesheet=new FileInputStream(userStylesheet);
                } catch (FileNotFoundException e) {
                    log.fatal("User defined stylesheet "+xslFile+" disappeared.");
                }
            }else{
                stylesheet= this.getClass().getClassLoader().getResourceAsStream(XSL_PATH + xslFile);
            }
            getConfig().getMarshaller().marshal(page, sourceOut);

            // if (log.isDebugEnabled()) {
            // log.debug("XML source: " + sourceOut.toString());
            // }

            ByteArrayInputStream sourceIn = new ByteArrayInputStream(sourceOut.toByteArray());

            // ByteArrayOutputStream result = new ByteArrayOutputStream();
            Document mods = XSLTransformer.transform(sourceIn, stylesheet);
            Element root = mods.getDocumentElement();
            return root;
        } catch (JAXBException e) {
            throw new ServiceException(e);
        }

    }

    protected Element createBiblioModsElement(ModsDefinition mods)throws ServiceException{
        if (mods == null){
            throw new ServiceException("MODS is null");
        }
        Document document = docBuilder.newDocument();
        //Element root = document.createElementNS("http://www.loc.gov/mods/v3", "mods:mods");


        ModsCollectionDefinition mcd = modsObjectFactory.createModsCollectionDefinition();
        mcd.getMods().add(mods);
        try {
            marshallerMODS.marshal(modsObjectFactory.createModsCollection(mcd), document);
        } catch (JAXBException e) {
            throw new ServiceException(e);
        }
        return document.getDocumentElement();
    }
    /**
     * Vytvori MODS datastream
     *
     * @param mods
     * @return
     * @throws ServiceException
     */
    private DatastreamType createBiblioModsStream(Element mods) throws ServiceException {
        DatastreamType stream = new DatastreamType();
        stream.setID(STREAM_ID_MODS);
        stream.setCONTROLGROUP("X");
        stream.setSTATE(StateType.A);
        stream.setVERSIONABLE(false);


        DatastreamVersionType version = new DatastreamVersionType();
        version.setID(STREAM_ID_MODS + STREAM_VERSION_SUFFIX);
        version.setLABEL("BIBLIO_MODS description of current object");
        version.setFORMATURI("http://www.loc.gov/mods/v3");
        version.setMIMETYPE("text/xml");
        version.setCREATED(getCurrentXMLGregorianCalendar());

        if (mods != null) {
            XmlContentType xmlContent = new XmlContentType();
            xmlContent.getAny().add(mods);
            version.setXmlContent(xmlContent);
            stream.getDatastreamVersion().add(version);
        }

        return stream;
    }

    /**
     * Vytvori base64 datastreamy pro dany objekt
     *
     * @param foxmlObject
     * @param files
     * @throws ServiceException
     */
    private void addBase64Streams(DigitalObject foxmlObject, FileDescriptor[] files, Foxml foxmlModel) throws ServiceException {
        boolean useImageServer = KConfiguration.getInstance().getConfiguration().getBoolean("convert.useImageServer", false);

        if (files != null) {
            for (FileDescriptor f : files) {
                if (f != null) {
                    File imageFile = new File(getConfig().getImportFolder() + System.getProperty("file.separator") + f.getFilename());
                    if (imageFile.exists() && imageFile.canRead()) {
                        switch (f.getFileType()){
                            case MASTER_IMAGE:
                            case USER_IMAGE:
                                BufferedImage img = null;
                                try{
                                    if (!useImageServer){
                                        img = readImage(getConfig().getImportFolder() + System.getProperty("file.separator") + f.getFilename());
                                    }
                                } catch (Exception e) {
                                    throw new ServiceException("Problem with file: "+f.getFilename(),e);
                                }
                                DatastreamType fullStream = this.createFullStream(img, f.getFilename(), foxmlModel);
                                if (fullStream != null) {
                                    addCheckedDataStream(foxmlObject,fullStream);
                                }
                                DatastreamType thumbnailStream = this.createThumbnailStream(img, f.getFilename());
                                if (thumbnailStream != null) {
                                    addCheckedDataStream(foxmlObject,thumbnailStream);
                                }
                                if (KConfiguration.getInstance().getConfiguration().getBoolean("convert.generatePreview", true)){
                                    DatastreamType previewStream = this.createPreviewStream(img, f.getFilename());
                                    if (previewStream != null) {
                                        addCheckedDataStream(foxmlObject,previewStream);
                                    }
                                }
                                break;
                            case ALTO:
                                DatastreamType altoStream = this.createAltoStream( f.getFilename());
                                if (altoStream != null) {
                                    addCheckedDataStream(foxmlObject,altoStream);
                                }
                                break;
                            case OCR:
                                DatastreamType base64Stream = this.createOCRStream(f.getFilename());
                                addCheckedDataStream(foxmlObject,base64Stream);
                                break;
                            case AMD:
                                DatastreamType amdStream = this.createAMDStream( f.getFilename());
                                if (amdStream != null) {
                                    addCheckedDataStream(foxmlObject,amdStream);
                                }
                                /*if (f.getImageMetaData() != null) {
                                DatastreamType imageAdmStream = this.createImageMetaStream(getBase64StreamId(f.getFilename()) + "_ADM", f.getImageMetaData());
                                addCheckedDataStream(foxmlObject, imageAdmStream);
                                }*/ //TODO support for AMD
                                break;
                        }
                    } else {
                        if (f.getFilename() != null){
                            log.warn(WARN_FILE_DOESNT_EXIST + ": " + f.getFilename());
                            if (!KConfiguration.getInstance().getConfiguration().getBoolean("convert.ignoreMissingFiles",false)){
                                log.fatal("CONVERSION WILL BE TERMINATED DUE TO MISSING ORIGINAL FILE(S). Set the property convert.ignoreMissingFiles=true  and restart the conversion process, if you want to continue anyway. ");
                                throw new IllegalStateException(WARN_FILE_DOESNT_EXIST + ": " + f.getFilename());
                            }
                        }
                    }
                }
            }
        }
    }


    private void addCheckedDataStream(DigitalObject foxml, DatastreamType stream){
        if (stream == null||foxml == null){
            return;
        }
        for (DatastreamType existing:foxml.getDatastream() ){
           if(existing.getID().equals(stream.getID())){
               throw new IllegalStateException("Attempt to add duplicate datastream ID:"+stream.getID()+", PID:"+foxml.getPID());
           }
        }
        foxml.getDatastream().add(stream);
    }

    /**
     * Ziska priponu ze jmena souboru
     *
     * @param filename
     * @return
     */
    private String getSuffix(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }



    private String getImageMime(String filename) {
        return mimeMap.get(getSuffix(filename));
    }

    /**
     * Extract uuid identifier from mods, strip invalid characters and convert to lowercase
     * @param mods
     * @return extracted uuid or null
     */
    protected String getUUIDfromMods(ModsDefinition mods){
        String uuid = null;
        for(Object mg:    mods.getModsGroup()){
            if (mg instanceof IdentifierDefinition){
                IdentifierDefinition id = (IdentifierDefinition)mg;
                if ("uuid".equalsIgnoreCase(id.getType1())){
                    String rawUUID = id.getValue();
                    if (rawUUID != null){
                        uuid = rawUUID.replace("{", "").replace("}", "").toLowerCase();
                    }
                }
            }
        }
        return uuid;
    }

    /**
     * Extract title + subtitle + partnumber as string from mods
     * @param mods
     * @return extracted title or empty string
     */
    protected String getTitlefromMods(ModsDefinition mods){
        StringBuilder title = new StringBuilder();
        for(Object mg:    mods.getModsGroup()){
            if (mg instanceof TitleInfoDefinition){
                TitleInfoDefinition ti = (TitleInfoDefinition)mg;
                for (JAXBElement<XsString> el:ti.getTitleOrSubTitleOrPartNumber()){
                    XsString val = el.getValue();
                    if (val != null) {
                        title.append(" ").append(val.getValue());
                    }
                }
            }
        }
        return title.toString().trim();
    }

    protected static enum Genre {NONE, CARTOGRAPHIC, SHEETMUSIC}

    protected Genre getGenrefromMods(ModsDefinition mods){
        StringBuilder title = new StringBuilder();
        for(Object mg:    mods.getModsGroup()){
            if (mg instanceof GenreDefinition){
                GenreDefinition genreDefinition = (GenreDefinition)mg;
                String gdString = genreDefinition.getValue();
                if ("cartographic".equalsIgnoreCase(gdString)){
                    return Genre.CARTOGRAPHIC;
                }
                if  ("sheetmusic".equalsIgnoreCase(gdString)){
                    return Genre.SHEETMUSIC;
                }
            }
        }
        return Genre.NONE;
    }
    /**
     * Encapsulate given String value into DC ElementType (used in JAXB DC object creation)
     * @param value
     * @return
     */
    protected ElementType createDcElementType(String value){
        ElementType dcid = dcObjectFactory.createElementType();
        dcid.setValue(value);
        return dcid;
    }

    protected static final String MC_GRP = "MC_IMGGRP";
    protected static final String UC_GRP = "UC_IMGGRP";
    protected static final String ALTO_GRP = "ALTOGRP";
    protected static final String TXT_GRP = "TXTGRP";
    protected static final String AMD_METS_GRP = "TECHMDGRP";


    protected StreamFileType getFileType(String filegrp){
        if (MC_GRP.equalsIgnoreCase(filegrp)){
            return StreamFileType.MASTER_IMAGE;
        } else if (UC_GRP.equalsIgnoreCase(filegrp)){
            return StreamFileType.USER_IMAGE;
        } else if (ALTO_GRP.equalsIgnoreCase(filegrp)){
            return StreamFileType.ALTO;
        } else if (TXT_GRP.equalsIgnoreCase(filegrp)){
            return StreamFileType.OCR;
        } else if (AMD_METS_GRP.equalsIgnoreCase(filegrp)){
            return StreamFileType.AMD;
        }
        throw new ServiceException("Unknown fileGrp: "+filegrp);
    }

    /**
     * Vytvori datastream obsahujici base64 zakodovana binarni data
     *
     * @param filename
     * @return stream
     */
    private DatastreamType createOCRStream(String filename) throws ServiceException {
        try {
            String streamType = KConfiguration.getInstance().getConfiguration().getString("convert.txt", "encoded");
            DatastreamType stream = new DatastreamType();
            stream.setID(STREAM_ID_TXT);
            if ("external".equalsIgnoreCase(streamType)){
                stream.setCONTROLGROUP("E");
            }else{
                stream.setCONTROLGROUP("M");
            }
            stream.setVERSIONABLE(false);
            stream.setSTATE(StateType.A);

            DatastreamVersionType version = new DatastreamVersionType();
            version.setCREATED(getCurrentXMLGregorianCalendar());
            version.setID(STREAM_ID_TXT + STREAM_VERSION_SUFFIX);

            version.setMIMETYPE(getImageMime(filename));

            // long start = System.currentTimeMillis();

            File pageFile = new File(getConfig().getImportFolder() + System.getProperty("file.separator") + filename);

            if ("encoded".equalsIgnoreCase(streamType)){
                byte[] binaryContent = FileUtils.readFileToByteArray(pageFile);
                version.setBinaryContent(binaryContent);
            }else{//external or referenced
                String binaryDirectory = getConfig().getExportFolder() + System.getProperty("file.separator") + "txt";
                // Destination directory
                File dir = IOUtils.checkDirectory(binaryDirectory);
                // Move file to new directory
                File target = new File(dir, pageFile.getName());
                //boolean success = pageFile.renameTo(target);
                //if (!success){
                    FileUtils.copyFile(pageFile, target);
                //}
                ContentLocationType cl = new ContentLocationType();
                String externalPrefix = KConfiguration.getInstance().getConfiguration().getString("convert.externalStreamsUrlPrefix");
                if (externalPrefix!= null && !"".equals(externalPrefix)){
                    cl.setREF(externalPrefix + "/"+PathEncoder.encPath(getConfig().getContract()+"/txt/"+pageFile.getName()));
                }else{
                    cl.setREF(PathEncoder.encPath(FILE_SCHEME_PREFIX + fixWindowsFileURL(target.getAbsolutePath())));
                }
                cl.setTYPE("URL");
                version.setContentLocation(cl);
            }
            stream.getDatastreamVersion().add(version);

            return stream;
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Vytvori datastream POLICY obsahujici odkaz na objekt s XACML pravidly ve
     * streamu POLICYDEF
     *
     * @param policyID
     *            identifikator odkazovaneho objektu
     * @return stream
     */
    private DatastreamType createPolicyStream(String policyID) throws ServiceException {
        DatastreamType stream = new DatastreamType();
        stream.setID(STREAM_ID_POLICY);
        stream.setCONTROLGROUP("E");
        stream.setVERSIONABLE(false);
        stream.setSTATE(StateType.A);

        DatastreamVersionType version = new DatastreamVersionType();
        version.setCREATED(getCurrentXMLGregorianCalendar());
        version.setID(STREAM_ID_POLICY + STREAM_VERSION_SUFFIX);
        version.setMIMETYPE("application/rdf+xml");
        ContentLocationType location = new ContentLocationType();
        location.setTYPE("URL");
        location.setREF("http://local.fedora.server/fedora/get/" + policyID + "/" + STREAM_ID_POLICY_DEF);
        version.setContentLocation(location);
        stream.getDatastreamVersion().add(version);

        return stream;
    }

    /**
     * Vytvori datastream obsahujici base64 zakodovana binarni data pro thumbnail
     *
     * @param img
     * @param filename
     * @return stream
     */
    private DatastreamType createFullStream(BufferedImage img, String filename, Foxml foxmlModel) throws ServiceException {
        try {
            String streamType = KConfiguration.getInstance().getConfiguration().getString("convert.files", "encoded");
            boolean convertToJPG = KConfiguration.getInstance().getConfiguration().getBoolean("convert.originalToJPG", false);
            boolean useImageServer = KConfiguration.getInstance().getConfiguration().getBoolean("convert.useImageServer", false);
            if (useImageServer){
                streamType = "external";
                convertToJPG=false;
            }
            DatastreamType stream = new DatastreamType();
            stream.setID(FedoraUtils.IMG_FULL_STREAM);
            if ("external".equalsIgnoreCase(streamType)){
                stream.setCONTROLGROUP("E");
            }else{
                stream.setCONTROLGROUP("M");
            }
            stream.setVERSIONABLE(false);
            stream.setSTATE(StateType.A);

            DatastreamVersionType version = new DatastreamVersionType();
            version.setCREATED(getCurrentXMLGregorianCalendar());
            version.setID(FedoraUtils.IMG_FULL_STREAM + STREAM_VERSION_SUFFIX);

            // long start = System.currentTimeMillis();

            String mime = getImageMime(filename);

            if (!convertToJPG || "application/pdf".equals(mime)){
                if (useImageServer){
                    version.setMIMETYPE("image/jpeg");
                }else{
                    version.setMIMETYPE(mime);
                }
                File pageFile = new File(getConfig().getImportFolder() + System.getProperty("file.separator") + filename);

                if ("encoded".equalsIgnoreCase(streamType)){
                    byte[] binaryContent = FileUtils.readFileToByteArray(pageFile);
                    version.setBinaryContent(binaryContent);
                }else{//external or referenced
                    String binaryDirectory = getConfig().getExportFolder() + System.getProperty("file.separator") + "img";
                    if (useImageServer){
                        String externalImagesDirectory = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerDirectory");
                        binaryDirectory = externalImagesDirectory + getConfig().getImgTreePath()+ System.getProperty("file.separator") + getConfig().getContract();
                    }

                    // Destination directory
                    File dir = IOUtils.checkDirectory(binaryDirectory);
                    // Move file to new directory
                    File target = new File(dir, pageFile.getName());
                    FileUtils.copyFile(pageFile, target);
                    ContentLocationType cl = new ContentLocationType();
                    if (useImageServer) {
                        String tilesPrefix = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerTilesURLPrefix")+ getConfig().getImgTreeUrl();
                        String imagesPrefix = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerImagesURLPrefix")+ getConfig().getImgTreeUrl();
                        String suffix = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerSuffix.big");

                        if(KConfiguration.getInstance().getConfiguration().getBoolean("convert.imageServerSuffix.removeFilenameExtensions", false)) {
                            String pageFileNameWithoutExtension = FilenameUtils.removeExtension(pageFile.getName());
                            cl.setREF(imagesPrefix + "/"+PathEncoder.encPath(getConfig().getContract()+"/"+pageFileNameWithoutExtension)+suffix);

                            //Adjust RELS-EXT
                            String suffixTiles = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerSuffix.tiles");
                            foxmlModel.getRe().addRelation(RelsExt.TILES_URL,tilesPrefix + "/"+PathEncoder.encPath(getConfig().getContract()+"/"+pageFileNameWithoutExtension)+ suffixTiles,true);
                        } else {
                            cl.setREF(imagesPrefix + "/" + PathEncoder.encPath(getConfig().getContract() + "/" + pageFile.getName()) + suffix);

                            //Adjust RELS-EXT
                            String suffixTiles = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerSuffix.tiles");
                            foxmlModel.getRe().addRelation(RelsExt.TILES_URL,tilesPrefix + "/"+PathEncoder.encPath(getConfig().getContract()+"/"+pageFile.getName())+ suffixTiles,true);
                        }

                    }   else{
                        String externalPrefix = KConfiguration.getInstance().getConfiguration().getString("convert.externalStreamsUrlPrefix");
                        if (externalPrefix!= null && !"".equals(externalPrefix)){
                            cl.setREF(externalPrefix + "/"+PathEncoder.encPath(getConfig().getContract()+"/img/"+pageFile.getName()));
                        }else{
                            cl.setREF(PathEncoder.encPath(FILE_SCHEME_PREFIX+fixWindowsFileURL(target.getAbsolutePath())));
                        }
                    }
                    cl.setTYPE("URL");
                    version.setContentLocation(cl);
                }
            }else{
                version.setMIMETYPE("image/jpeg");
                byte[] binaryContent = scaleImage(img, 0, 0);
                if (binaryContent.length == 0) {
                    return null;
                }

                if ("encoded".equalsIgnoreCase(streamType)){
                    version.setBinaryContent(binaryContent);
                }else{//external or referenced

                    String binaryDirectory = getConfig().getExportFolder() + System.getProperty("file.separator") + "img";
                    // Destination directory
                    File dir = IOUtils.checkDirectory(binaryDirectory);
                    // Move file to new directory
                    File target = new File(dir, filename.substring(filename.lastIndexOf("/"), filename.lastIndexOf('.'))+".jpg");
                    FileUtils.writeByteArrayToFile(target, binaryContent);

                    ContentLocationType cl = new ContentLocationType();
                    cl.setREF(PathEncoder.encPath(FILE_SCHEME_PREFIX+fixWindowsFileURL(target.getAbsolutePath())));
                    cl.setTYPE("URL");
                    version.setContentLocation(cl);
                }
            }

            stream.getDatastreamVersion().add(version);
            return stream;
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Vytvori datastream obsahujici base64 zakodovana binarni data pro thumbnail
     *
     * @param img
     * @param filename
     * @return stream
     */
    private DatastreamType createThumbnailStream(BufferedImage img, String filename) throws ServiceException {
        try {
            String streamType = KConfiguration.getInstance().getConfiguration().getString("convert.thumbnails", "encoded");
            boolean useImageServer = KConfiguration.getInstance().getConfiguration().getBoolean("convert.useImageServer", false);
            if (useImageServer){
                streamType = "external";
            }
            DatastreamType stream = new DatastreamType();
            stream.setID(FedoraUtils.IMG_THUMB_STREAM);
            if ("external".equalsIgnoreCase(streamType)){
                stream.setCONTROLGROUP("E");
            }else{
                stream.setCONTROLGROUP("M");
            }
            stream.setVERSIONABLE(false);
            stream.setSTATE(StateType.A);

            DatastreamVersionType version = new DatastreamVersionType();
            version.setCREATED(getCurrentXMLGregorianCalendar());
            version.setID(FedoraUtils.IMG_THUMB_STREAM + STREAM_VERSION_SUFFIX);

            version.setMIMETYPE("image/jpeg");

            byte[] binaryContent = null;
            if (!useImageServer){
                binaryContent = scaleImage(img, 0, FedoraUtils.THUMBNAIL_HEIGHT);
                if (binaryContent== null || binaryContent.length == 0) {
                    return null;
                }
            }

            if ("encoded".equalsIgnoreCase(streamType)){
                version.setBinaryContent(binaryContent);
            }else{//external or referenced
                ContentLocationType cl = new ContentLocationType();
                if (!useImageServer){
                    String binaryDirectory = getConfig().getExportFolder() + System.getProperty("file.separator") + "thumbnail";
                    // Destination directory
                    File dir = IOUtils.checkDirectory(binaryDirectory);
                    // Move file to new directory
                    File target = new File(dir, filename.substring(filename.lastIndexOf("/"), filename.lastIndexOf('.'))+".jpg");
                    FileUtils.writeByteArrayToFile(target, binaryContent);
                    String externalPrefix = KConfiguration.getInstance().getConfiguration().getString("convert.externalStreamsUrlPrefix");
                    if (externalPrefix!= null && !"".equals(externalPrefix)){
                        cl.setREF(externalPrefix + "/"+PathEncoder.encPath(getConfig().getContract()+"/thumbnail/"+filename.substring(filename.lastIndexOf("/"), filename.lastIndexOf('.'))+".jpg"));
                    }else{
                        cl.setREF(PathEncoder.encPath(FILE_SCHEME_PREFIX+fixWindowsFileURL(target.getAbsolutePath())));
                    }
                }else{
                    String imagesPrefix = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerImagesURLPrefix") + getConfig().getImgTreeUrl();
                	String suffix = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerSuffix.thumb");
                    if(KConfiguration.getInstance().getConfiguration().getBoolean("convert.imageServerSuffix.removeFilenameExtensions", false)) {
                        String pageFileNameWithoutExtension = FilenameUtils.removeExtension(filename.substring(filename.lastIndexOf("/")));
                        cl.setREF(imagesPrefix + "/"+PathEncoder.encPath(getConfig().getContract()+pageFileNameWithoutExtension+suffix));
                    } else {
                        cl.setREF(imagesPrefix + "/" + PathEncoder.encPath(getConfig().getContract() + filename.substring(filename.lastIndexOf("/"))) + suffix);
                    }
                }
                cl.setTYPE("URL");
                version.setContentLocation(cl);
            }

            stream.getDatastreamVersion().add(version);

            return stream;
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Vytvori datastream obsahujici base64 zakodovana binarni data pro preview
     *
     * @param img
     * @param filename
     * @return stream
     */
    private DatastreamType createPreviewStream(BufferedImage img, String filename) throws ServiceException {
        try {
            String streamType = KConfiguration.getInstance().getConfiguration().getString("convert.previews", "encoded");
            boolean useImageServer = KConfiguration.getInstance().getConfiguration().getBoolean("convert.useImageServer", false);
            if (useImageServer){
                streamType = "external";
            }
            DatastreamType stream = new DatastreamType();
            stream.setID(FedoraUtils.IMG_PREVIEW_STREAM);
            if ("external".equalsIgnoreCase(streamType)){
                stream.setCONTROLGROUP("E");
            }else{
                stream.setCONTROLGROUP("M");
            }
            stream.setVERSIONABLE(false);
            stream.setSTATE(StateType.A);

            DatastreamVersionType version = new DatastreamVersionType();
            version.setCREATED(getCurrentXMLGregorianCalendar());
            version.setID(FedoraUtils.IMG_PREVIEW_STREAM + STREAM_VERSION_SUFFIX);

            version.setMIMETYPE("image/jpeg");

            int previewSize =  KConfiguration.getInstance().getConfiguration().getInt("convert.previewSize", FedoraUtils.PREVIEW_HEIGHT);
            byte[] binaryContent = null;
            if (!useImageServer){
                binaryContent = scaleImage(img,previewSize, previewSize);
                if (binaryContent == null || binaryContent.length == 0) {
                    return null;
                }
            }


            if ("encoded".equalsIgnoreCase(streamType)){
                version.setBinaryContent(binaryContent);
            }else{//external or referenced
                ContentLocationType cl = new ContentLocationType();
                if (!useImageServer){
                    String binaryDirectory = getConfig().getExportFolder() + System.getProperty("file.separator") + "preview";
                    // Destination directory
                    File dir = IOUtils.checkDirectory(binaryDirectory);
                    // Move file to new directory
                    File target = new File(dir, filename.substring(filename.lastIndexOf("/"), filename.lastIndexOf('.'))+".jpg");
                    FileUtils.writeByteArrayToFile(target, binaryContent);
                    String externalPrefix = KConfiguration.getInstance().getConfiguration().getString("convert.externalStreamsUrlPrefix");
                    if (externalPrefix!= null && !"".equals(externalPrefix)){
                        cl.setREF(externalPrefix + "/"+PathEncoder.encPath(getConfig().getContract()+"/preview/"+filename.substring(filename.lastIndexOf("/"), filename.lastIndexOf('.'))+".jpg"));
                    }else{
                        cl.setREF(PathEncoder.encPath(FILE_SCHEME_PREFIX+fixWindowsFileURL(target.getAbsolutePath())));
                    }
                }else{
                    String imagesPrefix = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerImagesURLPrefix") + getConfig().getImgTreeUrl();
                    String suffix = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerSuffix.preview");
                    if(KConfiguration.getInstance().getConfiguration().getBoolean("convert.imageServerSuffix.removeFilenameExtensions", false)) {
                        String pageFileNameWithoutExtension = FilenameUtils.removeExtension(filename.substring(filename.lastIndexOf("/")));
                        cl.setREF(imagesPrefix +"/"+PathEncoder.encPath(getConfig().getContract()+pageFileNameWithoutExtension+suffix));
                    } else {
                        cl.setREF(imagesPrefix + "/" + PathEncoder.encPath(getConfig().getContract() + filename.substring(filename.lastIndexOf("/"))) + suffix);
                    }
                }
                cl.setTYPE("URL");
                version.setContentLocation(cl);
            }

            stream.getDatastreamVersion().add(version);

            return stream;
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }


    private DatastreamType createAltoStream(String filename) throws ServiceException {
        if (filename == null){
            return null;
        }

        try {
            File altoFile = new File(getConfig().getImportFolder() + System.getProperty("file.separator") + filename);
            if (! altoFile.exists() || !altoFile.canRead()) {
                return null;
            }
            String streamType = KConfiguration.getInstance().getConfiguration().getString("convert.txt", "encoded");
            DatastreamType stream = new DatastreamType();
            stream.setID(FedoraUtils.ALTO_STREAM);
            if ("external".equalsIgnoreCase(streamType)){
                stream.setCONTROLGROUP("E");
            }else{
                stream.setCONTROLGROUP("M");
            }
            stream.setVERSIONABLE(false);
            stream.setSTATE(StateType.A);

            DatastreamVersionType version = new DatastreamVersionType();
            version.setCREATED(getCurrentXMLGregorianCalendar());
            version.setID(FedoraUtils.ALTO_STREAM + STREAM_VERSION_SUFFIX);

            version.setMIMETYPE("text/xml");

            // long start = System.currentTimeMillis();


            if ("encoded".equalsIgnoreCase(streamType)){
                byte[] binaryContent = FileUtils.readFileToByteArray(altoFile);
                version.setBinaryContent(binaryContent);
            }else{//external or referenced
                String binaryDirectory = getConfig().getExportFolder() + System.getProperty("file.separator") + "txt";
                // Destination directory
                File dir = IOUtils.checkDirectory(binaryDirectory);
                // Move file to new directory
                File target = new File(dir, altoFile.getName());
                FileUtils.copyFile(altoFile, target);
                ContentLocationType cl = new ContentLocationType();
                String externalPrefix = KConfiguration.getInstance().getConfiguration().getString("convert.externalStreamsUrlPrefix");
                if (externalPrefix!= null && !"".equals(externalPrefix)){
                    cl.setREF(externalPrefix + "/"+PathEncoder.encPath(getConfig().getContract()+"/txt/"+altoFile.getName()));
                }else{
                    cl.setREF(PathEncoder.encPath(FILE_SCHEME_PREFIX+fixWindowsFileURL(target.getAbsolutePath())));
                }
                cl.setTYPE("URL");
                version.setContentLocation(cl);
            }
            stream.getDatastreamVersion().add(version);

            return stream;
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }


    private DatastreamType createAMDStream(String filename) throws ServiceException {
        if (filename == null){
            return null;
        }

        try {
            File amdFile = new File(getConfig().getImportFolder() + System.getProperty("file.separator") + filename);
            if (! amdFile.exists() || !amdFile.canRead()) {
                return null;
            }
            String streamType = KConfiguration.getInstance().getConfiguration().getString("convert.amd", "encoded");
            DatastreamType stream = new DatastreamType();
            stream.setID(FedoraUtils.IMG_FULL_STREAM+"_AMD");
            if ("external".equalsIgnoreCase(streamType)){
                stream.setCONTROLGROUP("E");
            }else{
                stream.setCONTROLGROUP("M");
            }
            stream.setVERSIONABLE(false);
            stream.setSTATE(StateType.A);

            DatastreamVersionType version = new DatastreamVersionType();
            version.setCREATED(getCurrentXMLGregorianCalendar());
            version.setID(FedoraUtils.IMG_FULL_STREAM+"_AMD" + STREAM_VERSION_SUFFIX);

            version.setMIMETYPE("text/xml");

            // long start = System.currentTimeMillis();


            if ("encoded".equalsIgnoreCase(streamType)){
                byte[] binaryContent = FileUtils.readFileToByteArray(amdFile);
                version.setBinaryContent(binaryContent);
            }else{//external or referenced
                String binaryDirectory = getConfig().getExportFolder() + System.getProperty("file.separator") + "amd";
                // Destination directory
                File dir = IOUtils.checkDirectory(binaryDirectory);
                // Move file to new directory
                File target = new File(dir, amdFile.getName());
                FileUtils.copyFile(amdFile, target);
                ContentLocationType cl = new ContentLocationType();
                String externalPrefix = KConfiguration.getInstance().getConfiguration().getString("convert.externalStreamsUrlPrefix");
                if (externalPrefix!= null && !"".equals(externalPrefix)){
                    cl.setREF(externalPrefix + "/"+PathEncoder.encPath(getConfig().getContract()+"/amd/"+amdFile.getName()));
                }else{
                    cl.setREF(PathEncoder.encPath(FILE_SCHEME_PREFIX+fixWindowsFileURL(target.getAbsolutePath())));
                }
                cl.setTYPE("URL");
                version.setContentLocation(cl);
            }
            stream.getDatastreamVersion().add(version);

            return stream;
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }


    private DatastreamType createEncodedStream(String streamID, String mimeType, String contents) throws ServiceException {

        try {
            DatastreamType stream = new DatastreamType();
            stream.setID(streamID);
            stream.setCONTROLGROUP("M");
            stream.setVERSIONABLE(false);
            stream.setSTATE(StateType.A);

            DatastreamVersionType version = new DatastreamVersionType();
            version.setCREATED(getCurrentXMLGregorianCalendar());
            version.setID(streamID + STREAM_VERSION_SUFFIX);
            version.setMIMETYPE(mimeType);

            byte[] binaryContent = contents.getBytes("UTF-8");
            version.setBinaryContent(binaryContent);

            stream.getDatastreamVersion().add(version);
            return stream;
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }


    private BufferedImage readImage(String fileName)throws IOException, MalformedURLException{
        return KrameriusImageSupport.readImage(new URL(FILE_SCHEME_PREFIX+fixWindowsFileURL(fileName)), ImageMimeType.loadFromMimeType(getImageMime(fileName)), 0);
    }

    private byte[] scaleImage(BufferedImage img, int width, int height) throws IOException, MalformedURLException {

        if (img != null) {
            BufferedImage scaledImage = scaleByHeightOrWidth(img, width, height);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try{
                ImageIO.setUseCache(KrameriusImageSupport.useCache());
                ImageIO.write(scaledImage, "jpg", outputStream);
                return outputStream.toByteArray();
            }finally{
                outputStream.close();
            }
        }
        return new byte[0];
    }

    private BufferedImage scaleByHeightOrWidth(BufferedImage img, int newWidth, int newHeight ) {
        if (newWidth == 0 && newHeight == 0){
            return img;
        }

        ImageObserver observer = new ImageObserver() {

            public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                return false;
            }
        };

        int nWidth=0, nHeight =0;

        if(newHeight >0){
            nHeight = newHeight;
            double div = (double) img.getHeight(observer) / (double) nHeight;
            nWidth =  (int) (img.getWidth(observer) / div);
            if (newWidth>0 &&nWidth>newWidth){
                nWidth = newWidth;
                div = (double) img.getWidth(observer) / (double) nWidth;
                nHeight =  (int) (img.getHeight(observer) / div);
            }
        }else if (newWidth >0){
            nWidth = newWidth;
            double div = (double) img.getWidth(observer) / (double) nWidth;
            nHeight =  (int) (img.getHeight(observer) / div);
            if (newHeight>0 && nHeight>newHeight){
                nHeight = newHeight;
                div = (double) img.getHeight(observer) / (double) nHeight;
                nWidth =  (int) (img.getWidth(observer) / div);
            }
        }
        BufferedImage scaledImage = KrameriusImageSupport.scale(img, nWidth, nHeight);
        return scaledImage;
    }



    private static final String NS_ADM =  "http://www.qbizm.cz/kramerius-fedora/image-adm-description";

    private DatastreamType createImageMetaStream(String id, ImageMetaData data) throws ServiceException {
        DatastreamType stream = new DatastreamType();
        stream.setID(id);
        stream.setCONTROLGROUP("X");
        stream.setSTATE(StateType.A);
        stream.setVERSIONABLE(false);

        DatastreamVersionType version = new DatastreamVersionType();
        version.setID(id + STREAM_VERSION_SUFFIX);
        version.setLABEL("Image administrative metadata");
        version.setMIMETYPE("text/xml");
        version.setCREATED(getCurrentXMLGregorianCalendar());

        XmlContentType xmlContent = new XmlContentType();
        version.setXmlContent(xmlContent);

        Document document = docBuilder.newDocument();

        Element root = document.createElementNS(NS_ADM, "adm:Description");

        if (!StringUtils.isEmpty(data.getUrn())) {
            this.appendChildNS(document, root,NS_ADM, "adm:URN", data.getUrn());
        }
        if (!StringUtils.isEmpty(data.getSici())) {
            this.appendChildNS(document, root,NS_ADM, "adm:SICI", data.getSici());
        }
        if (!StringUtils.isEmpty(data.getScanningDevice())) {
            this.appendChildNS(document, root,NS_ADM, "adm:ScanningDevice", data.getScanningDevice());
        }
        if (!StringUtils.isEmpty(data.getScanningParameters())) {
            this.appendChildNS(document, root,NS_ADM, "adm:ScanningParameters", data.getScanningParameters());
        }
        if (!StringUtils.isEmpty(data.getOtherImagingInformation())) {
            this.appendChildNS(document, root,NS_ADM, "adm:OtherImagingInformation", data.getOtherImagingInformation());
        }

        xmlContent.getAny().add(root);

        stream.getDatastreamVersion().add(version);
        return stream;
    }

    private static final String NS_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String NS_FEDORA = "info:fedora/fedora-system:def/model#";
    private static final String NS_KRAMERIUS = "http://www.nsdl.org/ontologies/relationships#";
    private static final String NS_OAI = "http://www.openarchives.org/OAI/2.0/";



    protected Element createRelsExtElement(RelsExt relsExt){
        Document document = docBuilder.newDocument();

        Element root = document.createElementNS(NS_RDF, "rdf:RDF");

        root.setAttribute("xmlns:fedora-model", NS_FEDORA);
        root.setAttribute("xmlns:" + CUSTOM_MODEL_PREFIX, NS_KRAMERIUS);
        root.setAttribute("xmlns:oai", NS_OAI);

        Element description = this.appendChildNS(document, root,NS_RDF, "rdf:Description", "");
        description.setAttributeNS(NS_RDF, "rdf:about", "info:fedora/" + relsExt.getPid());

        String modelPrefix;
        String relNs;
        for (RelsExt.Relation rel : relsExt.getRelations()) {
            if (RelsExt.HAS_MODEL.equals(rel.getKey())){
                modelPrefix =  "fedora-model";
                relNs = NS_FEDORA;
            }else if(RelsExt.ITEM_ID.equals(rel.getKey())){
                modelPrefix =  "oai";
                relNs = NS_OAI;
            }else{
                modelPrefix = CUSTOM_MODEL_PREFIX;
                relNs = NS_KRAMERIUS;
            }
            Element relElement = this.appendChildNS(document, description,relNs, modelPrefix + ":" + rel.getKey(), rel.isLiteral() ? rel.getId() : "");
            if (!rel.isLiteral()) {
                relElement.setAttributeNS(NS_RDF,"rdf:resource", "info:fedora/" + rel.getId());
            }
        }
        return root;
    }
    /**
     * Vytvori rels-ext datastream
     *
     * @param root
     * @throws ServiceException
     */
    private DatastreamType createRelsExtStream(Element root) throws ServiceException {
        DatastreamType stream = new DatastreamType();
        stream.setID(STREAM_ID_RELS_EXT);
        stream.setCONTROLGROUP("X");
        stream.setVERSIONABLE(false);
        stream.setSTATE(StateType.A);

        DatastreamVersionType version = new DatastreamVersionType();
        version.setCREATED(getCurrentXMLGregorianCalendar());
        version.setFORMATURI("info:fedora/fedora-system:FedoraRELSExt-1.0");
        version.setLABEL("RDF Statements about this object");
        version.setMIMETYPE("application/rdf+xml");
        version.setID(STREAM_ID_RELS_EXT + STREAM_VERSION_SUFFIX);

        XmlContentType xmlContent = new XmlContentType();
        version.setXmlContent(xmlContent);


        xmlContent.getAny().add(root);

        stream.getDatastreamVersion().add(version);
        return stream;
    }

    private XMLGregorianCalendar getCurrentXMLGregorianCalendar() {
        Calendar now = Calendar.getInstance();
        return XMLGregorianCalendarImpl.createDateTime(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY), now
                .get(Calendar.MINUTE), now.get(Calendar.SECOND));
    }

    protected String trimNull(String s) {
        return (s == null) ? "" : s;
    }

    //used in METSC
    protected String first(List<? extends Object> list) {
        return (list == null || list.size()==0 || list.get(0) == null) ? StringUtils.EMPTY : list.get(0).toString();
    }

    protected <T> T firstItem(List<T> list) {
        return (list == null || list.size()==0 || list.get(0) == null) ? null : list.get(0);
    }

    protected String concat(List<String> list) {
        if (list == null) return null;
        StringBuffer sb = new StringBuffer();
        for (String st : list){
            if (st != null){
                sb.append(st);
            }
        }
        return sb.length()>0?sb.toString():null;
    }


    protected void fillPageIdMap(Map<Integer, String> pageIdMap,
            String pageIndex, String ppid) {
        if (pageIndex != null) {
            String pageNumberStr = pageIndex.replaceAll("[^0-9]", "");
            if (NumberUtils.isDigits(pageNumberStr)) {
                Integer pageIndexInt = Integer.valueOf(pageNumberStr);
                pageIdMap.put(pageIndexInt, ppid);
            } else {
                log.warn("Page index invalid! Data inconsistency warning!!");
            }
        } else {
            log.warn("Page index missing! Data inconsistency warning!!");
        }
    }

    /**
     * @param re
     * @param piFrom
     * @param piTo
     * @param pageIdMap
     */
    protected void processPageIndex(RelsExt re, Integer piFrom, Integer piTo, Map<Integer, String> pageIdMap) {
        for (Map.Entry<Integer, String> e : pageIdMap.entrySet()) {
            Integer pageNumber =  e.getKey();
            if (pageNumber.compareTo(piFrom) >= 0 && pageNumber.compareTo(piTo) <= 0) {
                re.addRelation(RelsExt.IS_ON_PAGE, e.getValue(), false);
            }
        }
    }

    protected String generateUUID() throws ServiceException {
        String uuid = UUIDManager.generateUUID().toString();
        if (log.isDebugEnabled()) {
            log.debug("Generated new UUID: " + uuid);
        }
        return uuid;
    }

    public ConvertorConfig getConfig() {
        return config;
    }

    public int getObjectCounter() {
        return objectCounter;
    }

    /**
     * extracts filename from filename with or without sigla
     *
     * @param siglaName
     * @return
     */
    protected String removeSigla(String siglaName) {
        String fileName = siglaName;
        if (siglaName.matches("^[a-zA-Z]{3}\\d{3}_.*")) {
            fileName = siglaName.substring(7);
        }
        return fileName;
    }

    /**
     * Vytvori digitalni objekt dle zadanych parametru vcetne datastreamu
     *
     * @param foxml
     * @throws ServiceException
     * @return
     */
    protected DigitalObject createDigitalObject(  Foxml foxml) throws ServiceException {

        String pid = foxml.getPid();
        String title = foxml.getTitle();
        Element dc =  createDublinCoreElement(foxml.getDc());
        Element mods =  createBiblioModsElement(foxml.getMods());
        FileDescriptor[] files = null;
        if (foxml.getFiles()!= null && foxml.getFiles().size()>0){
            files = foxml.getFiles().toArray(new FileDescriptor[foxml.getFiles().size()]);
        }
        if (log.isInfoEnabled()) {
            log.info(  "title=" + title + "; pid=" + pid); //TODO: log model
        }



        DigitalObject foxmlObject = new DigitalObject();

        this.setCommonProperties(foxmlObject, pid, title);


        this.setCommonStreams(foxmlObject, dc, mods, policyID, files, foxml);

        return foxmlObject;
    }

    protected void exportFoxml(Foxml foxml){

        try{
            DigitalObject foxmlPeri = this.createDigitalObject( foxml);

            // /== RELS-EXT stream
            Element re = createRelsExtElement(foxml.getRe());
            DatastreamType relsExtStream = this.createRelsExtStream(re);
            addCheckedDataStream(foxmlPeri, relsExtStream);
            // \== RELS-EXT stream


            if (foxml.getOcr()!= null){
                addCheckedDataStream(foxmlPeri, createEncodedStream(STREAM_ID_TXT, "text/plain", foxml.getOcr()));
            }
            if (foxml.getStruct()!= null){
                addCheckedDataStream(foxmlPeri, createEncodedStream("STRUCT_MAP", "text/xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<parts>\n"+foxml.getStruct()+"</parts>\n"));
                //System.out.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<parts>\n"+foxml.getStruct()+"</parts>");
            }
            this.marshalDigitalObject(foxmlPeri);
        }catch (ServiceException ex){
            log.error("Error marshalling FOXML:"+foxml.getTitle());
            throw ex;
        }
    }

    protected boolean isPublic(String pid, boolean parentPublic, String tableName) {
        boolean retval = parentPublic;

        Connection con = config.getDbConnection();
        if (con != null) {
            int oldPublic = PFLAG_PRIVATE;
            Statement st = null;
            ResultSet rs = null;
            try {
                st = con.createStatement();
                rs = st.executeQuery("select cc.publicflag from " + tableName
                        + " t left outer join customizablecomponent cc on t.id_cc = cc.id where t.ui_uniqueidentifierurntype = \'" + pid + "\'");
                if (rs.next()) {
                    oldPublic = rs.getInt(1);
                }
            } catch (SQLException ex) {
                log.error("Error in reading visibility",ex );
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (st != null) {
                        st.close();
                    }
                    // con.close(); connection will be closed in the Main class
                } catch (SQLException eex) {

                }
            }
            if (oldPublic != PFLAG_INHERIT) {
                retval = (oldPublic == PFLAG_PUBLIC);
            }
        }
        return retval;
    }

    protected void convertHandle(String pid, DublinCore dc, RelsExt re) {
        String handle = null;

        Connection con = config.getDbConnection();
        if (con != null) {
            Statement st = null;
            ResultSet rs = null;
            try {
                st = con.createStatement();
                rs = st.executeQuery("select handle from handle where resourceuuid = \'" + pid + "\'");
                if (rs.next()) {
                    handle = rs.getString(1);
                }
            } catch (SQLException ex) {
                log.error("Error in reading handle",ex );
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (st != null) {
                        st.close();
                    }
                    // con.close(); connection will be closed in the Main class
                } catch (SQLException eex) {

                }
            }
        }
        dc.addQualifiedIdentifier(RelsExt.HANDLE, handle);
        re.addRelation(RelsExt.HANDLE, handle, true);
    }

    protected String pid(String uuid){
        return PID_PREFIX + uuid;
    }

    private String fixWindowsFileURL(String url){
        if(url == null){
            return null;
        }
        if (url.startsWith("/")){
            return url;
        }else{
            return "/"+url;
        }
    }

}
