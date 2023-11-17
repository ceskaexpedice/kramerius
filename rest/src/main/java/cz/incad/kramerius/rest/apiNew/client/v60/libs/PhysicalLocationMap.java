package cz.incad.kramerius.rest.apiNew.client.v60.libs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicalLocationMap {
    
    private static final Map<String,List<String>> MAPPING = new HashMap<>();
    private static final Map<String,String> DESCRIPTIONS = new HashMap<>();

    
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
        
        
        DESCRIPTIONS.put("ABA000", "Národni knihovna ČR");
        DESCRIPTIONS.put("ABA001", "Národni knihovna ČR");
        DESCRIPTIONS.put("ABA004", "Národni knihovna ČR - Slovanská knihovna");

        DESCRIPTIONS.put("ABA007", "Knihovna AVČR");
        DESCRIPTIONS.put("ABB045", "Knihovna AVČR - Etnologický ústav AV ČR");
        DESCRIPTIONS.put("ABB030", "Knihovna AVČR - Orientální ústav AV ČR");
        DESCRIPTIONS.put("ABE459", "Knihovna AVČR - Královská kanonie premonstrátů");
        DESCRIPTIONS.put("ABB060", "Knihovna AVČR - Ústav pro českou literaturu AV ČR");
        

        DESCRIPTIONS.put("BOA001", "Moravská zemská knihovna v Brně");
        DESCRIPTIONS.put("KME450", "MZK - Muzeum umění Olomouc, Arcidiecézní muzeum Kroměříž");
        DESCRIPTIONS.put("BOE950", "MZK - Benediktinské opatství Rajhrad");
        DESCRIPTIONS.put("BOE801", "MZK - Muzeum Brněnska");
        DESCRIPTIONS.put("BVE301", "MZK - Regionální muzeum v Mikulově");
        DESCRIPTIONS.put("HOE802", "MZK - Masarykovo muzeum v Hodoníně");


        DESCRIPTIONS.put("HKA001", "Studijní a vědecká knihovna v Hradci Králové");
        DESCRIPTIONS.put("JCE301", "SVKHK - Regionální muzeum v Jičíně");
        DESCRIPTIONS.put("RKE801", "SVKHK - Okresní muzeum Orlických hor");
        DESCRIPTIONS.put("NAG001", "SVKHK - Městská knihovna Náchod");
        DESCRIPTIONS.put("HKE302", "SVKHK - Muzeum východních Čech v Hradci Králové");
        DESCRIPTIONS.put("NAE802", "SVKHK - Městské muzeum v Jaroměři");
        DESCRIPTIONS.put("HKG001", "SVKHK - Knihovna města Hradce Králové");
        DESCRIPTIONS.put("TUE301", "SVKHK - Muzeum Podkrkonoší v Trutnově");
        DESCRIPTIONS.put("JCG001", "SVKHK - Knihovna Václava Čtvrtka v Jičíně");

        DESCRIPTIONS.put("ABA009", "Knihovna Antonína Švehly");
        DESCRIPTIONS.put("PAG001", "Krajská knihovna v Pardubicích");
        DESCRIPTIONS.put("ULG001", "Knihovna ústeckého kraje");
        DESCRIPTIONS.put("ABC135", "Národní filmový archiv");
        DESCRIPTIONS.put("ABG001", "Městská knihovna v Praze");
        DESCRIPTIONS.put("ABA010", "Národní muzeum");
        DESCRIPTIONS.put("CBA001", "Jihočeská vědecká knihovna");
        DESCRIPTIONS.put("OLA001", "Vědecká knihovna v Olomouci");
        
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

    public String getDescription(String sigla) {
        return DESCRIPTIONS.get(sigla);
    }
    public List<String> getAssocations(String acronym) {
        if (MAPPING.containsKey(acronym)) {
            return MAPPING.get(acronym);
        } else {
            return new ArrayList<>();
        }
    }
}
