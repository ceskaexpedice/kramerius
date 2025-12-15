package org.kramerius.plugin;

import cz.inovatika.collections.Backup;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.logging.Logger;

public class BackupCollectionsStarter {

    public static final Logger LOGGER = Logger.getLogger(BackupCollectionsStarter.class.getName());

    @ProcessMethod
    public static void backupMain(
            @ParameterName("target") @IsRequired String target,
            @ParameterName("nameOfBackup") @IsRequired String nameOfBackup
    ) throws IOException, ParserConfigurationException, TransformerException, SAXException {
        Backup.backupMain(target, nameOfBackup);
    }
}
