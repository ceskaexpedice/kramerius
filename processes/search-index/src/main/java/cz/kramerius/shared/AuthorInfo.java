package cz.kramerius.shared;

import java.util.Objects;

public class AuthorInfo {

    public final String name;
    public final String date;

    private final String[] roles;
    private final String authIdentifier;

    public AuthorInfo(String name, String date, String authIdentifier, String[] roles) {
        this.name = name;
        this.date = date;

        this.roles = roles;
        this.authIdentifier = authIdentifier;
    }

    // Authority identifier
    public String getAuthIdentifier() {
        return authIdentifier;
    }

    public String[] getRoles() {
        return roles;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthorInfo that = (AuthorInfo) o;
        return Objects.equals(name, that.name) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, date);
    }
}
