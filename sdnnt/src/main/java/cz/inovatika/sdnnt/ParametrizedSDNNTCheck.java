package cz.inovatika.sdnnt;

import java.io.IOException;
import java.util.logging.Logger;

import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ParametrizedSDNNTCheck {

	public static final Logger LOGGER = Logger.getLogger(ParametrizedSDNNTCheck.class.getName());
	
	@Process
	public static void check(@ParameterName("sdnntchange")String changeEndpoint, @ParameterName("krameriusInstance")String kramInstance) throws IOException {
		
		String folder = KConfiguration.getInstance().getProperty("sdnnt.folder");
		LOGGER.info("Sdnnt endpoint :"+changeEndpoint);
		LOGGER.info("Kramerius instance :"+kramInstance);
		LOGGER.info("Output folder :"+folder);

		String[] args = new String[] {
				changeEndpoint, 
				kramInstance,
				folder
		};
		SDNNTCheck.main(args);
	}
}
