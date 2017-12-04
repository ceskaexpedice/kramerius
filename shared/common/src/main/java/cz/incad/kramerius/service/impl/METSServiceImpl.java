package cz.incad.kramerius.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.service.METSService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.io.IOUtils;

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
			InputStream is = fedoraAccess.getFoxml(p);
			IOUtils.copy(is, os);
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
		throw new UnsupportedOperationException("this is unsupported in fedora 4 implemeantion");
	}
}
