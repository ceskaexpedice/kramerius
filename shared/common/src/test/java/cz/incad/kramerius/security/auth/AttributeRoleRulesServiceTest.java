package cz.incad.kramerius.security.auth;

import com.google.inject.Provider;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.UserImpl;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AttributeRoleRulesServiceTest {

    @Test
    public void appliesAttributeAndUsernameRulesWithMatchesAndEqualsAndAllAndAny() throws Exception {
        String rules = """
                {"version":1,"rules":[
                  {"name":"Department and email","enabled":true,"when":{"all":[
                    {"attribute":"department","operator":"equals","value":"Digital Library"},
                    {"attribute":"email","operator":"matches","value":"@example\\\\.org$"}
                  ]},"then":{"roles":["metadata_editor"]}},
                  {"name":"Named curator","enabled":true,"when":{"any":[
                    {"attribute":"username","operator":"equals","value":"alice.smith"},
                    {"attribute":"groups","operator":"matches","value":"(^|,)curator(,|$)"}
                  ]},"then":{"roles":["curator"]}},
                  {"name":"Keycloak identity","enabled":true,"when":{"all":[
                    {"attribute":"given_name","operator":"matches","value":"^Ali"},
                    {"attribute":"username","operator":"equals","value":"alice.smith"}
                  ]},"then":{"roles":["keycloak_user"]}}
                ]}
                """;

        User user = new UserImpl(42, "Alice", "Smith", "alice.smith", 0);
        Map<String, String> attributes = new HashMap<>();
        // Representative Keycloak user claims, with normalized username used by the auth flow.
        attributes.put("sub", "d3b07384-d9a0-4f1c-a2e1-123456789abc");
        attributes.put("preferred_username", user.getLoginname());
        attributes.put("username", user.getLoginname());
        attributes.put("given_name", user.getFirstName());
        attributes.put("family_name", user.getSurname());
        attributes.put("email", "alice.smith@example.org");
        attributes.put("department", "Digital Library");
        attributes.put("groups", "users,curator");

        List<String> roles = new ArrayList<>();
        List<AttributeRoleRulesService.RoleMapping> mappings =
                serviceWithRules(rules).applyAndGetRoleMappings(attributes, roles);

        assertEquals(List.of("metadata_editor", "curator", "keycloak_user"), roles);
        assertEquals(List.of("Department and email", "Named curator", "Keycloak identity"),
                mappings.stream().map(AttributeRoleRulesService.RoleMapping::getRuleName)
                        .collect(java.util.stream.Collectors.toList()));
    }

    @Test
    public void matchesDnntRuleAgainstKeycloakMultiValuedAttributes() throws Exception {
        String rules = """
                {"version":1,"rules":[
                  {"name":"Dnnt uživatelé","enabled":true,"when":{"all":[
                    {"attribute":"eduPersonEntitlement","operator":"equals","value":"urn:mace:dir:entitlement:common-lib-terms"},
                    {"attribute":"eduPersonScopedAffiliation","operator":"matches","value":"member.*"}
                  ]},"then":{"roles":["dnnt-users"]}}
                ]}
                """;

        User user = new UserImpl(0, "Pavel", "Šťastný", "dnnt-user", 0);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("username", user.getLoginname());
        attributes.put("email", "pavel.stastny@gmail.com");
        attributes.put("eduPersonEntitlement", String.join(",",
                "urn:mace:dir:entitlement:common-lib-terms",
                "urn:mace:terena.org:tcs:personal-user",
                "urn:mace:terena.org:tcs:escience-user"));
        attributes.put("eduPersonScopedAffiliation", String.join(",",
                "affiliate@vutbr.cz", "employee@vutbr.cz", "member@vutbr.cz"));

        List<String> roles = new ArrayList<>();
        List<AttributeRoleRulesService.RoleMapping> mappings =
                serviceWithRules(rules).applyAndGetRoleMappings(attributes, roles);

        assertEquals(List.of("dnnt-users"), roles);
        assertEquals("Dnnt uživatelé", mappings.get(0).getRuleName());
        assertEquals("dnnt-users", mappings.get(0).getRole());
    }

    private AttributeRoleRulesService serviceWithRules(String rules) throws Exception {
        Provider<Connection> provider = mock(Provider.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet result = mock(ResultSet.class);
        when(provider.get()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(result);
        when(result.next()).thenReturn(true);
        when(result.getString(1)).thenReturn(rules);
        return new AttributeRoleRulesService(provider);
    }
}
