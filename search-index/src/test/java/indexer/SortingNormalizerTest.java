package indexer;

import cz.kramerius.searchIndex.indexer.conversions.SortingNormalizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SortingNormalizerTest {

    private final SortingNormalizer sortingNormalizer = new SortingNormalizer();

    @Test
    public void leadingAndTrailingWhiteSpaces() {
        assertEquals("LEADING SPACE", sortingNormalizer.normalize(" leading space"));
        assertEquals("LEADING TAB", sortingNormalizer.normalize("   leading tab"));
        assertEquals("LEADING MULTIPLE", sortingNormalizer.normalize("            leading multiple"));
        assertEquals("TRAILING SPACE", sortingNormalizer.normalize("trailing space "));
        assertEquals("TRAILING TAB", sortingNormalizer.normalize("trailing tab   "));
        assertEquals("TRAILING MULTIPLE", sortingNormalizer.normalize("trailing multiple            "));
    }

    @Test
    public void leadingNonletters() {
        assertEquals("1", sortingNormalizer.normalize("[1]"));
        assertEquals("A POZDRAVUJ U NA|S DOMA", sortingNormalizer.normalize("... a pozdravuj u nás doma"));
        assertEquals("A NEZAPOMEN| NA MODLITBU", sortingNormalizer.normalize("\"-- a nezapomeň na modlitbu!\""));
        assertEquals("A JES|TE| KAFI|C|KO", sortingNormalizer.normalize("--a ještě kafíčko"));
        assertEquals("A VY|STUPY DO U|DOLI|", sortingNormalizer.normalize("-a výstupy do údolí"));
        assertEquals("AB NORMALIZACE = FAKE NORMALIZATION", sortingNormalizer.normalize("(Ab)normalizace = (Fake) normalization"));
        assertEquals("ANSIH|TEN VON DER INNEREN STADT", sortingNormalizer.normalize("[ Ansichten von der inneren Stadt]"));
        assertEquals("T GESIGHT VAN DE KNEUTER DYK SIENDE NA DE KLOOSTER KERK VUE DU KNEUTER DYK REGARDANT VERS L EGLISE DU CLOITRE",
                sortingNormalizer.normalize("'T Gesight van de Kneuter Dyk Siende na de Klooster Kerk Vue du Kneuter Dyk Regardant Vers l'Eglise du Cloitre"));
        assertEquals("JUVOSS V ROSTOVE| NAD DONEM RUSKO", sortingNormalizer.normalize("\" Juvoss\" v Rostově nad Donem (Rusko)"));
        assertEquals("JIRKA POSTRAH| RODINY IV JIRKA NEZBEDA", sortingNormalizer.normalize("(Jirka, postrach rodiny IV). Jirka nezbeda"));
        assertEquals("MULTI KULTU|RNA PERSPEKTI|VA EDUKA|CIE", sortingNormalizer.normalize("(Multi)kultúrna perspektíva edukácie"));
        assertEquals("NET FRAMEWORK PROGRAMOVA|NI| APLIKACI|", sortingNormalizer.normalize(".NET Framework programování aplikací"));
        assertEquals("TEH|NOLOGIE PRO PR|I|PRAVU A ENERGETICKE| VYUZ|ITI| BIOMASY", sortingNormalizer.normalize("\"Technologie pro přípravu a energetické využití biomasy\""));
        assertEquals("1 BR|EZEN 1881", sortingNormalizer.normalize("1. březen 1881"));
        //assertEquals("", sortingNormalizer.normalize(""));
    }

    @Test
    public void latinAlphabet() {
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", sortingNormalizer.normalize("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", sortingNormalizer.normalize("abcdefghijklmnopqrstuvwxyz"));
    }

    @Test
    public void czechAlphabetComplete() {
        assertEquals("AA|BCC|DD|EE|E|FGHH|II|JKLMNN|OO|PQRR|SS|TT|UU|U|VWXYY|ZZ|", sortingNormalizer.normalize("AÁBCČDĎEÉĚFGHChIÍJKLMNŇOÓPQRŘSŠTŤUÚŮVWXYÝZŽ"));
        assertEquals("AA|BCC|DD|EE|E|FGHH|II|JKLMNN|OO|PQRR|SS|TT|UU|U|VWXYY|ZZ|", sortingNormalizer.normalize("aábcčdďeéěfghchiíjklmnňoópqrřsštťuúůvwxyýzž"));
    }

    @Test
    public void czechDiacritics() {
        assertEquals("A|C|D|E|E|I|N|O|R|S|T|U|U|Y|Z|", sortingNormalizer.normalize("ÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ"));
        assertEquals("PR|I|LIS| Z|LUT|OUC|KY| KU|N| U|PE|L D|A|BELSKE| O|DY", sortingNormalizer.normalize("Příliš žluťoučký kůň úpěl ďábelské ódy"));
    }

    @Test
    public void czechLetterCh() {
        assertEquals("H|OH|OLKA", sortingNormalizer.normalize("Chocholka"));
    }

    @Test
    public void slovakAlphabetComplete() {
        assertEquals("AA|A|BCC|DD|D|D||EE|FGHH|II|JKLL|L|MNN|OO|O|PQRR|SS|TT|UU|VWXYY|ZZ|", sortingNormalizer.normalize("AÁÄBCČDĎDZDŽEÉFGHCHIÍJKLĹĽMNŇOÓÔPQRŔSŠTŤUÚVWXYÝZŽ"));
        assertEquals("AA|A|BCC|DD|D|D||EE|FGHH|II|JKLL|L|MNN|OO|O|PQRR|SS|TT|UU|VWXYY|ZZ|", sortingNormalizer.normalize("aáäbcčdďdzdžeéfghchiíjklĺľmnňoóôpqrŕsštťuúvwxyýzž"));
    }

    @Test
    public void slovakLettersDz() {
        assertEquals("D|URINDOVO D||UDO MED|I HRA|D|AMI D||EMU", sortingNormalizer.normalize("Dzurindovo džudo medzi hrádzami džemu"));
    }

    @Test
    public void germanAlphabetComplete() {
        assertEquals("AA|BCDEFGHIJKLMNOO|PQRSS|TUU|VWXYZ", sortingNormalizer.normalize("AÄBCDEFGHIJKLMNOÖPQRSẞTUÜVWXYZ"));
        assertEquals("AA|BCDEFGHIJKLMNOO|PQRSS|TUU|VWXYZ", sortingNormalizer.normalize("aäbcdefghijklmnoöpqrsßtuüvwxyz"));
    }

    @Test
    public void russianAlphabetComplete() {
        //cyrilic, hebrew, chinese, etc. will be kept as is, i.e. for example no toUpperCase()
        assertEquals("АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ", sortingNormalizer.normalize("АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"));
        assertEquals("абвгдеёжзийклмнопрстуфхцчшщъыьэюя", sortingNormalizer.normalize("абвгдеёжзийклмнопрстуфхцчшщъыьэюя"));
    }

    @Test
    public void frenchDiacriticsOrtographics() {
        assertEquals("ÀàÂâÆæÇçE|E|ÈèÊêËëÎîÏïO|O|ŒœÙùÛûU|U|Ÿÿ", sortingNormalizer.normalize("ÀàÂâÆæÇçÉéÈèÊêËëÎîÏïÔôŒœÙùÛûÜüŸÿ"));
    }
}
