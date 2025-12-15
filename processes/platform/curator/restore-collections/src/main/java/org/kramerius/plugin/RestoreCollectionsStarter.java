package org.kramerius.plugin;

import cz.inovatika.collections.Restore;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.logging.Logger;

public class RestoreCollectionsStarter {

    public static final Logger LOGGER = Logger.getLogger(RestoreCollectionsStarter.class.getName());

    @ProcessMethod
    public static void backupMain(
            @ParameterName("authToken") @IsRequired String authToken,
            @ParameterName("target") @IsRequired String target
    ) throws Exception {
        Restore.restoreMain(authToken, target);
    }
}
