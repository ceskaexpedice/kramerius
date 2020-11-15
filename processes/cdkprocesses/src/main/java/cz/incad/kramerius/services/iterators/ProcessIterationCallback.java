package cz.incad.kramerius.services.iterators;

import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

public interface ProcessIterationCallback {

    public void call(List<String> results) throws ParserConfigurationException, IOException;
    //public void call(Element results, String iterationToken) throws ParserConfigurationException, MigrateSolrIndexException, SAXException, IOException;

    //public void call(List<String> pids);
}
