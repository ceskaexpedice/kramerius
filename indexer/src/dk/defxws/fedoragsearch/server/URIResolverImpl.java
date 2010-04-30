//$Id:  $
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fedoragsearch.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.log4j.Logger;

import fedora.common.http.WebClient;
import java.util.Properties;

/**
 * custom URIResolver for ssl access
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class URIResolverImpl implements URIResolver {
	
	private Properties config;
    
    private final Logger logger = Logger.getLogger(URIResolverImpl.class);
    
    public void setConfig(Properties config) {
		this.config = config;
    }

	public Source resolve(String href, String base) throws TransformerException {
		Source source = null;
		URL url;
		try {
			url = new URL(href);
		} catch (MalformedURLException e) {
			throw new TransformerException("resolve new URL href="+href+" base="+base, e);
		}
		String reposName = config.getProperty("RepositoryNameFromUrl");
        if (logger.isDebugEnabled())
            logger.debug("resolve get href="+href+" base="+base+" url="+url.toString()+" reposName="+reposName);
//		System.setProperty("http.user", config.getFedoraUser(reposName));
//		System.setProperty("http.password", config.getFedoraPass(reposName));
		System.setProperty("javax.net.ssl.trustStore", config.getProperty("TrustStorePath"));
		System.setProperty("javax.net.ssl.trustStorePassword", config.getProperty("TrustStorePass"));
		WebClient client = new WebClient();
		try {
	        if (logger.isDebugEnabled())
	            logger.debug("resolve get source=\n"+
                            client.getResponseAsString(href, 
                            false, 
                            new UsernamePasswordCredentials(config.getProperty("FedoraUser"), 
                            config.getProperty("FedoraPass"))));
			source = new StreamSource(client.get(href, 
                                false, 
                                config.getProperty("FedoraUser"), 
                                config.getProperty("FedoraPass")));
		} catch (IOException e) {
			throw new TransformerException("resolve get href="+href+" base="+base, e);
		}
		return source;
	}

}
