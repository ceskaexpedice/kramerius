package cz.incad.kramerius.services.iterators.solr;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.iterators.ProcessIterationCallback;
import cz.incad.kramerius.services.iterators.ProcessIterationEndCallback;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SolrIterationUtils {

    public static final String DEFAULT_SORT_FIELD = "PID asc";

    public static Logger LOGGER = Logger.getLogger(SolrIterationUtils.class.getName());

    private SolrIterationUtils() {}


}
