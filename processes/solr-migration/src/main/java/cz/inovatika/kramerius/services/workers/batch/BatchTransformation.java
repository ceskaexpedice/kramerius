package cz.inovatika.kramerius.services.workers.batch;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Abstract class representing a transformation from a source XML element (e.g. from Kramerius)
 * to a destination XML document element used for indexing (e.g. in Solr).
 *
 * Implementations of this class define how different source formats are converted into
 * target indexing structures.
 */
public abstract class BatchTransformation {


    public abstract String getName();

    /**
     * Transforms a source XML element into a destination XML element for indexing.
     *
     * @param sourceDocElm   The source XML element (e.g. from Kramerius metadata).
     * @param destDocument   The destination XML {@link Document} where the result is built.
     * @param destDocElem    The destination root element to be populated.
     * @param consumer       A consumer that can receive and process replicated copy content.
     */
    public abstract void  transform(Element sourceDocElm, Document destDocument, Element destDocElem, BatchConsumer consumer);


    /**
     * Maps a field identifier from the source system to the destination system (CDK).
     *
     * <p>
     * Two cases are supported:
     * <ul>
     *   <li>If the source data is in K5 format, the field is mapped and transformed to the K7 format.</li>
     *   <li>If the source data is already in K7 format, the mapping is direct (one-to-one).</li>
     * </ul>
     * </p>
     *
     * @param fieldId The identifier of the source field to be mapped.
     * @return The corresponding destination field identifier.
     */
    public abstract String getField(String fieldId);


    /**
     * Resolves the original source PID from the given CDK/K7 PID.
     *
     * <p>
     * Depending on the source system:
     * <ul>
     *   <li>If the record originated from K5, returns the original K5 PID format.</li>
     *   <li>If the record is from K7, returns the PID unchanged (one-to-one mapping).</li>
     * </ul>
     * </p>
     *
     * @param cdkPid The PID in the CDK/K7 system.
     * @return The corresponding source PID (either original K5 or K7 PID).
     */
    public abstract String resolveSourcePid(String cdkPid);
}
