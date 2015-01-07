package com.im.lac.dwsearch.service

import chemaxon.jchem.db.CacheRegistrationUtil
import chemaxon.jchem.db.JChemSearch
import chemaxon.util.ConnectionHandler
import chemaxon.sss.search.JChemSearchOptions

import com.im.lac.dwsearch.model.SubsetInfo
import com.im.lac.dwsearch.util.Utils

import groovy.sql.Sql
import groovy.json.JsonSlurper

import java.sql.Connection

import javax.sql.DataSource

import groovy.util.logging.Log
import groovy.json.JsonOutput

import javax.inject.Singleton
import javax.ws.rs.GET
import javax.ws.rs.DELETE
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.PathParam
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Response
import javax.ws.rs.core.StreamingOutput

import javax.ws.rs.core.MediaType

/** Provides service for searching and retrieving data from the ChemCentral data warehouse.
 * The general approach is to
 * 
 * 1. run a query to generate a hit list. This is done through a REST POST operation
 * which will result in 201 CREATED response that contains the URL of the newly created 
 * hit list in the Loction header. This URL can be polled 
 * until the status of the list is OK (meaning the search is complete), or can be used 
 * to retrieve the current results without needing to wait for the query to finish.
 * Currently two types of search are supported:
 * A. #structuresWithDataSearch() which finds structures that have data for one of more
 * properties. 
 * B. #structuresWithStructureSearch() which runs a structure search. NOTE: the DW contains 
 * many millions of structures and it takes about 3 hours to load the structure cache, so 
 * no results will be retrieved until this is complete.
 * 
 * 2. Information on hit lists can be obtained using #getHitLists() and #getHitList().
 * A hit list can be deleted using #deleteHitList()
 * 
 * 3. Retrieve structure data for a specific hit list using #getStructureDataForHitList()
 * 
 * 4. Data for a particular property can be retreived using #fetchPropertyData()
 * 
 * 5. The available property defintions can be obtained using #fetchPropertyDefintions()
 * 
 * 6. Status of the service can be obtained using #serviceOK()
 * 
 * TODO - maybe move the JChem search into a different class so that this class can 
 * be request scope (issues with structure cache)
 * TODO - work out how to handle username
 * TODO - add security so you can only work with your hit lists
 * TODO - allow to share hit lists
 * TODO - this class mixes responsibilities of hit list creation/management and chemcentral 
 * searching/retrieval. Probably these should be broken out into separate classes 
 * TODO - find better way of providing the information in the *.properties files. This
 * approach won't work for war deployment. 
 *  
 *
 * @author Tim Dudgeon
 */
@Log
@Singleton
@Path("/")
class SimpleGroovyDWSearcher {
    
    private ConfigObject database, chemcentral, users
    private DataSource dataSource
    private boolean structureCacheLoaded = false
    private long structureCacheLoadTime
    private Date initTime
    
    public static final String RESOURCE_STRUCTURES = "/chemcentral/structures"
    public static final String RESOURCE_HITLISTS = "/chemcentral/hitlists"
    
    private String propertyTable, structureTable
	
    SimpleGroovyDWSearcher() {
        log.info("Creating DW searcher")
        database = new ConfigSlurper().parse(getClass().getClassLoader().getResource('database.properties'))
        chemcentral = new ConfigSlurper().parse(getClass().getClassLoader().getResource('chemcentral.properties'))
        users = new ConfigSlurper().parse(getClass().getClassLoader().getResource('users.properties'))
        dataSource = Utils.createDataSource(database, chemcentral.username, chemcentral.password)
        propertyTable = chemcentral.jchemPropertiesTable
        structureTable = chemcentral.structuresTable
        //propertyTable = 'vendordbs.jchemproperties'
        //structureTable = 'vendordbs.drugbank_feb_2014'
        initTime = new Date()
        loadStructureCache()
    }
    
