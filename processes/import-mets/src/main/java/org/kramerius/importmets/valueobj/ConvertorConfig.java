package org.kramerius.importmets.valueobj;

import cz.incad.kramerius.utils.IOUtils;
import org.kramerius.importmets.MetsConvertor;

import javax.xml.bind.Marshaller;
import java.sql.Connection;
import java.util.Calendar;

/**
 * Konfigurace konvertoru
 * 
 * @author xholcik
 */
public class ConvertorConfig {

    private Marshaller marshaller;

    private String importFolder;

    private String exportFolder;

    private int contractLength;
    
    private boolean policyPublic = false;
    
    private String contract;


    /**
     * holder for imageserver subfolder file path, filled when the property convert.imageServerDirectorySubfolders is true
     */

    private String imgTreePath = "";

    /**
     * holder for imageserver subfolder URL corresponding to treePath
     */

    private String imgTreeUrl = "";
    
    
    private Connection dbConnection;

    public String getImportFolder() {
        return importFolder;
    }

    public void setImportFolder(String importFolder) {
        this.importFolder = importFolder;
    }

    public String getExportFolder() {
        return exportFolder;
    }

    public void setExportFolder(String exportFolder) {
        this.exportFolder = exportFolder;
    }

    public Marshaller getMarshaller() {
        return marshaller;
    }

    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public int getContractLength() {
        return contractLength;
    }

    public void setContractLength(int contractLength) {
        this.contractLength = contractLength;
    }

    public boolean isPolicyPublic() {
        return policyPublic;
    }

    public void setPolicyPublic(boolean policyPublic) {
        this.policyPublic = policyPublic;
    }

    public Connection getDbConnection() {
        return dbConnection;
    }

    public void setDbConnection(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

	public void setContract(String contract) {
        this.contract = contract.replaceAll("[\\\\/:\"*?<>|]","_");
		if (MetsConvertor.useContractSubfolders()){
			this.exportFolder = this.exportFolder+ System.getProperty("file.separator")+this.contract;
			IOUtils.checkDirectory(this.exportFolder);
			String xmlSubfolder = this.exportFolder+ System.getProperty("file.separator")+"xml";//Issue 73
			IOUtils.checkDirectory(xmlSubfolder);
		}
	}
	
	public String getContract(){
		return contract;
	}

    public void setImgTree(){
        Calendar now = Calendar.getInstance();
        int intyear = now.get(Calendar.YEAR);
        int intmonth = now.get(Calendar.MONTH)+1;
        int intday = now.get(Calendar.DAY_OF_MONTH);
        String year =  String.format("%04d", intyear);
        String month =  String.format("%02d", intmonth);
        String day =  String.format("%02d", intday);
        imgTreePath = System.getProperty("file.separator")+year+System.getProperty("file.separator")+month+System.getProperty("file.separator")+day;
        imgTreeUrl = "/"+year+"/"+month+"/"+day;

    }

    public String getImgTreePath() {
        return imgTreePath;
    }

    public String getImgTreeUrl() {
        return imgTreeUrl;
    }


	
}
