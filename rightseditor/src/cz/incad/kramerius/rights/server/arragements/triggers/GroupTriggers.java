package cz.incad.kramerius.rights.server.arragements.triggers;

import java.util.List;
import java.util.logging.Logger;

import org.aplikator.client.data.RecordDTO;
import org.aplikator.client.descriptor.PropertyDTO;
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

    private Structure structure;

    public GroupTriggers(Structure structure) {
        super();
        this.structure = structure;
    }

    @Override
    public RecordDTO beforeCreate(RecordDTO record, Context ctx) {
        User user = GetCurrentLoggedUser.getCurrentLoggedUser(ctx.getHttpServletRequest());
        if ((user == null) || (!user.hasSuperAdministratorRole())) {
            List<Integer> groupsList = GetAdminGroupIds.getAdminGroupId(ctx);

            PropertyDTO<Integer> propertyDTO = structure.group.PERSONAL_ADMIN.clientClone(ctx);
            record.setValue(propertyDTO, groupsList.get(0));
        }

        return record;
    }

    @Override
    public RecordDTO afterCreate(RecordDTO record, Context ctx) {
        return record;
    }

    @Override
    public RecordDTO beforeUpdate(RecordDTO recordDTO, Context ctx) {
        User user = GetCurrentLoggedUser.getCurrentLoggedUser(ctx.getHttpServletRequest());
        if ((user == null) || (!user.hasSuperAdministratorRole())) {
            PropertyDTO<Integer> propertyDTO = structure.group.PERSONAL_ADMIN.clientClone(ctx);
            recordDTO.setNotForSave(propertyDTO, true);
        }
        return recordDTO;
    }

    @Override
    public RecordDTO afterUpdate(RecordDTO recordDTO, Context ctx) {
        return recordDTO;
    }

    @Override
    public RecordDTO beforeDelete(RecordDTO recordDTO, Context ctx) {
        return recordDTO;
    }

    @Override
    public RecordDTO afterDelete(RecordDTO recordDTO, Context ctx) {
        return recordDTO;
    }

}