    private void loadStructureCache() {
        Thread.start() {
            log.info("Loading structure cache")
            def con = dataSource.connection
            
            ConnectionHandler conh = createConenctionHandler(con, propertyTable)
            CacheRegistrationUtil cru = new CacheRegistrationUtil(conh)
            cru.registerPermanentCache("ChemCentral-" + this.hashCode())
            
            con.autoCommit = false
            
            JChemSearch searcher = createJChemSearch(conh, structureTable)
            searcher.setQueryStructure('CN1C=NC2=C1C(=O)N(C(=O)N2C)C')
 
            JChemSearchOptions searchOptions = new JChemSearchOptions(JChemSearch.FULL);
            searcher.setSearchOptions(searchOptions);
            searcher.setRunMode(JChemSearch.RUN_MODE_SYNCH_COMPLETE)
            long t0 = System.currentTimeMillis()
            searcher.run()
            con.commit()
            long t1 = System.currentTimeMillis()
            structureCacheLoadTime = t1 - t0
            structureCacheLoaded = new Date()
            log.info("Structure cache loaded in ${structureCacheLoadTime}ms")
        }
    }
    
        
    private String getUsername() {
        // TODO - how to get the username?
        return "username?"
    }
    
    /** Get a health check on the service
     */
    @GET
    @Produces("text/plain")
    @Path("/ping")
    public Response serviceOK() {
        log.fine("Being pinged")
        StringBuilder builder = new StringBuilder("Service was started on $initTime\n")
        builder.append("Current time is: ")
        .append(new Date())
        if (structureCacheLoaded) {
            builder.append("\nStructure cache loaded in ${structureCacheLoadTime}ms")
        } else {
            builder.append("\nStructure cache still loading")
        }
        
        builder.append("\nFree memory: ")
        .append(Runtime.getRuntime().freeMemory())
        .append("\nTotal memory: ")
        .append(Runtime.getRuntime().totalMemory())
        .append("\nMax memory: ")
        .append(Runtime.getRuntime().maxMemory())
        .append("\n")
        return Response.ok(builder.toString()).build()
    }
    
    
    private Integer createHitList(Sql db, String resource, String username, String listname) {
        log.info("Generating hit list named $listname for user $username")
        int id = db.executeInsert("""\
            |INSERT INTO ${users.hitListTable} (resource, list_owner, list_name, list_status, list_size)
            |  VALUES (?,?,?,?,?)""".stripMargin(), 
            [resource, username, listname, SubsetInfo.Status.Pending.toString(), 0])[0][0]
        log.info("Hitlist $id created for $resource")
        return id
    }
    
    private void updateHitListStatus(Sql db, Integer hitlistId, SubsetInfo.Status status) {
        log.info("Updating hit list $hitlistId")
        db.executeUpdate("""\
            |UPDATE ${users.hitListTable} SET list_status = ?, list_size =
            |  (SELECT COUNT(*) FROM ${users.hitListDataTable} WHERE hit_list_id = ?)
            |  WHERE id = ?""".stripMargin(), 
            [status.toString(), hitlistId, hitlistId])
        log.info("Hitlist $hitlistId updated")
    }
    
   
    /** Delete a hit list
     * 
     * @param id The id of the hit list to delete
     */
    @DELETE
    @Produces("text/plain")
    @Path("/hitlists/{id}")
    Response deleteHitList(@PathParam("id") Integer id) {
        def con = dataSource.connection
        Sql db = new Sql(con)
        try {
            int rows = db.executeUpdate("DELETE FROM ${users.hitListTable} WHERE id = ?".toString(), [id])
            if (rows == 1) {
                return Response.ok().build()
            } else if (rows == 0) {
                return Response.status(Response.Status.NOT_FOUND).build()
            } else {
                // should never happen?
                return Response.ok("Warning: muliple rows deleted. This should never happen.").build()
            }
        } finally {
            db.close()
        }
    }
    
