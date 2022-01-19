package cz.incad.Kramerius.security.rightscommands.post;

import cz.incad.Kramerius.security.rightscommands.ServletRightsCommand;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.LicensesManager;
import cz.incad.kramerius.security.licenses.LicensesManagerException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ChangeProrityLabel extends ServletRightsCommand {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ChangeProrityLabel.class.getName());


    @Override
    public void doCommand() throws IOException {
        HttpServletRequest req = this.requestProvider.get();
        //Right right = RightsServlet.createRightFromPost(req, rightsManager, userManager, criteriumWrapperFactory);
        Map values = new HashMap();
        Enumeration parameterNames = req.getParameterNames();

        Delete.parametersToJson(req, values, parameterNames);
        Map map = (Map) values.get("changepriority");
        String labelId = (String) map.get("id");
        String dir = (String) map.get("direction");
        if(labelId != null) {
            try {
                if (this.rightsResolver.isActionAllowed(SecuredActions.ADMINISTRATE.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null, new ObjectPidsPath(SpecialObjects.REPOSITORY.getPid())).flag()) {
                    Direction.valueOf(dir).move(Integer.parseInt(labelId), this.licensesManager);
                } else {
                    this.responseProvider.get().sendError(HttpServletResponse.SC_FORBIDDEN);
                }
            } catch (LicensesManagerException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        }
    }

    static enum Direction{
        UP {
            @Override
            protected void move(int id, LicensesManager licensesManager) throws LicensesManagerException {
                License license = licensesManager.getLabelById(id);
                if (license != null)  licensesManager.moveUp(license);
            }
        },
        DOWN {
            @Override
            protected void move(int id, LicensesManager licensesManager) throws LicensesManagerException {
                License license = licensesManager.getLabelById(id);
                if (license != null)  licensesManager.moveDown(license);
            }
        };
        protected abstract void move(int id, LicensesManager labelsManager) throws LicensesManagerException;
    }
}
