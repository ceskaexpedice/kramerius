package com.qbizm.kramerius.imptool.poc.convertor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.qbizm.kramerius.imp.jaxb.DatastreamType;
import com.qbizm.kramerius.imp.jaxb.DatastreamVersionType;
import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import com.qbizm.kramerius.imp.jaxb.ObjectPropertiesType;
import com.qbizm.kramerius.imp.jaxb.PropertyType;
import com.qbizm.kramerius.imp.jaxb.StateType;
import com.qbizm.kramerius.imp.jaxb.XmlContentType;
import com.qbizm.kramerius.imptool.poc.utils.UUIDManager;
import com.qbizm.kramerius.imptool.poc.utils.XSLTransformer;
import com.qbizm.kramerius.imptool.poc.valueobj.ConvertorConfig;
import com.qbizm.kramerius.imptool.poc.valueobj.DublinCore;
import com.qbizm.kramerius.imptool.poc.valueobj.ImageMetaData;
import com.qbizm.kramerius.imptool.poc.valueobj.ImageRepresentation;
import com.qbizm.kramerius.imptool.poc.valueobj.RelsExt;
import com.qbizm.kramerius.imptool.poc.valueobj.ServiceException;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

public abstract class BaseConvertor {

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

  private static final String CUSTOM_MODEL_PREFIX = "kramerius";

  /**
   * Nazvy a konstanty datastreamu
   */
  private static final String STREAM_ID_TXT = "TEXT_OCR";

  private static final String STREAM_ID_IMG = "IMG_FULL";

  private static final String STREAM_ID_MODS = "BIBLIO_MODS";

  private static final String STREAM_ID_RELS_EXT = "RELS-EXT";

  private static final String STREAM_VERSION_SUFFIX = ".1";

  private static final String SUFFIX_TXT = "txt";

  protected static final String XSL_PATH = "";

  /**
   * Atributy
   */
  private final ConvertorConfig config;

  private final DocumentBuilder docBuilder;

  private final Map<String, String> mimeMap = new TreeMap<String, String>();

  private SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

  private int objectCounter;
  
