package cz.incad.kramerius.fedora.om.impl;

import com.qbizm.kramerius.imp.jaxb.DatastreamType;
import com.qbizm.kramerius.imp.jaxb.DatastreamVersionType;
import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import com.qbizm.kramerius.imp.jaxb.PropertyType;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import cz.incad.kramerius.utils.SafeSimpleDateFormat;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Element;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class AkubraUtils {
    public static final Logger LOGGER = Logger.getLogger(AkubraUtils.class.getName());

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
            if (streamID.equals(datastreamType.getID())) {
                return true;
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
        conn.connect();
        if ("gzip".equals(conn.getContentEncoding())) {
            return new GZIPInputStream(conn.getInputStream());
        } else {
            return conn.getInputStream();
        }
    }

    public static XMLGregorianCalendar getCurrentXMLGregorianCalendar() {
        Calendar now = Calendar.getInstance();
        return XMLGregorianCalendarImpl.createDateTime(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY), now
                .get(Calendar.MINUTE), now.get(Calendar.SECOND));
    }

    public static String endpoint() {
        String apiPoint = KConfiguration.getInstance().getConfiguration().getString("api.point");
        return apiPoint + (apiPoint.endsWith("/") ? "" : "/") + "item/";
    }


    private static final SafeSimpleDateFormat dateFormat = new SafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'S'Z'");

    public static Date getLastModified(DigitalObject object) throws IOException {
        for (PropertyType propertyType : object.getObjectProperties().getProperty()) {
            if ("info:fedora/fedora-system:def/view#lastModifiedDate".equals(propertyType.getNAME())) {
                try {
                    return dateFormat.parse(propertyType.getVALUE());
                } catch (ParseException e) {
                    throw new IOException("Cannot parse LastModofiedDate: " + object.getPID() + ": " + propertyType.getVALUE());
                }
            }
        }
        return null;
    }

    public static String currentTimeString() {
        return dateFormat.format(new Date());
    }

    public static PropertyType createProperty(String name, String value) {
        PropertyType propertyType = new PropertyType();
        propertyType.setNAME(name);
        propertyType.setVALUE(value);
        return propertyType;
    }
}
