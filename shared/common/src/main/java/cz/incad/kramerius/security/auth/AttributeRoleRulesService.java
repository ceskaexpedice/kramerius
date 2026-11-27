package cz.incad.kramerius.security.auth;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/** Loads and evaluates attribute-to-role rules for authenticated third-party users. */
public class AttributeRoleRulesService {
    public static final String CONFIG_KEY = "auth.attribute-role-rules";
    private static final String EMPTY_RULES = "{\"version\":1,\"rules\":[]}";
    private final Provider<Connection> connectionProvider;

    @Inject
    public AttributeRoleRulesService(@Named("kramerius4") Provider<Connection> connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    /** Returns the persisted JSON rules document, or an empty version 1 document. */
    public String getRules() throws SQLException {
        Connection connection = connectionProvider.get();
        try (PreparedStatement statement = connection.prepareStatement("SELECT VALUE FROM CONFIG WHERE KEY = ?")) {
            statement.setString(1, CONFIG_KEY);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString(1) : EMPTY_RULES;
            }
        } finally {
            close(connection);
        }
    }

    /** Ensures the singleton rules entry exists after the CONFIG table is initialized. */
    public void initialize() throws SQLException {
        Connection connection = connectionProvider.get();
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO CONFIG(KEY, VALUE) VALUES (?, ?) ON CONFLICT (KEY) DO NOTHING")) {
            statement.setString(1, CONFIG_KEY);
            statement.setString(2, EMPTY_RULES);
            statement.executeUpdate();
        } finally {
            close(connection);
        }
    }

    /** Validates and stores the rules as JSON in the existing system CONFIG table. */
    public void saveRules(String rulesJson) throws SQLException {
        validate(rulesJson);
        Connection connection = connectionProvider.get();
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO CONFIG(KEY, VALUE) VALUES (?, ?) ON CONFLICT (KEY) DO UPDATE SET VALUE = EXCLUDED.VALUE")) {
            statement.setString(1, CONFIG_KEY);
            statement.setString(2, rulesJson);
            statement.executeUpdate();
        } finally {
            close(connection);
        }
    }

    /** Applies matching rule actions in document order. Matching roles are additive. */
    public void apply(Map<String, String> attributes, List<String> roles) throws SQLException {
        applyAndGetRoleMappings(attributes, roles);
    }

    /** Applies matching rules and returns the roles each matching rule granted. */
    public List<RoleMapping> applyAndGetRoleMappings(Map<String, String> attributes, List<String> roles) throws SQLException {
        List<RoleMapping> mappings = new java.util.ArrayList<>();
        JSONObject document = new JSONObject(getRules());
        JSONArray rules = document.optJSONArray("rules");
        if (rules == null) return mappings;
        for (int i = 0; i < rules.length(); i++) {
            JSONObject rule = rules.optJSONObject(i);
            if (rule == null || !rule.optBoolean("enabled", true) || !matches(rule.optJSONObject("when"), attributes)) continue;
            JSONObject actions = rule.optJSONObject("then");
            JSONArray grants = actions == null ? null : actions.optJSONArray("roles");
            if (grants != null) for (int j = 0; j < grants.length(); j++) {
                String role = grants.optString(j, "").trim();
                if (!role.isEmpty()) {
                    if (!roles.contains(role)) roles.add(role);
                    mappings.add(new RoleMapping(role, rule.optString("name", "")));
                }
            }
            JSONObject addedAttributes = actions == null ? null : actions.optJSONObject("attributes");
            if (addedAttributes != null) for (Object rawKey : addedAttributes.keySet()) {
                String key = String.valueOf(rawKey);
                attributes.put(key, addedAttributes.optString(key));
            }
        }
        return mappings;
    }

    public static final class RoleMapping {
        private final String role;
        private final String ruleName;

        private RoleMapping(String role, String ruleName) {
            this.role = role;
            this.ruleName = ruleName;
        }

        public String getRole() { return role; }
        public String getRuleName() { return ruleName; }
    }

    public static void validate(String json) {
        try {
            JSONObject document = new JSONObject(json);
            JSONArray rules = document.optJSONArray("rules");
            if (document.optInt("version", -1) != 1 || rules == null) throw new IllegalArgumentException("Expected rules document version 1 with a rules array");
            for (int i = 0; i < rules.length(); i++) {
                JSONObject rule = rules.getJSONObject(i);
                JSONObject when = rule.getJSONObject("when");
                JSONObject then = rule.getJSONObject("then");
                if (!when.has("all") && !when.has("any")) throw new IllegalArgumentException("Rule must contain all or any conditions");
                JSONArray all = when.optJSONArray("all");
                JSONArray any = when.optJSONArray("any");
                validateConditions(all);
                validateConditions(any);
                if ((all == null || all.length() == 0) && (any == null || any.length() == 0)) {
                    throw new IllegalArgumentException("Rule must contain at least one condition");
                }
                JSONArray roles = then.optJSONArray("roles");
                JSONObject attributes = then.optJSONObject("attributes");
                if ((roles == null || roles.length() == 0) && (attributes == null || attributes.length() == 0)) {
                    throw new IllegalArgumentException("Rule action requires roles or attributes");
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid authentication rules JSON: " + e.getMessage(), e);
        }
    }

    private static void validateConditions(JSONArray conditions) {
        if (conditions == null) return;
        for (int i = 0; i < conditions.length(); i++) {
            JSONObject condition = conditions.getJSONObject(i);
            if (condition.optString("attribute", "").trim().isEmpty()) throw new IllegalArgumentException("Condition requires attribute");
            String operator = condition.getString("operator");
            if (!"exists".equals(operator) && !condition.has("value")) throw new IllegalArgumentException("Condition requires value");
            if (!"equals".equals(operator) && !"matches".equals(operator) && !"contains".equals(operator) && !"exists".equals(operator)) throw new IllegalArgumentException("Unsupported operator: " + operator);
            if ("matches".equals(operator)) Pattern.compile(condition.getString("value"));
        }
    }

    private static boolean matches(JSONObject when, Map<String, String> attributes) {
        if (when == null) return false;
        JSONArray all = when.optJSONArray("all");
        JSONArray any = when.optJSONArray("any");
        if (all != null) for (int i = 0; i < all.length(); i++) if (!condition(all.getJSONObject(i), attributes)) return false;
        if (any != null && any.length() > 0) {
            boolean one = false;
            for (int i = 0; i < any.length(); i++) if (condition(any.getJSONObject(i), attributes)) { one = true; break; }
            if (!one) return false;
        }
        return (all != null || any != null) && (all == null || all.length() > 0 || any != null);
    }

    private static boolean condition(JSONObject condition, Map<String, String> attributes) {
        String actual = attributes.get(condition.optString("attribute"));
        String expected = condition.optString("value", "");
        switch (condition.optString("operator")) {
            case "exists": return actual != null;
            case "equals": return actual != null && equalsValue(actual, expected);
            case "contains": return actual != null && actual.contains(expected);
            case "matches":
                try { return actual != null && Pattern.compile(expected).matcher(actual).find(); }
                catch (PatternSyntaxException e) { return false; }
            default: return false;
        }
    }

    /** Keycloak multi-valued attributes are flattened with commas by the caller. */
    private static boolean equalsValue(String actual, String expected) {
        if (actual.equals(expected)) return true;
        for (String value : actual.split("\\s*,\\s*")) {
            if (value.equals(expected)) return true;
        }
        return false;
    }

    private static void close(Connection connection) {
        if (connection != null) try { connection.close(); } catch (SQLException ignored) { }
    }
}
