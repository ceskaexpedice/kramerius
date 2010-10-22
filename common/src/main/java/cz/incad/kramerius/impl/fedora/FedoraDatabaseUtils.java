package cz.incad.kramerius.impl.fedora;

import java.io.File;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class FedoraDatabaseUtils {

	static java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(FedoraDatabaseUtils.class.getName());
	
	private FedoraDatabaseUtils() {}
	
	public static List<String> getRelativeDataStreamPath(String uuid, Connection connection) throws SQLException {
		String dataStreamPath = getDataStreamPath(uuid, connection);
		List<String> folderList = new ArrayList<String>();
        File currentFile = new File(dataStreamPath);
        while(!currentFile.getName().equals("data")) {
            folderList.add(currentFile.getName());
            currentFile = currentFile.getParentFile();
        }
        return folderList;
	}
	
    public static String getDataStreamPath(String uuid, Connection connection) throws SQLException {
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement("select * from datastreampaths where token like ? order by tokendbid ASC");
            pstm.setString(1, "uuid:"+uuid+"+IMG_FULL%");
            rs = pstm.executeQuery();
            if(rs.next()) {
                return rs.getString("path");
            } else return null;
        } finally {
            if (rs != null) {
                try { 
                    rs.close(); 
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }	
}
