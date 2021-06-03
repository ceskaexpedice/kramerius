package cz.incad.Kramerius.security.rightscommands.post;

import cz.incad.Kramerius.security.rightscommands.ServletRightsCommand;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.labels.Label;
import cz.incad.kramerius.security.labels.LabelsManager;
import cz.incad.kramerius.security.labels.LabelsManagerException;
import cz.incad.kramerius.security.labels.impl.LabelImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class DeleteLabel extends ServletRightsCommand {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DeleteLabel.class.getName());


    @Override
    public void doCommand() throws IOException {
        HttpServletRequest req = this.requestProvider.get();
        //Right right = RightsServlet.createRightFromPost(req, rightsManager, userManager, criteriumWrapperFactory);
        Map values = new HashMap();
        Enumeration parameterNames = req.getParameterNames();

        Delete.parametersToJson(req, values, parameterNames);
        Map label = (Map) values.get("deletelabel");
        String labelId = (String) label.get("id");
        if (labelId != null) {
            try {
                if (this.actionAllowed.isActionAllowed(SecuredActions.ADMINISTRATE.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null, new ObjectPidsPath(SpecialObjects.REPOSITORY.getPid())).flag()) {
                    if (labelId != null && Integer.parseInt(labelId) > -1) {
                        Label foundLabel = labelsManager.getLabelById(Integer.parseInt(labelId));
                        if (foundLabel != null) {
                            labelsManager.removeLocalLabel(foundLabel);
                        }
                    }
                } else {
                    this.responseProvider.get().sendError(HttpServletResponse.SC_FORBIDDEN);
                }
            } catch (LabelsManagerException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        }

    }
}
