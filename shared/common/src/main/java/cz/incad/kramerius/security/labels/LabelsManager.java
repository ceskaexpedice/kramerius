package cz.incad.kramerius.security.labels;

import java.util.List;

public interface LabelsManager {

    public static final String LOCAL_GROUP_NAME="local";
    public static final String IMPORTED_GROUP_NAME ="imported";

    public void addLocalLabel(Label label) throws LabelsManagerException;
    public void removeLocalLabel(Label label) throws LabelsManagerException;

    public int getMinPriority() throws LabelsManagerException;
    public int getMaxPriority() throws LabelsManagerException;

    public Label getLabelByPriority(int priority) throws LabelsManagerException;
    public Label getLabelById(int id) throws LabelsManagerException;

    public List<Label> getLabels() throws LabelsManagerException;

    public void updateLabel(Label label) throws LabelsManagerException;
    public void moveUp(Label label) throws LabelsManagerException;
    public void moveDown(Label label) throws LabelsManagerException;


    public void refreshLabelsFromSolr() throws LabelsManagerException;
}
