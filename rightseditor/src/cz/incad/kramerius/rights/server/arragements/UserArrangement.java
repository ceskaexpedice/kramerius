package cz.incad.kramerius.rights.server.arragements;

import org.apache.empire.db.expr.compare.DBCompareExpr;
import org.aplikator.client.descriptor.QueryParameter;
import org.aplikator.server.Context;
import org.aplikator.server.descriptor.Arrangement;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.Function;
import org.aplikator.server.descriptor.HorizontalPanel;
import org.aplikator.server.descriptor.QueryGenerator;
import org.aplikator.server.descriptor.RefButton;
import org.aplikator.server.descriptor.RepeatedForm;
import org.aplikator.server.descriptor.TextArea;
import org.aplikator.server.descriptor.TextField;
import org.aplikator.server.descriptor.VerticalPanel;

import cz.incad.kramerius.rights.server.Structure;
import cz.incad.kramerius.rights.server.Structure.UserEntity;
import cz.incad.kramerius.rights.server.arragements.triggers.UserTriggers;

public class UserArrangement extends Arrangement {

	Structure struct;
	Structure.UserEntity userEntity;
	RefenrenceToPersonalAdminArrangement referenceToAdmin;

	public UserArrangement(Structure struct, UserEntity entity, RefenrenceToPersonalAdminArrangement reference, Function vygenerovatHeslo) {
		super(entity);
		this.struct = struct;
		this.userEntity = entity;
		this.referenceToAdmin = reference;
		
		addProperty(struct.user.LOGINNAME).addProperty(struct.user.NAME).addProperty(struct.user.SURNAME).addProperty(struct.user.PERSONAL_ADMIN.relate(struct.group.GNAME));
		setSortProperty(struct.user.LOGINNAME);
		setQueryGenerator(new UserQueryGenerator());
		setForm(createUserForm(vygenerovatHeslo));
		this.trigger = new UserTriggers(this.struct);
	}

	private Form createUserForm(Function vygenerovatHeslo) {
		Form form = new Form();
		form.setLayout(new VerticalPanel()
				.addChild(
						new VerticalPanel().addChild(
								new TextField(struct.user.NAME)).addChild(
								new TextField(struct.user.SURNAME)))

				.addChild(
						new VerticalPanel()
						    .addChild(new TextField(struct.user.LOGINNAME))
						    .addChild(new TextField(struct.user.PASSWORD))
						    .addChild(vygenerovatHeslo)
						    .addChild(new TextField(struct.user.EMAIL))
						    .addChild(new TextField(struct.user.ORGANISATION))
						)


				.addChild(
						new RefButton(struct.user.PERSONAL_ADMIN,
								this.referenceToAdmin,
								new HorizontalPanel().addChild(new TextField(
										struct.user.PERSONAL_ADMIN
												.relate(struct.group.GNAME)))))
				.addChild(new RepeatedForm(struct.user.GROUP_ASSOCIATIONS, new UserGroupsArrangement()))

		);
		return form;
	}
	
	public class UserGroupsArrangement extends Arrangement{
	    
	    public UserGroupsArrangement(){
	        super(struct.groupUserAssoction);
	        setReadableName(struct.group.getReadableName());
	        addProperty(struct.groupUserAssoction.GROUP.relate(struct.group.GNAME));
	        setForm(createForm());
	    }
	    
	    Form createForm() {
	        Form form = new Form();
	        form.setLayout(new VerticalPanel().addChild(
	                        new RefButton(struct.groupUserAssoction.GROUP,
	                                new RefGroupArrangement(),
	                                new HorizontalPanel().addChild(new TextField(
	                                        struct.groupUserAssoction.GROUP
	                                                .relate(struct.group.GNAME))))));
	        return form;
	    }
	    
	}
	
	public class RefGroupArrangement extends Arrangement{
    	public RefGroupArrangement() {
            super(struct.group);
            addProperty(struct.group.GNAME);
            setSortProperty(struct.group.GNAME);
            setForm(createGroupForm());
        }
        
        
        private Form createGroupForm() {
            Form form = new Form();
            form.setLayout(new VerticalPanel()
                        .addChild(new TextField(struct.group.GNAME))
                        .addChild(new TextArea(struct.group.DESCRIPTION).setWidth("100%"))
                        .addChild(
                                new RefButton(struct.group.PERSONAL_ADMIN,
                                        referenceToAdmin,
                                        new HorizontalPanel().addChild(new TextField(
                                                struct.group.PERSONAL_ADMIN
                                                        .relate(struct.group.GNAME)))))
    
            );
            return form;
        }
        
	}
	
	 public class UserQueryGenerator implements QueryGenerator {
	        
	        public QueryParameter[] getQueryParameters(Context ctx){
	            return new QueryParameter[]{};
	        }
	        
	        public DBCompareExpr createWhere(QueryParameter[] queryParameters, Context ctx) {
	            if (ctx.getHttpServletRequest().getUserPrincipal()!=null && ctx.getHttpServletRequest().getUserPrincipal().getName().equalsIgnoreCase("podadmin")){
	                return struct.group.GNAME.column.is("podspravci");
	            }
	            return null;
	        }
	    }
}
