package cz.incad.kramerius.resourceindex;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ProcessingIndexRebuildFromFoxmlByPidTest {

    @Test
    public void testPathSegmentExtractor_1() {
        assertEquals("a", extract("abcdefghijklmnopqrstuvwxyz", "#"));
        assertEquals("ab", extract("abcdefghijklmnopqrstuvwxyz", "##"));
        assertEquals("abc", extract("abcdefghijklmnopqrstuvwxyz", "###"));
        assertEquals("abcd", extract("abcdefghijklmnopqrstuvwxyz", "####"));
        assertEquals("abcde", extract("abcdefghijklmnopqrstuvwxyz", "#####"));
    }

    @Test
    public void testPathSegmentExtractor_2() {
        assertEquals("a/b", extract("abcdefghijklmnopqrstuvwxyz", "#/#"));
        assertEquals("ab/cd", extract("abcdefghijklmnopqrstuvwxyz", "##/##"));
        assertEquals("abc/def", extract("abcdefghijklmnopqrstuvwxyz", "###/###"));
    }

    @Test
    public void testPathSegmentExtractor_3() {
        assertEquals("a/b/c", extract("abcdefghijklmnopqrstuvwxyz", "#/#/#"));
        assertEquals("ab/cd/ef", extract("abcdefghijklmnopqrstuvwxyz", "##/##/##"));
        assertEquals("abc/def/ghi", extract("abcdefghijklmnopqrstuvwxyz", "###/###/###"));
    }

    @Test
    public void testPathSegmentExtractor_4() {
        assertEquals("a/bc/def", extract("abcdefghijklmnopqrstuvwxyz", "#/##/###"));
        assertEquals("abc/de/f", extract("abcdefghijklmnopqrstuvwxyz", "###/##/#"));
        assertEquals("a/bcd/ef", extract("abcdefghijklmnopqrstuvwxyz", "#/###/##"));
        assertEquals("abc/d/ef", extract("abcdefghijklmnopqrstuvwxyz", "###/#/##"));
        assertEquals("ab/c/def", extract("abcdefghijklmnopqrstuvwxyz", "##/#/###"));
        assertEquals("ab/cde/f", extract("abcdefghijklmnopqrstuvwxyz", "##/###/#"));
    }

    @Test
    public void testPathSegmentExtractor_invalid() {
        try {
            extract("abcdefghijklmnopqrstuvwxyz", "/#");
            fail();
        } catch (Throwable e) {
            //
        }
        try {
            extract("abcdefghijklmnopqrstuvwxyz", "#/");
            fail();
        } catch (Throwable e) {
            //
        }
        try {
            extract("abcdefghijklmnopqrstuvwxyz", "/#/");
            fail();
        } catch (Throwable e) {
            //
        }

        try {
            extract("abcdefghijklmnopqrstuvwxyz", "/#/#");
            fail();
        } catch (Throwable e) {
            //
        }
        try {
            extract("abcdefghijklmnopqrstuvwxyz", "#/#/");
            fail();
        } catch (Throwable e) {
            //
        }
        try {
            extract("abcdefghijklmnopqrstuvwxyz", "/#/#/");
            fail();
        } catch (Throwable e) {
            //
        }
    }

    @Test
    public void testPathSegmentExtractor_string_too_short() {
        try {
            extract("a", "##");
            fail();
        } catch (Throwable e) {
            //
        }
        try {
            extract("a", "#/#");
            fail();
        } catch (Throwable e) {
            //
        }

        try {
            extract("ab", "#/#/#");
            fail();
        } catch (Throwable e) {
            //
        }
        try {
            extract("ab", "##/#");
            fail();
        } catch (Throwable e) {
            //
        }
        try {
            extract("ab", "#/##");
            fail();
        } catch (Throwable e) {
            //
        }

        try {
            extract("abc", "#/#/#/#");
            fail();
        } catch (Throwable e) {
            //
        }
        try {
            extract("abc", "##/#/#");
            fail();
        } catch (Throwable e) {
            //
        }
        try {
            extract("abc", "##/##");
            fail();
        } catch (Throwable e) {
            //
        }
    }


    private String extract(String hash, String pattern) {
        return ProcessingIndexRebuildFromFoxmlByPid.PathSegmentExtractor.extractPathSegements(hash, pattern);
    }
}
