package cz.incad.kramerius.rest.apiNew.admin.v70.collections;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Iso639Converter {
    private Map<String, List<String>> iso639_1to2Map;

    public Iso639Converter() {

        
        // Inicializujeme mapování z ISO 639-1 na ISO 639-2
        iso639_1to2Map = new HashMap<>();
        // cze is not valid code
        iso639_1to2Map.put("cs", Arrays.asList("cze")); // ??
        

        iso639_1to2Map.put("aa", Arrays.asList("aar"));
        
        iso639_1to2Map.put("aa", Arrays.asList("aar"));
        iso639_1to2Map.put("ab", Arrays.asList("abk"));
        iso639_1to2Map.put("af", Arrays.asList("afr"));
        iso639_1to2Map.put("ak", Arrays.asList("aka"));
        iso639_1to2Map.put("am", Arrays.asList("amh"));
        iso639_1to2Map.put("an", Arrays.asList("arg"));
        iso639_1to2Map.put("ar", Arrays.asList("ara"));
        iso639_1to2Map.put("as", Arrays.asList("asm"));
        iso639_1to2Map.put("av", Arrays.asList("ava"));
        iso639_1to2Map.put("ay", Arrays.asList("aym"));
        iso639_1to2Map.put("az", Arrays.asList("aze"));
        iso639_1to2Map.put("ba", Arrays.asList("bak"));
        iso639_1to2Map.put("be", Arrays.asList("bel"));
        iso639_1to2Map.put("bg", Arrays.asList("bul"));
        iso639_1to2Map.put("bh", Arrays.asList("bih"));
        iso639_1to2Map.put("bi", Arrays.asList("bis"));
        iso639_1to2Map.put("bm", Arrays.asList("bam"));
        iso639_1to2Map.put("bn", Arrays.asList("ben"));
        iso639_1to2Map.put("bo", Arrays.asList("tib"));
        iso639_1to2Map.put("br", Arrays.asList("bre"));
        iso639_1to2Map.put("bs", Arrays.asList("bos"));
        iso639_1to2Map.put("ca", Arrays.asList("cat"));
        iso639_1to2Map.put("ce", Arrays.asList("che"));
        iso639_1to2Map.put("ch", Arrays.asList("cha"));
        iso639_1to2Map.put("co", Arrays.asList("cos"));
        iso639_1to2Map.put("cr", Arrays.asList("cre"));
        iso639_1to2Map.put("cu", Arrays.asList("chu"));
        iso639_1to2Map.put("cv", Arrays.asList("chv"));
        iso639_1to2Map.put("cy", Arrays.asList("wel"));
        iso639_1to2Map.put("da", Arrays.asList("dan"));
        iso639_1to2Map.put("de", Arrays.asList("ger"));
        iso639_1to2Map.put("dv", Arrays.asList("div"));
        iso639_1to2Map.put("dz", Arrays.asList("dzo"));
        iso639_1to2Map.put("ee", Arrays.asList("ewe"));
        iso639_1to2Map.put("el", Arrays.asList("gre"));
        iso639_1to2Map.put("en", Arrays.asList("eng"));
        iso639_1to2Map.put("eo", Arrays.asList("epo"));
        iso639_1to2Map.put("es", Arrays.asList("spa"));
        iso639_1to2Map.put("et", Arrays.asList("est"));
        iso639_1to2Map.put("eu", Arrays.asList("baq"));
        iso639_1to2Map.put("fa", Arrays.asList("fas"));
        iso639_1to2Map.put("ff", Arrays.asList("ful"));
        iso639_1to2Map.put("fi", Arrays.asList("fin"));
        iso639_1to2Map.put("fj", Arrays.asList("fij"));
        iso639_1to2Map.put("fo", Arrays.asList("fao"));
        iso639_1to2Map.put("fr", Arrays.asList("fre"));
        iso639_1to2Map.put("fy", Arrays.asList("fry"));
        iso639_1to2Map.put("ga", Arrays.asList("gle"));
        iso639_1to2Map.put("gd", Arrays.asList("gla"));
        iso639_1to2Map.put("gl", Arrays.asList("glg"));
        iso639_1to2Map.put("gn", Arrays.asList("grn"));
        iso639_1to2Map.put("gu", Arrays.asList("guj"));
        iso639_1to2Map.put("gv", Arrays.asList("glv"));
        iso639_1to2Map.put("ha", Arrays.asList("hau"));
        iso639_1to2Map.put("he", Arrays.asList("heb"));
        iso639_1to2Map.put("hi", Arrays.asList("hin"));
        iso639_1to2Map.put("ho", Arrays.asList("hmo"));
        iso639_1to2Map.put("hr", Arrays.asList("hrv"));
        iso639_1to2Map.put("ht", Arrays.asList("hat"));
        iso639_1to2Map.put("hu", Arrays.asList("hun"));
        iso639_1to2Map.put("hy", Arrays.asList("hye"));
        iso639_1to2Map.put("hz", Arrays.asList("her"));
        iso639_1to2Map.put("ia", Arrays.asList("ina"));
        iso639_1to2Map.put("id", Arrays.asList("ind"));
        iso639_1to2Map.put("ie", Arrays.asList("ile"));
        iso639_1to2Map.put("ig", Arrays.asList("ibo"));
        iso639_1to2Map.put("ii", Arrays.asList("iii"));
        iso639_1to2Map.put("ik", Arrays.asList("ipk"));
        iso639_1to2Map.put("io", Arrays.asList("ido"));
        iso639_1to2Map.put("is", Arrays.asList("ice"));
        iso639_1to2Map.put("it", Arrays.asList("ita"));
        iso639_1to2Map.put("iu", Arrays.asList("iku"));
        iso639_1to2Map.put("ja", Arrays.asList("jpn"));
        iso639_1to2Map.put("jv", Arrays.asList("jav"));
        iso639_1to2Map.put("ka", Arrays.asList("kat"));
        iso639_1to2Map.put("kg", Arrays.asList("kon"));
        iso639_1to2Map.put("ki", Arrays.asList("kik"));
        iso639_1to2Map.put("kj", Arrays.asList("kua"));
        iso639_1to2Map.put("kk", Arrays.asList("kaz"));
        iso639_1to2Map.put("kl", Arrays.asList("kal"));
        iso639_1to2Map.put("km", Arrays.asList("khm"));
        iso639_1to2Map.put("kn", Arrays.asList("kan"));
        iso639_1to2Map.put("ko", Arrays.asList("kor"));
        iso639_1to2Map.put("kr", Arrays.asList("kau"));
        iso639_1to2Map.put("ks", Arrays.asList("kas"));
        iso639_1to2Map.put("ku", Arrays.asList("kur"));
        iso639_1to2Map.put("kv", Arrays.asList("kom"));
        iso639_1to2Map.put("kw", Arrays.asList("cor"));
        iso639_1to2Map.put("ky", Arrays.asList("kir"));
        iso639_1to2Map.put("la", Arrays.asList("lat"));
        iso639_1to2Map.put("lb", Arrays.asList("ltz"));
        iso639_1to2Map.put("lg", Arrays.asList("lug"));
        iso639_1to2Map.put("li", Arrays.asList("lim"));
        iso639_1to2Map.put("ln", Arrays.asList("lin"));
        iso639_1to2Map.put("lo", Arrays.asList("lao"));
        iso639_1to2Map.put("lt", Arrays.asList("lit"));
        iso639_1to2Map.put("lu", Arrays.asList("lub"));
        iso639_1to2Map.put("lv", Arrays.asList("lav"));
        iso639_1to2Map.put("mg", Arrays.asList("mlg"));
        iso639_1to2Map.put("mh", Arrays.asList("mah"));
        iso639_1to2Map.put("mi", Arrays.asList("mao"));
        iso639_1to2Map.put("mk", Arrays.asList("mac"));
        iso639_1to2Map.put("ml", Arrays.asList("mal"));
        iso639_1to2Map.put("mn", Arrays.asList("mon"));
        iso639_1to2Map.put("mr", Arrays.asList("mar"));
        iso639_1to2Map.put("ms", Arrays.asList("msa"));
        iso639_1to2Map.put("mt", Arrays.asList("mlt"));
        iso639_1to2Map.put("my", Arrays.asList("bur"));
        iso639_1to2Map.put("na", Arrays.asList("nau"));
        iso639_1to2Map.put("nb", Arrays.asList("nob"));
        iso639_1to2Map.put("nd", Arrays.asList("nde"));
        iso639_1to2Map.put("ne", Arrays.asList("nep"));
        iso639_1to2Map.put("ng", Arrays.asList("ndo"));
        iso639_1to2Map.put("nl", Arrays.asList("dut"));
        iso639_1to2Map.put("nn", Arrays.asList("nno"));
        iso639_1to2Map.put("no", Arrays.asList("nor"));
        iso639_1to2Map.put("nr", Arrays.asList("nbl"));
        iso639_1to2Map.put("nv", Arrays.asList("nav"));
        iso639_1to2Map.put("ny", Arrays.asList("nya"));
        iso639_1to2Map.put("oc", Arrays.asList("oci"));
        iso639_1to2Map.put("oj", Arrays.asList("oji"));
        iso639_1to2Map.put("om", Arrays.asList("orm"));
        iso639_1to2Map.put("or", Arrays.asList("ori"));
        iso639_1to2Map.put("os", Arrays.asList("oss"));
        iso639_1to2Map.put("pa", Arrays.asList("pan"));
        iso639_1to2Map.put("pi", Arrays.asList("pli"));
        iso639_1to2Map.put("pl", Arrays.asList("pol"));
        iso639_1to2Map.put("ps", Arrays.asList("pus"));
        iso639_1to2Map.put("pt", Arrays.asList("por"));
        iso639_1to2Map.put("qu", Arrays.asList("que"));
        iso639_1to2Map.put("rm", Arrays.asList("roh"));
        iso639_1to2Map.put("rn", Arrays.asList("run"));
        iso639_1to2Map.put("ro", Arrays.asList("ron"));
        iso639_1to2Map.put("ru", Arrays.asList("rus"));
        iso639_1to2Map.put("rw", Arrays.asList("kin"));
        iso639_1to2Map.put("sa", Arrays.asList("san"));
        iso639_1to2Map.put("sc", Arrays.asList("srd"));
        iso639_1to2Map.put("sd", Arrays.asList("snd"));
        iso639_1to2Map.put("se", Arrays.asList("sme"));
        iso639_1to2Map.put("sg", Arrays.asList("sag"));
        iso639_1to2Map.put("si", Arrays.asList("sin"));
        iso639_1to2Map.put("sk", Arrays.asList("slo"));
        iso639_1to2Map.put("sl", Arrays.asList("slv"));
        iso639_1to2Map.put("sm", Arrays.asList("smo"));
        iso639_1to2Map.put("sn", Arrays.asList("sna"));
        iso639_1to2Map.put("so", Arrays.asList("som"));
        iso639_1to2Map.put("sq", Arrays.asList("sqi"));
        iso639_1to2Map.put("sr", Arrays.asList("srp"));
        iso639_1to2Map.put("ss", Arrays.asList("ssw"));
        iso639_1to2Map.put("st", Arrays.asList("sot"));
        iso639_1to2Map.put("su", Arrays.asList("sun"));
        iso639_1to2Map.put("sv", Arrays.asList("swe"));
        iso639_1to2Map.put("sw", Arrays.asList("swa"));
        iso639_1to2Map.put("ta", Arrays.asList("tam"));
        iso639_1to2Map.put("te", Arrays.asList("tel"));
        iso639_1to2Map.put("tg", Arrays.asList("tgk"));
        iso639_1to2Map.put("th", Arrays.asList("tha"));
        iso639_1to2Map.put("ti", Arrays.asList("tir"));
        iso639_1to2Map.put("tk", Arrays.asList("tuk"));
        iso639_1to2Map.put("tl", Arrays.asList("tgl"));
        iso639_1to2Map.put("tn", Arrays.asList("tsn"));
        iso639_1to2Map.put("to", Arrays.asList("ton"));
        iso639_1to2Map.put("tr", Arrays.asList("tur"));
        iso639_1to2Map.put("ts", Arrays.asList("tso"));
        iso639_1to2Map.put("tt", Arrays.asList("tat"));
        iso639_1to2Map.put("tw", Arrays.asList("twi"));
        iso639_1to2Map.put("ty", Arrays.asList("tah"));
        iso639_1to2Map.put("ug", Arrays.asList("uig"));
        iso639_1to2Map.put("uk", Arrays.asList("ukr"));
        iso639_1to2Map.put("ur", Arrays.asList("urd"));
        iso639_1to2Map.put("uz", Arrays.asList("uzb"));
        iso639_1to2Map.put("ve", Arrays.asList("ven"));
        iso639_1to2Map.put("vi", Arrays.asList("vie"));
        iso639_1to2Map.put("vo", Arrays.asList("vol"));
        iso639_1to2Map.put("wa", Arrays.asList("wln"));
        iso639_1to2Map.put("wo", Arrays.asList("wol"));
        iso639_1to2Map.put("xh", Arrays.asList("xho"));
        iso639_1to2Map.put("yi", Arrays.asList("yid"));
        iso639_1to2Map.put("yo", Arrays.asList("yor"));
        iso639_1to2Map.put("za", Arrays.asList("zha"));
        iso639_1to2Map.put("zh", Arrays.asList("chi"));
        iso639_1to2Map.put("zu", Arrays.asList("zul"));
    }

    public List<String> convert(String iso639_1) {
        // Získáme ISO 639-2 kód pro daný ISO 639-1 kód
        return iso639_1to2Map.get(iso639_1);
    }
    
    public boolean isConvertable(String iso639_1) {
        return iso639_1to2Map.containsKey(iso639_1);
    }
    
    /*
     * Iso639Converter converter = new Iso639Converter();
        String iso639_1Code = "en"; // Změňte kód podle potřeby
        String iso639_2Code = converter.convert(iso639_1Code);

     */
}