package cz.incad.kramerius.services.iterators;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

/** Iteration callback */
public interface ProcessIterationCallback {

    /** Informing about iteration process */
    public void call(List<IterationItem> results) throws ParserConfigurationException, IOException;
}
