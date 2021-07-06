package cz.kramerius.shared;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Title {
    private static final Set<String> nonsortsWithExpectedSpaces = initNonsortsWithExpectedSpaces();
    private final String nonsort;
    private final String value;

    private static Set<String> initNonsortsWithExpectedSpaces() {
        Set<String> result = new HashSet<>();
        //english
        result.addAll(Arrays.asList(new String[]{"The", "A", "An"}));
        //german
        result.addAll(Arrays.asList(new String[]{"Der", "Die", "Das", "Des", "Dem", "Den", "Ein", "Eine", "Einer", "Eines", "Einem", "Einen"}));
        //spanish
        result.addAll(Arrays.asList(new String[]{"El", "La", "Lo", "Los", "Las", "Un", "Una", "Unos", "Unas"}));
        //french
        result.addAll(Arrays.asList(new String[]{"Le", "La", "Les", "Un", "Une", "Des", "De", "D", "Du", "De la", "Des"}));
        //italian
        result.addAll(Arrays.asList(new String[]{"Il", "Li", "Lo", "La", "I", "Gli", "Le", "Del", "Dello", "Della", "Dei", "Degli", "Delle", "Uno", "Una", "Un"}));
        return result;
    }

    public Title(String nonsort, String value) {
        this.nonsort = nonsort;
        this.value = value;
    }

    public Title(String value) {
        this(null, value);
    }

    @Override
    public String toString() {
        if (nonsort == null) {
            return value;
        } else if (nonsort.endsWith(" ")) {
            return nonsort + value;
        } else if (nonsortsWithExpectedSpaces.contains(nonsort)) { //probably missing space, adding
            return nonsort + ' ' + value;
        } else { //nonSort not ending with space, possibly intentionally (<nonSort>L'</nonSort><title>Enfant</title> -> L'Enfant)
            return nonsort + value;
        }
    }

    public String getValueWithoutNonsort() {
        return value;
    }
}
