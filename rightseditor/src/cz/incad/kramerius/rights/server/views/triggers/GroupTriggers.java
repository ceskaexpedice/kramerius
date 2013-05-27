package cz.incad.kramerius.rights.server.views.triggers;

import java.util.List;
import java.util.logging.Logger;

import org.aplikator.client.shared.data.Record;
import org.aplikator.server.Context;
import org.aplikator.server.persistence.PersisterTriggers;

import cz.incad.kramerius.rights.server.Structure;
import cz.incad.kramerius.rights.server.utils.GetAdminGroupIds;
import cz.incad.kramerius.rights.server.utils.GetCurrentLoggedUser;
import cz.incad.kramerius.security.User;

public class GroupTriggers extends PersisterTriggers.Default {

    public static final String DEBUG_KEY = GroupTriggers.class.getName();

    @SuppressWarnings("unused")
    private static Logger LOGGER = Logger.getLogger(GroupTriggers.class.getName());


    public GroupTriggers(Structure structure) {
        super();
    }

    @Override
    public void onCreate(Record record, Context ctx) {
        User user = GetCurrentLoggedUser.getCurrentLoggedUser(ctx.getHttpServletRequest());
        if ((user == null) || (!user.hasSuperAdministratorRole())) {
            List<Integer> groupsList = GetAdminGroupIds.getAdminGroupId(ctx);
            Structure.group.PERSONAL_ADMIN.setValue(record, groupsList.get(0));
        }
    }


    @Override
    public void onUpdate(Record record, Context ctx) {
        /*User user = GetCurrentLoggedUser.getCurrentLoggedUser(ctx.getHttpServletRequest());
        if ((user == null) || (!user.hasSuperAdministratorRole())) {
            PropertyDTO<Integer> propertyDTO = structure.group.PERSONAL_ADMIN.clientClone(ctx);
            recordDTO.setNotForSave(propertyDTO, true);
        }*/
    }


}
