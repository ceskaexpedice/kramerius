package cz.incad.kramerius.plugin;

import cz.incad.kramerius.cdkcollection.CDKCollectionSynchronizer;
import cz.incad.kramerius.cdkcollection.ProcessingIndexService;
import cz.incad.kramerius.cdkcollection.SearchIndexService;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.utils.kubernetes.KubernetesEnvSupport;
import cz.inovatika.kramerius.services.Migration;
import cz.inovatika.kramerius.services.config.EffectiveMigrationConfigRenderer;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * CDK Index Collection starter
 */
public class CDKIndexCollection {
    private static final Logger LOGGER = Logger.getLogger(CDKIndexCollection.class.getName());

    @ProcessMethod
    public static void indexCollectionMain() {
        SearchIndexService searchIndexService = null; // TODO
        ProcessingIndexService processingIndexService = null; // TODO
        new CDKCollectionSynchronizer(searchIndexService, processingIndexService);
    }

}
