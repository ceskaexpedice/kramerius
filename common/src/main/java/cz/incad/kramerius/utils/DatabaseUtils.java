package cz.incad.kramerius.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import cz.incad.kramerius.utils.database.JDBCQueryTemplate;

public class DatabaseUtils {

	public static boolean tableExists(Connection con, String tableName) throws SQLException  {
		ResultSet rs = null;
		try {
			String[] types = {"TABLE"};
			rs = con.getMetaData().getTables(null, null, "%", types);
			while(rs.next()) {
				if (tableName.equals(rs.getString("TABLE_NAME").toUpperCase())) {
					return true;
				}
			}
			return false;
		} finally {
			if (rs != null) rs.close();
		}
	}

	public static boolean columnExists(Connection con, String tableName, String columnName) throws SQLException {
       PreparedStatement pstm = null; 
       ResultSet rs = null;
        try {
            pstm = con.prepareStatement("select * from "+tableName+" where 1>2");
            ResultSetMetaData pstmMetadata = pstm.getMetaData();
            int size = pstmMetadata.getColumnCount();
            for (int i = 0; i < size; i++) {
                String mtdColName = pstmMetadata.getColumnName(i+1);
                if (mtdColName.toLowerCase().equals(columnName.toLowerCase())) return true;
            }
            return false;
        } finally {
            if (rs != null) rs.close();
        }
	}
}
