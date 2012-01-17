/*
 * Copyright (C) 2010 Jan Pokorsky
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package cz.incad.kramerius.editor.server;

import cz.incad.kramerius.editor.share.rpc.GetSuggestionResult.Suggestion;
import cz.incad.kramerius.editor.server.GetSuggestionQueryHandler.SolrSuggestionQuery;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jan Pokorsky
 */
public class GetSuggestionQueryHandlerTest {

    public GetSuggestionQueryHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /** run only with local Kramerius! */
//    @Test
    public void testLiveQuery() throws Exception {
        SolrSuggestionQuery query = new SolrSuggestionQuery();
        InputStream is = query.runQuery("k", SolrSuggestionQuery.ROWS_THRESHOLD);
        byte[] dumpedStream = dumpStream(is);
        printStream(new ByteArrayInputStream(dumpedStream));
        List<Suggestion> suggestions = query.parseStream(new ByteArrayInputStream(dumpedStream));

        for (Suggestion suggestion : suggestions) {
            System.out.println(suggestion);
        }
    }

    @Test
    public void testQuery() throws Exception {
        SolrSuggestionQuery query = new SolrSuggestionQuery();
        InputStream is = this.getClass().getResourceAsStream("SuggestionSolrResult.xml");
        List<Suggestion> suggestions = query.parseStream(is);
        assertNotNull(suggestions);
        assertEquals(2, suggestions.size());
        assertEquals("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", suggestions.get(0).getPid());
        assertEquals("uuid:5fe0b160-62d5-11dd-bdc7-000d606f5dc6", suggestions.get(1).getPid());
    }

//    @Test
    // XXX add mocked KConfiguration.getInstance().getSolrHost() otherwise it uses live settings from .kramerius4
    public void testBuildSolrQuery() throws Exception {
        SolrSuggestionQuery query = new SolrSuggestionQuery();
        String exp = "http://localhost:8080/solr/select"
                + "?q=dc.title:d*%20-fedora.model:page"
                + "&fl=PID,root_title,dc.title,fedora.model,score"
                + "&wt=xml"
                + "&omitHeader=true"
                + "&rows=" + SolrSuggestionQuery.ROWS_THRESHOLD;
        URL res = query.buildSolrQuery("D", SolrSuggestionQuery.ROWS_THRESHOLD);
        assertNotNull(res);
        assertEquals(exp, res.toExternalForm());
    }

    private static byte[] dumpStream(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (true) {
            byte[] buffer = new byte[2000];
            int length = is.read(buffer);
            if (length < 0) {
                break;
            } else {
                bos.write(buffer, 0, length);
            }
        }
        return bos.toByteArray();
    }

    private static void printStream(InputStream is) throws IOException {
        System.out.println("#dumpStream.............");
        while (true) {
            byte[] buffer = new byte[2000];
            int length = is.read(buffer);
            if (length < 0) {
                break;
            } else {
                System.out.print(new String(buffer, 0, length, "UTF-8"));
            }
        }
        System.out.println(".............dumpStream#");
    }

}