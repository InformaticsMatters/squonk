package org.squonk.core.service.discovery

import groovy.sql.Sql
import groovy.util.logging.Log
import org.squonk.core.ServiceDescriptor
import org.squonk.core.ServiceDescriptorSet
import org.squonk.core.util.SquonkServerConfig
import org.squonk.types.io.JsonHandler

import javax.sql.DataSource
import java.sql.SQLException
import java.util.logging.Level

/**
 * Created by timbo on 29/11/16.
 */
@Log
class PostgresServiceDescriptorClient {

    protected final DataSource dataSource = SquonkServerConfig.INSTANCE.getSquonkDataSource();
    protected final Sql sql = new Sql(dataSource)
    private JsonHandler jsonHandler = JsonHandler.getInstance()

    /** Fetch all service descriptor sets
     *
     * @return
     * @throws SQLException
     */
    List<ServiceDescriptorSet> list() throws SQLException {

        def sdsetrows
        def sdrows
        sql.withTransaction {
            sdsetrows = sql.rows("SELECT * FROM users.service_descriptor_sets ORDER BY id")
            sdrows = sql.rows("SELECT * FROM users.service_descriptors ORDER BY id")
        }
        log.info "Found ${sdsetrows.size()} sets and ${sdrows.size()} service descriptors"

        Map sdsmap = [:]

        sdrows.each {
            long id = it.id
            long set = it.set_id
            String sd = it.sd_json
            Map<Long, String> map = sdsmap[set]
            if (map == null) {
                map = [:]
                sdsmap[set] = map
            }
            map[id] = sd // still as json
        }

        List<ServiceDescriptorSet> results = []
        sdsetrows.each {
            long setid = it.id
            String baseUrl = it.base_url
            String healthUrl = it.health_url
            Map<Long, ServiceDescriptor> map = buildServiceDescriptorsFromJson(sdsmap[setid])
            List<ServiceDescriptor> sds = map.values().collect { it }
            results << new ServiceDescriptorSet(baseUrl, healthUrl, sds)
        }
        return results
    }

    /** Fetch all service descriptors corresponding to this base url
     *
     * @param baseUrl
     * @return
     */
    List<ServiceDescriptor> fetch(String baseUrl) {
        Map<Long, ServiceDescriptor> sdsmap
        sql.withTransaction {
            sdsmap = doFetch(sql, baseUrl)
        }
        List<ServiceDescriptor> results = sdsmap.values().collect { it }
        return results;
    }

    private Map<Long, ServiceDescriptor> doFetch(Sql db, String baseUrl) {
        Map<Long, String> sdJson = [:]
        db.eachRow("""\n
                |SELECT d.id, d.sd_json::text FROM users.service_descriptors d
                |  JOIN users.service_descriptor_sets s ON s.id = d.set_id
                |  WHERE s.base_url = ? ORDER BY d.id""".stripMargin(), [baseUrl]) {

            long id = it[0]
            String j = it[1]
            sdJson[id] = j

        }
        Map<Long, ServiceDescriptor> results = buildServiceDescriptorsFromJson(sdJson)
        return results;
    }

    /** Update the status of all service descriptors for this base url
     *
     * @param baseUrl
     * @param status
     * @throws Exception
     */
    public void updateServiceDescriptorStatus(String baseUrl, ServiceDescriptor.Status status, Date when) throws Exception {

        log.info("Updating status of service descriptors for $baseUrl to $status")

        sql.withTransaction {
            Map<Long, ServiceDescriptor> map = doFetch(sql, baseUrl);
            sql.withBatch("UPDATE users.service_descriptors SET sd_json = ?::jsonb WHERE id = ?") { ps ->
                map.each { id, sd ->
                    sd.status = status
                    sd.statusLastChecked = when
                    try {
                        String json = jsonHandler.objectToJson(sd)
                        ps.addBatch([json, id])
                    } catch (Exception ex) {
                        // should have no problem converting back to json, but just in case ...
                        log.log(Level.INFO, "Failed to update service descriptor", ex)
                    }
                }
            }
        }
    }

    private Map<Long, ServiceDescriptor> buildServiceDescriptorsFromJson(Map<Long, String> sdJson) {
        Map<Long, ServiceDescriptor> sds = [:]
        sdJson.each { i, json ->
            try {
                ServiceDescriptor sd = jsonHandler.objectFromJson(json, ServiceDescriptor.class)
                log.fine("Retrieved service descriptor ${sd.id}")
                sds[i] = sd
            } catch (Exception ex) {
                log.log(Level.WARNING, "Failed to unmarshal ServiceDescriptor " + i + " -> " + json, ex)
            }
        }
        return sds
    }

    /** Gets the total count of service descriptor sets (ignoring their status)
     *
     * @return
     */
    int countServiceDescriptorSets() throws SQLException {
        return sql.firstRow("SELECT COUNT(*) FROM users.service_descriptor_sets")[0]
    }

    /** Gets the total count of service descriptors (ignoring their status)
     *
     * @return
     */
    int countServiceDescriptors() throws SQLException {
        return sql.firstRow("SELECT COUNT(*) FROM users.service_descriptors")[0]
    }

    void update(List<ServiceDescriptorSet> sdsets) throws SQLException {
        sql.withTransaction {
            sdsets.each { sdset ->
                doUpdateServiceDescriptorSet(sql, sdset)
            }
        }
    }

    void update(ServiceDescriptorSet sdset) throws SQLException {
        sql.withTransaction {
            doUpdateServiceDescriptorSet(sql, sdset)
        }
    }


    void doUpdateServiceDescriptorSet(Sql db, ServiceDescriptorSet sdset) throws SQLException {

        log.info("udpating sdset for $sdset.baseUrl containing ${sdset.serviceDescriptors.size()} service descriptors")

        db.executeInsert("""\
                |INSERT INTO users.service_descriptor_sets AS t (base_url, health_url, status, created, updated)
                |  VALUES (:base_url, :health_url, 'A', NOW(), NOW())
                |  ON CONFLICT ON CONSTRAINT uq_base_url DO UPDATE
                |    SET health_url=EXCLUDED.health_url, updated=NOW()
                |      WHERE t.base_url=EXCLUDED.base_url""".stripMargin(),
                [base_url: sdset.baseUrl, health_url: sdset.healthUrl])

        if (!sdset.getServiceDescriptors().isEmpty()) {
            def row = db.firstRow("SELECT id FROM users.service_descriptor_sets WHERE base_url = ${sdset.baseUrl}")
            if (row != null) {
                def setId = row[0]
                log.fine("udpating sds for set $setId")
                doUpdateServiceDescriptors(db, setId, sdset.getServiceDescriptors())
            }
        }
    }

    void doUpdateServiceDescriptors(Sql db, def setId, List<ServiceDescriptor> sds) throws SQLException {

        try {
            db.withBatch("""\
                |INSERT INTO users.service_descriptors AS t (set_id, sd_id, status, sd_json, created, updated)
                |  VALUES (?, ?, 'A', ?::jsonb, NOW(), NOW())
                |  ON CONFLICT ON CONSTRAINT uq_sd_id DO UPDATE
                |    SET updated=NOW()
                |      WHERE t.set_id=EXCLUDED.set_id AND t.sd_id=EXCLUDED.sd_id""".stripMargin()) { ps ->

                sds.each { sd ->
                    log.info("udpating service descriptor " + sd.id + " " + sd.getExecutionEndpoint())
                    String json = jsonHandler.objectToJson(sd)
                    ps.addBatch([setId, sd.id, json])
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace()
            ex.getNextException()?.printStackTrace()
            throw ex
        }
    }


}
