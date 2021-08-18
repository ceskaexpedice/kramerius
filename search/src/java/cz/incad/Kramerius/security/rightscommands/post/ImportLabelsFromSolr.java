package cz.incad.Kramerius.security.rightscommands.post;

import cz.incad.Kramerius.security.rightscommands.ServletRightsCommand;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.labels.LabelsManagerException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ImportLabelsFromSolr extends ServletRightsCommand {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ChangeProrityLabel.class.getName());


    @Override
    public void doCommand() throws IOException {
        try {
            if (this.actionAllowed.isActionAllowed(SecuredActions.CRITERIA_RIGHTS_MANAGE.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null, new ObjectPidsPath(SpecialObjects.REPOSITORY.getPid())).flag()) {
                this.labelsManager.refreshLabelsFromSolr();
            } else {
                this.responseProvider.get().sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (LabelsManagerException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

}
