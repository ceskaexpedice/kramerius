//$Id: GTransformer.java 7814 2008-10-31 13:23:21Z gertsp $
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fedoragsearch.server;

import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import java.util.Date;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.configuration.Configuration;

/**
 * performs the stylesheet transformations
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class GTransformer {

    private static final Logger logger =
            Logger.getLogger(GTransformer.class.getName());
    protected Configuration config;
    HashMap<String, Transformer> cache = new HashMap<String, Transformer>();

    public GTransformer() {
        config = KConfiguration.getInstance().getConfiguration();
    }

    public static class ClasspathResolver implements URIResolver {

        @Override
        public Source resolve(String href, String base) throws TransformerException {
            InputStream stream = this.getClass().getResourceAsStream("/cz/incad/kramerius/indexer/res/" + href);
            if (href != null) {
                return new StreamSource(stream);
            } else  return null;
        }
    }
    
    /**
     * 
     *
     * @throws TransformerConfigurationException, TransformerException.
     */
    public Transformer getTransformer(String xsltPath)
            throws Exception {
        
        return getTransformer(xsltPath, new ClasspathResolver(), true);
    }

    public Transformer getTransformer(String xsltPath, URIResolver uriResolver, boolean checkInHome)
            throws Exception {
        if (cache.containsKey(xsltPath)) {
            return cache.get(xsltPath);
        }
        Transformer transformer = null;
        String xsltPathName = xsltPath + ".xslt";
        try {
            InputStream stylesheet;
            try {
                if (checkInHome) {
                    String dirname = Constants.WORKING_DIR + File.separator + "xsl";
                    File textFile = new File(dirname, xsltPathName);
                    if ((textFile.exists()) && (textFile.canRead())) {
                        stylesheet = new FileInputStream(textFile);
                    } else {
                        stylesheet = this.getClass().getResourceAsStream("/cz/incad/kramerius/indexer/res/" + xsltPathName);
                    }
                } else {
                    stylesheet = new FileInputStream(new File(xsltPath));
                }
            } catch (Exception ex) {
                throw new Exception(xsltPath + " not found");
            }
            TransformerFactory tfactory = TransformerFactory.newInstance();
            if (uriResolver != null) {
                tfactory.setURIResolver(uriResolver);
            }
            StreamSource xslt = new StreamSource(stylesheet);
            transformer = tfactory.newTransformer(xslt);
            if (uriResolver != null) {
                transformer.setURIResolver(uriResolver);
            }
        } catch (TransformerConfigurationException e) {
            throw new Exception("getTransformer " + xsltPath + ":\n", e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new Exception("getTransformerFactory " + xsltPath + ":\n", e);
        }
        if (transformer != null) {
            cache.put(xsltPath, transformer);
        }
        return transformer;
    }

    /**
     * 
     *
     * @throws TransformerConfigurationException, TransformerException.
     */
    public void transform(String xsltName, StreamSource sourceStream, StreamResult destStream)
            throws Exception {
        Transformer transformer = getTransformer(xsltName);
        try {
            transformer.transform(sourceStream, destStream);
        } catch (TransformerException e) {
            throw new Exception("transform " + xsltName + ".xslt:\n", e);
        }
    }

    public StringBuffer transform(String xsltName, Source sourceStream, HashMap<String, String> params)
            throws Exception {
        return transform(xsltName, sourceStream, new GTransformer.ClasspathResolver(), params, true);
    }

    public StringBuffer transform(String xsltName, Source sourceStream,
            URIResolver uriResolver, HashMap<String, String> params, boolean checkInHome) throws Exception {
        logger.fine("xsltName=" + xsltName);
        Transformer transformer = getTransformer(xsltName, uriResolver, checkInHome);
        //logger.info(params);
        Iterator it = params.keySet().iterator();
        String key = "";
        String value = "";
        while (it.hasNext()) {
            key = (String) it.next();
            value = params.get(key);
            if (value == null) {
                value = "";
            }
            transformer.setParameter(key, value);

        }
        transformer.setParameter("DATETIME", new Date());
        StreamResult destStream = new StreamResult(new StringWriter());
        try {
            transformer.transform(sourceStream, destStream);
        } catch (TransformerException e) {
            logger.log(Level.SEVERE, "transform " + xsltName + ":\n", e);
            throw new Exception("transform " + xsltName + ":\n", e);
        }
        StringWriter sw = (StringWriter) destStream.getWriter();
//      if (logger.isDebugEnabled())
//      logger.debug("sw="+sw.getBuffer().toString());
        return sw.getBuffer();
    }

    /**
     * 
     *
     * @throws TransformerConfigurationException, TransformerException.
     */
    public void transformToFile(String xsltName, StreamSource sourceStream, Object[] params, String filePath)
            throws Exception {
        logger.fine("xsltName=" + xsltName);
        Transformer transformer = getTransformer(xsltName);
        for (int i = 0; i < params.length; i = i + 2) {
            Object value = params[i + 1];
            if (value == null) {
                value = "";
            }
            transformer.setParameter((String) params[i], value);
        }
        transformer.setParameter("DATETIME", new Date());
        StreamResult destStream = new StreamResult(new File(filePath));
        try {
            transformer.transform(sourceStream, destStream);
        } catch (TransformerException e) {
            throw new Exception("transform " + xsltName + ".xslt:\n", e);
        }
    }

    /**
     * 
     *
     * @throws TransformerConfigurationException, TransformerException.
     */
    public StringBuffer transform(String xsltName, StreamSource sourceStream)
            throws Exception {
        return transform(xsltName, sourceStream, new HashMap<String, String>());
    }

    /**
     * 
     *
     * @throws TransformerConfigurationException, TransformerException.
     */
    public StringBuffer transform(String xsltName, StringBuffer sb, HashMap<String, String> params)
            throws Exception {
//      if (logger.isDebugEnabled())
//      logger.debug("sb="+sb);
        StringReader sr = new StringReader(sb.toString());
        StringBuffer result = transform(xsltName, new StreamSource(sr), params);
//      if (logger.isDebugEnabled())
//      logger.debug("xsltName="+xsltName+" result="+result);
        return result;
    }

    /**
     * 
     *
     * @throws TransformerConfigurationException, TransformerException.
     */
    public StringBuffer transform(String xsltName, StringBuffer sb)
            throws Exception {
        return transform(xsltName, sb, new HashMap<String, String>());
    }

    public static void main(String[] args) {
        int argCount = 2;
        try {
            if (args.length == argCount) {
                File f = new File(args[1]);
                StreamSource ss = new StreamSource(new File(args[1]));
                GTransformer gt = new GTransformer();
                StreamResult destStream = new StreamResult(new StringWriter());
                gt.transform(args[0], ss, destStream);
                StringWriter sw = (StringWriter) destStream.getWriter();
                System.out.print(sw.getBuffer().toString());
            } else {
                throw new IOException("Must supply " + argCount + " arguments.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(
                    "Usage: GTransformer xsltName xmlFileName");
        }
    }
}
