package cz.inovatika.kramerius.services.iterators;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

/**
 * Callback interface for handling iteration results during a processing task.
 * <p>
 * Implementations of this interface define actions to be performed on each batch
 * of results obtained during the iteration process. This is typically used in
 * conjunction with {@link ProcessIterator} to process items in a structured manner.
 * </p>
 */
public interface ProcessIterationCallback {

    /**
     * Handles the results obtained during the iteration process.
     * <p>
     * This method is called each time a batch of results is available during iteration.
     * Implementations can process, transform, or store the results as needed.
     * </p>
     *
     * @param results A list of {@link IterationItem} objects representing the current batch of processed items.
     * @throws ParserConfigurationException If an XML parser configuration error occurs.
     * @throws IOException If an I/O error occurs during processing.
     */
    public void call(List<IterationItem> results) throws ParserConfigurationException, IOException;
}
