package cz.incad.kramerius.uiconfig;

import com.google.inject.Provider;

import javax.ws.rs.NotFoundException;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DbUIConfigService
 *
 * @author ppodsednik
 */
public class DbUIConfigService implements UIConfigService {

    private final JsonValidator validator;
    private Provider<Connection> connectionProvider;

    public DbUIConfigService(Provider<Connection> connectionProvider, JsonValidator validator) {
        this.connectionProvider = connectionProvider;
        this.validator = validator;
    }

    public void save(UIConfigType type, InputStream json) {
        try {
            byte[] data = json.readAllBytes();
            if(validator != null) {
                validator.validate(new ByteArrayInputStream(data));
            }
            try (Connection c = connectionProvider.get();
                 PreparedStatement ps = c.prepareStatement("""
                 INSERT INTO ui_config (config_type, config_json)
                 VALUES (?, ?::jsonb)
                 ON CONFLICT (config_type)
                 DO UPDATE SET config_json = EXCLUDED.config_json
             """)) {
                ps.setString(1, type.name());
                String jsonText = new String(data, StandardCharsets.UTF_8);
                ps.setString(2, jsonText);
                ps.executeUpdate();
            }
        } catch (InvalidJsonException e) {
            throw e;
        } catch (Exception e) {
            throw new UIConfigException(
                    "Failed saving config " + type, e);
        }
    }

    public InputStream load(UIConfigType type) {
        try (Connection c = connectionProvider.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT config_json FROM ui_config WHERE config_type = ?")) {
            ps.setString(1, type.name());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new NotFoundException("UI config not found: " + type);
            }
            return rs.getBinaryStream(1);
        } catch (SQLException e) {
            throw new UIConfigException("Failed loading config " + type, e);
        }
    }

    @Override
    public boolean exists(UIConfigType type) {
        try (Connection c = connectionProvider.get();
             PreparedStatement ps = c.prepareStatement("""
                         SELECT 1
                         FROM ui_config
                         WHERE config_type = ?
                         LIMIT 1
                     """)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new UIConfigException("Failed checking existence for " + type, e);
        }
    }
}
