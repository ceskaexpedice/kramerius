package cz.incad.Kramerius.security.rightscommands.post;

import cz.incad.Kramerius.security.rightscommands.ServletRightsCommand;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.licenses.LicensesManagerException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;

public class ImportLabelsFromSolr extends ServletRightsCommand {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ChangeProrityLabel.class.getName());


    @Override
    public void doCommand() throws IOException {
        try {
            if (this.rightsResolver.isActionAllowed(SecuredActions.CRITERIA_RIGHTS_MANAGE.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null, new ObjectPidsPath(SpecialObjects.REPOSITORY.getPid())).flag()) {
                this.licensesManager.refreshLabelsFromSolr();
            } else {
                this.responseProvider.get().sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (LicensesManagerException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

}
