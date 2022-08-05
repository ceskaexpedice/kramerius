package cz.inovatika.sdnnt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import cz.inovatika.sdnnt.utils.SDNNTCheckUtils;

import static cz.inovatika.sdnnt.utils.SDNNTCheckUtils.*;

public class SDNNTCheck {
	
	private static final SimpleDateFormat S_DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
	
	public static final Logger LOGGER = Logger.getLogger(SDNNTCheck.class.getName());
	
	public static void main(String[] args) throws IOException {
		process(args);
	}

	public static List<String> process(String[] args) throws IOException {
		List<String> retvals = new ArrayList<>();

		if (args.length >= 3) {
			String sdnntEndpoint = args[0];
			String checkingSolr = args[1];
			String folderDir = args[2];

			long start= System.currentTimeMillis();
			
			Map<String, List<String>> seLicenseMapping = new HashMap<>();
			Set<String> seRemovedLicense = new HashSet<>();

			Map<String, List<String>> bkLicenseMapping = new HashMap<>();
			Set<String> bkRemovedLicense = new HashSet<>();

			LOGGER.info("Connecting sdnnt list and iterating serials ");
			iterateSDNNTFormat(sdnntEndpoint, seLicenseMapping, seRemovedLicense, "SE");
			LOGGER.info("Connecting sdnnt list and iterating books ");
			iterateSDNNTFormat(sdnntEndpoint, bkLicenseMapping, bkRemovedLicense, "BK");

			long stop = System.currentTimeMillis();
			LOGGER.info("List fetched. It took "+(stop - start)+" ms");
			
			List<String> licenses = Arrays.asList("dnntt", "dnnto");
			List<String> formats = Arrays.asList("BK", "SE");
			for (String license : licenses) {
				for (String format : formats) {
					if (format.equals("BK")) {
						List<String> vals = batchFormatAndLicense(folderDir, checkingSolr, format, license, bkLicenseMapping.get(license));
						retvals.addAll(vals);
					} else {
						List<String> vals = batchFormatAndLicense(folderDir, checkingSolr, format, license, seLicenseMapping.get(license));
						retvals.addAll(vals);
					}
				}
			}
			LOGGER.info("Checking removed license for format BK");
			retvals.addAll(checkRemoveLicense(folderDir, checkingSolr, "BK", new ArrayList<>(bkRemovedLicense)));
			LOGGER.info("Checking removed license for format SE");
			retvals.addAll(checkRemoveLicense(folderDir, checkingSolr, "SE",new ArrayList<>(seRemovedLicense)));
		} else {
			LOGGER.warning("Expecting two parameters. <sdnnt_endpoint>, <solr_endpoint> <folder>");
		}
		return retvals;
	}
	
	
	private static List<String> checkRemoveLicense(String csvFolder, String checkingSolr, String format , List<String> list)
			throws IOException {
		
		List<String> retvals = new ArrayList<>();
		
		Map<String, List<String>> removeCSVOutput = new HashMap<>();
		int maxInBatch = 90;
		int numberOfBatches = list.size() / maxInBatch;
		if (list.size() % maxInBatch > 0) {
			numberOfBatches = numberOfBatches +1;
		}
		for (int j = 0; j < numberOfBatches; j++) {
			int from = j*maxInBatch;
			int to = Math.min((j+1)*maxInBatch, list.size());
			Map<String, List<String>> shouldBeRemoved = new HashMap<>();
			SDNNTCheckUtils.checkAgainstSolrIsLincenseRemoved(checkingSolr, list.subList(from, to), shouldBeRemoved);
			if (!shouldBeRemoved.isEmpty()) {
				for (String removingLicense : shouldBeRemoved.keySet()) {
					List<String> removingPIDS = shouldBeRemoved.get(removingLicense);
					if (!removeCSVOutput.containsKey(removingLicense)) {
						removeCSVOutput.put(removingLicense, new ArrayList<>());
					}
					removeCSVOutput.get(removingLicense).addAll(removingPIDS);
				}
			}
		}
		if (!removeCSVOutput.isEmpty()) {
			for (String license : removeCSVOutput.keySet()) {
				String printRemoveLicense = printRemoveLicense(csvFolder, format, license, removeCSVOutput.get(license));
				retvals.add(printRemoveLicense);
			}
		}
		
		return retvals;
	}
	
	
	
