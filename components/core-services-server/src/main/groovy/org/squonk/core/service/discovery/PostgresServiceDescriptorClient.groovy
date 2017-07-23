/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.core.service.discovery

import groovy.sql.Sql
import groovy.util.logging.Log
import org.squonk.core.HttpServiceDescriptor
import org.squonk.core.ServiceDescriptor
import org.squonk.core.ServiceDescriptorSet
import org.squonk.config.SquonkServerConfig
import org.squonk.types.io.JsonHandler

import javax.sql.DataSource
import java.sql.SQLException
import java.util.logging.Level

/**
 * Created by timbo on 29/11/16.
 */
@Log
class PostgresServiceDescriptorClient {

    protected final DataSource dataSource
    protected final Sql sql
    private JsonHandler jsonHandler = JsonHandler.getInstance()

    PostgresServiceDescriptorClient() {
        this( SquonkServerConfig.getSquonkDataSource());
    }


    PostgresServiceDescriptorClient(DataSource dataSource ) {
        this.dataSource = dataSource
        this.sql = new Sql(dataSource)
    }

    /** Fetch all service descriptor sets
     *
     * @return
     * @throws SQLException
     */
    List<ServiceDescriptorSet> list() throws SQLException {

        def sdsetrows
        def sdrows
        try {
            sql.withTransaction {
                sdsetrows = sql.rows("SELECT * FROM users.service_descriptor_sets ORDER BY id")
                sdrows = sql.rows("SELECT * FROM users.service_descriptors ORDER BY id")
            }
        } finally {
            sql.close()
        }
        log.info "Found ${sdsetrows.size()} sets and ${sdrows.size()} service descriptors"

        Map sdsmap = [:]

        sdrows.each {
            long id = it.id
            long set = it.set_id
            String sd = it.sd_json
            String javaClass = it.java_class
            Map<Long, String> map = sdsmap[set]
            if (map == null) {
                map = [:]
                sdsmap[set] = map
            }
            ServiceDescriptor descriptor = buildServiceDescriptorFromJson(id, sd, javaClass)
            if (descriptor != null) {
                map[id] = descriptor
            }
        }

        List<ServiceDescriptorSet> results = []
        sdsetrows.each {
            long setid = it.id
            String baseUrl = it.base_url
            String healthUrl = it.health_url
            Map<Long, ServiceDescriptor> map = sdsmap[setid]
            if (map) {
                List<ServiceDescriptor> sds = map.values().collect { it }
                results << new ServiceDescriptorSet(baseUrl, healthUrl, sds)
                log.info("Loaded ${sds.size()} service descriptors for $baseUrl")
            }
        }
        return results
    }

    /** Fetch all service descriptors corresponding to this base url
     *
     * @param baseUrl
     * @return
     */
    List<HttpServiceDescriptor> fetch(String baseUrl) {
        Map<Long, HttpServiceDescriptor> sdsmap
        try {
            sql.withTransaction {
                sdsmap = doFetch(sql, baseUrl)
            }
        } finally {
            sql.close()
        }
        List<HttpServiceDescriptor> results = sdsmap.values().collect { it }
        log.info("Loaded ${results.size()} service descriptors for $baseUrl")

        return results;
    }

    private Map<Long, HttpServiceDescriptor> doFetch(Sql db, String baseUrl) {
        Map<Long, ServiceDescriptor> sdJson = [:]
        db.eachRow("""\n
                |SELECT d.id, d.sd_json::text, d.java_class FROM users.service_descriptors d
                |  JOIN users.service_descriptor_sets s ON s.id = d.set_id
                |  WHERE s.base_url = ? ORDER BY d.id""".stripMargin(), [baseUrl]) {

            long id = it[0]
            String json = it[1]
            String javaClass = it[2]
            ServiceDescriptor descriptor = buildServiceDescriptorFromJson(id, json, javaClass)
            if (descriptor != null) {
                sdJson[id] = descriptor
            }
        }
        return sdJson;
    }

    private Map<String, Class> classesMap = [:]

