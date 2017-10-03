package cz.incad.kramerius.service.impl;


import cz.incad.kramerius.utils.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.service.ExportService;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ExportServiceImpl implements ExportService {
    public static final Logger LOGGER = Logger.getLogger(ExportServiceImpl.class.getName());
    private static int BUFFER_SIZE = 1024;

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    @Inject
    KConfiguration configuration;

    private static final String INFO = "info:fedora/";

    @Override
    public void exportTree(String pid) throws IOException {
        Set<String> pids = fedoraAccess.getPids(pid);
        if (pids.isEmpty())
            return;
        String exportRoot = configuration.getProperty("export.directory");
        IOUtils.checkDirectory(exportRoot);
        File exportDirectory = IOUtils.checkDirectory(exportRoot+File.separator+pid.replace("uuid:", "").replaceAll(":", "_"));//create subdirectory for given PID
        IOUtils.cleanDirectory(exportDirectory);
        for (String s : pids) {
            String p = s.replace(INFO, "");
            LOGGER.info("Exporting "+exportDirectory+" "+p);
            try{
                store(exportDirectory, p, fedoraAccess.getAPIM().export(p, "info:fedora/fedora-system:FOXML-1.1", "archive"));
            }catch(Exception ex){
                LOGGER.warning("Cannot export object "+p+", skipping: "+ex);
            }
        }
    }



    private void store(File exportDirectory, String name, byte[] contents) {
        String convertedName = name.replace("uuid:", "").replaceAll(":", "_")+ ".xml";
        File toFile = new File(exportDirectory, convertedName);
        OutputStream os = null;
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(contents);
            os = new FileOutputStream(toFile);
            byte[] buf = new byte[BUFFER_SIZE];
            for (int byteRead; (byteRead = is.read(buf, 0, BUFFER_SIZE)) >= 0;) {
                os.write(buf, 0, byteRead);
            }
            is.close();
            os.close();
        } catch (IOException e) {
            LOGGER.severe("IOException in export-store: " + e);
            throw new RuntimeException(e);
        }

    }

    /**
     * args[0] uuid of the root object (without uuid: prefix)
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        LOGGER.info("Export service: "+Arrays.toString(args));
        for (int i = 0; i < args.length; i++) {
            ExportServiceImpl inst = new ExportServiceImpl();
            inst.fedoraAccess = new FedoraAccessImpl(null, null);
            inst.configuration = KConfiguration.getInstance();
            inst.exportTree(args[i]);
            LOGGER.info("ExportService finished.");
		}
    }
}
