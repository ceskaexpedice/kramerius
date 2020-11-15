package cz.incad.kramerius.services.iterators.logfile;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.services.iterators.ProcessIteratorFactory;
import cz.incad.kramerius.services.iterators.solr.SolrCursorIterator;
import cz.incad.kramerius.services.iterators.solr.SolrFilterQueryIterator;
import cz.incad.kramerius.services.iterators.solr.SolrIteratorFactory;
import cz.incad.kramerius.services.iterators.solr.SolrPageIterator;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Element;

public class LogFileIteratorFactory extends ProcessIteratorFactory {

    @Override
    public ProcessIterator createProcessIterator(Element iteration, Client client) {
        Element urlElm = XMLUtils.findElement(iteration, "url");
        String url = urlElm != null ? urlElm.getTextContent() : "";

        Element rowsElm = XMLUtils.findElement(iteration, "rows");
        int rowSize = rowsElm != null ? Integer.parseInt(rowsElm.getTextContent()) : 100;

        return new LogFileIterator(url, rowSize);
    }
}
