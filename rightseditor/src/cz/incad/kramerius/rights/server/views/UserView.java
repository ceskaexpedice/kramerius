package cz.incad.kramerius.rights.server.views;

import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;
import static org.aplikator.server.descriptor.ReferenceField.reference;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.aplikator.client.data.ListItem;
import org.aplikator.client.descriptor.QueryParameter;
import org.aplikator.server.Context;
import org.aplikator.server.descriptor.ComboBox;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.Function;
import org.aplikator.server.descriptor.ListProvider;
import org.aplikator.server.descriptor.QueryGenerator;
import org.aplikator.server.descriptor.RepeatedForm;
import org.aplikator.server.descriptor.TextArea;
import org.aplikator.server.descriptor.TextField;
import org.aplikator.server.descriptor.View;
import org.aplikator.server.query.QueryCompareExpression;
import org.aplikator.server.query.QueryCompareOperator;
import org.aplikator.server.query.QueryExpression;

import cz.incad.kramerius.rights.server.GeneratePasswordExec;
import cz.incad.kramerius.rights.server.Mailer;
import cz.incad.kramerius.rights.server.Structure;
import cz.incad.kramerius.rights.server.impl.PropertiesMailer;
import cz.incad.kramerius.rights.server.utils.GetAdminGroupIds;
import cz.incad.kramerius.rights.server.utils.GetCurrentLoggedUser;
import cz.incad.kramerius.rights.server.views.triggers.UserTriggers;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.utils.SecurityDBUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;

public class UserView extends View {

    RefenrenceToPersonalAdminView referenceToAdmin;
    Function vygenerovatHeslo;
    Form createdForm;
    UserGroupsView userGroupsView;
    RefGroupView refGroupView;
    PropertiesMailer propertiesMailer =  new PropertiesMailer();

    public UserView() {
        super(Structure.user);
        this.trigger = new UserTriggers();
        this.referenceToAdmin = new RefenrenceToPersonalAdminView();
        GeneratePasswordExec generatePasswordForPrivate = new GeneratePasswordExec();
        generatePasswordForPrivate.setArrangement(this);
        generatePasswordForPrivate.setMailer(propertiesMailer);
        setMailer(propertiesMailer);
        this.vygenerovatHeslo = new Function("generatePasswordForPrivate","VygenerovatHeslo", generatePasswordForPrivate);

        addProperty(Structure.user.LOGINNAME).addProperty(Structure.user.NAME).addProperty(Structure.user.SURNAME);//.addProperty(Structure.user.PERSONAL_ADMIN.relate(Structure.group.GNAME));
        setSortProperty(Structure.user.LOGINNAME);
        setQueryGenerator(new UserQueryGenerator());
         //setForm(createUserFormForSuperAdmin(vygenerovatHeslo));
        refGroupView = new RefGroupView();
        userGroupsView = new UserGroupsView();


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
        form.setLayout(column(
                row(new TextField<String>(Structure.user.LOGINNAME), new TextField<String>(Structure.user.EMAIL)),
                row(new TextField<String>(Structure.user.NAME),new TextField<String>(Structure.user.SURNAME)),
                new TextField<String>(Structure.user.ORGANISATION),
                vygenerovatHeslo,
                new RepeatedForm(Structure.user.GROUP_ASSOCIATIONS, userGroupsView)
        ));
        //form.addProperty(Structure.user.PERSONAL_ADMIN);
        //form.addProperty(Structure.user.PASSWORD);
        return form;
    }

    private Form createUserFormForSuperAdmin(Function vygenerovatHeslo) {
        Form form = new Form();
        form.setLayout(column(

                row(new TextField<String>(Structure.user.LOGINNAME), new TextField<String>(Structure.user.EMAIL)),
                row(new TextField<String>(Structure.user.NAME),new TextField<String>(Structure.user.SURNAME)),
                new TextField<String>(Structure.user.ORGANISATION),
                vygenerovatHeslo,
                new RepeatedForm(Structure.user.GROUP_ASSOCIATIONS, userGroupsView)
                //new TextField<String>(Structure.user.PASSWORD),
                //.addChild(new RefButton(struct.user.PERSONAL_ADMIN, this.referenceToAdmin, new HorizontalPanel().addChild(new TextField<String>(struct.user.PERSONAL_ADMIN.relate(struct.group.GNAME)))))
        ));
        //form.addProperty(Structure.user.PASSWORD);
        return form;
    }

    public class UserGroupsView extends View {

        public UserGroupsView() {
            super(Structure.groupUserAssoction);
            setLocalizationKey(Structure.group.getLocalizationKey());
            addProperty(Structure.groupUserAssoction.GROUP.relate(Structure.group.GNAME));
            setForm(createForm());
        }

        Form createForm() {
            Structure.groupUserAssoction.GROUP.setListProvider(getGroupList());
            Form form = new Form();
            //form.setLayout(column().add(reference(Structure.groupUserAssoction.GROUP, refGroupView, row().add(new LabelField<String>(Structure.groupUserAssoction.GROUP.relate(Structure.group.GNAME))))));
            form.setLayout(column().add(new ComboBox<Integer>(Structure.groupUserAssoction.GROUP)));
            return form;
        }

    }

    public class RefGroupView extends View {
        public RefGroupView() {
            super(Structure.group);
            addProperty(Structure.group.GNAME);
            //addProperty(Structure.group.PERSONAL_ADMIN.relate(Structure.group.GNAME));
            setSortProperty(Structure.group.GNAME);
            setForm(createGroupForm());
            setQueryGenerator(new FormGroupGenerator());
        }

        private Form createGroupForm() {
            Form form = new Form();
            form.setLayout(column().add(new TextField<String>(Structure.group.GNAME)).add(new TextArea(Structure.group.DESCRIPTION).setWidth("100%"))
                    .add(reference(Structure.group.PERSONAL_ADMIN, referenceToAdmin, row().add(new TextField<String>(Structure.group.PERSONAL_ADMIN.relate(Structure.group.GNAME)))))

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
                    return new QueryCompareExpression<Integer>(Structure.group.PERSONAL_ADMIN, QueryCompareOperator.IS, admId.get(0));
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
                return new QueryCompareExpression<Integer>(Structure.user.PERSONAL_ADMIN, QueryCompareOperator.IS, admId.get(0));
            } else
                return null;
        }
    }


    public static ListProvider<Integer> getGroupList() {
        String query = "select group_id,gname from group_entity";

        List<ListItem<Integer>> groupsList = new JDBCQueryTemplate<ListItem<Integer>>(SecurityDBUtils.getConnection()) {
            @Override
            public boolean handleRow(ResultSet rs, List<ListItem<Integer>> retList) throws SQLException {
                int groupId = rs.getInt("group_id");
                String groupName = rs.getString("gname");
                retList.add(new ListItem.Default<Integer>(groupId, groupName));
                return true;
            }

        }.executeQuery(query);

        return new ListProvider.Default<Integer>(groupsList);
    }
}


