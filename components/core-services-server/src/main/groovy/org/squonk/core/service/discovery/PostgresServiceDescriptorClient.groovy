package org.squonk.core.service.discovery

import groovy.sql.Sql
import groovy.util.logging.Log
import org.squonk.core.ServiceDescriptor
import org.squonk.core.ServiceDescriptorSet
import org.squonk.core.util.SquonkServerConfig
import org.squonk.types.io.JsonHandler

import javax.sql.DataSource
import java.sql.SQLException

/**
 * Created by timbo on 29/11/16.
 */
@Log
class PostgresServiceDescriptorClient {

    protected final DataSource dataSource = SquonkServerConfig.INSTANCE.getSquonkDataSource();
    protected final Sql sql = new Sql(dataSource)
    private JsonHandler jsonHandler = JsonHandler.getInstance()

    List<ServiceDescriptorSet> list() throws SQLException {

        def sdsetrows
        def sdrows
        sql.withTransaction {
            sdsetrows = sql.rows("SELECT * FROM users.service_descriptor_sets WHERE status = 'A' ORDER BY id")
            sdrows = sql.rows("SELECT * FROM users.service_descriptors WHERE status = 'A' ORDER BY id")
        }
        log.info "Found ${sdsetrows.size()} sets and ${sdrows.size()} service descriptors"

        Map sdsmap = [:]

        sdrows.each {
            long set = it.set_id
            String sd = it.sd_json
            List<String> list = sdsmap[set]
            if (list == null) {
                list = []
                sdsmap[set] = list
            }
            list << sd // still as json
        }

        List<ServiceDescriptorSet> results = []

        sdsetrows.each {
            long setid = it.id
            String baseUrl = it.base_url
            String healthUrl = it.health_url
            List<String> sdsjson = sdsmap[setid]
            List<ServiceDescriptor> sds = []
            sdsjson.each {
                try {
                    ServiceDescriptor sd = jsonHandler.objectFromJson(it, ServiceDescriptor.class)
                    log.info("retrieved service descriptor ${sd.id}")
                    sds << sd
                } catch (Exception ex) {
                    log.warning("Failed to unmarshal ServiceDescriptor " + it)
                }
            }
            results << new ServiceDescriptorSet(baseUrl, healthUrl, sds)
        }
        return results

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

        log.info("udpating sdset for $sdset.baseUrl")

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
                    log.info("udpating service descriptor " + sd.id)
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
