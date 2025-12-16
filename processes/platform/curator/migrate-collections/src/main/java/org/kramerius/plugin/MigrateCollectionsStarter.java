package org.kramerius.plugin;

import cz.inovatika.collections.Backup;
import cz.inovatika.collections.migrations.FromK5Instance;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.logging.Logger;

public class MigrateCollectionsStarter {

    public static final Logger LOGGER = Logger.getLogger(MigrateCollectionsStarter.class.getName());

    @ProcessMethod
    public static void migrateMain(
            @ParameterName("url") @IsRequired String url
    ) throws Exception {
        FromK5Instance.migrateMain(url);
    }
}
