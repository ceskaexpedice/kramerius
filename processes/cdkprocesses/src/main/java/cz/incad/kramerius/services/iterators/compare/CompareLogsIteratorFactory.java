package cz.incad.kramerius.services.iterators.compare;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.services.iterators.ProcessIteratorFactory;
import cz.incad.kramerius.services.iterators.logfile.LogFileIterator;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class CompareLogsIteratorFactory extends ProcessIteratorFactory {

    @Override
    public ProcessIterator createProcessIterator(String timestamp, Element iteration, Client client) {
        try {
            Element urlElm = XMLUtils.findElement(iteration, "url");
            String url = urlElm != null ? urlElm.getTextContent() : "";

            Element rowsElm = XMLUtils.findElement(iteration, "rows");
            int rowSize = rowsElm != null ? Integer.parseInt(rowsElm.getTextContent()) : 100;

            Element esAddressElm = XMLUtils.findElement(iteration, "esAddress");
            String esAddress = esAddressElm != null ? esAddressElm.getTextContent() : "http://localhost:9200";

            Element esTypeElm = XMLUtils.findElement(iteration, "esType");
            boolean esType = esTypeElm != null ? Boolean.valueOf(esTypeElm.getTextContent()) : true;

            Element esBulkSizeElm = XMLUtils.findElement(iteration, "esBulkSize");
            int esBulkSize = esTypeElm != null ? Integer.parseInt(esBulkSizeElm.getTextContent()) : 1000;

            return new CompareLogsIterator(url, rowSize, esAddress, esType,esBulkSize);
        } catch ( URISyntaxException | IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
