package org.kramerius.replications;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.json.JSONObject;

import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.StringUtils;

public class ZeroPhase extends AbstractPhase {

	public static final Logger LOGGER = Logger.getLogger(ZeroPhase.class.getName());
	
	
	@Override
	public void start(String url, String userName, String pswd, String replicationCollections, String replicationImages) throws PhaseException {
        validate(url, replicationCollections);
	}



	private void validate(String url, String replicationCollections) throws PhaseException {
		try {
        	if (Boolean.parseBoolean(replicationCollections)) {
            	String pid = K4ReplicationProcess.pidFrom(url);
    			String infoURL = StringUtils.minus(StringUtils.minus(url, pid),"handle/")+"api/v5.0/info";
    			InputStream inputStream = RESTHelper.inputStream(infoURL, "", "");
    			int[] version = versions(inputStream);
    			try {
    				validate(version);
    			} catch (PhaseException e) {
    				List<Integer> ints = new ArrayList<Integer>();
    				for (Integer integer : version) { ints.add(integer); }
    				LOGGER.warning("Cannot replicate virtual collections; invalid version on the source "+ints);
				}
        	}
        } catch (IOException e) {
			throw new PhaseException(this, e.getMessage());
		}
	}



	protected void validate(int[] version) throws PhaseException {
		if (version.length == 3) {
			notLessThan(version[0],5, version);
			notLessThan(version[1],3, version);
			notLessThan(version[1],7, version);
		} else {
			throwInvalidVersionException(version);
		}
	}



	private void throwInvalidVersionException(int[] version) throws PhaseException {
		List<Integer> ints = new ArrayList<Integer>();
		for (Integer integer : version) { ints.add(integer); }
		throw new PhaseException(this, "not valid version "+ints);
	}



	private void notLessThan(int value, int expected, int[] currentVersion) throws PhaseException {
		if (value < expected) throwInvalidVersionException(currentVersion);
	}



	private static int[] versions(InputStream inputStream) throws IOException {
		JSONObject jsonobj = new JSONObject(IOUtils.readAsString(inputStream, Charset.forName("UTF-8"), true));
		String string = jsonobj.getString("version");
		StringTokenizer tokenizer = new StringTokenizer(string, ".");
		String masterVersion = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
		String minorVersion = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
		String patchVersion = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
		String qualifier = patchVersion.contains("_") ? patchVersion.substring(patchVersion.indexOf("_"), patchVersion.length()) : "";

		patchVersion = !qualifier.equals("") ? patchVersion.substring(0, patchVersion.indexOf("_")) : patchVersion;
		
		return  new int[] {Integer.parseInt(masterVersion),Integer.parseInt(minorVersion),Integer.parseInt(patchVersion)};
	}


	
	
	@Override
	public void restart(String previousProcessUUID, File previousProcessRoot, boolean phaseCompleted, String url,
			String userName, String pswd, String replicationCollections, String replicationImages) throws PhaseException {
        validate(url, replicationCollections);
	}

}
