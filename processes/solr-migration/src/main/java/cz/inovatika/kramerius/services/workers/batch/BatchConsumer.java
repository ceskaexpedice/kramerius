package cz.inovatika.kramerius.services.workers.batch;

import cz.inovatika.kramerius.services.config.ProcessConfig;
import org.w3c.dom.Element;


/**
 * Interface for customizing and modifying fields or entire documents during the replication
 * or transformation process when building an indexing batch.
 * <p>
 * Implementations can edit, remove, or compute field values dynamically, and also adjust
 * the structure or content of the whole document in the batch.
 */
public interface BatchConsumer {

    /**
     * Enum describing the result of a field modification.
     * Used as a response from the {@link #modifyField(Element)} method to indicate what
     * should be done with the given field.
     */
    public enum ModifyFieldResult {

        /** The field was edited or updated. */
        edit,

            /** The field should be removed from the document. */
        delete,

        /** No change was made to the field. */
        none,

        /** The field was calculated or derived (e.g. dynamically created). */
        calculated;
    }


    /**
     * Called for each field in the document to allow custom modification.
     * <p>
     * The implementor can modify the content or structure of the field, or decide
     * to delete it from the document. The return value signals what action was taken.
     *
     * @param field The XML element representing the field to modify.
     * @return A {@link ModifyFieldResult} indicating what was done with the field.
     */
    public ModifyFieldResult modifyField(Element field);

    /**
     * Called once per document, allowing modifications to the entire document element
     * before it is added to the indexing batch.
     * <p>
     * The implementor can change, enrich, or clean up the XML structure of the document
     * based on its PID or root PID context.
     *
     * @param processConfig
     * @param doc           The XML element representing the whole document.
     */
    public void changeDocument(ProcessConfig processConfig, Element doc);

}
