package indexer;

import cz.kramerius.searchIndex.indexer.SolrInput;
import cz.kramerius.searchIndex.indexer.conversions.SolrInputBuilder;
import cz.kramerius.searchIndex.indexer.nodes.RepositoryNode;
import org.dom4j.Document;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContainsLicensesIndexTest {

    @Test
    public void shouldAddContainsLicensesToSearchIndexInput() throws Exception {
        Document foxmlDoc = new FoxmlBuilder()
                .withPid("uuid:collection")
                .withMods("<modsCollection><mods><titleInfo><title>Collection</title></titleInfo></mods></modsCollection>")
                .withRelsExt(
                        "<RDF><Description about=\"info:fedora/uuid:collection\">" +
                        "<containsLicense>dnnto</containsLicense>" +
                        "<containsLicense>onsite</containsLicense>" +
                        "</Description></RDF>"
                )
                .build();
        RepositoryNode collectionNode = new RepositoryNode(
                "uuid:collection", "collection", null,
                "uuid:collection", "collection", Arrays.asList("uuid:collection"),
                "uuid:collection", "collection", null,
                null, null, null, null,
                null, null, null,
                null, null,
                null, null, null, null,
                null, null, null, null
        );

        SolrInput solrInput = new SolrInputBuilder().processObjectFromRepository(
                null, foxmlDoc, null, collectionNode, null, null, null, false
        );

        Map<String, java.util.List<String>> fields = solrInput.getFieldsCopy();
        assertEquals(
                new HashSet<>(Arrays.asList("dnnto", "onsite")),
                new HashSet<>(fields.get("contains_licenses"))
        );
    }
}
