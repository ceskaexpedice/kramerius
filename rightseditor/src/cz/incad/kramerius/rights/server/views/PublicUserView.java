package cz.incad.kramerius.rights.server.views;


import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;
import static org.aplikator.server.descriptor.ReferenceField.reference;

import java.util.List;

import org.aplikator.client.descriptor.QueryParameter;
import org.aplikator.server.Context;
import org.aplikator.server.descriptor.CheckBox;
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
import cz.incad.kramerius.rights.server.Structure.PublicUserEntity;
import cz.incad.kramerius.rights.server.utils.GetAdminGroupIds;
import cz.incad.kramerius.rights.server.utils.GetCurrentLoggedUser;
import cz.incad.kramerius.rights.server.views.triggers.UserTriggers;
import cz.incad.kramerius.security.User;

    public class PublicUserView extends View {

        Structure struct;
        Structure.PublicUserEntity publicUserEntity;
        RefenrenceToPersonalAdminView referenceToAdmin;
        Function vygenerovatHeslo;
        Form createdForm;

        public PublicUserView(Structure struct, PublicUserEntity entity, RefenrenceToPersonalAdminView reference, Function vygenerovatHeslo) {
            super(entity);
            this.struct = struct;
            this.publicUserEntity = entity;
            this.referenceToAdmin = reference;
            this.vygenerovatHeslo = vygenerovatHeslo;

            addProperty(Structure.user.LOGINNAME).addProperty(Structure.user.NAME).addProperty(Structure.user.SURNAME).addProperty(Structure.user.PERSONAL_ADMIN.relate(Structure.group.GNAME));
            setSortProperty(Structure.user.LOGINNAME);
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
            form.setLayout(column().add(column().add(new TextField<String>(Structure.user.NAME)).add(new TextField<String>(Structure.user.SURNAME)))

            .add(column().add(new TextField<String>(Structure.user.LOGINNAME))
            .add(new TextField<String>(Structure.user.PASSWORD))
            .add(new TextField<String>(Structure.user.EMAIL))
            .add(new TextField<String>(Structure.user.ORGANISATION))
            .add(new CheckBox(Structure.user.DEACTIVATED))
            )

            //.addChild(new RefButton(struct.user.PERSONAL_ADMIN, this.referenceToAdmin, new HorizontalPanel().addChild(new TextField<String>(struct.user.PERSONAL_ADMIN.relate(struct.group.GNAME)))))
            .add(new RepeatedForm(Structure.user.GROUP_ASSOCIATIONS, new UserGroupsView()))

            );
            form.addProperty(Structure.user.PASSWORD);
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
                Form form = new Form();
                form.setLayout(column().add(reference(Structure.groupUserAssoction.GROUP, new RefGroupView(), row().add(new TextField<String>(Structure.groupUserAssoction.GROUP.relate(Structure.group.GNAME))))));
                return form;
            }

        }

        public class RefGroupView extends View {
            public RefGroupView() {
                super(Structure.group);
                addProperty(Structure.group.GNAME);
                // TODO: problem with joins
                //addProperty(struct.group.PERSONAL_ADMIN.relate(struct.group.GNAME));
                //setSortProperty(struct.group.GNAME);
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
    }