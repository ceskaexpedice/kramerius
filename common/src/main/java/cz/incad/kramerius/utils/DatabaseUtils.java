package cz.incad.kramerius.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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

}