    /** Get information for a particular hit list
     * @param id The hit list to retrieve
     */
    @GET
    @Produces("application/json")
    @Path("/hitlists/{id}")
    Response getHitList(@PathParam("id") Integer id) {
        String sql1 = """\
            |SELECT id, resource, list_owner, list_name, list_status, list_size, created_timestamp
            |  FROM ${users.hitListTable}
            |  WHERE id = ?
            |""".stripMargin()
        String sql2 = """\
            |SELECT id_item
            |  FROM ${users.hitListDataTable}
            |  WHERE hit_list_id = ?
            |""".stripMargin()
        
        StreamingOutput output = new StreamingOutput() {
            @Override
            public void write(OutputStream os) {

                Writer writer = new BufferedWriter(new OutputStreamWriter(os))
                
                def con = dataSource.connection
                Sql db = new Sql(con)
                try {
                    def listRow = db.firstRow(sql1, [id])
                    SubsetInfo si = createSubsetInforFromRow(listRow)
                    def items = []
                    db.eachRow(sql2, [id]) {
                        items << new Integer(it[0])
                    }
                    si.items = items
                    writer.write(JsonOutput.toJson(si))
                } finally {
                    db.close()
                    writer.flush()
                }
            }
        }        
        return Response.ok(output).build()
    }
    

    
    /** List all the hit lists
     */
    @GET
    @Produces("application/json")
    @Path("/hitlists")
    Response getHitLists() {
        String sql = """\
            |SELECT id, resource, list_owner, list_name, list_status, list_size, created_timestamp
            |  FROM ${users.hitListTable}
            |""".stripMargin()
        
        StreamingOutput output = new StreamingOutput() {
            @Override
            public void write(OutputStream os) {

                Writer writer = new BufferedWriter(new OutputStreamWriter(os))
                writer << '['
                def con = dataSource.connection
                Sql db = new Sql(con)
                try {
                    def first = true
                    db.eachRow(sql) {
                        if (first) {
                            first = false
                        } else {
                            writer << ",\n"
                        }
                        SubsetInfo si = createSubsetInforFromRow(it)
                        writer.write(JsonOutput.toJson(si))
                    }
                } finally {
                    db.close()
                    writer << ']\n'
                    writer.flush()
                }
            }
        }
                    
        return Response.ok(output).build()
    }
    
    private SubsetInfo createSubsetInforFromRow(def row) {
        SubsetInfo si = new SubsetInfo(
            row['id'], 
            row['list_name'],
            new Date(row['created_timestamp'].getTime()), 
            row['list_owner'], 
            row['resource'],
            SubsetInfo.Status.valueOf(row['list_status']),
            row['list_size'],
            null)
                        
        return si
    }
    
    /** Retrieve structure data for a particular hit list
     * 
     * @param hitlistId The ID of the hit list
     */
    @GET
    @Produces("application/json")
    @Path("/hitlists/{id}/structures")
    Response getStructureDataForHitList(@PathParam("id") Integer hitlistId) {
        String sql = """\
            |SELECT cd_id, cd_structure, cd_molweight, cd_formula
            |  FROM ${chemcentral.structuresTable} s
            |  JOIN ${users.hitListDataTable} hld ON hld.id_item = s.cd_id
            |  WHERE hld.hit_list_id = ?
            |""".stripMargin()
        StreamingOutput output = fetchStructures(sql) { [hitlistId] }
        return Response.ok(output).build()
    }
    
    /** Run a search for structures containing data for a particular property (or properties)
     *  Generates a hit list and returns immediately with a 201 response containing the URL
     *  of the hit list which can then be polled for the results. Initally the status is Pending.
     *  Once the search is complete the hit list info is updated and the the status set to OK. 
     * 
     * @param propertyId One or more property original IDs
     * @param sourceId The source ID of the property (in theory properties from different
     * sources could have the same Original ID so this is needed to disinguish them)
     * @return A 201 response with the Location of the generated hit list resource.
     */
    @POST
    @Path("/hitlists/structures/data")
    Response structuresWithDataSearch(
        @QueryParam("sourceId") Integer sourceId, 
        @QueryParam("propertyId")List<String> propDefOriginalIds) {
        
        Sql db = new Sql(dataSource.connection)
        Integer hitlistId = createHitList(db, RESOURCE_STRUCTURES, getUsername(),
            "Search for structures with data for ${propDefOriginalIds.join(',')}", )
        Thread.start() {
            structuresWithDataSearchImpl(db, hitlistId, sourceId, propDefOriginalIds)
        }
        URI uri = new URI(RESOURCE_HITLISTS + "/$hitlistId")
        return Response.created(uri).build()
    }
    
