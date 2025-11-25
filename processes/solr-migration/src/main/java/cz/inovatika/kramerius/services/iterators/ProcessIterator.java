package cz.inovatika.kramerius.services.iterators;

import com.sun.jersey.api.client.Client;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

/**
 * Interface for process iteration over a data source, such as a Solr collection or a file.
 * <p>
 * Implementations of this interface are responsible for performing the actual iteration and
 * invoking provided callback interfaces during and after the iteration process.
 * </p>
 * <p>
 * Two versions of the {@code iterate} method are provided — one using a Jersey {@link Client},
 * and the other using an Apache {@link CloseableHttpClient}.
 * </p>
 * <p><b>Important Notice:</b> The Jersey-based iteration method is considered deprecated and
 * will be removed in a future version of the software. The Apache HTTP Client version is
 * the preferred and future-proof option.
 * </p>
 */
public interface ProcessIterator {

	/**
	 * Iterates over a Solr collection or file using Jersey Client.
	 * <p>
	 * This method supports older implementations that rely on Jersey Client. Each processed
	 * item triggers the {@link ProcessIterationCallback}, and at the end of the iteration,
	 * {@link ProcessIterationEndCallback} is invoked.
	 * </p>
	 * <p><b>Note:</b> This method is planned for removal in a future version of the software.
	 * The preferred alternative is {@link #iterate(CloseableHttpClient, ProcessIterationCallback, ProcessIterationEndCallback)}.
	 * </p>
	 *
	 * @param client Jersey client used for communication
	 * @param iterationCallback callback invoked after each processed item
	 * @param endCallback callback invoked after the entire iteration process completes
	 */
	public void iterate(Client client, ProcessIterationCallback iterationCallback, ProcessIterationEndCallback endCallback);


	/**
	 * Iterates over a Solr collection or file using Apache CloseableHttpClient.
	 * <p>
	 * This method is the preferred way to iterate in newer versions of the software.
	 * Each processed item triggers the {@link ProcessIterationCallback}, and at the end of the iteration,
	 * {@link ProcessIterationEndCallback} is invoked.
	 * </p>
	 *
	 * @param client Apache HTTP client used for communication
	 * @param iterationCallback callback invoked after each processed item
	 * @param endCallback callback invoked after the entire iteration process completes
	 */
	public void iterate(CloseableHttpClient client, ProcessIterationCallback iterationCallback, ProcessIterationEndCallback endCallback);
}
