package cz.incad.kramerius.security.licenses;

import java.util.List;

//TODO: rename to licenses manager
public interface LicensesManager {

    public static final String LOCAL_GROUP_NAME="local";
    public static final String IMPORTED_GROUP_NAME ="imported";

    public void addLocalLabel(License license) throws LicensesManagerException;
    public void removeLocalLabel(License license) throws LicensesManagerException;

    public int getMinPriority() throws LicensesManagerException;
    public int getMaxPriority() throws LicensesManagerException;

    public License getLabelByPriority(int priority) throws LicensesManagerException;
    public License getLabelById(int id) throws LicensesManagerException;
    public License getLabelByName(String name) throws LicensesManagerException;

    public List<License> getLabels() throws LicensesManagerException;

    public void updateLabel(License license) throws LicensesManagerException;
    public void moveUp(License license) throws LicensesManagerException;
    public void moveDown(License license) throws LicensesManagerException;


    public void refreshLabelsFromSolr() throws LicensesManagerException;
}