	/**
	 * Check if current objetct has set license
	 */
	private static List<String> batchFormatAndLicense(String csvFolder, String checkingSolr, String format, String checkingLicense, List<String> list)
			throws IOException {

		List<String> retvals = new ArrayList<>();
		
		List<String> addCVSOutput = new ArrayList<>();
		Map<String, List<String>> removeCSVOutput = new HashMap<>();
		
		int maxInBatch = 90;
		int numberOfBatches = list.size() / maxInBatch;
		if (list.size() % maxInBatch > 0) {
			numberOfBatches = numberOfBatches +1;
		}
		for (int j = 0; j < numberOfBatches; j++) {
			int from = j*maxInBatch;
			int to = Math.min((j+1)*maxInBatch, list.size());
			
			List<String> missingLicense = new ArrayList<>();
			Map<String, List<String>> shouldBeRemoved = new HashMap<>();
			
			SDNNTCheckUtils.checkAgainstSolrIsLicensePresent(checkingSolr, list.subList(from, to), checkingLicense, missingLicense, shouldBeRemoved);

			if (!missingLicense.isEmpty()) {
				addCVSOutput.addAll(missingLicense);
			}
			if (!shouldBeRemoved.isEmpty()) {
				for (String removingLicense : shouldBeRemoved.keySet()) {
					List<String> removingPIDS = shouldBeRemoved.get(removingLicense);
					if (!removeCSVOutput.containsKey(removingLicense)) {
						removeCSVOutput.put(removingLicense, new ArrayList<>());
					}
					removeCSVOutput.get(removingLicense).addAll(removingPIDS);
				}
			}
		}
		
		if (!addCVSOutput.isEmpty()) {
			String printAddLicense = printAddLicense(csvFolder, format, checkingLicense, addCVSOutput);
			retvals.add(printAddLicense);
			
		}
		if (!removeCSVOutput.isEmpty()) {
			for (String license : removeCSVOutput.keySet()) {
				List<String> removeLicList = removeCSVOutput.get(license);
				String reducedLicense = printReduceLicense(csvFolder, format, license, removeLicList);
				retvals.add(reducedLicense);
			}
		}
		
		return retvals;
	}

	
	private static String printRemoveLicense(String csvFolder, String format, String removingLicense, List<String> list) throws IOException {
		String sFormat = S_DATE_FORMAT.format(new Date());
		// format_license_sdate
		String name = String.format("remove_%s_%s_%s.csv",  format, removingLicense, sFormat);
		File csvFile = new File(csvFolder, name);
		csvFile.createNewFile();
		LOGGER.info(String.format("Removing license - print to %s", csvFile.getAbsolutePath()));
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(csvFile), Charset.forName("UTF-8"));
        try (CSVPrinter printer = new CSVPrinter(outputStreamWriter, CSVFormat.DEFAULT.withHeader("pid"))) {
        	for (int i = 0; i < list.size(); i++) {
				String pid = list.get(i);
	        	printer.printRecord(pid);
			}
        }
        return csvFile.getAbsolutePath();
	}
	
	private static String printReduceLicense(String csvFolder, String format, String removingLicense, List<String> list) throws IOException {
		String sFormat = S_DATE_FORMAT.format(new Date());
		// format_license_sdate
		String name = String.format("reduced_%s_%s_%s.csv",  format, removingLicense, sFormat);
		File csvFile = new File(csvFolder, name);
		csvFile.createNewFile();
		LOGGER.info(String.format("Reducing license - print to %s", csvFile.getAbsolutePath()));
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(csvFile), Charset.forName("UTF-8"));
        try (CSVPrinter printer = new CSVPrinter(outputStreamWriter, CSVFormat.DEFAULT.withHeader("pid"))) {
        	for (int i = 0; i < list.size(); i++) {
				String pid = list.get(i);
	        	printer.printRecord(pid);
			}
        }
        return csvFile.getAbsolutePath();
		
	}

	private static String printAddLicense(String csvFolder, String format, String addingLicense, List<String> missingLicense) throws IOException {
		String sFormat = S_DATE_FORMAT.format(new Date());
		// format_license_sdate
		String name = String.format("add_%s_%s_%s.csv", format, addingLicense, sFormat);
		File csvFile = new File(csvFolder, name);
		csvFile.createNewFile();
		LOGGER.info(String.format("Adding license - print to %s", csvFile.getAbsolutePath()));
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(csvFile), Charset.forName("UTF-8"));
        try (CSVPrinter printer = new CSVPrinter(outputStreamWriter, CSVFormat.DEFAULT.withHeader("pid"))) {
        	for (int i = 0; i < missingLicense.size(); i++) {
				String pid = missingLicense.get(i);
	        	printer.printRecord(pid);
			}
        }
        return csvFile.getAbsolutePath();
	}

	private static void iterateSDNNTFormat(String sdnntChangesEndpoint, Map<String, List<String>> licenseMapping, Set<String> removedLicense, String format) {
		String token = "*";
		String prevToken = "";
		Client c = Client.create();
		LOGGER.info(String.format("SDNNT changes endpoint is %s", sdnntChangesEndpoint));
		String sdnntApiEndpoint = sdnntChangesEndpoint+"?format="+format+"&rows=500&resumptionToken=%s";
		while(token != null && !token.equals(prevToken)) {
			String formatted = String.format(sdnntApiEndpoint,token);
			WebResource r = c.resource(formatted);
			String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(String.class);
			JSONObject resObject = new JSONObject(t);
			prevToken = token;
			token = resObject.optString("resumptiontoken");
			
			JSONArray items = resObject.getJSONArray("items");
			for (int i = 0; i < items.length(); i++) {
				JSONObject mainObject = items.getJSONObject(i);
				String state = mainObject.getString("state");
				String pid = mainObject.getString("pid");
				if (state.equals("A")) {
					// license - 
					String license = mainObject.getString("license");
					if (mainObject.has("granularity")) {
						JSONArray jsonArray = mainObject.getJSONArray("granularity");
						for (int j = 0; j < jsonArray.length(); j++) {
							JSONObject gItem = jsonArray.getJSONObject(j);
							String gPid = gItem.optString("pid");
							String gItemState = gItemState(gItem);
							if (gItemState != null && (!gItemState.equals("N") && !gItemState.equals("X"))) {
								String gItemLicense = gItem.getString("license");
								if (!licenseMapping.containsKey(gItemLicense)) {
									licenseMapping.put(gItemLicense, new ArrayList<>());
								}
								licenseMapping.get(gItemLicense).add(gPid);
							} else if (gItemState != null && (gItemState.equals("N"))) {
								removedLicense.add(gPid);
							}
						}
					} else {
						// nesmi byt periodikum 
						if (!licenseMapping.containsKey(license)) {
							licenseMapping.put(license, new ArrayList<>());
						}
						licenseMapping.get(license).add(pid);
					}
				} else if (state.equals("N")){
					if (mainObject.has("granularity")) {
						JSONArray jsonArray = mainObject.getJSONArray("granularity");
						for (int j = 0; j < jsonArray.length(); j++) {
							JSONObject gItem = jsonArray.getJSONObject(j);
							String gPid = gItem.optString("pid");
							removedLicense.add(gPid);
						}
					} else {
						removedLicense.add(pid);
					}
				}
			}
		}
	}

	private static String gItemState(JSONObject gItem) {
		if (gItem.has("states")) {
			Object object = gItem.get("states");
			if (object instanceof JSONArray) {
				JSONArray jsArray = (JSONArray) object;
				return jsArray.getString(0);
			} else {
				return object.toString();
			}
		} 
		return null;
	}
}
