package cz.incad.kramerius.intconfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

@Deprecated
public class InternalConfiguration {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(InternalConfiguration.class.getName());
    private static InternalConfiguration _internal;
    private Properties properties = new Properties();

    private InternalConfiguration() throws IOException {
        super();
        InputStream stream = null;
        try {
            stream = this.getClass().getResourceAsStream("res/internalconfiguration.properties");
            if (stream != null) {
                properties.load(stream);
            }
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public static InternalConfiguration get() {
        if (_internal == null) {
            try {
                _internal = new InternalConfiguration();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return _internal;
    }

    public Properties getProperties() {
        return properties;
    }

}
