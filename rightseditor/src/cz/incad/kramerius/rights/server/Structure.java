package cz.incad.kramerius.rights.server;


import org.aplikator.client.data.ListItem;
import org.aplikator.client.descriptor.PropertyType;
import org.aplikator.server.descriptor.Application;
import org.aplikator.server.descriptor.Collection;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.Reference;

public class Structure extends Application {

	public class UserEntity extends Entity {

		public final Property NAME;
        public final Property SURNAME;
        public final Property LOGINNAME;
        public final Property PASSWORD;
        
        public UserEntity() {
            super("Users_table", "USER_ENTITY", "USER_ID", Structure.this);
            NAME= addProperty("NAME", PropertyType.STRING, 255, true);
            SURNAME = addProperty("SURNAME", PropertyType.STRING, 255, true);
            LOGINNAME= addProperty("LOGINNAME", PropertyType.STRING, 255, true);
            PASSWORD= addProperty("PSWD", PropertyType.STRING, 255, true);
            
            addIndex("UNAME_IDX", false, NAME);
            addIndex("SURNAME_IDX", false, SURNAME);
            addIndex("LOGINNAME_IDX", false, LOGINNAME);
            addIndex("PASSWORD_IDX", false, PASSWORD);
        }
	}
	

	public class GroupEntity extends Entity {

		public final Property GNAME;
		//public Collection USERS;
		
        public GroupEntity() {
            super("Groups_table", "GROUP_ENTITY", "GROUP_ID", Structure.this);
            GNAME= addProperty("GNAME", PropertyType.STRING, 255, true);
            addIndex("GNAME_IDX", false, GNAME);
        }
	}

	public class GroupUserAssoction extends Entity {

		public final Reference USERS;
		public final Reference GROUP;
		
		public GroupUserAssoction() {
			super("GroupTable assoc", "GROUP_USER_ASSOC", "GROUP_USER_ASSOC_ID", Structure.this);
			USERS=addReference(user, "USER_ID");
			GROUP=addReference(group, "GROUP_ID");
		}
	}
	
	
	public class RightsEntity extends Entity {

		public final Property UUID;
		public final Property ACTION;
		public final Property FIXED_PRIORITY;

		public Reference RIGHT_CRITERIUM;
        public Reference USER;
        public Reference GROUP;

		
        public RightsEntity() {
            super("Rights_table", "RIGHT_ENTITY", "RIGHT_ID", Structure.this);
            UUID= addProperty("UUID", PropertyType.STRING, 255, true);
            ACTION= addProperty("ACTION", PropertyType.STRING, 255, true);
            
            RIGHT_CRITERIUM= addReference(rightCriterium, "RIGHTS_CRIT");
            USER= addReference(user, "user");
            GROUP= addReference(group, "group");

            FIXED_PRIORITY = addProperty("FIXED_PRIORITY", PropertyType.INTEGER,0.0,false);

            addIndex("UUID_IDX", false, UUID);
            addIndex("ACTION_IDX", false, ACTION);
        }
	}
	

	public class RightCriteriumEntity extends Entity {
		
		public final Property QNAME;
		public final Property TYPE;
		public Reference PARAM;
		
		
        public RightCriteriumEntity() {
            super("Rights_criterium_table", "RIGHTS_CRITERIUM_ENTITY", "CRIT_ID", Structure.this);
            TYPE = addProperty("FIXED_PRIORITY", PropertyType.INTEGER,0.0,false);

    		QNAME= addProperty("QNAME", PropertyType.STRING, 255, true);
            QNAME.setListValues(
            		new ListItem("cz.incad.kramerius.security.impl.criteria.MovingWall", "cz.incad.kramerius.security.impl.criteria.MovingWall"),
            		new ListItem("cz.incad.kramerius.security.impl.criteria.DefaultIPAddressFilter","cz.incad.kramerius.security.impl.criteria.DefaultIPAddressFilter"));

            PARAM = addReference(criteriumParam, "citeriumParam");
        }
	}
	
	
	public class RightCriteriumParamEntity extends Entity {
		
		public final Property VALS;
		public final Property LONG_DESC;
		public final Property SHORT_DESC;
		
        public RightCriteriumParamEntity() {
            super("Criterium_param_table", "CRITERIUM_PARAM_ENTITY", "CRIT_PARAM_ID", Structure.this);

            VALS= addProperty("VALS", PropertyType.STRING,1024,  true);
            LONG_DESC= addProperty("LONG_DESC", PropertyType.STRING,1024,  true);
            SHORT_DESC= addProperty("SHORT_DESC", PropertyType.STRING,256,  true);
        }
	}
	

    public final UserEntity user = new UserEntity();
    public final GroupEntity group = new GroupEntity();
    public final GroupUserAssoction groupUserAssoction = new GroupUserAssoction();
    
    public final RightCriteriumParamEntity criteriumParam = new RightCriteriumParamEntity();
    public final RightCriteriumEntity rightCriterium = new RightCriteriumEntity();
    public final RightsEntity rights = new RightsEntity();
    
    public Structure() {

//    	group.USERS = groupUserAssoction.addReverseCollection("USERS", groupUserAssoction, groupUserAssoction.USERS);
//    	user.GROUPS = groupUserAssoction.addReverseCollection("GROUPS", groupUserAssoction, groupUserAssoction.GROUP);
    }
}
