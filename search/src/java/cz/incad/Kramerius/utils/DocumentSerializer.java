package cz.incad.Kramerius.utils;

import cz.incad.kramerius.utils.XMLUtils;
import org.ehcache.spi.serialization.Serializer;
import org.ehcache.spi.serialization.SerializerException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * DocumentSerializer
 *
 * @author Martin Rumanek
 */
public class DocumentSerializer implements Serializer<Document> {

    public DocumentSerializer(ClassLoader classLoader) {
    }

    @Override
    public ByteBuffer serialize(Document object) throws SerializerException {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(object);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            StreamResult result=new StreamResult(outputStream);
            transformer.transform(source, result);

            return ByteBuffer.wrap(outputStream.toByteArray());
        } catch (TransformerConfigurationException e) {
            throw new SerializerException(e);
        } catch (TransformerException e) {
            throw new SerializerException(e);
        }
    }

    @Override
    public Document read(ByteBuffer binary) throws ClassNotFoundException, SerializerException {
        try {
            return XMLUtils.parseDocument(new ByteArrayInputStream(binary.array()), true);
        } catch (ParserConfigurationException e) {
            throw new SerializerException(e);
        } catch (SAXException e) {
            throw new SerializerException(e);
        } catch (IOException e) {
            throw new SerializerException(e);
        }
    }

    @Override
    public boolean equals(Document object, ByteBuffer binary) throws ClassNotFoundException, SerializerException {
        return serialize(object).equals(binary);
    }
}
