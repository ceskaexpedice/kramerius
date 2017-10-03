package cz.incad.kramerius;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.google.inject.Provider;

public class ConProvider4T implements Provider<Connection> {

    public static Connection openConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection("jdbc:postgresql://194.108.215.227/kramerius4fortests",
                "fedoraAdmin", "fedoraAdmin");
        return con;
    }

    public static Connection openLocalConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection("jdbc:postgresql://localhost/kramerius4", "fedoraAdmin",
                "fedoraAdmin");
        return con;
    }

    @Override
    public Connection get() {
        try {
            return openLocalConnection();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
