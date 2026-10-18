package cz.kramerius.searchIndex.indexer.execution;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class IndexerOcrTextNormalizationTest {

    @Test
    public void keepsMisdecodedUtf8OcrTextUnchangedByDefault() {
        String valid = misdecodeOnce("Pr\u00E1v\u011B p\u0159ed sedmi lety ");
        String misdecoded = misdecodeTwice("D\u00E1le \u00FA\u010Dinkovali P\u011Bveck\u00FD krou\u017Eek z Le\u0161tiny, p\u00EDse\u0148 \u201EHranice vzpl\u00E1la\".");

        String normalized = Indexer.normalizeWhitespacesForOcrText(valid + misdecoded);

        Assert.assertEquals(valid + misdecoded, normalized);
    }

    @Test
    public void repairsMisdecodedUtf8OcrTextWhenEnabled() {
        String valid = misdecodeOnce("Pr\u00E1v\u011B p\u0159ed sedmi lety ");
        String misdecoded = misdecodeTwice("D\u00E1le \u00FA\u010Dinkovali P\u011Bveck\u00FD krou\u017Eek z Le\u0161tiny, p\u00EDse\u0148 \u201EHranice vzpl\u00E1la\".");

        String normalized = Indexer.normalizeWhitespacesForOcrText(valid + misdecoded, true);

        Assert.assertFalse((valid + misdecoded).equals(normalized));
    }

    @Test
    public void keepsValidCzechOcrTextUnchanged() {
        String text = misdecodeOnce("\u010Cest jeho pam\u00E1tce! D\u00E1le p\u0159e\u010Detla p\u00ED. Z\u00E1kov\u00E1 posledn\u00ED dopis.");

        String normalized = Indexer.normalizeWhitespacesForOcrText(text);

        Assert.assertEquals(text, normalized);
    }

    private static String misdecodeOnce(String text) {
        return new String(text.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
    }

    private static String misdecodeTwice(String text) {
        return misdecodeOnce(misdecodeOnce(text));
    }
}
