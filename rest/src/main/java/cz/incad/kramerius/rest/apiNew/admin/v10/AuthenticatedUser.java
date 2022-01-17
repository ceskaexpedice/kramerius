package cz.incad.kramerius.rest.apiNew.admin.v10;

import java.util.List;

public class AuthenticatedUser {

    private final String id;
    private final String name;
    private final List<String> roles;

    public AuthenticatedUser(String id, String name, List<String> roles) {
        this.id = id;
        this.name = name;
        this.roles = roles;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return "AuthenticatedUser{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", roles=" + listToString(roles) +
                '}';
    }

    private String listToString(List<String> list) {
        if (list == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (String item : list) {
            builder.append(item).append(',');
        }
        builder.append(']');
        return builder.toString();
    }
}
