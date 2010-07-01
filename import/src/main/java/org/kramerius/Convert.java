package org.kramerius;

import com.qbizm.kramerius.imptool.poc.Main;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class Convert {

	/**
	 * @param args[0] visibility (true, false)
	 */
	public static void main(String[] args) {
		boolean visible = false;
		if (args.length>0){
			visible = Boolean.parseBoolean(args[0]);
		}
		String uuid = Main.convert(KConfiguration.getInstance().getProperty("import.directory"), KConfiguration.getInstance().getProperty("import.directory")+Download.CONV_SUFFIX, false, visible);
        Import.ingest(KConfiguration.getInstance().getProperty("ingest.url"), KConfiguration.getInstance().getProperty("ingest.user"), KConfiguration.getInstance().getProperty("ingest.password"), KConfiguration.getInstance().getProperty("import.directory")+Download.CONV_SUFFIX);
        Download.startIndexing("", uuid);
	}

}
