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

import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.editor.share.rpc.GetSuggestionQuery;
import cz.incad.kramerius.editor.share.rpc.GetSuggestionResult;
import cz.incad.kramerius.editor.share.rpc.GetSuggestionResult.Suggestion;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Fetches Kramerius object suggestions according to passed filter and result limit.
 *
 * @author Jan Pokorsky
 */
public final class GetSuggestionQueryHandler
        implements ActionHandler<GetSuggestionQuery, GetSuggestionResult> {

    @Override
    public Class<GetSuggestionQuery> getActionType() {
        return GetSuggestionQuery.class;
    }

    @Override
    public GetSuggestionResult execute(GetSuggestionQuery action, ExecutionContext context)
            throws DispatchException {

        GetSuggestionResult result = new GetSuggestionResult();
        try {
            List<Suggestion> suggestions = doExecute(action);
            result.setSuggestions(suggestions);
        } catch (Throwable ex) {
            result.setServerFailure();
            Logger.getLogger(GetSuggestionQueryHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private List<Suggestion> doExecute(GetSuggestionQuery action) throws Exception {
        InputStream istream = null;
        try {
            SolrSuggestionQuery query = new SolrSuggestionQuery();
            istream = query.runQuery(action.getFilter(), action.getLimit());
            List<Suggestion> suggestions = query.parseStream(istream);
            return suggestions;
        } finally {
            if (istream != null) {
                istream.close();
            }
        }
    }

    @Override
    public void rollback(GetSuggestionQuery action, GetSuggestionResult result, ExecutionContext context) throws DispatchException {
        // ignore -> read only query
    }

    /**
     * Solr implementation.
     */
    static final class SolrSuggestionQuery {

        private static final Logger LOG = Logger.getLogger(SolrSuggestionQuery.class.getName());
        public static final int ROWS_THRESHOLD = 20;

        public InputStream runQuery(String filter, int limit) throws MalformedURLException, IOException, URISyntaxException {
            // XXX validate filter content? Lucene does not support e.g. '"'
            // check limit to prevent system overloading
            limit = Math.min(ROWS_THRESHOLD, limit);
            URL url = buildSolrQuery(filter, limit);
            InputStream stream = openStream(url);
            return stream;
        }

        InputStream openStream(URL url) throws IOException {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            InputStream stream = url.openStream();
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException(conn.getResponseMessage());
            }
            return stream;
        }

        URL buildSolrQuery(String filter, int limit) throws URISyntaxException, MalformedURLException {
            filter = filter.toLowerCase(); // XXX use new Locale("cs", "cz")?
            String solrHost = KConfiguration.getInstance().getSolrHost();
            String solrQuery = String.format(
                    // search dc.title field for <filter>* and skip all pages
                    "q=dc.title:%s* -fedora.model:page"
                    // select following fields for result
                    + "&fl=PID,root_title,dc.title,fedora.model,score"
                    + "&wt=xml"
                    + "&omitHeader=true"
                    // limit result records
                    + "&rows=%s",
                    filter,
                    limit);

            LOG.log(Level.FINE, solrQuery);
            URI uri = URI.create(solrHost);
            // used to encode special characters of the query
            URI fullURI = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
                    uri.getPort(), uri.getPath() + "/select",
                    solrQuery, null);
            URL url = fullURI.normalize().toURL();
            LOG.log(Level.FINE, url.toExternalForm());
            return url;
        }

        public List<Suggestion> parseStream(InputStream is) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
            Document doc = XMLUtils.parseDocument(is);
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath paramPath = xpathFactory.newXPath();
            List<Suggestion> suggestions = new ArrayList<Suggestion>();

            NodeList nodes = findDocElements(doc, xpathFactory.newXPath());
            for (int i = 0, length = nodes.getLength(); i < length; i++) {
                Element item = (Element) nodes.item(i);
                Suggestion suggestion = processDocNode(item, paramPath);
                if (suggestion != null) {
                    suggestions.add(suggestion);
                }
            }
            return suggestions;
        }

        private Suggestion processDocNode(Node docNode, XPath xpath) throws XPathExpressionException {
            Suggestion suggestion = null;
            String pid = findDocChild("PID", docNode, xpath);
            String title = findDocChild("dc.title", docNode, xpath);
            String fmodel = findDocChild("fedora.model", docNode, xpath);
            KrameriusModels kmodel = KrameriusModels.parseString(fmodel);

            if (pid == null || pid.isEmpty()) {
                LOG.log(Level.WARNING, "missing pid: " + suggestion, new IllegalStateException());
            } else {
                suggestion = new Suggestion(pid, title, EditorServerUtils.resolveKind(kmodel));
            }
            return suggestion;
        }

        private NodeList findDocElements(Document doc, XPath xpath) throws XPathExpressionException {
            String expression = "/response/result/doc";
            return (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
        }
        private String findDocChild(String name, Node docNode, XPath xpath) throws XPathExpressionException {
            return xpath.evaluate(String.format("*[@name='%s']/text()", name), docNode).trim();
        }
    }

}
