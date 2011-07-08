package cz.incad.kramerius.rights.server.arragements;

import java.util.List;

import org.aplikator.client.descriptor.QueryParameter;
import org.aplikator.server.Context;
import org.aplikator.server.descriptor.Arrangement;
import org.aplikator.server.descriptor.Form;
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

import cz.incad.kramerius.rights.server.Structure;
import cz.incad.kramerius.rights.server.Structure.GroupEntity;
import cz.incad.kramerius.rights.server.arragements.triggers.GroupTriggers;
import cz.incad.kramerius.rights.server.utils.GetAdminGroupIds;
import cz.incad.kramerius.rights.server.utils.GetCurrentLoggedUser;
import cz.incad.kramerius.security.User;

public class GroupArrangement extends Arrangement {

    Structure struct;
    Structure.GroupEntity groupEntity;
    RefenrenceToPersonalAdminArrangement reference;
    Form form;

    public GroupArrangement(Structure structure, GroupEntity entity, RefenrenceToPersonalAdminArrangement reference) {
        super(entity);
        this.struct = structure;
        this.groupEntity = entity;
        this.reference = reference;
        setReadableName(struct.group.getName());
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
        form.setLayout(new VerticalPanel().addChild(new TextField<String>(struct.group.GNAME)).addChild(new TextArea(struct.group.DESCRIPTION).setWidth("100%")).addChild(new RepeatedForm(struct.group.USER_ASSOCIATIONS, new GroupUsersArrangement()))

        );
        form.addProperty(struct.group.PERSONAL_ADMIN);
        return form;

    }

    private Form createAdminGroupForm() {
        Form form = new Form();
        form.setLayout(new VerticalPanel().addChild(new TextField<String>(struct.group.GNAME)).addChild(new TextArea(struct.group.DESCRIPTION).setWidth("100%"))
                .addChild(new RefButton(struct.group.PERSONAL_ADMIN, this.reference, new HorizontalPanel().addChild(new TextField<String>(struct.group.PERSONAL_ADMIN.relate(struct.group.GNAME))))).addChild(new RepeatedForm(struct.group.USER_ASSOCIATIONS, new GroupUsersArrangement()))

        );
        return form;
    }

    /** uzivatele skupiny */
    public class GroupUsersArrangement extends Arrangement {

        public GroupUsersArrangement() {
            super(struct.groupUserAssoction);
            setReadableName(struct.user.getReadableName());

            // addProperty(structure.groupUserAssoction.GROUP);
            addProperty(struct.groupUserAssoction.USERS.relate(struct.user.LOGINNAME));
            setForm(createForm());
            // setQueryGenerator(new FormUsersGenerator());
        }

        Form createForm() {
            Form form = new Form();
            form.setLayout(new VerticalPanel().addChild(new RefButton(struct.groupUserAssoction.USERS, new RefUserArrangement(), new VerticalPanel().addChild(new TextField<String>(struct.groupUserAssoction.USERS.relate(struct.user.LOGINNAME))).addChild(new TextField<String>(struct.groupUserAssoction.USERS.relate(struct.user.NAME)))
                    .addChild(new TextField<String>(struct.groupUserAssoction.USERS.relate(struct.user.SURNAME))))));
            return form;
        }

    }

    public class RefUserArrangement extends Arrangement {
        public RefUserArrangement() {
            super(struct.user);
            setReadableName(struct.user.getName());
            addProperty(struct.user.LOGINNAME);
            setSortProperty(struct.user.LOGINNAME);
            setForm(createUserForm());
            setQueryGenerator(new RefUsersGenerator());
        }

        private Form createUserForm() {
            Form form = new Form();
            form.setLayout(new VerticalPanel().addChild(new VerticalPanel().addChild(new TextField<String>(struct.user.NAME)).addChild(new TextField<String>(struct.user.SURNAME)))

            .addChild(new VerticalPanel().addChild(new TextField<String>(struct.user.LOGINNAME)).addChild(new TextField<String>(struct.user.PASSWORD)))

            .addChild(new RefButton(struct.user.PERSONAL_ADMIN, reference, new HorizontalPanel().addChild(new TextField<String>(struct.user.PERSONAL_ADMIN.relate(struct.group.GNAME)))))

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
