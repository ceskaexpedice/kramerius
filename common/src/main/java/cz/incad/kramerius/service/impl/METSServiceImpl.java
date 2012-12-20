package cz.incad.kramerius.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.service.METSService;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class METSServiceImpl implements METSService {
	public static final Logger LOGGER = Logger.getLogger(METSServiceImpl.class.getName());
	
	@Inject
	@Named("securedFedoraAccess")
	FedoraAccess fedoraAccess;
	@Inject
	KConfiguration configuration;

	private static final String INFO = "info:fedora/";

	@Override
	public void exportMETS(String pid, OutputStream os) {
		String p = pid.replace(INFO, "");
		try {
			os.write(fedoraAccess.getAPIM().export(p,"info:fedora/fedora-system:METSFedoraExt-1.1", "public"));
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
			throw new RuntimeException(e);
		}

	}

	
	/**
	 * args[0] uuid of the root object (without uuid: prefix)
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		LOGGER.info("METS Service: "+Arrays.toString(args));
		METSServiceImpl inst = new METSServiceImpl();
		inst.fedoraAccess = new FedoraAccessImpl(null, null);
		inst.configuration = KConfiguration.getInstance();
		inst.exportMETS("uuid:" + args[0],System.out);
		LOGGER.info("METS Service finished.");
	}
}
