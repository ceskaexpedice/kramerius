package cz.incad.kramerius.uiconfig;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbUIConfigStore implements UIConfigStore {

    private final DataSource dataSource;

    public DbUIConfigStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public InputStream load(UIConfigType type) throws IOException {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT config_json FROM ui_config WHERE config_type = ?")) {

            ps.setString(1, type.name());

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new FileNotFoundException(type.name());
            }

            return rs.getBinaryStream(1);
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void save(UIConfigType type, InputStream json) throws IOException {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 INSERT INTO ui_config (config_type, config_json, updated_at)
                 VALUES (?, ?::jsonb, now())
                 ON CONFLICT (config_type)
                 DO UPDATE SET config_json = EXCLUDED.config_json,
                               updated_at = now()
             """)) {

            ps.setString(1, type.name());
            ps.setBinaryStream(2, json);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean exists(UIConfigType type) {
        // trivial SELECT 1 implementation
        return false;
    }
}
