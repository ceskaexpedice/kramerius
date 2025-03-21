package cz.incad.kramerius.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.service.METSService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.ceskaexpedice.akubra.AkubraRepository;

public class METSServiceImpl implements METSService {
	public static final Logger LOGGER = Logger.getLogger(METSServiceImpl.class.getName());
	
	@Inject
	@Named("securedAkubraAccess")
	AkubraRepository akubraRepository;

	KConfiguration configuration = KConfiguration.getInstance();

	private static final String INFO = "info:fedora/";

	@Override
	public void exportMETS(String pid, OutputStream os) {
		throw new UnsupportedOperationException("METS export format is not supported");

	}

	
	/**
	 * args[0] uuid of the root object (without uuid: prefix)
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		throw new UnsupportedOperationException("this is unsupported in fedora 4 implemeantion");
	}
}
