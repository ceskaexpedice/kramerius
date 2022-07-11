package cz.incad.kramerius.security.impl;

import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.database.SecurityDatabaseUtils;
import cz.incad.kramerius.security.impl.criteria.ReadDNNTLabels;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;
import org.antlr.stringtemplate.StringTemplate;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseRightsManagerImplTest {

    @Test
    public void testupdateRightImpl() {
        RightCriterium criterium = new ReadDNNTLabels();
        RightCriteriumWrapper wrapper = new RightCriteriumWrapperImpl(criterium, 3, CriteriumType.CLASS);

        Right right = new RightImpl(1, wrapper, "uuid:xxx", "read", new RoleImpl(1, "test", -1));

        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("updateRight");
        template.setAttribute("right", right);
        //template.setAttribute("association", right.getRole() instanceof Role ? "group_id" : "user_id");
        template.setAttribute("priority", right.getFixedPriority() == 0 ? "NULL" : "" + right.getFixedPriority());
        String sql = template.toString();
        System.out.println(sql);

    }

    @Test
    public void testFindAllRightByCriteriumNames() {


        //String actionName, String[] criteriumNames, User user
        User user = new UserImpl(-1, "PAvel","Stastny", "stp", -1);
        List<Role> r = new ArrayList<>();
        r.add(new RoleImpl(1, "common_users",-1));
        ((UserImpl) user).setGroups(r.toArray(new Role[r.size()]));

        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("findAllRightsWithGroupsAndCriteriums");
        template.setAttribute("userid", 1);
        template.setAttribute("groupids", Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList()));
        template.setAttribute("action", "read");
        template.setAttribute("criteriums", "test");

        String sql = template.toString();
        System.out.println(sql);
    }
}