package cz.incad.kramerius.cdk.index;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface SearchIndexService {

    /**
     * Vrátí dokument podle PID.
     */
    SearchDocument get(String pid);

    /**
     * Vrátí přímé děti dokumentu.
     */
    List<String> getChildren(String pid);

    /**
     * Vrátí stránku dokumentů.
     * <p>
     * cursor je při prvním volání null.
     * Další cursor se vezme z výsledku předchozího volání.
     */
    SearchDocumentPage findDocuments(String cursor, int limit);

    /**
     * Aktualizuje collection fields dokumentu.
     */
    void updateCollectionsFields(String pid, List<String> inCollections, String inCollectionsDirect);
}