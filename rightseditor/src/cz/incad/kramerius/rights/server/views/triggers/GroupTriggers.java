package cz.incad.kramerius.rights.server.views.triggers;

import java.util.List;
import java.util.logging.Logger;

import org.aplikator.client.data.Record;
import org.aplikator.server.Context;
import org.aplikator.server.persistence.PersisterTriggers;

import cz.incad.kramerius.rights.server.Structure;
import cz.incad.kramerius.rights.server.utils.GetAdminGroupIds;
import cz.incad.kramerius.rights.server.utils.GetCurrentLoggedUser;
import cz.incad.kramerius.security.User;

public class GroupTriggers extends AbstractUserTriggers implements PersisterTriggers {

    public static final String DEBUG_KEY = GroupTriggers.class.getName();

    @SuppressWarnings("unused")
    private static Logger LOGGER = Logger.getLogger(GroupTriggers.class.getName());


    public GroupTriggers(Structure structure) {
        super();
    }

    @Override
    public Record beforeCreate(Record record, Context ctx) {
        User user = GetCurrentLoggedUser.getCurrentLoggedUser(ctx.getHttpServletRequest());
        if ((user == null) || (!user.hasSuperAdministratorRole())) {
            List<Integer> groupsList = GetAdminGroupIds.getAdminGroupId(ctx);
            Structure.group.PERSONAL_ADMIN.setValue(record, groupsList.get(0));
        }

        return record;
    }

    @Override
    public Record afterCreate(Record record, Context ctx) {
        return record;
    }

    @Override
    public Record beforeUpdate(Record record, Context ctx) {
        /*User user = GetCurrentLoggedUser.getCurrentLoggedUser(ctx.getHttpServletRequest());
        if ((user == null) || (!user.hasSuperAdministratorRole())) {
            PropertyDTO<Integer> propertyDTO = structure.group.PERSONAL_ADMIN.clientClone(ctx);
            recordDTO.setNotForSave(propertyDTO, true);
        }*/
        return record;
    }

    @Override
    public Record afterUpdate(Record record, Context ctx) {
        return record;
    }

    @Override
    public Record beforeDelete(Record record, Context ctx) {
        return record;
    }

    @Override
    public Record afterDelete(Record record, Context ctx) {
        return record;
    }

}
