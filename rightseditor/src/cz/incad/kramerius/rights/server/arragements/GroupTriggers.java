package cz.incad.kramerius.rights.server.arragements;

import java.security.Principal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.aplikator.client.data.RecordDTO;
import org.aplikator.client.descriptor.PropertyDTO;
import org.aplikator.server.Context;
import org.aplikator.server.persistence.PersisterTriggers;

import sun.security.acl.GroupImpl;

import cz.incad.kramerius.rights.server.SecuredActions;
import cz.incad.kramerius.rights.server.Structure;
import cz.incad.kramerius.security.Group;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.security.jaas.K4UserPrincipal;
import cz.incad.kramerius.security.utils.SecurityDBUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;

public class GroupTriggers implements PersisterTriggers {
	
	public static final String DEBUG_KEY = GroupTriggers.class.getName();
	
	private static Logger LOGGER = Logger.getLogger(GroupTriggers.class.getName());
	
	private Structure structure;
	
	
	
	public GroupTriggers(Structure structure) {
		super();
		this.structure = structure;
	}

	@Override
	public RecordDTO beforeCreate(RecordDTO record, Context ctx) {
		User curUser = getCurrentLoggedUser(ctx.getHttpServletRequest());
		String queryPattern = "select ent.user_id,ent.group_id from right_entity ent"+
			"left join  user_entity users on  (ent.user_id = users.user_id)"+
			"left join  group_entity groups on  (ent.group_id = groups.group_id)"+
		" where uuid=''uuid:1'' and \"action\"=''{0}'' and (ent.user_id="+curUser.getId()+" or ent.group_id in ("+getGroupIds(curUser)+"))";
		String query = null;
		if (curUser.hasSuperAdministratorRole()) {
			query = MessageFormat.format(queryPattern, SecuredActions.rightsadmin.getFormalName());
		} else {
			query = MessageFormat.format(queryPattern, SecuredActions.rightssubadmin.getFormalName());
		}
		
        List<Integer> groupsList = new JDBCQueryTemplate<Integer>(SecurityDBUtils.getConnection()){
            @Override
            public boolean handleRow(ResultSet rs, List<Integer> retList) throws SQLException {
            	int groupId = rs.getInt("group_id");
            	retList.add(groupId);
            	return true;
            }
            
        }.executeQuery(query);

		
		PropertyDTO propertyDTO = structure.group.PERSONAL_ADMIN.clientClone(ctx);
		record.setValue(propertyDTO, groupsList.get(0));
		
		return record;
	}

	private String getGroupIds(User curUser) {
		StringBuffer buffer = new StringBuffer();
		Group[] grps = curUser.getGroups();
		for (int i = 0; i < grps.length; i++) {
			Group grp = grps[i];
			buffer.append(grp.getId());
			if (i <= grps.length -2) {
				buffer.append(",");
			}
		}
		return buffer.toString();
	}

	@Override
	public RecordDTO afterCreate(RecordDTO record, Context ctx) {
		return record;
	}

	@Override
	public RecordDTO beforeUpdate(RecordDTO recordDTO, Context ctx) {
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
	
	

	public User getCurrentLoggedUser(HttpServletRequest request) {
		return getJosefVomacka();
//		if (Boolean.getBoolean(DEBUG_KEY)) {
//			// pouze pro debug
//			return getKarelPoslusny();
//		} else {
//			Principal principal = request.getUserPrincipal();
//	        if (principal != null) {
//	            K4UserPrincipal k4principal = (K4UserPrincipal) principal;
//	            User user = k4principal.getUser();
//	            return user;
//	        } else return null;		
//		}
	}
	

	public User getJosefVomacka() {
		UserImpl userImpl = new UserImpl(1, "josef", "vomacka", "josef.vomacka@mzz.cz", 0);	
		Group knav_subadmins = new cz.incad.kramerius.security.impl.GroupImpl(4,"knav_subadmins",3);
		userImpl.setGroups(new Group[] {knav_subadmins});
		return userImpl;
	}

	
	public User getPavelStastny() {
		UserImpl userImpl = new UserImpl(2, "Pavel", "Stastny", "pavels@incad.cz", 0);	
		Group k4_admins = new cz.incad.kramerius.security.impl.GroupImpl(3,"k4_admins",0);
		Group knav_users = new cz.incad.kramerius.security.impl.GroupImpl(2,"knav_users",4);
		userImpl.setGroups(new Group[] {k4_admins, knav_users});
		return userImpl;
	}

	public User getKarelPoslusny() {
		UserImpl userImpl = new UserImpl(3, "Karel", "Poslusny", "karels@poslusny.cz", 0);	
		Group knav_users = new cz.incad.kramerius.security.impl.GroupImpl(2,"knav_users",4);
		userImpl.setGroups(new Group[] { knav_users});
		return userImpl;
	}

}
