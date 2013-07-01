package cz.incad.kramerius.rights.server.views;

import cz.incad.kramerius.rights.server.Structure;
import cz.incad.kramerius.rights.server.Structure.GroupEntity;
import cz.incad.kramerius.rights.server.utils.GetAdminGroupIds;
import cz.incad.kramerius.rights.server.utils.GetCurrentLoggedUser;
import cz.incad.kramerius.rights.server.views.triggers.GroupTriggers;
import cz.incad.kramerius.security.User;
import org.aplikator.client.shared.descriptor.QueryParameter;
import org.aplikator.server.Context;
import org.aplikator.server.descriptor.*;
import org.aplikator.server.query.QueryCompareExpression;
import org.aplikator.server.query.QueryCompareOperator;
import org.aplikator.server.query.QueryExpression;

import java.util.List;

import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;

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
        setDefaultSortProperty(Structure.group.GNAME);
        // setForm(createGroupForm());
        addQueryDescriptor(new QueryDescriptor("default", "default") {
            @Override
            public QueryExpression getQueryExpression(List<QueryParameter> queryParameters, Context ctx) {
                User user = GetCurrentLoggedUser.getCurrentLoggedUser(ctx.getHttpServletRequest());
                if (!user.hasSuperAdministratorRole()) {
                    List<Integer> admId = GetAdminGroupIds.getAdminGroupId(ctx);
                    if (admId == null || admId.isEmpty()) {
                        return null;
                    }
                    return new QueryCompareExpression<Integer>(Structure.group.PERSONAL_ADMIN, QueryCompareOperator.IS, admId.get(0));
                } else
                    return null;
            }
        }
        );
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
        Form form = new Form(true);
        form.setLayout(column().add(new TextField<String>(Structure.group.GNAME)).add(new TextArea(Structure.group.DESCRIPTION)).add(new RepeatedForm(Structure.group.USER_ASSOCIATIONS, new GroupUsersView()))

        );
        form.addProperty(Structure.group.PERSONAL_ADMIN);
        return form;

    }

    private Form createAdminGroupForm() {
        Form form = new Form(true);
        form.setLayout(column().add(new TextField<String>(Structure.group.GNAME)).add(new TextArea(Structure.group.DESCRIPTION)).add(ReferenceField.reference(Structure.group.PERSONAL_ADMIN, this.reference, row().add(new TextField<String>(Structure.group.PERSONAL_ADMIN.relate(Structure.group.GNAME)))))
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
            Form form = new Form(true);
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
            setDefaultSortProperty(Structure.user.LOGINNAME);
            setForm(createUserForm());
            addQueryDescriptor(new QueryDescriptor("default", "default"){
                @Override
                public QueryExpression getQueryExpression(List<QueryParameter> queryParameters, Context ctx) {
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
            );
        }

        private Form createUserForm() {
            Form form = new Form(true);
            form.setLayout(column().add(column().add(new TextField<String>(Structure.user.NAME)).add(new TextField<String>(Structure.user.SURNAME)))

            .add(column().add(new TextField<String>(Structure.user.LOGINNAME)).add(new TextField<String>(Structure.user.PASSWORD)))

            .add(ReferenceField.reference(Structure.user.PERSONAL_ADMIN, reference, row().add(new TextField<String>(Structure.user.PERSONAL_ADMIN.relate(Structure.group.GNAME)))))

            );
            return form;
        }



    }



}
