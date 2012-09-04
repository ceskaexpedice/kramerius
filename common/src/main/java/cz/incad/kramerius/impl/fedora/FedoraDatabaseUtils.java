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

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Provider;

import cz.incad.kramerius.utils.database.JDBCQueryTemplate;

/**
 * Utility class for getting informations from fedora database
 * @author pavels
 */
@Deprecated
public class FedoraDatabaseUtils {

	static java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(FedoraDatabaseUtils.class.getName());
	
	private FedoraDatabaseUtils() {}
	
	
	public static List<String> getRelativeDataStreamPath(String uuid, Provider<Connection> provider) throws SQLException {
		String dataStreamPath = getDataStreamPath(uuid, provider);
		if (dataStreamPath == null) return new ArrayList<String>();
		LOGGER.info("datastream path is '"+dataStreamPath+"'");
		List<String> folderList = new ArrayList<String>();
		File currentFile = new File(dataStreamPath);
        while(!currentFile.getName().equals("data")) {
            folderList.add(currentFile.getName());
            currentFile = currentFile.getParentFile();
        }
        return folderList;
	}
	
	public static String getRelativeDataStreamPathAsString(String uuid, Provider<Connection> provider) throws SQLException {
	    StringTemplate template = new StringTemplate("$path;separator=\",\"$");
	    template.setAttribute("path", getRelativeDataStreamPath(uuid, provider));
	    return template.toString();
	}
	
	/**
	 * Returns path to file
	 * @param uuid UUID of the object 
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	@Deprecated
    public static String getDataStreamPath(String uuid, Provider<Connection> provider) throws SQLException {
        String sql = "select * from datastreampaths where token like ? order by tokendbid ASC";
        List<String> returnList = new JDBCQueryTemplate<String>(provider.get()){
            @Override
            public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                String path = rs.getString("path");
                returnsList.add(path);
                return super.handleRow(rs, returnsList);
            }
            
        }.executeQuery(sql, "uuid:"+uuid+"+IMG_FULL+%");
        return (returnList != null && !returnList.isEmpty()) ? returnList.get(0) : null;
    }	
}
