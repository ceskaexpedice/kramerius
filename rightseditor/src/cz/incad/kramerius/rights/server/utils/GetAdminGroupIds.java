package cz.incad.kramerius.rights.server.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;

import org.aplikator.server.Context;

import cz.incad.kramerius.rights.server.SecuredActions;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.utils.SecurityDBUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;

public class GetAdminGroupIds {

    public static String constructGroupIdsString(User curUser) {
        StringBuffer buffer = new StringBuffer();
        Role[] roles = curUser.getGroups();
        for (int i = 0; i < roles.length; i++) {
            Role role = roles[i];
            buffer.append(role.getId());
            if (i <= roles.length - 2) {
                buffer.append(",");
            }
        }
        return buffer.toString();
    }

    public static List<Integer> getAdminGroupId(Context ctx) {
        User curUser = GetCurrentLoggedUser.getCurrentLoggedUser(ctx.getHttpServletRequest());
        String queryPattern = "select ent.user_id,ent.group_id from right_entity ent " + " left join  user_entity users on  (ent.user_id = users.user_id) " + " left join  group_entity groups on  (ent.group_id = groups.group_id) " + " where uuid=''uuid:1'' and \"action\"=''{0}'' and (ent.user_id=" + curUser.getId()
                + " or ent.group_id in (" + constructGroupIdsString(curUser) + "))";
        String query = null;
        if (curUser.hasSuperAdministratorRole()) {
            query = MessageFormat.format(queryPattern, SecuredActions.rightsadmin.getFormalName());
        } else {
            query = MessageFormat.format(queryPattern, SecuredActions.rightssubadmin.getFormalName());
        }

        List<Integer> groupsList = new JDBCQueryTemplate<Integer>(SecurityDBUtils.getConnection()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Integer> retList) throws SQLException {
                int groupId = rs.getInt("group_id");
                retList.add(groupId);
                return true;
            }

        }.executeQuery(query);
        return groupsList;
    }

}
