package cz.incad.kramerius.uiconfig;

import com.google.inject.Provider;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbUIConfigService implements UIConfigService {

    private final JsonValidator validator;
    Provider<Connection> connectionProvider;

    public DbUIConfigService(Provider<Connection> connectionProvider, JsonValidator validator) {
        this.connectionProvider = connectionProvider;
        this.validator = validator;
    }

    public void save(UIConfigType type, InputStream json) throws IOException {
        byte[] data = json.readAllBytes();

        if(validator != null) {
            validator.validate(new ByteArrayInputStream(data));
        }

        try (Connection c = connectionProvider.get();
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

    public InputStream load(UIConfigType type) throws IOException {
        try (Connection c = connectionProvider.get();
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

    public boolean exists(UIConfigType type) {
        // trivial SELECT 1 implementation
        return false;
    }
}
