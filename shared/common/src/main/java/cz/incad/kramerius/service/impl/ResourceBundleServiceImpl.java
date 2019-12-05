package cz.incad.kramerius.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.Constants;
import cz.incad.kramerius.service.ResourceBundleService;

public class ResourceBundleServiceImpl implements ResourceBundleService {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(ResourceBundleServiceImpl.class.getName());

    @Inject(optional = true)
    @Named("workingDir")
    private String workingDir;

    @Override
    public File bundlesFolder() {
        String dirName = Constants.WORKING_DIR + File.separator + "bundles";
        if (workingDir != null)
            dirName = workingDir + File.separator + "bundles";
        File dir = new File(dirName);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created)
                throw new RuntimeException("cannot create dir '"
                        + dir.getAbsolutePath() + "'");
        }
        return dir;
    }

    @Override
    public ResourceBundle getResourceBundle(final String name,
            final Locale locale) throws IOException {
        LOGGER.fine("resource bundle " + name + "  " + locale);
        final File resourcesDir = checkFiles(name);
        return ResourceBundle.getBundle(name, locale, new ResourceClassLoader(
                resourcesDir));
    }

    public File checkFiles(String name) throws IOException {
        File resourcesDir = bundlesFolder();
        createDefault(name);
        return resourcesDir;
    }

    private void createDefault(String name) throws IOException {
        // TODO Auto-generated method stub
        String bundleName = name + ".properties";
        File pfile = new File(bundlesFolder(), bundleName);
        if (!pfile.exists()) {
            Properties properties = new Properties();
            boolean created = pfile.createNewFile();
            if (!created) {
                throw new IOException("cannot create file '"
                        + pfile.getAbsolutePath() + "'");
            }
            FileOutputStream fos = new FileOutputStream(pfile);
            properties.store(fos, "Default resource bundle");
        }
    }

    public static class ResourceClassLoader extends ClassLoader {
        /**
         * Charset used when reading a properties file.
         */
        private static final String CHARSET = "UTF-8";

        /**
         * Buffer size used when reading a properties file.
         */
        private static final int BUFFER_SIZE = 2000;

        private File folder;

        public ResourceClassLoader(File folder) {
            super();
            this.folder = folder;
        }

        static InputStream readUTFStreamToEscapedASCII(InputStream is)
                throws IOException {
            Reader reader = new InputStreamReader(is, CHARSET);

            StringBuilder builder = new StringBuilder(BUFFER_SIZE);
            char[] buffer = new char[BUFFER_SIZE];

            while (true) {
                int length = reader.read(buffer);

                if (length < 0)
                    break;

                for (int i = 0; i < length; i++) {
                    char ch = buffer[i];

                    if (ch <= '\u007f') {
                        builder.append(ch);
                        continue;
                    }

                    builder.append(String.format("\\u%04x", (int) ch));
                }
            }

            reader.close();

            byte[] resourceContent = builder.toString().getBytes();

            return new ByteArrayInputStream(resourceContent);
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            LOGGER.fine("reading stream '" + name + "'");
            if (name.endsWith("_en.properties"))
                name = name.substring(0,
                        name.length() - "_en.properties".length())
                        + ".properties";
            InputStream defaultPropsInputStream = null;
            InputStream filePropsInputStream = null;
            try {
                File propsFile = new File(this.folder, name);

                Properties defaultProps = new Properties();

                defaultPropsInputStream = this.getClass().getClassLoader()
                        .getResourceAsStream(name);
                if (defaultPropsInputStream != null || propsFile.exists()) {
                    defaultProps.load(new InputStreamReader(
                            defaultPropsInputStream, CHARSET));

                    Properties fileProps = new Properties();
                    if (propsFile.exists()) {
                        filePropsInputStream = new FileInputStream(propsFile);
                        fileProps.load(new InputStreamReader(
                                filePropsInputStream, CHARSET));
                    }

                    Set<Object> keySet = defaultProps.keySet();
                    for (Object key : keySet) {
                        if (!fileProps.containsKey(key)) {
                            fileProps.setProperty(key.toString(),
                                    defaultProps.getProperty(key.toString()));
                        }
                    }

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    fileProps.store(bos, "");

                    return readUTFStreamToEscapedASCII(new ByteArrayInputStream(
                            bos.toByteArray()));
                } else
                    return null;

            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            } finally {
                try {
                    if (defaultPropsInputStream != null) {
                        defaultPropsInputStream.close();
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
                try {
                    if (filePropsInputStream != null) {
                        filePropsInputStream.close();
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            return null;
        }

        @Override
        protected URL findResource(String name) {
            return null;
        }

        @Override
        protected Enumeration<URL> findResources(String name)
                throws IOException {
            Enumeration<URL> elements = new Vector().elements();
            return elements;
        }
    }

}
