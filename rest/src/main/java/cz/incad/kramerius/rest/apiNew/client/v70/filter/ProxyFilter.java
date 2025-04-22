package cz.incad.kramerius.rest.apiNew.client.v70.filter;

import java.util.List;

import cz.inovatika.monitoring.ApiCallEvent;
import org.json.JSONObject;
import org.w3c.dom.Element;

/**
 * Interface for managing the filtering of digital libraries connected to the server.
 * <p>
 * This interface defines methods for generating and enhancing Solr filters,
 * modifying facet terms, and filtering specific document values.
 * </p>
 */
public interface ProxyFilter {

	/**
	 * Generates a new Solr filter based on the currently active digital library.
	 * <p>
	 * This filter determines which digital library is currently enabled
	 * and should be applied to Solr queries.
	 * </p>
	 *
	 * @return A Solr filter string representing the active digital library.
	 */
	String newFilter();

	/**
	 * Enhances an existing Solr filter by modifying or appending additional conditions.
	 * <p>
	 * This method allows customization of the filter based on external
	 * factors or request-specific parameters.
	 * </p>
	 *
	 * @param f The existing Solr filter string to be enhanced.
	 * @return The modified and enhanced Solr filter string.
	 */
	String enhancedFilter(String f);


	/**
	 * Modifies the facet terms used in Solr queries to better fit the active filter.
	 * <p>
	 * This method allows adjustments in how facet values are computed, ensuring
	 * that they reflect the currently applied digital library filter.
	 * </p>
	 *
	 * @return A modified facet filter string to be used in Solr queries.
	 */
	String enhanceFacetsTerms();

	/**
	 * Filters the list of digital libraries stored in the `cdk.collection` field of an XML-based Solr document.
	 * <p>
	 * This method processes the `cdk.collection` field, which contains a list of all digital libraries
	 * associated with a document, and removes any values corresponding to disabled libraries.
	 * The resulting document will only contain collections that are currently enabled.
	 * </p>
	 *
	 * @param rawDoc The XML document containing the `cdk.collection` field to be filtered.
	 * @param event  The API call event associated with this filtering process.
	 */
	void filterValue(Element rawDoc, ApiCallEvent event);

	/**
	 * Filters the list of digital libraries stored in the `cdk.collection` field of a JSON-based Solr document.
	 * <p>
	 * Similar to {@link #filterValue(Element, ApiCallEvent)}, but processes JSON documents.
	 * The method removes disabled digital libraries from the `cdk.collection` array, ensuring that
	 * only currently enabled libraries remain.
	 * </p>
	 *
	 * @param rawDoc The JSON document containing the `cdk.collection` field to be filtered.
	 * @param event  The API call event associated with this filtering process.
	 */
	void filterValue(JSONObject rawDoc, ApiCallEvent event);
}
