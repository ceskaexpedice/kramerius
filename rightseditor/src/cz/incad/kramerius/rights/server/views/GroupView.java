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
        setLocalizationKey(Structure.group.getId());
        addProperty(Structure.group.GNAME);
        setSortProperty(Structure.group.GNAME);
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
        form.setLayout(column().add(new TextField<String>(Structure.group.GNAME)).add(new TextArea(Structure.group.DESCRIPTION).setWidth("100%")).add(new RepeatedForm(Structure.group.USER_ASSOCIATIONS, new GroupUsersView()))

        );
        form.addProperty(Structure.group.PERSONAL_ADMIN);
        return form;

    }

    private Form createAdminGroupForm() {
        Form form = new Form();
        form.setLayout(column().add(new TextField<String>(Structure.group.GNAME)).add(new TextArea(Structure.group.DESCRIPTION).setWidth("100%")).add(ReferenceField.reference(Structure.group.PERSONAL_ADMIN, this.reference, row().add(new TextField<String>(Structure.group.PERSONAL_ADMIN.relate(Structure.group.GNAME)))))
                .add(new RepeatedForm(Structure.group.USER_ASSOCIATIONS, new GroupUsersView()))

        );
        return form;
    }

    /** uzivatele skupiny */
    public class GroupUsersView extends View {

        public GroupUsersView() {
            super(Structure.groupUserAssoction);
            setLocalizationKey(Structure.user.getLocalizationKey());

            // addProperty(structure.groupUserAssoction.GROUP);
            addProperty(Structure.groupUserAssoction.USERS.relate(Structure.user.LOGINNAME));
            setForm(createForm());
            // setQueryGenerator(new FormUsersGenerator());
        }

        Form createForm() {
            Form form = new Form();
            form.setLayout(column().add(
                    ReferenceField.reference(Structure.groupUserAssoction.USERS, new RefUserView(),
                            column().add(new TextField<String>(Structure.groupUserAssoction.USERS.relate(Structure.user.LOGINNAME))).add(new TextField<String>(Structure.groupUserAssoction.USERS.relate(Structure.user.NAME))).add(new TextField<String>(Structure.groupUserAssoction.USERS.relate(Structure.user.SURNAME))))));
            return form;
        }

    }

    public class RefUserView extends View {
        public RefUserView() {
            super(Structure.user);
            setLocalizationKey(Structure.user.getId());
            addProperty(Structure.user.LOGINNAME);
            setSortProperty(Structure.user.LOGINNAME);
            setForm(createUserForm());
            setQueryGenerator(new RefUsersGenerator());
        }

        private Form createUserForm() {
            Form form = new Form();
            form.setLayout(column().add(column().add(new TextField<String>(Structure.user.NAME)).add(new TextField<String>(Structure.user.SURNAME)))

            .add(column().add(new TextField<String>(Structure.user.LOGINNAME)).add(new TextField<String>(Structure.user.PASSWORD)))

            .add(ReferenceField.reference(Structure.user.PERSONAL_ADMIN, reference, row().add(new TextField<String>(Structure.user.PERSONAL_ADMIN.relate(Structure.group.GNAME)))))

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
                    if (admId == null || admId.isEmpty()){
                        return null;
                    }
                    return new QueryCompareExpression<Integer>(Structure.user.PERSONAL_ADMIN, QueryCompareOperator.IS, admId.get(0));
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
                if (admId == null || admId.isEmpty()){
                    return null;
                }
                return new QueryCompareExpression<Integer>(Structure.group.PERSONAL_ADMIN, QueryCompareOperator.IS, admId.get(0));
            } else
                return null;
        }
    }

}
