package cz.incad.kramerius.service.impl;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.logging.Level;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.Constants;
import cz.incad.kramerius.service.ResourceBundleService;

public class ResourceBundleServiceImpl implements ResourceBundleService {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(ResourceBundleServiceImpl.class.getName());

    @Override
    public File bundlesFolder() {
        String workingDir = null; // TODO pepo
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
        return ResourceBundle.getBundle(name, ResourceBundleCache.resolveSupportedLocale(locale), UTF8cl, control);
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

    private UTF8ClassLoader UTF8cl = new UTF8ClassLoader();
    private ResourceBundle.Control control = new UserControl();

    /**
     * ResourceBundle Control implementation that loads resource bundle from the property file located in the directory
     * defined in HOME property (by default ${user.home}/.kramerius). The resource bundle has parent bundle loaded by
     * Default Control implementation
     */
    public class UserControl extends ResourceBundle.Control {

        public final List<String> FORMATS = Collections.unmodifiableList(Arrays.asList("java.properties"));

        public List<String> getFormats(String baseName) {
            if (baseName == null)
                throw new NullPointerException();
            return FORMATS;
        }

        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
            if (baseName == null || locale == null || format == null || loader == null)
                throw new NullPointerException();
            ResourceBundle bundle = null;
            if (format.equals("java.properties")) {
                ResourceBundle defaultBundle = super.newBundle(baseName, locale, "java.properties", loader, reload);
                String bundleName = toBundleName(baseName, locale);
                String resourceName = "file://" + checkFiles(baseName) + "/" + bundleName + ".properties";
                InputStream stream = null;
                try {
                    URL url = new URL(resourceName);
                    if (url != null) {
                        URLConnection connection = url.openConnection();
                        if (connection != null) {
                            if (reload) {
                                // Disable caches to get fresh data for
                                // reloading.
                                connection.setUseCaches(false);
                            }
                            stream = connection.getInputStream();
                        }
                    }
                } catch (Throwable t) {
                }
                if (stream != null) {
                    BufferedInputStream bis = new BufferedInputStream(stream);
                    bundle = new UserResourceBundle(UTF8ClassLoader.readUTFStreamToEscapedASCII(bis), defaultBundle);
                    bis.close();
                } else {
                    return defaultBundle;
                }
            }
            return bundle;
        }

    }



    public static class UTF8ClassLoader extends ClassLoader {

        /**
         * Charset used when reading a properties file.
         */
        private static final String CHARSET = "UTF-8";

        /**
         * Buffer size used when reading a properties file.
         */
        private static final int BUFFER_SIZE = 2000;

        public UTF8ClassLoader() {
            super(UTF8ClassLoader.class.getClassLoader());
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            try {
                return readUTFStreamToEscapedASCII(super.getResourceAsStream(name));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        // The following utility method is extracted from the Tapestry5 project
        // class org.apache.tapestry5.internal.services.MessagesSourceImpl
        //
        // Copyright 2006, 2007, 2008 The Apache Software Foundation
        //
        // Licensed under the Apache License, Version 2.0 (the "License");
        // you may not use this file except in compliance with the License.
        // You may obtain a copy of the License at
        //
        // http://www.apache.org/licenses/LICENSE-2.0
        //
        // Unless required by applicable law or agreed to in writing, software
        // distributed under the License is distributed on an "AS IS" BASIS,
        // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
        // implied.
        // See the License for the specific language governing permissions and
        // limitations under the License.

        /**
         * Reads a UTF-8 stream, performing a conversion to ASCII (i.e.,
         * ISO8859-1 encoding). Characters outside the normal range for
         * ISO8859-1 are converted to unicode escapes. In effect, it is
         * performing native2ascii on the files, on the fly.
         */
        private static InputStream readUTFStreamToEscapedASCII(InputStream is) throws IOException {
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

    }

    public class UserResourceBundle extends PropertyResourceBundle {

        public UserResourceBundle(InputStream stream, ResourceBundle parent) throws IOException {
            super(stream);
            setParent(parent);
        }

    }
}
