package cz.incad.kramerius.gwtviewers.server.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;


/**
 * Uklada informace o obrazcich 
 * <ul>
 *  <li> maximalni sirka obrazku 
 * </ul>
 * @author pavels
 */
public class MetadataStore {

	public static final String WIDTH_KEY = "maxWidth";
	public static final String STANDARD_FOLDER_PATH=System.getProperty("user.home")+File.separator+".kramerius";
	public static final String DATA_FILE_NAME="thumbnails.properties";
	
	public File getFolder() {
		return new File(STANDARD_FOLDER_PATH);
	}
	
	
	public void storeMaxWidth(String width, String uuid) throws FileNotFoundException, IOException {
		File folder = prepareFolder(uuid);
		Properties props = loadPropertiesFile(folder);
		props.setProperty(WIDTH_KEY, width);
		storeProperties(folder, props);
	}


	private File prepareFolder(String uuid) {
		File folder =  new File(getFolder(), "."+uuid);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		return folder;
	}


	private Properties loadPropertiesFile(File folder) throws IOException,
			FileNotFoundException {
		File data = new File(folder, DATA_FILE_NAME);
		Properties props = new Properties();
		FileInputStream inStream = null;
		try {
			if (data.exists()) {
				inStream = new FileInputStream(data);
				props.load(inStream);
			}
		} finally {
			if (inStream != null) inStream.close();
		}
		return props;
	}
	
	
	private void storeProperties(File folder, Properties props) throws IOException {
		File data = new File(folder, DATA_FILE_NAME);
		FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(data);
			props.store(outStream, "Kramerius system");
		} finally {
			outStream.close();
		}
	}

	public String loadMaxWidth(String uuid) throws FileNotFoundException, IOException {
		File folder = prepareFolder(uuid);
		Properties props = loadPropertiesFile(folder);
		return props.getProperty(WIDTH_KEY);
	}
	
	public void storeCollected(String uuid,Properties props) throws FileNotFoundException, IOException {
		File folder = prepareFolder(uuid);
		Properties loadedProps = loadPropertiesFile(folder);
		Set<Object> keySet = props.keySet();
		for (Object object : keySet) {
			String key = object.toString();
			String value = props.getProperty(key);
			loadedProps.setProperty(key, value);
		}
		storeProperties(folder, loadedProps);
	}


	public Properties loadCollected(String uuid) throws FileNotFoundException, IOException {
		File folder = prepareFolder(uuid);
		Properties loadedProps = loadPropertiesFile(folder);
		return loadedProps;
	}
}