  public BaseConvertor(ConvertorConfig config) throws ServiceException {
    this.config = config;

    try {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      this.docBuilder = documentBuilderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new ServiceException(e);
    }

    mimeMap.put("txt", "text/plain");
    mimeMap.put("djvu", "image/djvu");
    mimeMap.put("jpg", "image/jpeg");
    mimeMap.put("jpeg", "image/jpeg");
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

    setProperty(digitalObject, "info:fedora/fedora-system:def/model#label", title);
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
   * @param sourceObject
   * @param dc
   * @param re
   * @param xslFile
   * @param files
   * @throws ServiceException
   */
  protected void setCommonStreams(
    DigitalObject foxmlObject,
    Object sourceObject,
    DublinCore dc,
    RelsExt re,
    String xslFile,
    ImageRepresentation[] files) throws ServiceException {
    // /== DUBLIN CORE
    DatastreamType dcStream = this.createDublinCoreStream(dc);
    foxmlObject.getDatastream().add(dcStream);
    // \== DUBLIN CORE

    // /== BASE64 stream
    this.addBase64Streams(foxmlObject, files);
    // \== BASE64 stream

    // /== BIBLIO_MODS stream
    DatastreamType biblioModsStream = this.createBiblioModsStream(sourceObject, xslFile);
    foxmlObject.getDatastream().add(biblioModsStream);
    // \== BIBLIO_MODS stream

    // /== RELS-EXT stream
    DatastreamType relsExtStream = this.createRelsExtStream(re);
    foxmlObject.getDatastream().add(relsExtStream);
    // \== RELS-EXT stream
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

  /**
   * Ulozeni digitalniho objektu
   * 
   * @param foxmlObject
   * @throws ServiceException
   */
  protected void marshalDigitalObject(DigitalObject foxmlObject) throws ServiceException {
    String fileName = foxmlObject.getPID().substring(foxmlObject.getPID().lastIndexOf(':') + 1)
      + ".xml";
    this.marshalDigitalObject(foxmlObject, getConfig().getExportFolder(), fileName);
  }

  /**
   * Ulozeni digitalniho objektu
   * 
   * @param foxmlObject
   * @throws ServiceException
   */
  private void marshalDigitalObject(DigitalObject foxmlObject, String directory, String file)
    throws ServiceException {
    if (!Pattern.matches(PID_PATTERN, foxmlObject.getPID())) {
      throw new ServiceException("Invalid PID format: " + foxmlObject.getPID());
    }
    try {
      long start = System.currentTimeMillis();
      String path = directory + System.getProperty("file.separator") + file;
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

  /**
   * Vytvori rels-ext datastream
   * 
   * @param foxmlPage
   * @param dcStream
   * @throws ServiceException
   */
  private DatastreamType createDublinCoreStream(DublinCore dc) throws ServiceException {
    DatastreamType stream = new DatastreamType();
    stream.setID("DC");
    stream.setSTATE(StateType.A);
    stream.setVERSIONABLE(true);
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

    Document document = docBuilder.newDocument();

    Element root = document.createElementNS(
      "http://www.openarchives.org/OAI/2.0/oai_dc/",
      "oai_dc:dc");
    // root.setAttributeNS(
    // "http://www.w3.org/2001/XMLSchema-instance",
    // "xsi:schemaLocation",
    // "http://www.openarchives.org/OAI/2.0/oai_dc/ "
    // + "http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
    root.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");

    this.appendChild(document, root, "dc:title", dc.getTitle());

    if (dc.getCreator() != null) {
      for (String creator : dc.getCreator()) {
        this.appendChild(document, root, "dc:creator", creator);
      }
    }
    if (dc.getPublisher() != null) {
      for (String publisher : dc.getPublisher()) {
        this.appendChild(document, root, "dc:publisher", publisher);
      }
    }
    if (dc.getContributor() != null) {
      for (String contributor : dc.getContributor()) {
        this.appendChild(document, root, "dc:contributor", contributor);
      }
    }

    xmlContent.getAny().add(root);

    return stream;
  }

  /**
   * Vytvori MODS datastream pomoci xsl transformace
   * 
   * @param page
   * @return
   * @throws ServiceException
   */
  private DatastreamType createBiblioModsStream(Object page, String xslFile)
    throws ServiceException {
    DatastreamType stream = new DatastreamType();
    stream.setID(STREAM_ID_MODS);
    stream.setCONTROLGROUP("X");
    stream.setSTATE(StateType.A);
    stream.setVERSIONABLE(true);

    DatastreamVersionType version = new DatastreamVersionType();
    version.setID(STREAM_ID_MODS + STREAM_VERSION_SUFFIX);
    version.setLABEL("BIBLIO_MODS description of current object");
    version.setMIMETYPE("text/xml");
    version.setCREATED(getCurrentXMLGregorianCalendar());

    if (StringUtils.isEmpty(xslFile)) {
      return stream;
    }

    try {
      ByteArrayOutputStream sourceOut = new ByteArrayOutputStream();

      InputStream stylesheet = this.getClass().getClassLoader().getResourceAsStream(
        XSL_PATH + xslFile);

      getConfig().getMarshaller().marshal(page, sourceOut);

      // if (log.isDebugEnabled()) {
      // log.debug("XML source: " + sourceOut.toString());
      // }

      ByteArrayInputStream sourceIn = new ByteArrayInputStream(sourceOut.toByteArray());

      // ByteArrayOutputStream result = new ByteArrayOutputStream();
      Document mods = XSLTransformer.transform(sourceIn, stylesheet);
      Element root = mods.getDocumentElement();

      // if (log.isDebugEnabled()) {
      // log.debug("XSLT output: " + XSLTransformer.documentToString(mods));
      // }
      XmlContentType xmlContent = new XmlContentType();
      xmlContent.getAny().add(root);

      version.setXmlContent(xmlContent);
      // version.setBinaryContent(result.toByteArray());

      // if (log.isDebugEnabled()) {
      // log.debug("XSLT result: " + result.toString());
      // }
    } catch (JAXBException e) {
      throw new ServiceException(e);
    }

    stream.getDatastreamVersion().add(version);

    return stream;
  }

  /**
   * Vytvori base64 datastreamy pro dany objekt
   * 
   * @param foxmlObject
   * @param files
   * @throws ServiceException
   */
  private void addBase64Streams(DigitalObject foxmlObject, ImageRepresentation[] files)
    throws ServiceException {
    if (files != null) {
      for (ImageRepresentation f : files) {
        if (f != null) {
          File imageFile = new File(getConfig().getImportFolder() + "/" + f.getFilename());
          if (imageFile.exists() && imageFile.canRead()) {
            DatastreamType base64Stream = this.createBase64Stream(f.getFilename());
            foxmlObject.getDatastream().add(base64Stream);

            if (f.getImageMetaData() != null) {
              DatastreamType imageAdmStream = this.createImageMetaStream(
                getBase64StreamId(f.getFilename()) + "_ADM",
                f.getImageMetaData());
              foxmlObject.getDatastream().add(imageAdmStream);
            }
          } else {
            log.warn(WARN_FILE_DOESNT_EXIST + ": " + f.getFilename());
          }
        }
      }
    }
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

  /**
   * Je zadany soubor obrazek?
   */
  private boolean isImage(String filename) {
    return !SUFFIX_TXT.equals(getSuffix(filename));
  }

  private String getBase64StreamId(String filename) {
    return (isImage(filename)) ? STREAM_ID_IMG : STREAM_ID_TXT;
  }

  private String getImageMime(String filename) {
    return mimeMap.get(getSuffix(filename));
  }

  /**
   * Vytvori datastream obsahujici base64 zakodovana binarni data
   * 
   * @param pageHref
   * @return stream
   */
  private DatastreamType createBase64Stream(String filename) throws ServiceException {
    try {
      String streamId = getBase64StreamId(filename);

      DatastreamType stream = new DatastreamType();
      stream.setID(streamId);
      stream.setCONTROLGROUP("M");
      stream.setVERSIONABLE(true);
      stream.setSTATE(StateType.A);

      DatastreamVersionType version = new DatastreamVersionType();
      version.setCREATED(getCurrentXMLGregorianCalendar());
      version.setID(streamId + STREAM_VERSION_SUFFIX);

      version.setMIMETYPE(getImageMime(filename));

      // long start = System.currentTimeMillis();

      File pageFile = new File(getConfig().getImportFolder()
        + System.getProperty("file.separator")
        + filename);
      byte[] binaryContent = FileUtils.readFileToByteArray(pageFile);

      version.setBinaryContent(binaryContent);

      // if (log.isDebugEnabled()) {
      // log.debug("Binary attachment: time(read)="
      // + (end - start)
      // + "ms; filesize="
      // + (pageFile.length() / 1024)
      // + "kB; file="
      // + pageFile.getName());
      // }
      stream.getDatastreamVersion().add(version);

      return stream;
    } catch (IOException e) {
      throw new ServiceException(e);
    }
  }

  private DatastreamType createImageMetaStream(String id, ImageMetaData data)
    throws ServiceException {
    DatastreamType stream = new DatastreamType();
    stream.setID(id);
    stream.setCONTROLGROUP("X");
    stream.setSTATE(StateType.A);
    stream.setVERSIONABLE(true);

    DatastreamVersionType version = new DatastreamVersionType();
    version.setID(id + STREAM_VERSION_SUFFIX);
    version.setLABEL("Image administrative metadata");
    version.setMIMETYPE("text/xml");
    version.setCREATED(getCurrentXMLGregorianCalendar());

    XmlContentType xmlContent = new XmlContentType();
    version.setXmlContent(xmlContent);

    Document document = docBuilder.newDocument();

    Element root = document.createElementNS(
      "http://www.qbizm.cz/kramerius-fedora/image-adm-description",
      "adm:Description");

    if (!StringUtils.isEmpty(data.getUrn())) {
      this.appendChild(document, root, "adm:URN", data.getUrn());
    }
    if (!StringUtils.isEmpty(data.getSici())) {
      this.appendChild(document, root, "adm:SICI", data.getSici());
    }
    if (!StringUtils.isEmpty(data.getScanningDevice())) {
      this.appendChild(document, root, "adm:ScanningDevice", data.getScanningDevice());
    }
    if (!StringUtils.isEmpty(data.getScanningParameters())) {
      this.appendChild(document, root, "adm:ScanningParameters", data.getScanningParameters());
    }
    if (!StringUtils.isEmpty(data.getOtherImagingInformation())) {
      this.appendChild(
        document,
        root,
        "adm:OtherImagingInformation",
        data.getOtherImagingInformation());
    }

    xmlContent.getAny().add(root);

    stream.getDatastreamVersion().add(version);
    return stream;
  }

  /**
   * Vytvori rels-ext datastream
   * 
   * @param foxmlPage
   * @param dcStream
   * @throws ServiceException
   */
  private DatastreamType createRelsExtStream(RelsExt relsExt) throws ServiceException {
    DatastreamType stream = new DatastreamType();
    stream.setID(STREAM_ID_RELS_EXT);
    stream.setCONTROLGROUP("X");
    stream.setVERSIONABLE(true);
    stream.setSTATE(StateType.A);

    DatastreamVersionType version = new DatastreamVersionType();
    version.setCREATED(getCurrentXMLGregorianCalendar());
    version.setFORMATURI("info:fedora/fedora-system:FedoraRELSExt-1.0");
    version.setLABEL("RDF Statements about this object");
    version.setMIMETYPE("application/rdf+xml");
    version.setID(STREAM_ID_RELS_EXT + STREAM_VERSION_SUFFIX);

    XmlContentType xmlContent = new XmlContentType();
    version.setXmlContent(xmlContent);

    Document document = docBuilder.newDocument();

    Element root = document.createElementNS(
      "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
      "rdf:RDF");
    root.setAttribute("xmlns:fedora-model", "info:fedora/fedora-system:def/model#");
    root.setAttribute(
      "xmlns:" + CUSTOM_MODEL_PREFIX,
      "http://www.nsdl.org/ontologies/relationships#");

    Element description = this.appendChild(document, root, "rdf:Description", "");
    description.setAttribute("rdf:about", "info:fedora/" + relsExt.getPid());

    String modelPrefix;
    for (RelsExt.Relation rel : relsExt.getRelations()) {
      modelPrefix = (RelsExt.HAS_MODEL.equals(rel.getKey()) ? "fedora-model" : CUSTOM_MODEL_PREFIX);
      Element relElement = this.appendChild(
        document,
        description,
        modelPrefix + ":" + rel.getKey(),
        "");
      relElement.setAttribute("rdf:resource", "info:fedora/" + rel.getId());
    }

    xmlContent.getAny().add(root);

    stream.getDatastreamVersion().add(version);
    return stream;
  }

  private XMLGregorianCalendar getCurrentXMLGregorianCalendar() {
    Calendar now = Calendar.getInstance();
    return XMLGregorianCalendarImpl.createDateTime(
      now.get(Calendar.YEAR),
      now.get(Calendar.MONTH) + 1,
      now.get(Calendar.DAY_OF_MONTH),
      now.get(Calendar.HOUR_OF_DAY),
      now.get(Calendar.MINUTE),
      now.get(Calendar.SECOND));
  }

  protected String trimNull(String s) {
    return (s == null) ? "" : s;
  }

  protected String first(List<? extends Object> list) {
    return (list == null || list.get(0) == null) ? StringUtils.EMPTY : list.get(0).toString();
  }

  /**
   * @param re
   * @param piFrom
   * @param piTo
   * @param pageIdMap
   */
  protected void processPageIndex(
    RelsExt re,
    Integer piFrom,
    Integer piTo,
    Map<String, String> pageIdMap) {
    for (Map.Entry<String, String> e : pageIdMap.entrySet()) {
      String pageNumberStr = e.getKey().replaceAll("[^0-9]", "");
      if (NumberUtils.isDigits(pageNumberStr)) {
        Integer pageNumber = Integer.valueOf(pageNumberStr);
        if (pageNumber.compareTo(piFrom) >= 0 && pageNumber.compareTo(piTo) <= 0) {
          re.addRelation(RelsExt.IS_ON_PAGE, e.getValue());
        }
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

}
