package cz.incad.kramerius.rights.server.views;

import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;
import static org.aplikator.server.descriptor.ReferenceField.reference;

import java.util.List;

import org.aplikator.client.descriptor.QueryParameter;
import org.aplikator.server.Context;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.Function;
import org.aplikator.server.descriptor.QueryGenerator;
import org.aplikator.server.descriptor.RepeatedForm;
import org.aplikator.server.descriptor.TextArea;
import org.aplikator.server.descriptor.TextField;
import org.aplikator.server.descriptor.View;
import org.aplikator.server.query.QueryCompareExpression;
import org.aplikator.server.query.QueryCompareOperator;
import org.aplikator.server.query.QueryExpression;

import cz.incad.kramerius.rights.server.Mailer;
import cz.incad.kramerius.rights.server.Structure;
import cz.incad.kramerius.rights.server.Structure.UserEntity;
import cz.incad.kramerius.rights.server.utils.GetAdminGroupIds;
import cz.incad.kramerius.rights.server.utils.GetCurrentLoggedUser;
import cz.incad.kramerius.rights.server.views.triggers.UserTriggers;
import cz.incad.kramerius.security.User;

public class UserView extends View {

    Structure struct;
    Structure.UserEntity userEntity;
    RefenrenceToPersonalAdminView referenceToAdmin;
    Function vygenerovatHeslo;
    Form createdForm;

    public UserView(Structure struct, UserEntity entity, RefenrenceToPersonalAdminView reference, Function vygenerovatHeslo) {
        super(entity);
        this.struct = struct;
        this.userEntity = entity;
        this.referenceToAdmin = reference;
        this.vygenerovatHeslo = vygenerovatHeslo;

        addProperty(struct.user.LOGINNAME).addProperty(struct.user.NAME).addProperty(struct.user.SURNAME).addProperty(struct.user.PERSONAL_ADMIN.relate(struct.group.GNAME));
        setSortProperty(struct.user.LOGINNAME);
        setQueryGenerator(new UserQueryGenerator());
         //setForm(createUserFormForSuperAdmin(vygenerovatHeslo));
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
        } else {
            form = createUserFormForSubadmin(vygenerovatHeslo);
        }
        return form;
    }

    private Form createUserFormForSubadmin(Function vygenerovatHeslo) {
        Form form = new Form();
        form.setLayout(column().add(column().add(new TextField<String>(struct.user.NAME)).add(new TextField<String>(struct.user.SURNAME)))

        .add(column().add(new TextField<String>(struct.user.LOGINNAME)).add(vygenerovatHeslo).add(new TextField<String>(struct.user.EMAIL)).add(new TextField<String>(struct.user.ORGANISATION)))

        .add(new RepeatedForm(struct.user.GROUP_ASSOCIATIONS, new UserGroupsView()))

        );
        form.addProperty(struct.user.PERSONAL_ADMIN);
        form.addProperty(struct.user.PASSWORD);
        return form;
    }

    private Form createUserFormForSuperAdmin(Function vygenerovatHeslo) {
        Form form = new Form();
        form.setLayout(column().add(column().add(new TextField<String>(struct.user.NAME)).add(new TextField<String>(struct.user.SURNAME)))

        .add(column().add(new TextField<String>(struct.user.LOGINNAME)).add(vygenerovatHeslo)
        .add(new TextField<String>(struct.user.PASSWORD))
        .add(new TextField<String>(struct.user.EMAIL))
        .add(new TextField<String>(struct.user.ORGANISATION)))

        //.addChild(new RefButton(struct.user.PERSONAL_ADMIN, this.referenceToAdmin, new HorizontalPanel().addChild(new TextField<String>(struct.user.PERSONAL_ADMIN.relate(struct.group.GNAME)))))
        .add(new RepeatedForm(struct.user.GROUP_ASSOCIATIONS, new UserGroupsView()))

        );
        form.addProperty(struct.user.PASSWORD);
        return form;
    }

    public class UserGroupsView extends View {

        public UserGroupsView() {
            super(struct.groupUserAssoction);
            setLocalizationKey(struct.group.getLocalizationKey());
            addProperty(struct.groupUserAssoction.GROUP.relate(struct.group.GNAME));
            setForm(createForm());
        }

        Form createForm() {
            Form form = new Form();
            form.setLayout(column().add(reference(struct.groupUserAssoction.GROUP, new RefGroupView(), row().add(new TextField<String>(struct.groupUserAssoction.GROUP.relate(struct.group.GNAME))))));
            return form;
        }

    }

    public class RefGroupView extends View {
        public RefGroupView() {
            super(struct.group);
            addProperty(struct.group.GNAME);
            addProperty(struct.group.PERSONAL_ADMIN.relate(struct.group.GNAME));
            setSortProperty(struct.group.GNAME);
            setForm(createGroupForm());
            setQueryGenerator(new FormGroupGenerator());
        }

        private Form createGroupForm() {
            Form form = new Form();
            form.setLayout(column().add(new TextField<String>(struct.group.GNAME)).add(new TextArea(struct.group.DESCRIPTION).setWidth("100%"))
                    .add(reference(struct.group.PERSONAL_ADMIN, referenceToAdmin, row().add(new TextField<String>(struct.group.PERSONAL_ADMIN.relate(struct.group.GNAME)))))

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