    private ServiceDescriptor buildServiceDescriptorFromJson(Long id, String json, String javaClass) {
        log.finer("Building service descriptor of class " + javaClass)
        Class cls = classesMap.get(javaClass)
        if (cls == null) {
            try {
                cls = Class.forName(javaClass)
                classesMap[javaClass] = cls
            } catch (Exception ex) {
                log.warning("Could not instantiate class " + javaClass)
            }
        }
        try {
            ServiceDescriptor sd = jsonHandler.objectFromJson(json, cls)
            return sd
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed to unmarshal ServiceDescriptor " + id + " JSON: "+ json, ex)
            return null
        }
    }

    /** Gets the total count of service descriptor sets (ignoring their status)
     *
     * @return
     */
    int countServiceDescriptorSets() throws SQLException {

        try {
            def result = null
            sql.withTransaction {
                result = sql.firstRow("SELECT COUNT(*) FROM users.service_descriptor_sets")[0]
            }
            return result
        } finally {
            sql.close()
        }
    }

    /** Gets the total count of service descriptors (ignoring their status)
     *
     * @return
     */
    int countServiceDescriptors() throws SQLException {
        try {
            def result = null
            sql.withTransaction {
                result = sql.firstRow("SELECT COUNT(*) FROM users.service_descriptors")[0]
            }
            return result
        } finally {
            sql.close()
        }
    }

    void update(List<ServiceDescriptorSet> sdsets) throws SQLException {
        try {
            sql.withTransaction {
                sdsets.each { sdset ->
                    doUpdateServiceDescriptorSet(sql, sdset)
                }
            }
        } finally {
            sql.close()
        }
    }

    void update(ServiceDescriptorSet sdset) throws SQLException {
        try {
            sql.withTransaction {
                doUpdateServiceDescriptorSet(sql, sdset)
            }
        } finally {
            sql.close()
        }
    }


    void doUpdateServiceDescriptorSet(Sql db, ServiceDescriptorSet sdset) throws SQLException {

        log.fine("Udpating service descriptors for $sdset.baseUrl with ${sdset.getServiceDescriptors().size()} service descriptors")

        db.executeInsert("""\
                |INSERT INTO users.service_descriptor_sets AS t (base_url, health_url, status, created, updated)
                |  VALUES (:base_url, :health_url, 'A', NOW(), NOW())
                |  ON CONFLICT ON CONSTRAINT uq_base_url DO UPDATE
                |    SET health_url=EXCLUDED.health_url, updated=NOW()
                |      WHERE t.base_url=EXCLUDED.base_url""".stripMargin(),
                [base_url: sdset.baseUrl, health_url: sdset.healthUrl])

        if (!sdset.getServiceDescriptors().isEmpty()) {
            def row = db.firstRow("SELECT id FROM users.service_descriptor_sets WHERE base_url = ?", [sdset.baseUrl])
            if (row != null) {
                def setId = row[0]
                log.fine("udpating sds for set $setId")
                doUpdateServiceDescriptors(db, setId, sdset.getServiceDescriptors())
            } else {
                log.warning("ServiceDescriptorSet for ${sdset.baseUrl} not found. ServiceDescriptors will not be updated")
            }
        }
    }

    void doUpdateServiceDescriptors(Sql db, def setId, List<ServiceDescriptor> sds) throws SQLException {

        try {
            db.withBatch("""\
                |INSERT INTO users.service_descriptors AS t (set_id, sd_id, status, sd_json, java_class, created, updated)
                |  VALUES (?, ?, 'A', ?::jsonb, ?, NOW(), NOW())
                |  ON CONFLICT ON CONSTRAINT uq_sd_id DO UPDATE
                |    SET updated=NOW(), sd_json=EXCLUDED.sd_json, status='A'
                |      WHERE t.set_id=EXCLUDED.set_id AND t.sd_id=EXCLUDED.sd_id""".stripMargin()) { ps ->

                sds.each { sd ->
                    String json = jsonHandler.objectToJson(sd)
                    String javaClass = sd.getClass().getName()
                    log.fine("udpating service descriptor " + sd.id + " of class " + javaClass)
                    ps.addBatch([setId, sd.id, json, javaClass])
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace()
            ex.getNextException()?.printStackTrace()
            throw ex
        }
    }

}
