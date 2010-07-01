package cz.incad.kramerius.service.impl;

import java.util.logging.Logger;

import cz.incad.kramerius.processes.impl.ProcessStarter;



public class IndexerProcessStarter {
	
	private static final Logger log = Logger.getLogger(IndexerProcessStarter.class.getName());


	public static void spawnIndexer(String title, String uuid) {
		String base = System.getProperty(ProcessStarter.LR_SERVLET_URL);
	    if (base == null)
	        return;
	    String url = base + "?action=start&def=reindex&out=text&params=fromKrameriusModel,"+uuid+","+title;
	    try {
	        ProcessStarter.httpGet(url);
	    } catch (Exception e) {
	        log.severe("Error spawning indexer for "+uuid+":"+e);
	    }
	}

	public static void spawnIndexRemover(String pid_path, String uuid) {
		String base = System.getProperty(ProcessStarter.LR_SERVLET_URL);
	    if (base == null)
	        return;
	    String url = base +"?action=start&def=reindex&out=text&params=deleteDocument,"+pid_path+","+uuid;
	    try {
	        ProcessStarter.httpGet(url);
	    } catch (Exception e) {
	        log.severe("Error spawning indexer for "+uuid+":"+e);
	    }
	}
}


