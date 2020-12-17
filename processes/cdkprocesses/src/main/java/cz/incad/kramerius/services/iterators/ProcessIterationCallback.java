package cz.incad.kramerius.services.iterators;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

public interface ProcessIterationCallback {

    public void call(List<IterationItem> results) throws ParserConfigurationException, IOException;
}
