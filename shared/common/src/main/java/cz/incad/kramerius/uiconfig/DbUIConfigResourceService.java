package cz.incad.kramerius.uiconfig;

import com.google.inject.Provider;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DbUIResourceService
 *
 * @author ppodsednik
 */
public class DbUIConfigResourceService implements UIConfigResourceService {

    private final Provider<Connection> connectionProvider;

    public DbUIConfigResourceService(Provider<Connection> connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void save(String resourceKey, String contentType, InputStream content) {
        try (Connection c = connectionProvider.get();
             PreparedStatement ps = c.prepareStatement("""
                     INSERT INTO ui_config_resource
                         (resource_key, content_type, content)
                     VALUES
                         (?, ?, ?)
                     ON CONFLICT (resource_key)
                     DO UPDATE SET
                         content_type = EXCLUDED.content_type,
                         content = EXCLUDED.content
                     """)) {
            ps.setString(1, resourceKey);
            ps.setString(2, contentType);
            ps.setBinaryStream(3, content);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new UIConfigException("Failed saving resource " + resourceKey, e);
        }
    }

    @Override
    public UIConfigResourceContent load(String resourceKey) {
        try (Connection c = connectionProvider.get();
             PreparedStatement ps = c.prepareStatement("""
                     SELECT content_type, content
                     FROM ui_config_resource
                     WHERE resource_key = ?
                     """)) {
            ps.setString(1, resourceKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new UIConfigResourceContent(
                        resourceKey,
                        rs.getString("content_type"),
                        rs.getBytes("content")
                );
            }
        } catch (SQLException e) {
            throw new UIConfigException("Failed loading resource " + resourceKey, e);
        }
    }

    @Override
    public List<UIConfigResourceInfo> list() {
        try (Connection c = connectionProvider.get();
             PreparedStatement ps = c.prepareStatement("""
                     SELECT resource_key, content_type
                     FROM ui_config_resource
                     ORDER BY resource_key
                     """);
             ResultSet rs = ps.executeQuery()) {
            List<UIConfigResourceInfo> resources = new ArrayList<>();
            while (rs.next()) {
                resources.add(new UIConfigResourceInfo(
                        rs.getString("resource_key"),
                        rs.getString("content_type")
                ));
            }
            return resources;
        } catch (SQLException e) {
            throw new UIConfigException("Failed listing resources", e);
        }
    }

    @Override
    public void delete(String resourceKey) {
        try (Connection c = connectionProvider.get();
             PreparedStatement ps = c.prepareStatement("""
                     DELETE FROM ui_config_resource
                     WHERE resource_key = ?
                     """)) {
            ps.setString(1, resourceKey);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new UIConfigException("Failed deleting resource " + resourceKey, e);
        }
    }

    @Override
    public boolean exists(String resourceKey) {
        try (Connection c = connectionProvider.get();
             PreparedStatement ps = c.prepareStatement("""
                     SELECT 1
                     FROM ui_config_resource
                     WHERE resource_key = ?
                     LIMIT 1
                     """)) {
            ps.setString(1, resourceKey);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new UIConfigException("Failed checking resource " + resourceKey, e);
        }
    }
}
