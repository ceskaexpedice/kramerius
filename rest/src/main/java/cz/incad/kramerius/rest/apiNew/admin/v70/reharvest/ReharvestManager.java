package cz.incad.kramerius.rest.apiNew.admin.v70.reharvest;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.List;

import org.json.JSONException;

/**
 * Interface for managing reharvest operations in the digital library system.
 * Provides methods to register, update, retrieve, and deregister reharvest items.
 */
public interface ReharvestManager {

    /**
     * Registers a new reharvest item.
     *
     * @param item The ReharvestItem to be registered.
     * @param registerIfAlreadyExists  TA flag indicating whether registration is allowed if the PID is already in the reharvesting list.
     * @throws AlreadyRegistedPidsException If the PID is already registered.
     */
    public void register(ReharvestItem item, boolean registerIfAlreadyExists ) throws AlreadyRegistedPidsException;

    /**
     * Updates an existing reharvest item.
     *
     * @param item The ReharvestItem with updated data.
     * @return The updated ReharvestItem.
     * @throws UnsupportedEncodingException If encoding issues occur.
     * @throws JSONException If JSON processing fails.
     * @throws ParseException If date parsing fails.
     */
    public ReharvestItem update(ReharvestItem item) throws UnsupportedEncodingException, JSONException, ParseException;

    /**
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
