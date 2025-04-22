package cz.incad.kramerius.rest.apiNew.admin.v70.reharvest;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.List;

import org.json.JSONException;



/**
 * Interface for managing the reharvesting process in the Czech Digital Library (CDK).
 * Reharvesting involves deleting and re-downloading digital works from all connected libraries
 * to ensure data consistency and synchronization.
 */
public interface ReharvestManager {

    /**
     * Registers a new reharvest item.
     *
     * @param item The reharvest item to be registered.
     * @throws AlreadyRegistedPidsException If the item is already registered.
     */
    public void register(ReharvestItem item) throws AlreadyRegistedPidsException;

    /**
     * Registers a new reharvest item, with an option to prevent multiple registrations.
     *
     * @param item The reharvest item to be registered.
     * @param preventMultipleRegistrationFlag If true, prevents the item from being registered multiple times.
     * @throws AlreadyRegistedPidsException If the item is already registered and multiple registration is not allowed.
     */
    public void register(ReharvestItem item, boolean preventMultipleRegistrationFlag) throws AlreadyRegistedPidsException;

    /**
     * Updates an existing reharvest item.
     *
     * @param item The reharvest item to be updated.
     * @return The updated reharvest item.
     * @throws UnsupportedEncodingException If encoding issues occur.
     * @throws JSONException If JSON processing fails.
     * @throws ParseException If a parsing error occurs.
     */
    public ReharvestItem update(ReharvestItem item) throws UnsupportedEncodingException, JSONException, ParseException;

    /**
     * Retrieves a list of all registered reharvest items.
     *
     * @return A list of all reharvest items.
     */
    public List<ReharvestItem> getAllItems();


    /**
     * Searches for reharvest items based on filters.
     *
     * @param start The starting index for pagination.
     * @param rows The number of results to return.
     * @param filters A list of filters to apply to the search.
     * @return A JSON string containing the search results.
     */
    public String searchItems(int start, int rows, List<String> filters);

    /**
     * Retrieves the top-priority reharvest item based on its status.
     *
     * @param status The status of the desired top item.
     * @return The top reharvest item matching the specified status.
     * Retrieves a list of all reharvest items.
     *
     * @return A list of ReharvestItem objects.
     */
    public List<ReharvestItem> getItems();

    /**
     * Retrieves the highest-priority reharvest item with a given status.
     *
     * @param status The status of the item to retrieve.
     * @return The top-priority ReharvestItem with the specified status.
     */
    public ReharvestItem getTopItem(String status);

    /**
     * Retrieves a reharvest item by its unique identifier.
     *
     * @param id The unique identifier of the item.
     * @return The corresponding reharvest item.
     */
    public ReharvestItem getItemById(String id);

    public List<ReharvestItem> getItemByConflictId(String id);

    /**
     * Retrieves an open reharvest item based on its persistent identifier (PID).
     *
     * @param pid The persistent identifier of the item.
     * @return The open reharvest item matching the specified PID.
     */
    public ReharvestItem getOpenItemByPid(String pid);

    /**
     * Deregisters a reharvest item, removing it from the system.
     *
     * @param id The unique identifier of the item to be deregistered.
     * @param id The ID of the item.
     * @return The corresponding ReharvestItem.
     */
    public ReharvestItem getItemById(String id);

    /**
     * Retrieves an open reharvest item by its PID.
     *
     * @param pid The PID of the item.
     * @return The open ReharvestItem associated with the given PID.
     */
    public ReharvestItem getOpenItemByPid(String pid);

    /**
     * Deregisters a reharvest item by its ID.
     *
     * @param id The ID of the item to be removed.
     */
    public void deregister(String id);
}
