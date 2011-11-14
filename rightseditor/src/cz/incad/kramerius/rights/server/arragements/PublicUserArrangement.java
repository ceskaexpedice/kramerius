package cz.incad.kramerius.rights.server.arragements;

import java.util.List;

import org.aplikator.client.descriptor.QueryParameter;
import org.aplikator.server.Context;
import org.aplikator.server.descriptor.Arrangement;
import org.aplikator.server.descriptor.CheckBox;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.Function;
import org.aplikator.server.descriptor.HorizontalPanel;
import org.aplikator.server.descriptor.QueryGenerator;
import org.aplikator.server.descriptor.RefButton;
import org.aplikator.server.descriptor.RepeatedForm;
import org.aplikator.server.descriptor.TextArea;
import org.aplikator.server.descriptor.TextField;
import org.aplikator.server.descriptor.VerticalPanel;
import org.aplikator.server.query.QueryCompareExpression;
import org.aplikator.server.query.QueryCompareOperator;
import org.aplikator.server.query.QueryExpression;

import cz.incad.kramerius.rights.server.Mailer;
import cz.incad.kramerius.rights.server.Structure;
import cz.incad.kramerius.rights.server.Structure.PublicUserEntity;
import cz.incad.kramerius.rights.server.arragements.triggers.UserTriggers;
import cz.incad.kramerius.rights.server.utils.GetAdminGroupIds;
import cz.incad.kramerius.rights.server.utils.GetCurrentLoggedUser;
import cz.incad.kramerius.security.User;

public class PublicUserArrangement extends Arrangement {

    Structure struct;
    Structure.PublicUserEntity publicUserEntity;
    RefenrenceToPersonalAdminArrangement referenceToAdmin;
    Function vygenerovatHeslo;
    Form createdForm;

    public PublicUserArrangement(Structure struct, PublicUserEntity entity, RefenrenceToPersonalAdminArrangement reference, Function vygenerovatHeslo) {
        super(entity);
        this.struct = struct;
        this.publicUserEntity = entity;
        this.referenceToAdmin = reference;
        this.vygenerovatHeslo = vygenerovatHeslo;

        addProperty(struct.user.LOGINNAME).addProperty(struct.user.NAME).addProperty(struct.user.SURNAME).addProperty(struct.user.PERSONAL_ADMIN.relate(struct.group.GNAME));
        setSortProperty(struct.user.LOGINNAME);
        setQueryGenerator(new UserQueryGenerator());
        // setForm(createUserFormFormSuperAdmin(vygenerovatHeslo));
        this.trigger = new UserTriggers(this.struct);

    }

    public Mailer getMailer() {
        return ((UserTriggers) this.trigger).getMailer();
    }

    public void setMailer(Mailer mailer) {
        ((UserTriggers) this.trigger).setMailer(mailer);
    }

    @Override
    public synchronized Form getForm(Context context) {
        Form form = null;
        User user = GetCurrentLoggedUser.getCurrentLoggedUser(context.getHttpServletRequest());
        if ((user != null) && (user.hasSuperAdministratorRole())) {
            form = createUserFormForSuperAdmin(vygenerovatHeslo);
        }
        return form;
    }


    private Form createUserFormForSuperAdmin(Function vygenerovatHeslo) {
        Form form = new Form();
        form.setLayout(new VerticalPanel().addChild(new VerticalPanel().addChild(new TextField<String>(struct.user.NAME)).addChild(new TextField<String>(struct.user.SURNAME)))

        .addChild(new VerticalPanel().addChild(new TextField<String>(struct.user.LOGINNAME))
		.addChild(new TextField<String>(struct.user.PASSWORD))
		.addChild(new TextField<String>(struct.user.EMAIL))
		.addChild(new TextField<String>(struct.user.ORGANISATION))
		.addChild(new CheckBox(struct.user.DEACTIVATED))
        )

        //.addChild(new RefButton(struct.user.PERSONAL_ADMIN, this.referenceToAdmin, new HorizontalPanel().addChild(new TextField<String>(struct.user.PERSONAL_ADMIN.relate(struct.group.GNAME)))))
        .addChild(new RepeatedForm(struct.user.GROUP_ASSOCIATIONS, new UserGroupsArrangement()))

        );
        form.addProperty(struct.user.PASSWORD);
        return form;
    }

    public class UserGroupsArrangement extends Arrangement {

        public UserGroupsArrangement() {
            super(struct.groupUserAssoction);
            setReadableName(struct.group.getReadableName());
            addProperty(struct.groupUserAssoction.GROUP.relate(struct.group.GNAME));
            setForm(createForm());
        }

        Form createForm() {
            Form form = new Form();
            form.setLayout(new VerticalPanel().addChild(new RefButton(struct.groupUserAssoction.GROUP, new RefGroupArrangement(), new HorizontalPanel().addChild(new TextField<String>(struct.groupUserAssoction.GROUP.relate(struct.group.GNAME))))));
            return form;
        }

    }

    public class RefGroupArrangement extends Arrangement {
        public RefGroupArrangement() {
            super(struct.group);
            addProperty(struct.group.GNAME);
            // TODO: problem with joins 
            //addProperty(struct.group.PERSONAL_ADMIN.relate(struct.group.GNAME));
            //setSortProperty(struct.group.GNAME);
            setForm(createGroupForm());
            setQueryGenerator(new FormGroupGenerator());
        }

        private Form createGroupForm() {
            Form form = new Form();
            form.setLayout(new VerticalPanel().addChild(new TextField<String>(struct.group.GNAME)).addChild(new TextArea(struct.group.DESCRIPTION).setWidth("100%"))
                    .addChild(new RefButton(struct.group.PERSONAL_ADMIN, referenceToAdmin, new HorizontalPanel().addChild(new TextField<String>(struct.group.PERSONAL_ADMIN.relate(struct.group.GNAME)))))

            );
            return form;
        }

        public class FormGroupGenerator implements QueryGenerator {

            @Override
            public QueryParameter[] getQueryParameters(Context ctx) {
                return new QueryParameter[] {};
            }

            @Override
            public QueryExpression createWhere(QueryParameter[] queryParameters, Context ctx) {
                User user = GetCurrentLoggedUser.getCurrentLoggedUser(ctx.getHttpServletRequest());
                if (!user.hasSuperAdministratorRole()) {
                    List<Integer> admId = GetAdminGroupIds.getAdminGroupId(ctx);
                    return new QueryCompareExpression<Integer>(struct.group.PERSONAL_ADMIN, QueryCompareOperator.IS, admId.get(0));
                } else {
                    return null;
                }

            }
        }
    }

    public class UserQueryGenerator implements QueryGenerator {

        public QueryParameter[] getQueryParameters(Context ctx) {
            return new QueryParameter[] {};
        }

        public QueryExpression createWhere(QueryParameter[] queryParameters, Context ctx) {
            User user = GetCurrentLoggedUser.getCurrentLoggedUser(ctx.getHttpServletRequest());
            if (!user.hasSuperAdministratorRole()) {
                List<Integer> admId = GetAdminGroupIds.getAdminGroupId(ctx);
                return new QueryCompareExpression<Integer>(struct.user.PERSONAL_ADMIN, QueryCompareOperator.IS, admId.get(0));
            } else
                return null;
        }
    }
}