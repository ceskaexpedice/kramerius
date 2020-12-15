package cz.kramerius.shared;

public class AuthorInfo {
    public final String name;
    public final String date;

    public AuthorInfo(String name, String date) {
        this.name = name;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }
}
