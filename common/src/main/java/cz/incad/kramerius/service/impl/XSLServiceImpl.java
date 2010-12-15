package cz.incad.kramerius.service.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.File;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Locale;

import cz.incad.kramerius.Constants;
import cz.incad.kramerius.service.XSLService;
import cz.incad.kramerius.utils.IOUtils;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author Administrator
 */
public class XSLServiceImpl implements XSLService {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(XSLServiceImpl.class.getName());
    private Provider<Locale> localeProvider;

    @Inject
    public XSLServiceImpl(Provider<Locale> localeProvider) {
        super();
        try {
            this.init();
            this.localeProvider = localeProvider;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void init() throws IOException {
    }

    @Override
    public String getXSL(String name) throws IOException {
        File textFile = xslFile(name);
        if ((!textFile.exists()) || (!textFile.canRead())) {
            throw new IOException("cannot read from file '" + name + "'");
        }
        String retVal = IOUtils.readAsString(new FileInputStream(textFile), Charset.forName("UTF-8"), true);
        return retVal;
    }

    private Transformer getTransformer(String xsltName)
            throws Exception {
        Transformer transformer = null;
        try {
            InputStream stylesheet;
            try {
                File f = xslFile(xsltName);
                if ((!f.exists()) || (!f.canRead())) {
                    throw new IOException("cannot read from file '" + xsltName + "'");
                }
                stylesheet = new FileInputStream(f);
            } catch (Exception ex) {
                throw new Exception(xsltName + " not found");
            }
            TransformerFactory tfactory = TransformerFactory.newInstance();
            StreamSource xslt = new StreamSource(stylesheet);
            transformer = tfactory.newTransformer(xslt);
        } catch (TransformerConfigurationException e) {
            throw new Exception("getTransformer " + xsltName + ":\n", e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new Exception("getTransformerFactory " + xsltName + ":\n", e);
        }
        return transformer;
    }

    private String createBundleURL(Locale locale) {
//        String i18nUrl = ApplicationURL.applicationURL(req)+"/i18n";
//        if ((config.getApplicationURL() != null) && (!configuration.getApplicationURL().equals(""))){
//                i18nUrl = config.getApplicationURL()+"i18n";
//        }
        String i18nUrl = "http://localhost:8080/search/i18n";
        return i18nUrl + "?action=bundle&alang=" + locale.getLanguage() + "&country=" + locale.getCountry() + "&name=labels";
    }

    @Override
    public String transform(String xml, String xsltName) throws Exception {

        Transformer transformer = getTransformer(xsltName);

        StreamResult destStream = new StreamResult(new StringWriter());
        
        transformer.setParameter("bundle_url", createBundleURL(localeProvider.get()));
        transformer.transform(new StreamSource(new StringReader(xml)), destStream);

        StringWriter sw = (StringWriter) destStream.getWriter();
        return sw.getBuffer().toString();
    }

    private File xslFile(String name) {
        File textFile = xslFile(xslsFolder(), name);
        return textFile;
    }

    @Override
    public boolean isAvailable(String name) {
        File textFile = xslFile(name);
        return ((textFile.exists()) && (textFile.canRead()));
    }

    @Override
    public File xslsFolder() {
        String dirName = Constants.WORKING_DIR + File.separator + "xsl";
        File dir = new File(dirName);
        if (!dir.exists()) {
            boolean mkdirs = dir.mkdirs();
            if (!mkdirs) {
                throw new RuntimeException("cannot create dir '" + dir.getAbsolutePath() + "'");
            }
        }
        return dir;
    }

    private File xslFile(File textsDir, String name) {
        File textFile = new File(textsDir, name);
        return textFile;
    }

    public static void main(String[] args) {
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            System.out.println(locale);
        }
    }
}
