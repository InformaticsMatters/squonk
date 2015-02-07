package com.im.lac.dwsearch.service;

import java.util.List;
import javax.ws.rs.core.Response;

/**
 *
 * @author timbo
 */
public interface DWSearcher {

    /**
     * Get a health check on the service
     */
    Response serviceOK();

    /**
     * Delete a hit list
     *
     * @param hitlistId The id of the hit list to delete
     */
    Response deleteHitList(Integer hitlistId);

    /**
     * Get information for a particular hit list
     *
     * @param hitlistId The hit list to retrieve
     */
    Response getHitList(Integer hitlistId);

    /**
     * List all the hit lists
     */
    Response getHitLists();

    /**
     * Retrieve structure data for a particular hit list
     *
     * @param hitlistId The ID of the hit list
     */
    Response getStructureDataForHitList(Integer hitlistId);

    /**
     * Run a search for structures containing data for a particular property (or
     * properties) Generates a hit list and returns immediately with a 201
     * response containing the URL of the hit list which can then be polled for
     * the results. Initially the status is Pending. Once the search is complete
     * the hit list info is updated and the the status set to OK.
     *
     * @param sourceId The source ID of the property (in theory properties from
     * different sources could have the same Original ID so this is needed to
     * distinguish them)
     * @param propDefOriginalIds One or more property original IDs
     * @return A 201 response with the Location of the generated hit list
     * resource.
     */
    Response structuresWithDataSearch(Integer sourceId, List<String> propDefOriginalIds);

    /**
     * Perform a structure search
     *
     * @param queryStructure The query structure
     * @param jchemSearchOptions The search options in string format e.g. t:s
     */
    Response structuresWithStructureSearch(String queryStructure, String jchemSearchOptions);

    /**
     * Retrieve structure data for a particular structure
     *
     * @param id The ID of the structure (cd_id column value)
     */
    Response getStructureDataForId(Integer id);

    /**
     * Retrieve structure data for specific structures
     *
     * @param ids The IDs of the structure (cd_id column values)
     */
    Response getStructureDataForIds(List<Integer> ids);

    /**
     * Retrieve property data for a specific hit list
     *
     * @param hitListId The hit list ID
     * @param propertyDefOrigId The original ID of the property
     */
    Response fetchPropertyData(Integer hitListId, String propertyDefOrigId);

    /**
     * Get property definitions
     *
     * @param filter A text String to used to filter the property descriptions
     * (using a LIKE '%?%' search (case insensitive) on the property_description
     * column
     * @param limit retrieve this many rows.
     */
    Response fetchPropertyDefintions(String filter, Integer limit);

}