    private void structuresWithDataSearchImpl(Sql db, Integer hitListId, Integer sourceId, List<String> propDefOriginalIds) {

        def arr = db.connection.createArrayOf("text", propDefOriginalIds.toArray())
        try {
            log.info("Executing query for source $sourceId and props $arr")
            long t0 = System.currentTimeMillis()
            db.executeInsert("""\
                |INSERT INTO ${users.hitListDataTable} (id_item, hit_list_id) (
                |  SELECT distinct(s.cd_id), ? from ${chemcentral.structuresTable} s 
                |    JOIN ${chemcentral.structurePropsTable} sp ON s.cd_id = sp.structure_id
                |    JOIN ${chemcentral.propertyDefinitionsTable} pd ON pd.property_id = sp.property_id
                |    WHERE pd.source_id = ? AND pd.original_id = ANY (?))"""
                .stripMargin(), [hitListId, sourceId, arr])
            long t1 = System.currentTimeMillis()
            log.info("Query complete in ${t1-t0}ms")
            updateHitListStatus(db, hitListId, SubsetInfo.Status.OK)
        } finally {
            db.close()
        }
    }
    
   
    
    /** Perform a structure search
     * 
     * @param queryStructure The query structure
     * @param jchemSearchOptions The search optiosn in string format e.g. t:s
     */
    @POST
    @Path("/hitlists/structures/structuresearch")
    Response structuresWithStructureSearch(
        @QueryParam("queryStructure") String queryStructure, 
        @QueryParam("searchOptions") String jchemSearchOptions) {
        
        if (!structureCacheLoaded) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("503. Structure cache not loaded").build()
        }
        
