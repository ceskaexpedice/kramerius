package cz.incad.kramerius.rest.apiNew.client.v60.libs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicalLocationMap {
    
    private static final Map<String,List<String>> MAPPING = new HashMap<>();

    static {
        MAPPING.put("nkp", Arrays.asList(
                "ABA001",
                "ABA000",
                "ABA004"));

        MAPPING.put("knav", Arrays.asList(
                "ABA007",
                "ABB045",
                "ABB030",
                "ABE459",
                "ABB060"
                ));

        MAPPING.put("mzk", Arrays.asList(
                "BOA001",
                "KME450",
                "BOE950",
                "BOE801",
                "BVE301",
                "HOE802"
                ));

        MAPPING.put("svkhk", Arrays.asList(
                "HKA001",
                "JCE301",
                "RKE801",
                "HKE302",
                "NAE802",
                "HKG001",
                "TUE301"
                ));

        MAPPING.put("uzei", Arrays.asList(
                "ABA009"));

        MAPPING.put("kkp", Arrays.asList(
                "PAG001"));

        MAPPING.put("svkul", Arrays.asList(
                "ULG001"));

        MAPPING.put("nfa", Arrays.asList(
                "ABC135"));

        MAPPING.put("mlp", Arrays.asList(
                "ABG001"));

        MAPPING.put("nm", Arrays.asList(
                "ABA010"));

        MAPPING.put("cbvk", Arrays.asList(
                "CBA001"));

        MAPPING.put("vkol", Arrays.asList(
                "OLA001"));
    }
    
    public PhysicalLocationMap() {}
    
    
    public String findBySigla(String sigla) {
        for (String key : MAPPING.keySet()) {
            if (MAPPING.get(key).contains(sigla)) {
                return key;
            }
        }
        return null;
    }
    
}
