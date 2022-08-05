package cz.inovatika.sdnnt;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.service.impl.IndexerProcessStarter.TokensFilter;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ParametrizedSDNNTCheck {
    private static final String AUTH_TOKEN_HEADER_KEY = "auth-token";
    private static final String TOKEN_ATTRIBUTE_KEY = "token";

	public static final Logger LOGGER = Logger.getLogger(ParametrizedSDNNTCheck.class.getName());
	
	@Process
	public static void check(@ParameterName("sdnntchange")String changeEndpoint, 
			@ParameterName("krameriusInstance")String kramInstance,
			@ParameterName("addlicense")String addlicense,
			@ParameterName("removelicense")String removelicense
			) throws IOException {
		
		String folder = KConfiguration.getInstance().getProperty("sdnnt.folder");

        try {
            String formatted = String.format("Syncrhonizace s %s  <-> %s", kramInstance, changeEndpoint);
            ProcessStarter.updateName(formatted);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
        }
		
		LOGGER.info("Sdnnt endpoint :"+changeEndpoint);
		LOGGER.info("Kramerius instance :"+kramInstance);
		LOGGER.info("Output folder :"+folder);

		String[] args = new String[] {
				changeEndpoint, 
				kramInstance,
				folder
		};
		// Povolit check - pozdeji 
		List<String> generatedFiles = SDNNTCheck.process(args);
		
		//dnntcsvlabeledflag
		boolean aLic = Boolean.parseBoolean(addlicense);
		boolean rLic = Boolean.parseBoolean(removelicense);
		if (aLic) {
			List<File> selectedFiles = generatedFiles.stream().filter(it -> it.startsWith("add_")).map(it->new File(it)).collect(Collectors.toList());
			if (selectedFiles != null) {
				for (File csvFile : selectedFiles) {
					String[] split = csvFile.getName().split("_");
					if (split.length > 3) {
						String action = split[0];
						String format = split[1];
						String license = split[2];
						
						planProcess(folder, "parametrizeddnntlabelset",  license, csvFile.getAbsolutePath());
						
					} else {
						LOGGER.warning(String.format("Ommiting file %s", split[0]));
					}
						
				}
			}
		}
		
		if (rLic) {

			List<File> selectedFiles = generatedFiles.stream().filter(it -> it.startsWith("remove_")).map(it->new File(it)).collect(Collectors.toList());
			
			if (selectedFiles != null) {
				for (File csvFile : selectedFiles) {
					String[] split = csvFile.getName().split("_");
					if (split.length > 3) {
						String action = split[0];
						String format = split[1];
						String license = split[2];
						planProcess(folder, "parametrizeddnntlabelunset",  license, csvFile.getAbsolutePath());
						
					} else {
						LOGGER.warning(String.format("Ommiting file %s", split[0]));
					}
				}
			}
		}
	}
	

    public static String planProcess(String folder ,String pName, String label,  String csvFile) {
        Client c = Client.create();
        WebResource r = c.resource(ProcessUtils.getApiPoint()+"?def="+pName);
        r.addFilter(new TokensFilter());
        
        JSONObject object = new JSONObject();

        
        JSONObject mapping = new JSONObject();
        mapping.put("csvfile", csvFile);
        mapping.put("label", label);
        object.put("mapping", mapping);

        
        LOGGER.info("Planned with parameters "+mapping);
        
        
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(object.toString(), MediaType.APPLICATION_JSON)
                .post(String.class);
        return t;
    }

}
