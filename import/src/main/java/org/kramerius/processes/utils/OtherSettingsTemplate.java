package org.kramerius.processes.utils;

public  enum OtherSettingsTemplate {

    importFedoraNoIndexer, importFedoraStartIndexer,noFedoraNoIndexer;
    
    public static OtherSettingsTemplate disectTemplate(Boolean importToFedora, Boolean startIndexer) {
        OtherSettingsTemplate oSettings = (importToFedora && startIndexer) ? OtherSettingsTemplate.importFedoraStartIndexer : ((importToFedora && !startIndexer) ? OtherSettingsTemplate.importFedoraNoIndexer : OtherSettingsTemplate.noFedoraNoIndexer);
        return oSettings;
    }
}
