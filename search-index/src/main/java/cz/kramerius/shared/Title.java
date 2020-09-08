package cz.kramerius.shared;

public class Title {
    public final String nonsort;
    public final String value;

    public Title(String nonsort, String value) {
        this.nonsort = nonsort;
        this.value = value;
    }

    public Title(String value) {
        this(null, value);
    }

    @Override
    public String toString() {
        return nonsort != null ? nonsort + value : value;
    }
}
