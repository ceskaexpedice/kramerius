package com.qbizm.kramerius.imptool.poc.valueobj;

import java.sql.Connection;

import javax.xml.bind.Marshaller;

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
    
    private boolean defaultVisibility = false;
    
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

    public boolean isDefaultVisibility() {
        return defaultVisibility;
    }

    public void setDefaultVisibility(boolean defaultVisibility) {
        this.defaultVisibility = defaultVisibility;
    }

    public Connection getDbConnection() {
        return dbConnection;
    }

    public void setDbConnection(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

}