        Sql db = new Sql(dataSource.connection)
        Integer hitlistId = createHitList(db, RESOURCE_STRUCTURES, getUsername(),
            "Structure search for structures", )
        Thread.start() {
            structureSearchImpl(db, hitlistId, queryStructure, jchemSearchOptions)
        }
        URI uri = new URI(RESOURCE_HITLISTS + "/$hitlistId")
        return Response.created(uri).build()
    }
    
    private void structureSearchImpl(Sql db, Integer hitListId, String queryStructure, String jchemSearchOptions) {
        
        //db.withTransaction {
        try {
            
            log.info("Executing structure search")
            JChemSearch searcher = createJChemSearch(db.connection, propertyTable, structureTable)
            searcher.setQueryStructure(queryStructure)
 
            JChemSearchOptions searchOptions = new JChemSearchOptions(JChemSearch.SUBSTRUCTURE);
            searchOptions.setOptions(jchemSearchOptions)
            searcher.setSearchOptions(searchOptions);
 
            searcher.setRunMode(JChemSearch.RUN_MODE_SYNCH_COMPLETE)
            long t0 = System.currentTimeMillis()
            searcher.run()
            int[] hits = searcher.getResults()
            log.info("JChem search complete")
            db.withBatch(100, "INSERT INTO $users.hitListDataTable (hit_list_id, id_item) VALUES (?, ?)".toString()) { ps ->
                hits.each { hit ->
                    ps.addBatch([hitListId, hit])
                }
            }
            
            long t1 = System.currentTimeMillis()
            log.info("Structure search complete in ${t1-t0}ms")
            updateHitListStatus(db, hitListId, SubsetInfo.Status.OK)
            
        } finally {
            db.close()
        }
        //}
    }
    
    private JChemSearch createJChemSearch(ConnectionHandler conh, String structureTable) {
        JChemSearch searcher = new JChemSearch()
        searcher.setConnectionHandler(conh)
        searcher.setStructureTable(structureTable)
        return searcher
    }
    
    private createConenctionHandler(Connection con, String propertyTable) {
        return new ConnectionHandler(con, propertyTable)
    }
        
    
    /** Retrieve structure data for a particular structure
     * 
     * @param id The ID of the structure (cd_id column value)
     */
    @GET
    @Produces("application/json")
    @Path("/structures/{id}")
    Response getStructureDataForId(@PathParam("id") Integer id) {
        return getStructureDataForIds([id])
    }
    
    /** Retrieve structure data for specific structures
     * 
     * @param ids The IDs of the structure (cd_id column values)
     */
    @GET
    @Produces("application/json")
    @Path("/structures")
    Response getStructureDataForIds(@QueryParam("id") List<Integer> ids) {
        String sql = """\
            |SELECT cd_id, cd_structure, cd_molweight, cd_formula
            |  FROM ${chemcentral.structuresTable} s
            |  WHERE s.cd_id = ANY (?)
            |""".stripMargin()
        StreamingOutput output = fetchStructures(sql, ) { con ->
            [con.createArrayOf("int4", ids.toArray())] 
        }
        return Response.ok(output).build()
    }
    
    private StreamingOutput fetchStructures(String sql, Closure closure) {
        
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) {

                Writer writer = new BufferedWriter(new OutputStreamWriter(os))
                writer << '['
                def con = dataSource.connection
                Sql db = new Sql(con)
                try {
                    def first = true
                    db.eachRow(sql, closure(con)) {
                        if (first) {
                            first = false
                        } else {
                            writer << ",\n"
                        }
                        String json = JsonOutput.toJson([
                                cd_id: it[0], 
                                cd_structure: new String(it[1]), 
                                cd_molweight: it[2], 
                                cd_formula: it[3]
                            ])
                        writer.write(json)
                    }
                } finally {
                    db.close()
                    writer << ']\n'
                    writer.flush()
                }
            }
        }
        return stream;
    }
    
    /** Retrieve property data for a specific hit list 
     * 
     * @param id The hit list ID
     * @param origid The original ID of the property
     */
    @GET
    @Produces("application/json")
    @Path("/hitlists/{id}/properties/{origid}")
    Response fetchPropertyData(
        @PathParam("id") Integer hitListId, 
        @PathParam("origid") String propertyDefOrigId) {
        String sql = """\
            |SELECT p.id, p.structure_id, p.property_data::text
            |  FROM ${chemcentral.structurePropsTable} p
            |  JOIN ${chemcentral.structuresTable} s ON p.structure_id = s.cd_id
            |  JOIN ${chemcentral.propertyDefinitionsTable} pd ON p.property_id = pd.property_id
            |  JOIN users.hit_list_data d ON d.id_item = s.cd_id
            |  JOIN users.hit_lists l ON l.id = d.hit_list_id
            |  WHERE l.id = ? AND pd.original_id = ?""".stripMargin()
        StreamingOutput output = fetchPropertyDataImpl(sql) { [hitListId, propertyDefOrigId] }
        return Response.ok(output).build()
    }
    
    private StreamingOutput fetchPropertyDataImpl(String sql, Closure paramsClosure) {

        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) {

                Writer writer = new BufferedWriter(new OutputStreamWriter(os))
                def con = dataSource.connection
                Sql db = new Sql(con)
                try {
                    def first = true
                    writer << '['
                    db.eachRow(sql, paramsClosure(con)) {
                        if (first) {
                            first = false
                        } else {
                            writer << ",\n"
                        }
                        writer << '{"property_id":'
                        writer << it[0]
                        writer << ',"structure_id":'
                        writer << it[1]
                        writer << ',"property_data":'
                        writer << it[2]
                        writer << '}'
                    }
                    
                } finally {
                    db.close()
                    writer << ']\n'
                    writer.flush()
                }
            }
        }
        return stream
    }
    
        /** Get property defintions
     * 
     * @param filter A text tring to used to filter the property descriptions (using 
     * a LIKE '%?%' search (case insensitive) on the property_description column
     * @param limit retrieve this many rows. If not provided a default of 100 is used 
     */
    @GET
    @Produces("application/json")
    @Path("/propertyDefintions")
    Response fetchPropertyDefintions(
        @QueryParam("filter") String filter, 
        @QueryParam("limit") Integer limit) {
        def params = []
        if (!limit) limit = 100 // apply a default to prevent the whole lot being loaded
        String sql = "SELECT property_id, property_description, est_size FROM ${chemcentral.propertyDefinitionsTable}"
        if (filter) {
            sql += "\n  WHERE lower(property_description) LIKE ?"
            params << '%' + filter.toLowerCase() + '%'
        }
        sql += "\n  LIMIT ?"
        params << limit
        log.info("SQL: $sql")
        
        StreamingOutput output = new StreamingOutput() {
            @Override
            public void write(OutputStream os) {

                Writer writer = new BufferedWriter(new OutputStreamWriter(os))
                writer << '['
                def con = dataSource.connection
                Sql db = new Sql(con)
                try {
                    def first = true
                    db.eachRow(sql, params) {
                        if (first) {
                            first = false
                        } else {
                            writer << ",\n"
                        }
                        def data = [
                            property_id: it['property_id'], 
                            property_description: it['property_description'],
                            est_size: it['est_size']
                        ]
                        writer.write(JsonOutput.toJson(data))
                    }
                } finally {
                    db.close()
                    writer << ']\n'
                    writer.flush()
                }
            }
        } 
        return Response.ok(output).build()
    }
    
}

