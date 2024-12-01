package cz.incad.kramerius.fedora.om.impl;

import com.qbizm.kramerius.imp.jaxb.DatastreamType;
import com.qbizm.kramerius.imp.jaxb.DatastreamVersionType;
import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import com.qbizm.kramerius.imp.jaxb.PropertyType;
import cz.incad.kramerius.utils.SafeSimpleDateFormat;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.akubraproject.map.IdMapper;
import org.apache.commons.io.IOUtils;
import org.fcrepo.common.PID;
import org.fcrepo.server.storage.lowlevel.akubra.HashPathIdMapper;
import org.w3c.dom.Element;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class AkubraUtils {
    private static final Logger LOGGER = Logger.getLogger(AkubraUtils.class.getName());
    public static final SafeSimpleDateFormat DATE_FORMAT = new SafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");

    private AkubraUtils() {
    }

    public static DatastreamVersionType getLastStreamVersion(DigitalObject object, String streamID) {
        for (DatastreamType datastreamType : object.getDatastream()) {
            if (streamID.equals(datastreamType.getID())) {
                return getLastStreamVersion(datastreamType);
            }
        }
        return null;
    }

    public static DatastreamVersionType getLastStreamVersion(DatastreamType datastreamType) {
        List<DatastreamVersionType> datastreamVersionList = datastreamType.getDatastreamVersion();
        if (datastreamVersionList == null || datastreamVersionList.isEmpty()) {
            return null;
        } else {
            return datastreamVersionList.get(datastreamVersionList.size() - 1);
        }
    }

    public static boolean streamExists(DigitalObject object, String streamID) {
        for (DatastreamType datastreamType : object.getDatastream()) {
            if (datastreamType == null) {
                LOGGER.log(Level.SEVERE, "Repository inconsistency: object %s has datastream %s that is null", new String[]{object.getPID(), streamID});
            } else {
                if (streamID.equals(datastreamType.getID())) {
                    return true;
                }
            }
        }
        return false;
    }


    private static final String LOCAL_REF_PREFIX = "http://local.fedora.server/fedora/get/";


    public static InputStream getStreamContent(DatastreamVersionType stream, AkubraDOManager manager) throws TransformerException, IOException {
        if (stream.getXmlContent() != null) {
            StringWriter wrt = new StringWriter();
            for (Element element : stream.getXmlContent().getAny()) {
                XMLUtils.print(element, wrt);
            }
            return IOUtils.toInputStream(wrt.toString(), Charset.forName("UTF-8"));
        } else if (stream.getContentLocation() != null) {
            if (stream.getContentLocation().getTYPE().equals("INTERNAL_ID")) {
                return manager.retrieveDatastream(stream.getContentLocation().getREF());
            } else if (stream.getContentLocation().getTYPE().equals("URL")) {
                if (stream.getContentLocation().getREF().startsWith(LOCAL_REF_PREFIX)) {
                    String[] refArray = stream.getContentLocation().getREF().replace(LOCAL_REF_PREFIX, "").split("/");
                    if (refArray.length == 2) {
                        return manager.retrieveDatastream(refArray[0] + "+" + refArray[1] + "+" + refArray[1] + ".0");
                    } else {
                        throw new IOException("Invalid datastream local reference: " + stream.getContentLocation().getREF());
                    }
                } else {
                    return readFromURL(stream.getContentLocation().getREF());
                }
            } else {
                throw new IOException("Unsupported datastream reference type: " + stream.getContentLocation().getTYPE() + "(" + stream.getContentLocation().getREF() + ")");
            }
        } else if (stream.getBinaryContent() != null) {
            LOGGER.warning("Reading binaryContent from the managed stream.");
            return new ByteArrayInputStream(stream.getBinaryContent());
        } else {
            throw new IOException("Unsupported datastream content type: " + stream.getID());
        }
    }

    private static InputStream readFromURL(String url) throws IOException {
        URL searchURL = new URL(url);
        URLConnection conn = searchURL.openConnection();
        conn.setUseCaches(true);
        HttpURLConnection.setFollowRedirects(true);
        conn.connect();
        if ("gzip".equals(conn.getContentEncoding())) {
            return new GZIPInputStream(conn.getInputStream());
        } else {
            return conn.getInputStream();
        }
    }

    public static XMLGregorianCalendar getCurrentXMLGregorianCalendar() {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(DATE_FORMAT.format(new Date()));
        } catch (DatatypeConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }

//            return DatatypeFactory.newInstance().newXMLGregorianCalendar(DATE_FORMAT.format(new Date()));
//        } catch (DatatypeConfigurationException e) {
//            LOGGER.log(Level.SEVERE, e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
    }

    /**
     * Return Akubra object store internal path for provided PID
     *
     * @param pid PID of the FOXML object (uuid:xxxxxx...)
     * @return internal file path relative to object store root, depends ob the property objectStore.pattern
     */
    public static String getAkubraInternalId(String pid) {
        if (pid == null) {
            return "";
        }
        String objectPattern = KConfiguration.getInstance().getProperty("objectStore.pattern");
        return getAkubraInternalIdWitPattern(pid, objectPattern);
    }

    public  static String getAkubraInternalIdWitPattern(String pid, String objectPattern) {
        IdMapper mapper = new HashPathIdMapper(objectPattern);
        URI extUri = null;
        try {
            extUri = new URI(new PID(pid).toURI());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        URI internalId = mapper.getInternalId(extUri);
        return internalId.toString();
    }

    public static Date getLastModified(DigitalObject object) throws IOException {
        for (PropertyType propertyType : object.getObjectProperties().getProperty()) {
            if ("info:fedora/fedora-system:def/view#lastModifiedDate".equals(propertyType.getNAME())) {
                try {
                    return DATE_FORMAT.parse(propertyType.getVALUE());
                } catch (ParseException e) {
                    throw new IOException("Cannot parse lastModifiedDate: " + object.getPID() + ": " + propertyType.getVALUE());
                }
            }
        }
        return null;
    }

    public static String currentTimeString() {
        return DATE_FORMAT.format(new Date());
    }

    public static PropertyType createProperty(String name, String value) {
        PropertyType propertyType = new PropertyType();
        propertyType.setNAME(name);
        propertyType.setVALUE(value);
        return propertyType;
    }
}
