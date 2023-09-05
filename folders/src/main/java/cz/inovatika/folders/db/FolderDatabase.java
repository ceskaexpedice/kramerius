package cz.inovatika.folders.db;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class FolderDatabase {


    @Inject
    @Named("kramerius4")
    Provider<Connection> provider;


    public List<Folder> getAllFolders() {
        List<Folder> result = new ArrayList<>();
        String sql = "SELECT * FROM folder ORDER BY updated_at DESC, name ASC";
        try (Connection conn = provider.get(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String uuid = rs.getString("uuid");
                String name = rs.getString("name");
                int itemsCount = rs.getInt("items_count");
                String updatedAt = rs.getTimestamp("updated_at").toString();
                result.add(new Folder(uuid, name, itemsCount, updatedAt, getFolderUsers(uuid)));
                //TODO: optimalizovat joinem ziskavani uzivatelu
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public List<Folder> getFolders(String userId) {
        List<Folder> result = new ArrayList<>();
        String sql = "SELECT * FROM folder, folder_user WHERE folder.uuid=folder_user.folder_uuid AND folder_user.user_id = ? ORDER BY folder.updated_at DESC, folder.name ASC";
        try (Connection conn = provider.get(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String uuid = rs.getString("uuid");
                String name = rs.getString("name");
                int itemsCount = rs.getInt("items_count");
                String updatedAt = rs.getTimestamp("updated_at").toString();
                result.add(new Folder(uuid, name, itemsCount, updatedAt, getFolderUsers(uuid)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public Folder getFolderByUuid(String folderUuid) {
        String sql = "SELECT * FROM folder WHERE uuid = ?";
        try (Connection conn = this.provider.get(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, folderUuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                int itemsCount = rs.getInt("items_count");
                String updatedAt = rs.getTimestamp("updated_at").toString();
                return new Folder(folderUuid, name, itemsCount, updatedAt, getFolderUsers(folderUuid));
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<FolderUser> getFolderUsers(String folderUuid) {
        String sql = "SELECT * FROM folder_user WHERE folder_uuid = ? ORDER BY created_at DESC";
        List<FolderUser> result = new ArrayList<>();
        try (Connection conn = this.provider.get(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, folderUuid);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String userId = rs.getString("user_id");
                String userRole = rs.getString("user_role");
                String createdAt = rs.getTimestamp("created_at").toString();
                result.add(new FolderUser(userId, userRole, createdAt));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Folder createFolder(String name, String folderUuid, long createdAtMs) {
        Timestamp createdAt = new Timestamp(createdAtMs);
        String sql = "INSERT INTO folder(uuid, name, updated_at) VALUES(?,?,?)";
        try (Connection conn = this.provider.get(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, folderUuid);
            pstmt.setString(2, name);
            pstmt.setTimestamp(3, createdAt);
            pstmt.executeUpdate();
            return new Folder(folderUuid, name, 0, createdAt.toString(), new ArrayList<>());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public FolderUser createFolderUser(String folderUuid, String userId, String userRole, long createdAtMs) {
        String sql = "INSERT INTO folder_user(folder_uuid, user_id, user_role, created_at) VALUES(?,?,?,?)";
        Timestamp createdAt = new Timestamp(createdAtMs);
        try (Connection conn = this.provider.get(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, folderUuid);
            pstmt.setString(2, userId);
            pstmt.setString(3, userRole);
            pstmt.setTimestamp(4, createdAt);
            pstmt.executeUpdate();
            return new FolderUser(userId, userRole, createdAt.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateFolderName(String folderUuid, String name) {
        String sql = "UPDATE folder SET name = ? WHERE uuid = ?";
        try (Connection conn = this.provider.get(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, folderUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteFolderUsers(String folderUuid) {
        String sql = "DELETE FROM folder_user WHERE folder_uuid = ?";
        try (Connection conn = this.provider.get(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, folderUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteFolder(String folderUuid) {
        String sql = "DELETE FROM folder WHERE uuid = ?";
        try (Connection conn = this.provider.get(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, folderUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addFolderItem(String folderUuid, String itemId, long createdAtMs) {
        String sql = "INSERT INTO folder_item(folder_uuid, item_id, created_at) VALUES(?,?,?)";
        try (Connection conn = this.provider.get(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, folderUuid);
            pstmt.setString(2, itemId);
            pstmt.setTimestamp(3, new Timestamp(createdAtMs));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<FolderItem> getItems(String folderUuid) {
        String sql = "SELECT * FROM folder_item WHERE folder_uuid = ? ORDER BY created_at DESC";
        List<FolderItem> result = new ArrayList<>();
        try (Connection conn = this.provider.get(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, folderUuid);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String itemId = rs.getString("item_id");
                String createdAt = rs.getTimestamp("created_at").toString();
                result.add(new FolderItem(itemId, folderUuid, createdAt));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeFolderItem(String folderUuid, String itemId) {
        String sql = "DELETE FROM folder_item WHERE folder_uuid = ? AND item_id = ?";
        try (Connection conn = this.provider.get(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, folderUuid);
            pstmt.setString(2, itemId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeFolderUser(String folderUuid, String userId) {
        String sql = "DELETE FROM folder_user WHERE folder_uuid = ? AND user_id = ?";
        try (Connection conn = this.provider.get(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, folderUuid);
            pstmt.setString(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static final class Folder {
        public String uuid;
        public String name;

        public Integer itemsCount;

        public String updatedAt;

        public List<FolderUser> users;

        public Folder(String uuid, String name, Integer itemsCount, String updatedAt, List<FolderUser> users) {
            this.uuid = uuid;
            this.name = name;
            this.users = users;
            this.itemsCount = itemsCount;
            this.updatedAt = updatedAt;
        }
    }

    public static final class FolderUser {
        public String userId;
        public String userRole;

        public String createdAt;

        public FolderUser(String userId, String userRole, String createdAt) {
            this.userId = userId;
            this.userRole = userRole;
            this.createdAt = createdAt;
        }
    }

    public static final class FolderItem {
        public String id;
        public String folderUuid;
        public String createdAt;

        public FolderItem(String id, String folderUuid, String createdAt) {
            this.id = id;
            this.folderUuid = folderUuid;
            this.createdAt = createdAt;
        }
    }

}
