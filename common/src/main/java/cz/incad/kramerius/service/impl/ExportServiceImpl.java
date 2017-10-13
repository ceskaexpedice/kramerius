package cz.incad.kramerius.service.impl;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.impl.FedoraAccessImpl;
import cz.incad.kramerius.service.ExportService;
import cz.incad.kramerius.service.impl.export.EnahanceInformation;
import cz.incad.kramerius.service.impl.export.MigrationInformation;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ExportServiceImpl implements ExportService {
    public static final Logger LOGGER = Logger.getLogger(ExportServiceImpl.class.getName());
    private static int BUFFER_SIZE = 1024;

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    @Inject
    KConfiguration configuration;
    
    List<EnahanceInformation> enhancers = new ArrayList<EnahanceInformation>();
    
    
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


    private static byte[] jsonFromItem(String p) {
        Client c = Client.create();
        c.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);        
        String apiPoint = KConfiguration.getInstance().getConfiguration().getString("api.point");
        WebResource r = c.resource(apiPoint+"/item/" + p );
        byte[] t = r.accept(MediaType.MEDIA_TYPE_WILDCARD).get(new byte[0].getClass());
        return t;
    }

   private void store(File exportDirectory, String name, byte[] contents) {
        String convertedName = name.replace("uuid:", "").replaceAll(":", "_")+ ".xml";
        File toFile = new File(exportDirectory, convertedName);
        OutputStream os = null;
        InputStream is = null;
        try {
            contents = enhanceMigrationData(name, contents);
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
        } catch (Exception e) {
            LOGGER.severe("Exception in export-store: " + e);
            throw new RuntimeException(e);
        }
    }


   private byte[] enhanceMigrationData(String name, byte[] foxml) throws Exception {
       byte[] json = jsonFromItem(name);
       byte[] process = foxml;
       for (EnahanceInformation einf : this.enhancers) {
           process = einf.enhance(process, json);
       }
       return process;
   }

    /**
     * args[0] uuid of the root object (without uuid: prefix)
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        LOGGER.info("Export service: "+Arrays.toString(args));
        ExportServiceImpl inst = new ExportServiceImpl();
        inst.fedoraAccess = new FedoraAccessImpl(null, null);
        inst.configuration = KConfiguration.getInstance();
        inst.enhancers.add(new MigrationInformation());
        inst.exportTree(args[0]);
        LOGGER.info("ExportService finished.");
    }
}
