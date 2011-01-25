package cz.incad.kramerius.rights.server;


import org.aplikator.client.data.ListItem;
import org.aplikator.client.descriptor.PropertyType;
import org.aplikator.server.descriptor.Application;
import org.aplikator.server.descriptor.Collection;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.Reference;

/**
 * Struktura databaze
 * @author pavels
 */
public class Structure extends Application {
	
	/**
	 * Entita uzivatel
	 * @author pavels
	 */
	public class UserEntity extends Entity {
		// vlastnosti uzivatele
		public final Property NAME;
        public final Property SURNAME;
        public final Property LOGINNAME;
        public final Property PASSWORD;

        public final Property EMAIL;
        public final Property ORGANISATION;
        
        // administrator uzivatele
		public  Reference PERSONAL_ADMIN;
		
		public Collection GROUP_ASSOCIATIONS;

        
        public UserEntity() {
            super("Users_table", "USER_ENTITY", "USER_ID", Structure.this);
            NAME= addProperty("NAME", PropertyType.STRING, 255, true);
            SURNAME = addProperty("SURNAME", PropertyType.STRING, 255, true);
            LOGINNAME= addProperty("LOGINNAME", PropertyType.STRING, 255, true);
            PASSWORD= addProperty("PSWD", PropertyType.STRING, 255, false);

            EMAIL= addProperty("EMAIL", PropertyType.STRING, 255, false);
            ORGANISATION= addProperty("ORGANISATION", PropertyType.STRING, 255, false);

            addIndex("UNAME_IDX", false, NAME);
            addIndex("SURNAME_IDX", false, SURNAME);
            addIndex("LOGINNAME_IDX", false, LOGINNAME);
            addIndex("PASSWORD_IDX", false, PASSWORD);
        }
	}
	

	/**
	 * Skupina 
	 * @author pavels
	 */
	public class GroupEntity extends Entity {
		
		// vlastnosti skupiny
		public final Property GNAME;
		public final Property DESCRIPTION;
		
		//admin skupiny
		public  Reference PERSONAL_ADMIN;
		
		public Collection USER_ASSOCIATIONS;


        public GroupEntity() {
            super("Groups_table", "GROUP_ENTITY", "GROUP_ID", Structure.this);
            GNAME= addProperty("GNAME", PropertyType.STRING, 255, true);
            DESCRIPTION=addProperty("DESCRIPTION", PropertyType.STRING, 1024, false);
            addIndex("GNAME_IDX", true, GNAME);
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
            USER= addReference(user, "USER_ID");
            GROUP= addReference(group, "GROUP_ID");

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
            TYPE = addProperty("TYPE", PropertyType.INTEGER,0.0,false);

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

    	group.USER_ASSOCIATIONS = group.addReverseCollection("USERS", groupUserAssoction, groupUserAssoction.GROUP);
    	user.GROUP_ASSOCIATIONS = user.addReverseCollection("GROUPS", groupUserAssoction, groupUserAssoction.USERS);

        user.PERSONAL_ADMIN=user.addReference(group, "PERSONAL_ADMIN_ID");
        group.PERSONAL_ADMIN=group.addReference(group, "PERSONAL_ADMIN_ID");
        
        
//        digitalniReprezentace.ZVEREJNENO = digitalniReprezentace.addReverseCollection("DIGITALNI_REPREZENTACE", zverejneno, zverejneno.DIGITALNI_REPREZENTACE);

    }
}
