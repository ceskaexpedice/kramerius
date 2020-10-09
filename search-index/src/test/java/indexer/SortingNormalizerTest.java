package indexer;

import cz.kramerius.searchIndex.indexer.conversions.SortingNormalizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SortingNormalizerTest {

    private final SortingNormalizer sortingNormalizer = new SortingNormalizer();

    @Test
    public void leadingNonletters() {
        assertEquals("1", sortingNormalizer.normalize("[1]"));
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
}
