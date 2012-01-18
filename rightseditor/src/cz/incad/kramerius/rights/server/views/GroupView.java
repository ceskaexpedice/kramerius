package cz.incad.kramerius.rights.server.views;

import static org.aplikator.server.descriptor.Panel.*;
import java.util.List;

import org.aplikator.client.descriptor.QueryParameter;
import org.aplikator.server.Context;
import org.aplikator.server.descriptor.View;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.QueryGenerator;
import org.aplikator.server.descriptor.ReferenceField;
import org.aplikator.server.descriptor.RepeatedForm;
import org.aplikator.server.descriptor.TextArea;
import org.aplikator.server.descriptor.TextField;
import org.aplikator.server.query.QueryCompareExpression;
import org.aplikator.server.query.QueryCompareOperator;
import org.aplikator.server.query.QueryExpression;

import cz.incad.kramerius.rights.server.Structure;
import cz.incad.kramerius.rights.server.Structure.GroupEntity;
import cz.incad.kramerius.rights.server.utils.GetAdminGroupIds;
import cz.incad.kramerius.rights.server.utils.GetCurrentLoggedUser;
import cz.incad.kramerius.rights.server.views.triggers.GroupTriggers;
import cz.incad.kramerius.security.User;

public class GroupView extends View {

    Structure struct;
    Structure.GroupEntity groupEntity;
    RefenrenceToPersonalAdminView reference;
    Form form;

    public GroupView(Structure structure, GroupEntity entity, RefenrenceToPersonalAdminView reference) {
        super(entity);
        this.struct = structure;
        this.groupEntity = entity;
        this.reference = reference;
        setLocalizationKey(struct.group.getId());
        addProperty(struct.group.GNAME);
        setSortProperty(struct.group.GNAME);
        // setForm(createGroupForm());
        setQueryGenerator(new GroupQueryGenerator());
        this.trigger = new GroupTriggers(this.struct);
    }

    @Override
    public synchronized Form getForm(Context context) {
        Form form = null;
        User user = GetCurrentLoggedUser.getCurrentLoggedUser(context.getHttpServletRequest());
        if ((user != null) && (user.hasSuperAdministratorRole())) {
            form = createAdminGroupForm();
        } else {
            form = createSubadminGroupForm();
        }
        return form;
    }

    private Form createSubadminGroupForm() {
        Form form = new Form();
        form.setLayout(column().add(new TextField<String>(struct.group.GNAME)).add(new TextArea(struct.group.DESCRIPTION).setWidth("100%")).add(new RepeatedForm(struct.group.USER_ASSOCIATIONS, new GroupUsersView()))

        );
        form.addProperty(struct.group.PERSONAL_ADMIN);
        return form;

    }

    private Form createAdminGroupForm() {
        Form form = new Form();
        form.setLayout(column().add(new TextField<String>(struct.group.GNAME)).add(new TextArea(struct.group.DESCRIPTION).setWidth("100%"))
                .add(ReferenceField.reference(struct.group.PERSONAL_ADMIN, this.reference, row().add(new TextField<String>(struct.group.PERSONAL_ADMIN.relate(struct.group.GNAME))))).add(new RepeatedForm(struct.group.USER_ASSOCIATIONS, new GroupUsersView()))

        );
        return form;
    }

    /** uzivatele skupiny */
    public class GroupUsersView extends View {

        public GroupUsersView() {
            super(struct.groupUserAssoction);
            setLocalizationKey(struct.user.getLocalizationKey());

            // addProperty(structure.groupUserAssoction.GROUP);
            addProperty(struct.groupUserAssoction.USERS.relate(struct.user.LOGINNAME));
            setForm(createForm());
            // setQueryGenerator(new FormUsersGenerator());
        }

        Form createForm() {
            Form form = new Form();
            form.setLayout(column().add(ReferenceField.reference(struct.groupUserAssoction.USERS, new RefUserView(), column().add(new TextField<String>(struct.groupUserAssoction.USERS.relate(struct.user.LOGINNAME))).add(new TextField<String>(struct.groupUserAssoction.USERS.relate(struct.user.NAME)))
                    .add(new TextField<String>(struct.groupUserAssoction.USERS.relate(struct.user.SURNAME))))));
            return form;
        }

    }

    public class RefUserView extends View {
        public RefUserView() {
            super(struct.user);
            setLocalizationKey(struct.user.getId());
            addProperty(struct.user.LOGINNAME);
            setSortProperty(struct.user.LOGINNAME);
            setForm(createUserForm());
            setQueryGenerator(new RefUsersGenerator());
        }

        private Form createUserForm() {
            Form form = new Form();
            form.setLayout(column().add(column().add(new TextField<String>(struct.user.NAME)).add(new TextField<String>(struct.user.SURNAME)))

            .add(column().add(new TextField<String>(struct.user.LOGINNAME)).add(new TextField<String>(struct.user.PASSWORD)))

            .add(ReferenceField.reference(struct.user.PERSONAL_ADMIN, reference, row().add(new TextField<String>(struct.user.PERSONAL_ADMIN.relate(struct.group.GNAME)))))

            );
            return form;
        }

        public class RefUsersGenerator implements QueryGenerator {

            @Override
            public QueryParameter[] getQueryParameters(Context ctx) {
                return new QueryParameter[] {};
            }

            @Override
            public QueryExpression createWhere(QueryParameter[] queryParameters, Context ctx) {
                User user = GetCurrentLoggedUser.getCurrentLoggedUser(ctx.getHttpServletRequest());
                if (!user.hasSuperAdministratorRole()) {
                    List<Integer> admId = GetAdminGroupIds.getAdminGroupId(ctx);
                    return new QueryCompareExpression<Integer>(struct.user.PERSONAL_ADMIN, QueryCompareOperator.IS, admId.get(0));
                } else {
                    return null;
                }

            }
        }

    }

    public class GroupQueryGenerator implements QueryGenerator {

        public QueryParameter[] getQueryParameters(Context ctx) {
            return new QueryParameter[] {};
        }

        public QueryExpression createWhere(QueryParameter[] queryParameters, Context ctx) {
            User user = GetCurrentLoggedUser.getCurrentLoggedUser(ctx.getHttpServletRequest());
            if (!user.hasSuperAdministratorRole()) {
                List<Integer> admId = GetAdminGroupIds.getAdminGroupId(ctx);
                return new QueryCompareExpression<Integer>(struct.group.PERSONAL_ADMIN, QueryCompareOperator.IS, admId.get(0));
            } else
                return null;
        }
    }

}
