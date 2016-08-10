package cz.incad.kramerius;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;

import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;

public abstract class AbstractGuiceTestCase {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(AbstractGuiceTestCase.class.getName());

    public Connection connection() {
        Injector inj = injector();
        Provider<Connection> kramerius4Provider = inj.getProvider(Key.get(Connection.class, Names.named("kramerius4")));
        Connection connection = kramerius4Provider.get();
        return connection;
    }

    protected abstract Injector injector();
}
