package cz.incad.kramerius.rights.server.utils;

import java.security.Principal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.security.utils.SecurityDBUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;

public class GetCurrentLoggedUser {

    public static final String USER_SESSION_KEY = "rightsEditorSession";


    public static User getCurrentLoggedUser(HttpServletRequest request) {
        if (Boolean.getBoolean(DebugLoggedUser.DEBUG_KEY)) {
            return DebugLoggedUser.getCurrentLoggedUser();
        } else {

            HttpSession session = request.getSession(true);
            if (session.getAttribute(USER_SESSION_KEY) == null) {
                Principal principal = request.getUserPrincipal();
                if (principal != null) {


                    String loginName = principal.getName();
//                	K4UserPrincipal k4principal = (K4UserPrincipal) principal;
//                	User user = k4principal.getUser();

                    List<User> usersList = new JDBCQueryTemplate<User>(SecurityDBUtils.getConnection()) {

                        @Override
                        public boolean handleRow(ResultSet rs,
                                List<User> returnsList) throws SQLException {
                            returnsList.add(SecurityDBUtils.createUser(rs));
                            return true;
                        }

                    }.executeQuery("select * from user_entity where loginname=?", loginName);
                    User user = !usersList.isEmpty() ?  usersList.get(0) : null;

                    List<Role> rolesList = new JDBCQueryTemplate<Role>(SecurityDBUtils.getConnection()) {
                        @Override
                        public boolean handleRow(ResultSet rs, List<Role> retList) throws SQLException {
                            retList.add(SecurityDBUtils.createRole(rs));
                            return true;
                        }
                    }.executeQuery("select * from user_group_mapping where user_id=?", user.getId());


                    List<Role> cmnUsers = new JDBCQueryTemplate<Role>(SecurityDBUtils.getConnection()) {
                        @Override
                        public boolean handleRow(ResultSet rs, List<Role> retList) throws SQLException {
                            retList.add(SecurityDBUtils.createRole(rs));
                            return true;
                        }
                    }.executeQuery("select * from group_entity where gname='common_users'");

                    if (!cmnUsers.isEmpty()) {
                        rolesList.add(cmnUsers.get(0));
                    } else throw new IllegalStateException("cannot find common group");


                    // TODO:Zmenit
                    ((UserImpl) user).setGroups((Role[]) rolesList.toArray(new Role[rolesList.size()]));
                    session.setAttribute(USER_SESSION_KEY, user);
                    return user;
                } else
                    return null;


            } else {
                return (User) session.getAttribute(USER_SESSION_KEY);
            }


        }
    }

}
